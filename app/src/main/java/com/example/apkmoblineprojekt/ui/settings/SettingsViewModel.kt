package com.example.apkmoblineprojekt.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkmoblineprojekt.data.SettingsRepository
import com.example.apkmoblineprojekt.data.model.AppSettings
import com.example.apkmoblineprojekt.data.model.DeviceCalendarInfo
import com.example.apkmoblineprojekt.data.model.target
import com.example.apkmoblineprojekt.system.DeviceCalendar
import com.example.apkmoblineprojekt.system.PermissionChecker
import com.example.apkmoblineprojekt.system.PermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val permissions: PermissionState? = null,
    /** Zapisywalne kalendarze telefonu (puste bez uprawnień). */
    val calendars: List<DeviceCalendarInfo> = emptyList(),
) {
    val calendarSyncActive: Boolean
        get() = settings.calendarSyncEnabled && permissions?.calendar == true

    /** Kalendarz, do którego faktycznie trafią zadania. */
    val targetCalendar: DeviceCalendarInfo?
        get() = calendars.target(settings.calendarId)
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val permissionChecker: PermissionChecker,
    private val deviceCalendar: DeviceCalendar,
) : ViewModel() {

    private val permissions = MutableStateFlow<PermissionState?>(null)
    private val calendars = MutableStateFlow<List<DeviceCalendarInfo>>(emptyList())

    val uiState: StateFlow<SettingsUiState> =
        combine(settingsRepository.settings, permissions, calendars, ::SettingsUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** Wołane przy każdym powrocie na ekran – użytkownik mógł zmienić uprawnienia w systemie. */
    fun refreshPermissions() {
        permissions.value = permissionChecker.current()
        deviceCalendar.refresh()
        viewModelScope.launch { calendars.value = deviceCalendar.writableCalendars() }
    }

    fun setQuietHours(enabled: Boolean) = update { it.copy(quietHoursEnabled = enabled) }

    fun setDefaultRadius(meters: Int) = update { it.copy(defaultRadiusMeters = meters) }

    fun setDefaultRemind(minutes: Int) = update { it.copy(defaultRemindMinutes = minutes) }

    /** true → przed włączeniem synchronizacji trzeba poprosić o uprawnienia do kalendarza. */
    fun needsCalendarPermission(): Boolean = permissions.value?.calendar != true

    fun setCalendarSync(enabled: Boolean) = update { it.copy(calendarSyncEnabled = enabled) }

    fun onCalendarPermissionResult(granted: Boolean) {
        refreshPermissions()
        if (granted) setCalendarSync(true)
    }

    fun setCalendar(calendar: DeviceCalendarInfo) = update { it.copy(calendarId = calendar.id) }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    companion object {
        val RadiusOptions = listOf(100, 200, 300, 500, 1000)
    }
}
