package com.example.apkmoblineprojekt.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wydarzenia kalendarza telefonu usunięte przez aplikację (id → czas usunięcia w ms).
 * Synchronizacja konta (np. Google) potrafi przywrócić świeżo usunięte wydarzenie – dzięki tej
 * liście usuwamy je ponownie, zamiast importować jako nowe zadanie.
 */
interface DeletedEventStore {
    suspend fun all(): Map<Long, Long>
    suspend fun add(eventId: Long, deletedAtMillis: Long)
    suspend fun remove(eventId: Long)
}

class SharedPrefsDeletedEventStore(context: Context) : DeletedEventStore {
    private val prefs = context.applicationContext.getSharedPreferences("deleted_calendar_events", Context.MODE_PRIVATE)

    override suspend fun all(): Map<Long, Long> = withContext(Dispatchers.IO) {
        prefs.all.mapNotNull { (key, value) ->
            val id = key.toLongOrNull() ?: return@mapNotNull null
            id to ((value as? Long) ?: 0L)
        }.toMap()
    }

    override suspend fun add(eventId: Long, deletedAtMillis: Long) = withContext(Dispatchers.IO) {
        prefs.edit().putLong(eventId.toString(), deletedAtMillis).apply()
    }

    override suspend fun remove(eventId: Long) = withContext(Dispatchers.IO) {
        prefs.edit().remove(eventId.toString()).apply()
    }
}
