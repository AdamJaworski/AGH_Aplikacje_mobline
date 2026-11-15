package com.example.apkmoblineprojekt.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.apkmoblineprojekt.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Ustawienia trwałe między uruchomieniami aplikacji. */
class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val store = context.applicationContext.settingsDataStore

    override val settings: Flow<AppSettings> = store.data.map { it.toSettings() }.distinctUntilChanged()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.edit { prefs ->
            val updated = transform(prefs.toSettings())
            prefs[QUIET_ENABLED] = updated.quietHoursEnabled
            prefs[QUIET_FROM] = updated.quietFrom.toString()
            prefs[QUIET_TO] = updated.quietTo.toString()
            prefs[DEFAULT_RADIUS] = updated.defaultRadiusMeters
            prefs[DEFAULT_REMIND] = updated.defaultRemindMinutes
            prefs[CALENDAR_SYNC] = updated.calendarSyncEnabled
            val calendarId = updated.calendarId
            if (calendarId == null) prefs.remove(CALENDAR_ID) else prefs[CALENDAR_ID] = calendarId
        }
    }

    private fun Preferences.toSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            quietHoursEnabled = this[QUIET_ENABLED] ?: defaults.quietHoursEnabled,
            quietFrom = this[QUIET_FROM]?.let(LocalTime::parse) ?: defaults.quietFrom,
            quietTo = this[QUIET_TO]?.let(LocalTime::parse) ?: defaults.quietTo,
            defaultRadiusMeters = this[DEFAULT_RADIUS] ?: defaults.defaultRadiusMeters,
            defaultRemindMinutes = this[DEFAULT_REMIND] ?: defaults.defaultRemindMinutes,
            calendarSyncEnabled = this[CALENDAR_SYNC] ?: defaults.calendarSyncEnabled,
            calendarId = this[CALENDAR_ID] ?: defaults.calendarId,
        )
    }

    private companion object {
        val QUIET_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        val QUIET_FROM = stringPreferencesKey("quiet_from")
        val QUIET_TO = stringPreferencesKey("quiet_to")
        val DEFAULT_RADIUS = intPreferencesKey("default_radius_m")
        val DEFAULT_REMIND = intPreferencesKey("default_remind_min")
        val CALENDAR_SYNC = booleanPreferencesKey("calendar_sync_enabled")
        val CALENDAR_ID = longPreferencesKey("calendar_id")
    }
}
