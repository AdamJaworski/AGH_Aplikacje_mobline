package com.example.apkmoblineprojekt.system

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Instances
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.apkmoblineprojekt.data.model.DeviceCalendarInfo
import com.example.apkmoblineprojekt.data.model.ExternalEvent
import com.example.apkmoblineprojekt.data.model.preferredDefault
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Dane wydarzenia, które aplikacja zapisuje w kalendarzu telefonu. `time == null` → całodniowe.
 * [tag] trafia do `CUSTOM_APP_URI` i pozwala odnaleźć wydarzenie zadania, gdy aplikacja zgubi jego id.
 */
data class EventDraft(
    val tag: String,
    val title: String,
    val description: String,
    val location: String,
    val date: LocalDate,
    val time: LocalTime?,
    val durationMinutes: Int = 60,
)

/**
 * Dostęp do kalendarza systemowego (CalendarContract). Zapis do kalendarza konta Google
 * sprawia, że wydarzenia trafiają też do chmury i na inne urządzenia.
 */
interface DeviceCalendar {
    /** true, gdy mamy READ_CALENDAR i WRITE_CALENDAR. Odświeżane przez [refresh]. */
    val access: StateFlow<Boolean>

    /** Ponowne sprawdzenie uprawnień – wołać po prośbie o uprawnienia i przy powrocie do aplikacji. */
    fun refresh()

    /** Emituje od razu po subskrypcji i przy każdej zmianie w kalendarzach telefonu. */
    fun changes(): Flow<Unit>

    /** Wystąpienia wydarzeń w przedziale [from, toExclusive), aktualizowane na bieżąco. */
    fun observeEvents(from: LocalDate, toExclusive: LocalDate): Flow<List<ExternalEvent>>

    /** Kalendarze, do których możemy zapisywać. */
    suspend fun writableCalendars(): List<DeviceCalendarInfo>

    /** Główny zapisywalny kalendarz; gdy w telefonie nie ma żadnego – tworzy lokalny „Blisko”. */
    suspend fun defaultCalendarId(): Long?

    /** Usuwa kalendarz (razem z wydarzeniami). Używane tylko dla lokalnego kalendarza aplikacji. */
    suspend fun deleteAppLocalCalendar(calendarId: Long)

    // Poniższe rzucają wyjątek przy błędzie dostępu – synchronizacja musi odróżnić błąd od braku wydarzenia.

    /** Wydarzenie (wiersz główny) albo null, jeśli zostało usunięte. */
    suspend fun getEvent(eventId: Long): ExternalEvent?

    /** Pojedyncze (niecykliczne) wydarzenia z [calendarId] zaczynające się w [from, toExclusive). */
    suspend fun calendarEvents(calendarId: Long, from: LocalDate, toExclusive: LocalDate): List<ExternalEvent>

    /**
     * Zapisuje wydarzenie zadania w [calendarId]: aktualizuje [eventId] (albo wydarzenie z tym samym
     * tagiem), przenosi je, jeśli leży w innym kalendarzu, a gdy go nie ma – tworzy nowe. Zwraca id.
     */
    suspend fun upsertEvent(eventId: Long?, calendarId: Long, draft: EventDraft): Long

    suspend fun deleteEvent(eventId: Long)
}

class AndroidDeviceCalendar(context: Context) : DeviceCalendar {
    private val appContext = context.applicationContext
    private val resolver = appContext.contentResolver
    private val zone: ZoneId get() = ZoneId.systemDefault()

    private val _access = MutableStateFlow(hasPermission())
    override val access: StateFlow<Boolean> = _access.asStateFlow()

    override fun refresh() {
        _access.value = hasPermission()
    }

