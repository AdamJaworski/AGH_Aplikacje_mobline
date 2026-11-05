package com.example.apkmoblineprojekt.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.data.model.AppSettings
import com.example.apkmoblineprojekt.data.model.DeviceCalendarInfo
import com.example.apkmoblineprojekt.ui.calendar.CalendarPermissions
import com.example.apkmoblineprojekt.system.LocationAccess
import com.example.apkmoblineprojekt.system.PermissionState
import com.example.apkmoblineprojekt.ui.components.BliskoCard
import com.example.apkmoblineprojekt.ui.components.BliskoSwitch
import com.example.apkmoblineprojekt.ui.components.ChevronRight
import com.example.apkmoblineprojekt.ui.components.Eyebrow
import com.example.apkmoblineprojekt.ui.components.IconTile
import com.example.apkmoblineprojekt.ui.components.ScreenHeader
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText
import com.example.apkmoblineprojekt.ui.theme.BliskoTheme
import com.example.apkmoblineprojekt.ui.util.ReminderOptions
import com.example.apkmoblineprojekt.ui.util.appViewModel
import com.example.apkmoblineprojekt.ui.util.formatDistance
import com.example.apkmoblineprojekt.ui.util.hhmm
import com.example.apkmoblineprojekt.ui.util.reminderLabel

@Composable
fun SettingsRoute() {
    val viewModel = appViewModel { SettingsViewModel(it.settingsRepository, it.permissionChecker, it.deviceCalendar) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> viewModel.onCalendarPermissionResult(result.values.all { it }) }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissions()
        onPauseOrDispose { }
    }

    SettingsScreen(
        state = state,
        // Prośby o uprawnienia w momencie potrzeby (HLD §8) – tutaj tylko skrót do ustawień systemu.
        onOpenSystemSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
            )
        },
        onQuietHoursChange = viewModel::setQuietHours,
        onRadiusChange = viewModel::setDefaultRadius,
        onRemindChange = viewModel::setDefaultRemind,
        onCalendarSyncChange = { enabled ->
            if (enabled && viewModel.needsCalendarPermission()) {
                calendarPermissionLauncher.launch(CalendarPermissions)
            } else {
                viewModel.setCalendarSync(enabled)
            }
        },
        onCalendarSelect = viewModel::setCalendar,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onOpenSystemSettings: () -> Unit,
    onQuietHoursChange: (Boolean) -> Unit,
    onRadiusChange: (Int) -> Unit,
    onRemindChange: (Int) -> Unit,
    onCalendarSyncChange: (Boolean) -> Unit = {},
    onCalendarSelect: (DeviceCalendarInfo) -> Unit = {},
) {
    val settings = state.settings
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 19.dp, end = 19.dp, top = 25.dp, bottom = 32.dp),
    ) {
        ScreenHeader(
            eyebrow = stringResource(R.string.settings_eyebrow),
            title = stringResource(R.string.settings_title),
        )
        StatusPanel(state.permissions)

        SettingsTitle(stringResource(R.string.settings_permissions))
        SettingsGroup {
            val notificationsOn = state.permissions?.notificationsEnabled == true
            SettingRow(
                icon = Icons.Outlined.Notifications,
                label = stringResource(R.string.settings_notifications),
                value = stringResource(if (notificationsOn) R.string.settings_enabled else R.string.settings_disabled),
                valueColor = if (notificationsOn) BliskoColors.Good else BliskoColors.Coral,
                onClick = onOpenSystemSettings,
            )
            GroupDivider()
            val location = state.permissions?.location ?: LocationAccess.NONE
            SettingRow(
                icon = Icons.Outlined.MyLocation,
                label = stringResource(R.string.settings_location),
                value = stringResource(
                    when (location) {
                        LocationAccess.ALWAYS -> R.string.settings_location_always
                        LocationAccess.WHILE_IN_USE -> R.string.settings_location_in_use
                        LocationAccess.NONE -> R.string.settings_location_none
                    },
                ),
                valueColor = if (location == LocationAccess.ALWAYS) BliskoColors.Good else BliskoColors.Coral,
                onClick = onOpenSystemSettings,
            )
            GroupDivider()
            val calendarOn = state.permissions?.calendar == true
            SettingRow(
                icon = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.settings_calendar_permission),
                value = stringResource(if (calendarOn) R.string.settings_enabled else R.string.settings_location_none),
                valueColor = if (calendarOn) BliskoColors.Good else BliskoColors.Muted,
                onClick = onOpenSystemSettings,
            )
        }

        SettingsTitle(stringResource(R.string.settings_calendar))
        SettingsGroup {
            Row(
                Modifier
                    .heightIn(min = 58.dp)
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                IconTile(Icons.Outlined.Sync)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_calendar_sync), style = BliskoText.Button)
                    Text(
                        stringResource(R.string.settings_calendar_sync_body),
                        Modifier.padding(top = 2.dp),
                        style = BliskoText.Small,
                    )
                }
                BliskoSwitch(
                    checked = state.calendarSyncActive,
                    onCheckedChange = onCalendarSyncChange,
                    contentDescription = stringResource(R.string.settings_calendar_sync),
                )
            }
            val target = state.targetCalendar
            if (state.calendarSyncActive && target != null) {
                GroupDivider()
                val labels = calendarLabels(state.calendars)
                OptionSettingRow(
                    icon = Icons.Outlined.CalendarMonth,
                    label = stringResource(R.string.settings_calendar_target),
                    value = labels[target.id] ?: calendarLabel(target),
                    options = state.calendars,
                    selected = target,
                    optionLabel = { calendar -> labels[calendar.id] ?: calendarLabel(calendar) },
                    onSelect = onCalendarSelect,
                )
            }
        }

        SettingsTitle(stringResource(R.string.settings_reminders))
        SettingsGroup {
            Row(
                Modifier
                    .heightIn(min = 58.dp)
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                IconTile(Icons.Outlined.Schedule)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_quiet_hours), style = BliskoText.Button)
                    Text(
                        "${settings.quietFrom.hhmm()}–${settings.quietTo.hhmm()}",
                        Modifier.padding(top = 2.dp),
                        style = BliskoText.Small,
                    )
                }
                BliskoSwitch(
                    checked = settings.quietHoursEnabled,
                    onCheckedChange = onQuietHoursChange,
                    contentDescription = stringResource(R.string.settings_quiet_hours),
                )
            }
            GroupDivider()
            OptionSettingRow(
                icon = Icons.Outlined.Navigation,
                label = stringResource(R.string.settings_default_radius),
                value = formatDistance(settings.defaultRadiusMeters),
                options = SettingsViewModel.RadiusOptions,
                selected = settings.defaultRadiusMeters,
                optionLabel = { formatDistance(it) },
                onSelect = onRadiusChange,
            )
            GroupDivider()
            OptionSettingRow(
                icon = Icons.Outlined.Notifications,
                label = stringResource(R.string.settings_default_remind),
                value = reminderShort(settings.defaultRemindMinutes),
                options = ReminderOptions,
                selected = settings.defaultRemindMinutes,
                optionLabel = { reminderLabel(it) },
                onSelect = onRemindChange,
            )
        }

        Text(
            stringResource(R.string.settings_footnote),
            Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 17.dp),
            style = BliskoText.Small.copy(color = Color(0xFF8C928F), lineHeight = 14.sp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun reminderShort(minutes: Int): String =
    if (minutes == 0) stringResource(R.string.reminder_on_time) else stringResource(R.string.minutes_short, minutes)

@Composable
private fun StatusPanel(permissions: PermissionState?) {
    val ok = permissions?.remindersFullyWorking ?: true
    Row(
        Modifier
            .fillMaxWidth()
            .background(BliskoColors.Forest, RoundedCornerShape(21.dp))
            .padding(18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IconTile(
            Icons.Outlined.NotificationsActive,
            background = Color.White.copy(alpha = 0.12f),
            tint = Color(0xFFFFE9DF),
            size = 43.dp,
            cornerRadius = 14.dp,
            iconSize = 22.dp,
        )
        Column {
            Eyebrow(
                stringResource(if (ok) R.string.settings_status_ok_eyebrow else R.string.settings_status_warn_eyebrow),
                color = BliskoColors.CoralSoft,
            )
            Text(
                stringResource(if (ok) R.string.settings_status_ok_title else R.string.settings_status_warn_title),
                Modifier.padding(bottom = 5.dp),
                style = BliskoText.NoteTitle.copy(color = Color.White),
            )
            Text(
                stringResource(if (ok) R.string.settings_status_ok_body else R.string.settings_status_warn_body),
                style = BliskoText.Small.copy(fontSize = 9.5.sp, lineHeight = 14.sp, color = Color(0xFFD1DDD8)),
            )
        }
    }
}

@Composable
private fun SettingsTitle(text: String) {
    Text(
        text,
        Modifier.padding(start = 2.dp, end = 2.dp, top = 25.dp, bottom = 10.dp),
        style = BliskoText.Button.copy(fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold),
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    BliskoCard(Modifier.fillMaxWidth()) {
        Column { content() }
    }
}

@Composable
private fun GroupDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BliskoColors.Line),
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    valueColor: Color = BliskoColors.Muted,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 58.dp)
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        IconTile(icon)
        Text(label, Modifier.weight(1f), style = BliskoText.Button)
        Text(value, style = BliskoText.Button.copy(fontSize = 10.sp, color = valueColor))
        ChevronRight()
    }
}

