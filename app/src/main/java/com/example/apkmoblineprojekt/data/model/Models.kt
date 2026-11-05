package com.example.apkmoblineprojekt.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/** Zadanie z kalendarza (HLD §7). `date == null` – zadanie bez terminu, np. tylko z miejscem. */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val remindMinutesBefore: Int? = null,
    val location: TaskLocation? = null,
    val subtasks: List<Subtask> = emptyList(),
    val done: Boolean = false,
    val color: TaskColor = TaskColor.CORAL,
    /** Id wydarzenia w kalendarzu telefonu (CalendarContract), jeśli zadanie jest zsynchronizowane. */
    val calendarEventId: Long? = null,
    /**
     * Stały, globalnie unikalny identyfikator – w przeciwieństwie do [id] nie powtarza się między
     * instalacjami/sesjami. Służy jako znacznik wydarzenia w kalendarzu telefonu.
     */
    val uid: String = UUID.randomUUID().toString(),
)

data class Subtask(
    val id: Long = 0,
    val title: String,
    val done: Boolean = false,
)

enum class TaskColor { CORAL, BLUE, GREEN }

enum class LocationType { POINT, CATEGORY, HOME, WORK, SAVED }

/**
 * Gdzie zadanie ma przypomnieć o sobie. Dla [LocationType.CATEGORY] nazwa jest pusta,
 * a wyświetla się etykieta kategorii; dla HOME/WORK/SAVED [placeId] wskazuje [Place].
 */
data class TaskLocation(
    val type: LocationType,
    val name: String = "",
    val category: PlaceCategory? = null,
    val placeId: Long? = null,
    val radiusMeters: Int = DEFAULT_RADIUS_METERS,
)

/** Zamknięta lista kategorii (HLD §6), każda mapowana na tag OpenStreetMap. */
enum class PlaceCategory(val osmTag: String) {
    GROCERY("shop=supermarket"),
    PHARMACY("amenity=pharmacy"),
    PARCEL_LOCKER("amenity=parcel_locker"),
    HARDWARE_STORE("shop=hardware"),
    POST_OFFICE("amenity=post_office"),
    ATM("amenity=atm"),
}

enum class PlaceKind { HOME, WORK, SAVED }

data class Place(
    val id: Long = 0,
    val name: String,
    val address: String,
    val kind: PlaceKind,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

/** Wynik wyszukiwania „W pobliżu”. */
data class NearbyPlace(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val distanceMeters: Int,
    val openUntil: LocalTime? = null,
    val alwaysOpen: Boolean = false,
)

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val pinned: Boolean = false,
    val updatedAt: LocalDateTime,
    val taskId: Long? = null,
)

data class AppSettings(
    val quietHoursEnabled: Boolean = true,
    val quietFrom: LocalTime = LocalTime.of(22, 0),
    val quietTo: LocalTime = LocalTime.of(7, 0),
    val defaultRadiusMeters: Int = DEFAULT_RADIUS_METERS,
    val defaultRemindMinutes: Int = 15,
    /** Dwukierunkowa synchronizacja z kalendarzem telefonu. */
    val calendarSyncEnabled: Boolean = false,
    /** Kalendarz, do którego zapisujemy zadania; `null` – główny kalendarz telefonu. */
    val calendarId: Long? = null,
)

/** Kalendarz z telefonu (np. konto Google), do którego aplikacja może zapisywać. */
data class DeviceCalendarInfo(
    val id: Long,
    val name: String,
    val accountName: String,
    val color: Int,
    val primary: Boolean,
    val accountType: String = "",
    /** Zapasowy lokalny kalendarz „Blisko” – niewidoczny np. w aplikacji Google Calendar. */
    val appLocal: Boolean = false,
)

/**
 * Domyślny kalendarz do zapisu: główny kalendarz prawdziwego konta (Google itp.),
 * a lokalny kalendarz aplikacji dopiero w ostateczności.
 */
fun List<DeviceCalendarInfo>.preferredDefault(): DeviceCalendarInfo? {
    val real = filterNot { it.appLocal }
    return real.firstOrNull { it.primary && it.accountType == GOOGLE_ACCOUNT_TYPE }
        ?: real.firstOrNull { it.primary }
        ?: real.firstOrNull()
        ?: firstOrNull()
}

/** Kalendarz z ustawień, jeśli nadal istnieje; w przeciwnym razie [preferredDefault]. */
fun List<DeviceCalendarInfo>.target(selectedId: Long?): DeviceCalendarInfo? =
    firstOrNull { it.id == selectedId } ?: preferredDefault()

private const val GOOGLE_ACCOUNT_TYPE = "com.google"

/**
 * Wystąpienie wydarzenia z kalendarza telefonu. Dla wydarzeń całodniowych [start]/[end]
 * to północ danego dnia (koniec wyłącznie), a [allDay] == true.
 */
data class ExternalEvent(
    val eventId: Long,
    val calendarId: Long,
    val calendarName: String,
    val title: String,
    val description: String,
    val location: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean,
    val color: Int,
    val beginMillis: Long,
    val endMillis: Long,
    /** `CUSTOM_APP_URI` wydarzenia zapisanego przez tę aplikację (wskazuje uid zadania). */
    val tag: String? = null,
) {
    fun occursOn(day: LocalDate): Boolean {
        val dayStart = day.atStartOfDay()
        val dayEnd = day.plusDays(1).atStartOfDay()
        return if (end > start) start < dayEnd && end > dayStart else start.toLocalDate() == day
    }
}

data class AiSuggestion(
    val subtasks: List<String>,
    val location: TaskLocation?,
)

const val DEFAULT_RADIUS_METERS = 300
