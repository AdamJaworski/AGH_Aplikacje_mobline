package com.example.apkmoblineprojekt.data

import android.util.Log
import com.example.apkmoblineprojekt.data.model.ExternalEvent
import com.example.apkmoblineprojekt.data.model.Task
import com.example.apkmoblineprojekt.data.model.TaskColor
import com.example.apkmoblineprojekt.data.model.target
import com.example.apkmoblineprojekt.system.DeviceCalendar
import com.example.apkmoblineprojekt.system.EventDraft
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.UUID

/**
 * Dekorator [TaskRepository] utrzymujący zadania z datą jako lustro kalendarza docelowego w telefonie
 * (np. głównego kalendarza Google):
 * - aplikacja → telefon: zapis/usunięcie zadania tworzy/aktualizuje/usuwa wydarzenie,
 * - telefon → aplikacja: dodanie/zmiana/usunięcie wydarzenia (także na innym urządzeniu przez
 *   konto Google) dodaje/aktualizuje/usuwa zadanie.
 *
 * Do zadań nie kopiujemy wydarzeń cyklicznych ani tych z innych kalendarzy (np. święta, urodziny) –
 * ekran kalendarza pokazuje je tylko do odczytu, z [DeviceCalendar.observeEvents].
 *
 * Działa niezależnie od implementacji [local], więc po przejściu na Room wystarczy podmienić
 * repozytorium wewnętrzne (z kolumną `calendarEventId`).
 */