    private fun hasPermission() = listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        .all { ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun changes(): Flow<Unit> = access.flatMapLatest { granted ->
        if (!granted) emptyFlow() else observerFlow().debounce(OBSERVER_DEBOUNCE_MS)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeEvents(from: LocalDate, toExclusive: LocalDate): Flow<List<ExternalEvent>> =
        access.flatMapLatest { granted ->
            if (!granted) flowOf(emptyList()) else changes().mapLatest { queryInstances(from, toExclusive) }
        }

    private fun observerFlow(): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(CalendarContract.CONTENT_URI, true, observer)
        send(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }

    private suspend fun queryInstances(from: LocalDate, toExclusive: LocalDate): List<ExternalEvent> =
        readSafely(emptyList()) {
            // Wydarzenia całodniowe są zapisane w UTC – poszerzamy okno o dzień i filtrujemy po konwersji.
            val begin = from.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = toExclusive.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val uri = Instances.CONTENT_URI.buildUpon()
                .appendPath(begin.toString())
                .appendPath(end.toString())
                .build()
            val selection = "${Instances.VISIBLE} = 1 AND " +
                "(${Instances.STATUS} IS NULL OR ${Instances.STATUS} != ${Events.STATUS_CANCELED})"
            val fromDateTime = from.atStartOfDay()
            val toDateTime = toExclusive.atStartOfDay()
            resolver.query(uri, INSTANCE_PROJECTION, selection, null, "${Instances.BEGIN} ASC")
                ?.use { cursor ->
                    cursor.rows().map { c ->
                        val allDay = c.getInt(5) == 1
                        val beginMillis = c.getLong(1)
                        val endMillis = c.getLong(2)
                        ExternalEvent(
                            eventId = c.getLong(0),
                            calendarId = c.getLong(7),
                            calendarName = c.getString(8).orEmpty(),
                            title = c.getString(3).orEmpty(),
                            description = c.getString(9).orEmpty(),
                            location = c.getString(4).orEmpty(),
                            start = beginMillis.toLocal(allDay),
                            end = endMillis.toLocal(allDay),
                            allDay = allDay,
                            color = c.getInt(6),
                            beginMillis = beginMillis,
                            endMillis = endMillis,
                        )
                    }.filter { it.start < toDateTime && (it.end > fromDateTime || it.start >= fromDateTime) }
                        .toList()
                }
                .orEmpty()
        }

    override suspend fun writableCalendars(): List<DeviceCalendarInfo> = readSafely(emptyList()) {
        val selection = "${Calendars.VISIBLE} = 1 AND ${Calendars.CALENDAR_ACCESS_LEVEL} >= ${Calendars.CAL_ACCESS_CONTRIBUTOR}"
        resolver.query(Calendars.CONTENT_URI, CALENDAR_PROJECTION, selection, null, null)
            ?.use { cursor ->
                cursor.rows().map { c ->
                    val accountName = c.getString(2).orEmpty()
                    val accountType = c.getString(5).orEmpty()
                    DeviceCalendarInfo(
                        id = c.getLong(0),
                        name = c.getString(1).orEmpty(),
                        accountName = accountName,
                        color = c.getInt(3),
                        primary = c.getInt(4) == 1,
                        accountType = accountType,
                        appLocal = accountType == CalendarContract.ACCOUNT_TYPE_LOCAL && accountName == LOCAL_ACCOUNT,
                    )
                }.toList()
            }
            .orEmpty()
    }

    override suspend fun defaultCalendarId(): Long? {
        if (!hasPermission()) return null
        return writableCalendars().preferredDefault()?.id
            ?: withContext(Dispatchers.IO) { createLocalCalendar() }
    }

    override suspend fun deleteAppLocalCalendar(calendarId: Long) {
        withContext(Dispatchers.IO) {
            resolver.delete(
                Calendars.CONTENT_URI.asSyncAdapter(),
                "${Calendars._ID} = ? AND ${Calendars.ACCOUNT_TYPE} = ? AND ${Calendars.ACCOUNT_NAME} = ?",
                arrayOf(calendarId.toString(), CalendarContract.ACCOUNT_TYPE_LOCAL, LOCAL_ACCOUNT),
            )
        }
    }

    /** Kalendarz tylko na tym urządzeniu – zapasowo, gdy w telefonie nie ma żadnego konta. */
    private fun createLocalCalendar(): Long? {
        val uri = Calendars.CONTENT_URI.asSyncAdapter()
        val values = ContentValues().apply {
            put(Calendars.ACCOUNT_NAME, LOCAL_ACCOUNT)
            put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(Calendars.NAME, LOCAL_CALENDAR_NAME)
            put(Calendars.CALENDAR_DISPLAY_NAME, LOCAL_CALENDAR_DISPLAY_NAME)
            put(Calendars.CALENDAR_COLOR, LOCAL_CALENDAR_COLOR)
            put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER)
            put(Calendars.OWNER_ACCOUNT, LOCAL_ACCOUNT)
            put(Calendars.CALENDAR_TIME_ZONE, zone.id)
            put(Calendars.VISIBLE, 1)
            put(Calendars.SYNC_EVENTS, 1)
        }
        return resolver.insert(uri, values)?.let(ContentUris::parseId)
    }

    override suspend fun getEvent(eventId: Long): ExternalEvent? = withContext(Dispatchers.IO) {
        val cursor = resolver.query(
            ContentUris.withAppendedId(Events.CONTENT_URI, eventId),
            EVENT_PROJECTION,
            null,
            null,
            null,
        ) ?: throw IllegalStateException("Calendar provider unavailable")
        cursor.use { c ->
            if (!c.moveToFirst()) return@use null
            val deleted = c.getInt(8) == 1
            val canceled = !c.isNull(9) && c.getInt(9) == Events.STATUS_CANCELED
            if (deleted || canceled) null else c.toStoredEvent()
        }
    }

    override suspend fun calendarEvents(calendarId: Long, from: LocalDate, toExclusive: LocalDate): List<ExternalEvent> =
        withContext(Dispatchers.IO) {
            val selection = "${Events.CALENDAR_ID} = ? AND ${Events.DELETED} = 0 AND " +
                "${Events.RRULE} IS NULL AND ${Events.ORIGINAL_ID} IS NULL AND " +
                "(${Events.STATUS} IS NULL OR ${Events.STATUS} != ${Events.STATUS_CANCELED}) AND " +
                "${Events.DTSTART} >= ? AND ${Events.DTSTART} < ?"
            val args = arrayOf(
                calendarId.toString(),
                from.minusDays(1).atStartOfDay(zone).toInstant().toEpochMilli().toString(),
                toExclusive.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli().toString(),
            )
            val cursor = resolver.query(Events.CONTENT_URI, EVENT_PROJECTION, selection, args, null)
                ?: throw IllegalStateException("Calendar provider unavailable")
            cursor.use { c -> c.rows().map { it.toStoredEvent() }.toList() }
        }

    /** Wiersz z [EVENT_PROJECTION] → wydarzenie (z tagiem, jeśli zapisała je ta aplikacja). */
    private fun Cursor.toStoredEvent(): ExternalEvent {
        val allDay = getInt(7) == 1
        val beginMillis = getLong(5)
        val endMillis = if (isNull(6)) beginMillis else getLong(6)
        return ExternalEvent(
            eventId = getLong(0),
            calendarId = getLong(1),
            calendarName = "",
            title = getString(2).orEmpty(),
            description = getString(3).orEmpty(),
            location = getString(4).orEmpty(),
            start = beginMillis.toLocal(allDay),
            end = endMillis.toLocal(allDay),
            allDay = allDay,
            color = 0,
            beginMillis = beginMillis,
            endMillis = endMillis,
            tag = getString(11).takeIf { getString(10) == appContext.packageName },
        )
    }

    override suspend fun upsertEvent(eventId: Long?, calendarId: Long, draft: EventDraft): Long =
        withContext(Dispatchers.IO) {
            val values = draft.toContentValues()
            val existing = eventId?.let { findStored(it, null) } ?: findStored(null, draft.tag)
            if (existing != null) {
                if (existing.calendarId == calendarId) {
                    if (!existing.matches(values)) {
                        resolver.update(ContentUris.withAppendedId(Events.CONTENT_URI, existing.id), values, null, null)
                    }
                    return@withContext existing.id
                }
                // Zmienił się kalendarz docelowy – wydarzenia nie da się przenieść, więc tworzymy je od nowa.
                resolver.delete(ContentUris.withAppendedId(Events.CONTENT_URI, existing.id), null, null)
            }
            values.put(Events.CALENDAR_ID, calendarId)
            val uri = resolver.insert(Events.CONTENT_URI, values)
                ?: throw IllegalStateException("Event insert failed")
            ContentUris.parseId(uri)
        }

    override suspend fun deleteEvent(eventId: Long) {
        withContext(Dispatchers.IO) {
            resolver.delete(ContentUris.withAppendedId(Events.CONTENT_URI, eventId), null, null)
        }
    }

    private class StoredEvent(val id: Long, val calendarId: Long, val values: ContentValues) {
        /** Czy zapis niczego by nie zmienił – unikamy zbędnych zmian (i synchronizacji z chmurą). */
        fun matches(draft: ContentValues): Boolean = COMPARED_COLUMNS.all { column ->
            draft.getAsString(column).orEmpty() == values.getAsString(column).orEmpty()
        }
    }

    /** Nieusunięte wydarzenie po id albo po tagu zadania. */
    private fun findStored(eventId: Long?, tag: String?): StoredEvent? {
        val (uri, selection, args) = if (eventId != null) {
            Triple(ContentUris.withAppendedId(Events.CONTENT_URI, eventId), "${Events.DELETED} = 0", null)
        } else {
            Triple(
                Events.CONTENT_URI,
                "${Events.DELETED} = 0 AND ${Events.CUSTOM_APP_PACKAGE} = ? AND ${Events.CUSTOM_APP_URI} = ?",
                arrayOf(appContext.packageName, tag),
            )
        }
        val projection = arrayOf(Events._ID, Events.CALENDAR_ID) + COMPARED_COLUMNS
        return resolver.query(uri, projection, selection, args, null)?.use { c ->
            if (!c.moveToFirst()) return@use null
            val values = ContentValues()
            COMPARED_COLUMNS.forEachIndexed { index, column -> values.put(column, c.getString(index + 2)) }
            StoredEvent(c.getLong(0), c.getLong(1), values)
        }
    }

    private fun EventDraft.toContentValues() = ContentValues().apply {
        put(Events.CUSTOM_APP_PACKAGE, appContext.packageName)
        put(Events.CUSTOM_APP_URI, tag)
        put(Events.TITLE, title)
        put(Events.DESCRIPTION, description)
        put(Events.EVENT_LOCATION, location)
        if (time == null) {
            val start = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            put(Events.DTSTART, start)
            put(Events.DTEND, date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            put(Events.ALL_DAY, 1)
            put(Events.EVENT_TIMEZONE, "UTC")
        } else {
            val start = date.atTime(time).atZone(zone)
            put(Events.DTSTART, start.toInstant().toEpochMilli())
            put(Events.DTEND, start.plusMinutes(durationMinutes.toLong()).toInstant().toEpochMilli())
            put(Events.ALL_DAY, 0)
            put(Events.EVENT_TIMEZONE, zone.id)
        }
        // Wydarzenie z DTEND nie może mieć DURATION (np. po wcześniejszej edycji w innej aplikacji).
        putNull(Events.DURATION)
    }

    private fun Long.toLocal(allDay: Boolean): LocalDateTime =
        Instant.ofEpochMilli(this).atZone(if (allDay) ZoneOffset.UTC else zone).toLocalDateTime()

    private fun Cursor.rows(): Sequence<Cursor> = generateSequence { if (moveToNext()) this else null }

    private fun Uri.asSyncAdapter(): Uri = buildUpon()
        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
        .appendQueryParameter(Calendars.ACCOUNT_NAME, LOCAL_ACCOUNT)
        .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
        .build()

    /** Odczyty dla UI: przy braku uprawnień albo błędzie dostawcy zwracamy [fallback]. */
    private suspend fun <T> readSafely(fallback: T, block: () -> T): T = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext fallback
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Calendar read failed", e)
            fallback
        }
    }