@Composable
private fun <T> OptionSettingRow(
    icon: ImageVector,
    label: String,
    value: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        SettingRow(icon, label, value, onClick = { expanded = true })
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = BliskoColors.Card,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    trailingIcon = if (option == selected) {
                        { Icon(Icons.Outlined.Check, null, tint = BliskoColors.Forest) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6F1, widthDp = 412, heightDp = 800)
@Composable
private fun SettingsScreenPreview() {
    BliskoTheme {
        SettingsScreen(
            SettingsUiState(AppSettings(), PermissionState(true, LocationAccess.ALWAYS)),
            {}, {}, {}, {},
        )
    }
}

/**
 * Czytelna nazwa kalendarza. Główny kalendarz konta (np. Google) nazywa się adresem e-mail –
 * wtedy pokazujemy nazwę usługi, np. „Kalendarz Google”.
 */
@Composable
private fun calendarLabel(calendar: DeviceCalendarInfo): String {
    val name = calendar.name
    if (name.isNotBlank() && '@' !in name) return name
    return when (calendar.accountType) {
        "com.google" -> stringResource(R.string.settings_calendar_google)
        else -> stringResource(R.string.settings_calendar_default)
    }
}

/** Etykiety wszystkich kalendarzy; adres konta dopisujemy tylko, gdy etykiety by się powtarzały. */
@Composable
private fun calendarLabels(calendars: List<DeviceCalendarInfo>): Map<Long, String> {
    val base = calendars.associate { it.id to calendarLabel(it) }
    val duplicated = base.values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
    return calendars.associate { calendar ->
        val label = base.getValue(calendar.id)
        calendar.id to if (label in duplicated && calendar.accountName.isNotBlank()) {
            "$label · ${calendar.accountName}"
        } else {
            label
        }
    }
}