class CalendarSyncingTaskRepository(
    private val local: TaskRepository,
    private val deviceCalendar: DeviceCalendar,
    private val settingsRepository: SettingsRepository,
    private val deletedEvents: DeletedEventStore,
    private val today: () -> LocalDate = LocalDate::now,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : TaskRepository {

    private val mutex = Mutex()

    /** Uruchamia nasłuch zmian w kalendarzu telefonu. Wołane raz, w zasięgu aplikacji. */
    fun start(scope: CoroutineScope) {
        scope.launch {
            deviceCalendar.changes().collect { pullFromDevice() }
        }
        // Po włączeniu synchronizacji, nadaniu uprawnień albo zmianie kalendarza docelowego
        // wysyłamy zadania do (właściwego) kalendarza telefonu.
        scope.launch {
            combine(settingsRepository.settings, deviceCalendar.access) { settings, access ->
                if (settings.calendarSyncEnabled && access) settings.calendarId ?: NO_CALENDAR else null
            }
                .distinctUntilChanged()
                .filterNotNull()
                .collect { pushAll() }
        }
    }

    override fun observeTasks(): Flow<List<Task>> = local.observeTasks()

    override suspend fun getTask(id: Long): Task? = local.getTask(id)

    override suspend fun saveTask(task: Task): Long = mutex.withLock {
        // Edytor buduje zadanie od nowa – powiązanie z wydarzeniem bierzemy z zapisanej wersji.
        val linked = task.calendarEventId ?: task.id.takeIf { it != 0L }?.let { local.getTask(it) }?.calendarEventId
        val id = local.saveTask(task.copy(calendarEventId = linked))
        val saved = task.copy(id = id, calendarEventId = linked)
        val eventId = push(saved)
        if (eventId != linked) local.saveTask(saved.copy(calendarEventId = eventId))
        id
    }

    override suspend fun setDone(id: Long, done: Boolean) = local.setDone(id, done)

    override suspend fun deleteTask(id: Long) {
        mutex.withLock {
            val eventId = local.getTask(id)?.calendarEventId
            local.deleteTask(id)
            if (eventId != null && deviceCalendar.access.value) {
                deleteLinkedEvent(eventId)
            }
        }
    }

    /** Wysyła zadanie do kalendarza. Zwraca id wydarzenia, z którym zadanie ma być powiązane. */
    private suspend fun push(task: Task): Long? {
        val linked = task.calendarEventId
        val settings = settingsRepository.settings.first()
        if (!settings.calendarSyncEnabled || !deviceCalendar.access.value) return linked
        val date = task.date
        if (date == null) {
            // Zadanie bez terminu nie ma miejsca w kalendarzu.
            if (linked != null) deleteLinkedEvent(linked)
            return null
        }
        val calendarId = targetCalendarId(settings.calendarId) ?: return linked
        val draft = EventDraft(
            tag = "$TAG_PREFIX${task.uid}",
            title = task.title,
            description = task.description,
            location = task.location?.name.orEmpty(),
            date = date,
            time = task.time,
        )
        return attempt("upsert event") { deviceCalendar.upsertEvent(linked, calendarId, draft) } ?: linked
    }

    /** Kalendarz z ustawień, jeśli nadal istnieje, w przeciwnym razie domyślny. */
    private suspend fun targetCalendarId(selectedId: Long?): Long? =
        deviceCalendar.writableCalendars().target(selectedId)?.id ?: deviceCalendar.defaultCalendarId()

    /**
     * Wysyła powiązane zadania (przeniesie je, jeśli zmienił się kalendarz) i nowe zadania od dziś.
     * Potem usuwa zapasowy lokalny kalendarz, jeśli w telefonie jest już prawdziwy.
     */
    private suspend fun pushAll() {
        val candidates = local.observeTasks().first().filter { task ->
            val date = task.date ?: return@filter false
            task.calendarEventId != null || (!task.done && date >= today())
        }
        candidates.forEach { saveTask(it) }

        attempt("cleanup local calendar") {
            val calendars = deviceCalendar.writableCalendars()
            val target = calendars.target(settingsRepository.settings.first().calendarId)
            if (target != null && !target.appLocal) {
                calendars.filter { it.appLocal }.forEach { deviceCalendar.deleteAppLocalCalendar(it.id) }
            }
        }
    }

    /**
     * Przenosi do zadań zmiany z kalendarza telefonu – zadania i wydarzenia kalendarza docelowego
     * są swoim lustrem:
     * - zmiana wydarzenia → zmiana zadania,
     * - usunięcie wydarzenia → usunięcie zadania,
     * - nowe wydarzenie (np. dodane w Google Calendar) → nowe zadanie.
     */
    suspend fun pullFromDevice(): Unit = mutex.withLock {
        val settings = settingsRepository.settings.first()
        if (!settings.calendarSyncEnabled || !deviceCalendar.access.value) return@withLock
        try {
            // Bez kalendarza docelowego (np. konto usunięte z telefonu) nie ruszamy zadań –
            // brak wydarzeń nie oznaczałby wtedy, że użytkownik je usunął.
            val target = deviceCalendar.writableCalendars().target(settings.calendarId) ?: return@withLock
            val deleted = enforceDeletions()
            applyDeviceChanges()
            importNewEvents(target.id, skip = deleted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Błąd dostępu ≠ usunięte wydarzenie – nic nie usuwamy, spróbujemy przy następnej zmianie.
            Log.w(TAG, "Pull aborted", e)
        }
    }

    /** Usuwa wydarzenie zadania i zapamiętuje to, by nie wróciło jako nowe zadanie. */
    private suspend fun deleteLinkedEvent(eventId: Long) {
        attempt("delete event") {
            deviceCalendar.deleteEvent(eventId)
            deletedEvents.add(eventId, nowMillis())
        }
    }

    /**
     * Ponownie usuwa wydarzenia, które synchronizacja konta przywróciła po usunięciu w aplikacji
     * (np. przy konflikcie z wersją z serwera). Zwraca id wszystkich pamiętanych usuniętych wydarzeń.
     */
    private suspend fun enforceDeletions(): Set<Long> {
        val now = nowMillis()
        val remembered = mutableSetOf<Long>()
        for ((eventId, deletedAt) in deletedEvents.all()) {
            if (now - deletedAt > DELETION_MEMORY_MILLIS) {
                deletedEvents.remove(eventId)
                continue
            }
            remembered += eventId
            if (deviceCalendar.getEvent(eventId) != null) {
                Log.i(TAG, "Event $eventId restored by sync – deleting again")
                deviceCalendar.deleteEvent(eventId)
            }
        }
        return remembered
    }

    private suspend fun applyDeviceChanges() {
        val linkedTasks = local.observeTasks().first().filter { it.calendarEventId != null }
        for (task in linkedTasks) {
            val event = deviceCalendar.getEvent(task.calendarEventId!!)
            if (event == null) {
                local.deleteTask(task.id)
            } else {
                val updated = task.mergedWith(event)
                if (updated != task) local.saveTask(updated)
            }
        }
    }

    private suspend fun importNewEvents(calendarId: Long, skip: Set<Long>) {
        val tasks = local.observeTasks().first()
        val linkedEventIds = tasks.mapNotNullTo(mutableSetOf()) { it.calendarEventId }
        val tasksByUid = tasks.associateBy { it.uid }
        val now = today()
        val events = deviceCalendar.calendarEvents(calendarId, now.minusDays(IMPORT_PAST_DAYS), now.plusDays(IMPORT_FUTURE_DAYS))
        for (event in events) {
            if (event.eventId in linkedEventIds || event.eventId in skip) continue
            val uid = event.tag?.removePrefix(TAG_PREFIX)
            val owner = uid?.let(tasksByUid::get)
            when {
                // Wydarzenie zadania, które straciło powiązanie (np. po restarcie aplikacji) – wiążemy ponownie.
                owner != null && owner.calendarEventId == null ->
                    local.saveTask(owner.copy(calendarEventId = event.eventId).mergedWith(event))
                // Duplikat wydarzenia już powiązanego zadania – pomijamy.
                owner != null -> Unit
                else -> local.saveTask(
                    Task(
                        title = event.title.ifBlank { "(bez tytułu)" },
                        calendarEventId = event.eventId,
                        color = TaskColor.BLUE,
                        uid = uid ?: UUID.randomUUID().toString(),
                    ).mergedWith(event),
                )
            }
        }
    }

    private fun Task.mergedWith(event: ExternalEvent): Task {
        val eventTime = if (event.allDay) null else event.start.toLocalTime().withSecond(0).withNano(0)
        return copy(
            title = event.title.ifBlank { title },
            description = event.description,
            date = event.start.toLocalDate(),
            time = eventTime,
            remindMinutesBefore = remindMinutesBefore.takeIf { eventTime != null },
        )
    }

    private suspend fun <T> attempt(what: String, block: suspend () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Calendar sync: $what failed", e)
        null
    }

    private companion object {
        const val TAG = "CalendarSync"
        const val TAG_PREFIX = "blisko://task/"
        const val NO_CALENDAR = -1L
        const val IMPORT_PAST_DAYS = 90L
        const val IMPORT_FUTURE_DAYS = 365L
        /** Jak długo pilnujemy, by usunięte wydarzenie nie wróciło przez synchronizację. */
        const val DELETION_MEMORY_MILLIS = 2 * 24 * 60 * 60 * 1000L
    }
}