    private companion object {
        const val TAG = "DeviceCalendar"
        const val OBSERVER_DEBOUNCE_MS = 300L
        const val LOCAL_ACCOUNT = "Blisko"
        const val LOCAL_CALENDAR_NAME = "blisko"
        const val LOCAL_CALENDAR_DISPLAY_NAME = "Blisko"
        const val LOCAL_CALENDAR_COLOR = 0xFF24483D.toInt()

        val INSTANCE_PROJECTION = arrayOf(
            Instances.EVENT_ID, // 0
            Instances.BEGIN, // 1
            Instances.END, // 2
            Instances.TITLE, // 3
            Instances.EVENT_LOCATION, // 4
            Instances.ALL_DAY, // 5
            Instances.DISPLAY_COLOR, // 6
            Instances.CALENDAR_ID, // 7
            Instances.CALENDAR_DISPLAY_NAME, // 8
            Instances.DESCRIPTION, // 9
        )

        val CALENDAR_PROJECTION = arrayOf(
            Calendars._ID, // 0
            Calendars.CALENDAR_DISPLAY_NAME, // 1
            Calendars.ACCOUNT_NAME, // 2
            Calendars.CALENDAR_COLOR, // 3
            Calendars.IS_PRIMARY, // 4
            Calendars.ACCOUNT_TYPE, // 5
        )

        val COMPARED_COLUMNS = arrayOf(
            Events.TITLE,
            Events.DESCRIPTION,
            Events.EVENT_LOCATION,
            Events.DTSTART,
            Events.DTEND,
            Events.ALL_DAY,
            Events.EVENT_TIMEZONE,
        )

        val EVENT_PROJECTION = arrayOf(
            Events._ID, // 0
            Events.CALENDAR_ID, // 1
            Events.TITLE, // 2
            Events.DESCRIPTION, // 3
            Events.EVENT_LOCATION, // 4
            Events.DTSTART, // 5
            Events.DTEND, // 6
            Events.ALL_DAY, // 7
            Events.DELETED, // 8
            Events.STATUS, // 9
            Events.CUSTOM_APP_PACKAGE, // 10
            Events.CUSTOM_APP_URI, // 11
        )
    }
}
