package com.example.apkmoblineprojekt.location

import com.example.apkmoblineprojekt.data.model.AppSettings
import java.time.LocalTime

/** Reguły z HLD §5 p. 3 – kiedy wolno pokazać przypomnienie lokalizacyjne. Czyste funkcje, łatwe do testów. */
object ReminderPolicy {
    /** Ponowne przypomnienie o tym samym zadaniu najwcześniej po 4 h. */
    const val MIN_INTERVAL_MILLIS = 4 * 60 * 60 * 1000L

    /** Limit Androida: 100 geofence'ów na aplikację. */
    const val MAX_GEOFENCES = 100

    fun intervalElapsed(lastNotifiedAt: Long?, now: Long): Boolean =
        lastNotifiedAt == null || now - lastNotifiedAt >= MIN_INTERVAL_MILLIS

    /** Cisza nocna może przechodzić przez północ (np. 22:00–07:00). */
    fun inQuietHours(settings: AppSettings, time: LocalTime): Boolean {
        if (!settings.quietHoursEnabled) return false
        val from = settings.quietFrom
        val to = settings.quietTo
        return when {
            from == to -> false
            from < to -> time >= from && time < to
            else -> time >= from || time < to
        }
    }

    /** Przy przekroczeniu limitu rejestrujemy strefy zadań z najbliższym terminem (bez terminu – na końcu). */
    fun selectForRegistration(zones: List<GeofenceZone>, limit: Int = MAX_GEOFENCES): List<GeofenceZone> =
        if (zones.size <= limit) zones else zones.sortedBy { it.dueAt ?: Long.MAX_VALUE }.take(limit)
}
