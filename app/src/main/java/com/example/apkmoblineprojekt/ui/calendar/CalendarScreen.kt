package com.example.apkmoblineprojekt.ui.calendar

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.example.apkmoblineprojekt.data.model.ExternalEvent
import com.example.apkmoblineprojekt.ui.components.IconTile
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.data.SampleData
import com.example.apkmoblineprojekt.data.model.Task
import com.example.apkmoblineprojekt.ui.components.BliskoCard
import com.example.apkmoblineprojekt.ui.components.CircleIconButton
import com.example.apkmoblineprojekt.ui.components.Eyebrow
import com.example.apkmoblineprojekt.ui.components.ScreenHeader
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText
import com.example.apkmoblineprojekt.ui.theme.BliskoTheme
import com.example.apkmoblineprojekt.ui.util.PolishDates
import com.example.apkmoblineprojekt.ui.util.appViewModel
import com.example.apkmoblineprojekt.ui.util.color
import com.example.apkmoblineprojekt.ui.util.dayMonth
import com.example.apkmoblineprojekt.ui.util.displayName
import com.example.apkmoblineprojekt.ui.util.hhmm
import com.example.apkmoblineprojekt.ui.util.longTitle
import com.example.apkmoblineprojekt.ui.util.tasksCountLabel
import com.example.apkmoblineprojekt.ui.util.title
import com.example.apkmoblineprojekt.ui.util.weekRangeLabel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarRoute(
    onAddTask: (LocalDate) -> Unit,
    onOpenTask: (Task) -> Unit,
    onOpenMenu: () -> Unit,
) {
    val viewModel = appViewModel { CalendarViewModel(it.taskRepository, it.deviceCalendar, it.settingsRepository) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> viewModel.onCalendarPermissionResult(result.values.all { it }) }

    CalendarScreen(
        state = state,
        onSelectDate = viewModel::selectDate,
        onModeChange = viewModel::setMode,
        onPrevious = viewModel::showPrevious,
        onNext = viewModel::showNext,
        onToggleDone = viewModel::toggleDone,
        onOpenTask = onOpenTask,
        onOpenEvent = { context.openDeviceEvent(it) },
        onConnectCalendar = {
            if (viewModel.needsCalendarPermission()) {
                permissionLauncher.launch(CalendarPermissions)
            } else {
                viewModel.onCalendarPermissionResult(true)
            }
        },
        onDismissCalendarPrompt = viewModel::dismissCalendarPrompt,
        onAddTask = { onAddTask(state.selectedDate) },
        onOpenMenu = onOpenMenu,
    )
}

val CalendarPermissions = arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)

/** Otwiera wydarzenie w systemowej aplikacji kalendarza. */
private fun Context.openDeviceEvent(event: ExternalEvent) {
    val intent = Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId))
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.beginMillis)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endMillis)
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Brak aplikacji kalendarza – nic nie robimy.
    }
}

@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onSelectDate: (LocalDate) -> Unit,
    onModeChange: (CalendarMode) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToggleDone: (Task) -> Unit,
    onOpenTask: (Task) -> Unit,
    onAddTask: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenEvent: (ExternalEvent) -> Unit = {},
    onConnectCalendar: () -> Unit = {},
    onDismissCalendarPrompt: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 19.dp, end = 19.dp, top = 25.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            item {
                ScreenHeader(
                    eyebrow = YearMonth.from(state.today).title().uppercase(PolishDates.locale),
                    title = state.today.longTitle(),
                ) {
                    CircleIconButton(Icons.Outlined.Menu, stringResource(R.string.cd_menu), onOpenMenu)
                }
            }
            item {
                CalendarCard(state, onSelectDate, onModeChange, onPrevious, onNext)
            }
            if (state.showCalendarPrompt) {
                item {
                    CalendarConnectCard(onConnectCalendar, onDismissCalendarPrompt)
                }
            }
            item {
                DaySummary(state)
            }
            if (state.dayItems.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.calendar_empty_day),
                        Modifier.padding(horizontal = 2.dp, vertical = 12.dp),
                        style = BliskoText.Caption,
                    )
                }
            }
            // Jedna oś dnia: zadania (zsynchronizowane z kalendarzem) i wydarzenia tylko do odczytu
            // (np. święta, wydarzenia cykliczne) – posortowane razem po godzinie.
            items(state.dayItems, key = { it.key }) { item ->
                when (item) {
                    is DayItem.TaskItem -> TaskCard(
                        item.task,
                        onToggleDone = { onToggleDone(item.task) },
                        onClick = { onOpenTask(item.task) },
                    )
                    is DayItem.EventItem -> EventCard(item.event, onClick = { onOpenEvent(item.event) })
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddTask,
            icon = { Icon(Icons.Outlined.Add, null, Modifier.size(24.dp)) },
            text = {
                Text(
                    stringResource(R.string.calendar_new_task),
                    style = BliskoText.Button.copy(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                )
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = BliskoColors.Forest,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 21.dp, bottom = 14.dp)
                .height(51.dp),
        )
    }
}

@Composable
private fun CalendarCard(
    state: CalendarUiState,
    onSelectDate: (LocalDate) -> Unit,
    onModeChange: (CalendarMode) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    BliskoCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(23.dp), ambientColor = BliskoColors.Forest.copy(alpha = 0.1f), spotColor = BliskoColors.Forest.copy(alpha = 0.1f)),
        cornerRadius = 23.dp,
    ) {
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 13.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 2.dp, end = 2.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val previousLabel: String
                val nextLabel: String
                val rangeLabel: String
                if (state.mode == CalendarMode.WEEK) {
                    previousLabel = stringResource(R.string.cd_previous_week)
                    nextLabel = stringResource(R.string.cd_next_week)
                    rangeLabel = weekRangeLabel(state.visibleWeekStart)
                } else {
                    previousLabel = stringResource(R.string.cd_previous_month)
                    nextLabel = stringResource(R.string.cd_next_month)
                    rangeLabel = state.visibleMonth.title()
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ArrowButton(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, previousLabel, onPrevious)
                    Text(rangeLabel, style = BliskoText.Button.copy(fontSize = 12.sp))
                    ArrowButton(Icons.AutoMirrored.Outlined.KeyboardArrowRight, nextLabel, onNext)
                }
                ModeSwitch(state.mode, onModeChange)
            }
            when (state.mode) {
                CalendarMode.WEEK -> WeekRow(state, onSelectDate)
                CalendarMode.MONTH -> MonthGrid(state, onSelectDate)
            }
        }
    }
}

@Composable
private fun ArrowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Icon(
        icon,
        contentDescription,
        Modifier
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(4.dp)
            .size(18.dp),
        tint = Color(0xFFA0A6A2),
    )
}

@Composable
private fun ModeSwitch(mode: CalendarMode, onModeChange: (CalendarMode) -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BliskoColors.Segment)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(
            CalendarMode.WEEK to R.string.calendar_mode_week_short,
            CalendarMode.MONTH to R.string.calendar_mode_month_short,
        ).forEach { (option, label) ->
            val selected = option == mode
            Box(
                Modifier
                    .size(width = 29.dp, height = 25.dp)
                    .then(if (selected) Modifier.shadow(2.dp, RoundedCornerShape(7.dp)) else Modifier)
                    .clip(RoundedCornerShape(7.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable(role = Role.Tab) { onModeChange(option) }
                    .semantics { this.selected = selected },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(label),
                    style = BliskoText.Eyebrow.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.sp,
                        color = if (selected) BliskoColors.Forest else BliskoColors.Label,
                    ),
                )
            }
        }
    }
}

@Composable
private fun WeekRow(state: CalendarUiState, onSelectDate: (LocalDate) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(7) { index ->
            val date = state.visibleWeekStart.plusDays(index.toLong())
            val selected = date == state.selectedDate
            val shape = RoundedCornerShape(14.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(68.dp)
                    .then(if (selected) Modifier.shadow(8.dp, shape, spotColor = BliskoColors.Forest) else Modifier)
                    .clip(shape)
                    .background(if (selected) BliskoColors.Forest else Color.Transparent)
                    .clickable { onSelectDate(date) }
                    .semantics { this.selected = selected },
            ) {
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val color = if (selected) Color.White else Color(0xFF5C6560)
                    Text(
                        PolishDates.weekdayShort[index],
                        style = BliskoText.Eyebrow.copy(fontSize = 8.sp, letterSpacing = 0.04.em, color = color),
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        date.dayOfMonth.toString(),
                        style = BliskoText.Button.copy(
                            fontSize = 15.sp,
                            color = color,
                            fontWeight = if (date == state.today) FontWeight.ExtraBold else FontWeight.Bold,
                        ),
                    )
                }
                state.dotColor(date, selected)?.let { dotColor ->
                    Dot(
                        color = dotColor,
                        size = 4.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(state: CalendarUiState, onSelectDate: (LocalDate) -> Unit) {
    val month = state.visibleMonth
    val leadingBlanks = month.atDay(1).dayOfWeek.value - 1
    val cells = leadingBlanks + month.lengthOfMonth()
    val rows = (cells + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            PolishDates.weekdayInitials.forEach { label ->
                Text(
                    label,
                    Modifier
                        .weight(1f)
                        .padding(top = 4.dp, bottom = 5.dp),
                    style = BliskoText.Eyebrow.copy(fontSize = 9.sp, letterSpacing = 0.sp, color = Color(0xFF9B9F9C)),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        repeat(rows) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(7) { column ->
                    val day = row * 7 + column - leadingBlanks + 1
                    if (day < 1 || day > month.lengthOfMonth()) {
                        Spacer(Modifier.weight(1f).height(31.dp))
                    } else {
                        val date = month.atDay(day)
                        val selected = date == state.selectedDate
                        Box(
                            Modifier
                                .weight(1f)
                                .height(31.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selected) BliskoColors.Forest else Color.Transparent)
                                .clickable { onSelectDate(date) }
                                .semantics { this.selected = selected },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                day.toString(),
                                style = BliskoText.Button.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (selected || date == state.today) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) Color.White else Color(0xFF626A66),
                                ),
                            )
                            state.dotColor(date, selected)?.let { dotColor ->
                                Dot(
                                    color = dotColor,
                                    size = 3.dp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 3.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Dot(color: Color, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .background(color, CircleShape),
    )
}

@Composable
private fun DaySummary(state: CalendarUiState) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, end = 2.dp, top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Eyebrow(
                if (state.selectedDate == state.today) {
                    stringResource(R.string.calendar_today)
                } else {
                    state.selectedDate.dayMonth().uppercase(PolishDates.locale)
                },
            )
            Text(stringResource(R.string.calendar_todo), style = BliskoText.SectionTitle)
        }
        Text(tasksCountLabel(state.openTaskCount), style = BliskoText.Caption)
    }
}

@Composable
fun TaskCard(task: Task, onToggleDone: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = task.color.color
    BliskoCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (task.done) 0.56f else 1f),
        onClick = onClick,
    ) {
        Box {
            Row(
                Modifier
                    .heightIn(min = 94.dp)
                    .padding(start = 16.dp, end = 17.dp, top = 17.dp, bottom = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                TaskCheckbox(task, accent, onToggleDone)
                Column(Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = BliskoText.CardTitle.copy(
                            textDecoration = if (task.done) TextDecoration.LineThrough else null,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            task.description,
                            Modifier.padding(top = 4.dp),
                            style = BliskoText.Detail,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        Modifier.padding(top = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        task.location?.let { MetaItem(Icons.Outlined.LocationOn, it.displayName()) }
                        task.time?.let { MetaItem(Icons.Outlined.Schedule, it.hhmm()) }
                    }
                }
            }
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp)
                    .size(width = 3.dp, height = 31.dp)
                    .background(accent, RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp)),
            )
        }
    }
}

/** Kropka pod dniem: koralowa – zadania z aplikacji, niebieska – tylko wydarzenia z telefonu. */
private fun CalendarUiState.dotColor(date: LocalDate, selected: Boolean): Color? = when (date) {
    in datesWithTasks -> if (selected) BliskoColors.CoralSoft else BliskoColors.Coral
    in datesWithEvents -> if (selected) Color.White.copy(alpha = 0.75f) else BliskoColors.Blue
    else -> null
}

@Composable
private fun CalendarConnectCard(onConnect: () -> Unit, onDismiss: () -> Unit) {
    BliskoCard(Modifier.fillMaxWidth().padding(top = 8.dp), color = BliskoColors.CoralTint) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Outlined.CalendarMonth, background = Color.White, tint = BliskoColors.Coral)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.calendar_connect_title), style = BliskoText.CardTitle)
                    Text(
                        stringResource(R.string.calendar_connect_body),
                        Modifier.padding(top = 3.dp),
                        style = BliskoText.Detail,
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.calendar_connect_later), style = BliskoText.Button.copy(color = BliskoColors.Muted))
                }
                TextButton(onClick = onConnect) {
                    Text(stringResource(R.string.calendar_connect_action), style = BliskoText.Button.copy(color = BliskoColors.Forest))
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: ExternalEvent, onClick: () -> Unit) {
    val accent = if (event.color != 0) Color(event.color) else BliskoColors.Blue
    BliskoCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier
                .heightIn(min = 70.dp)
                .padding(start = 16.dp, end = 17.dp, top = 14.dp, bottom = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                Modifier
                    .padding(top = 2.dp)
                    .size(width = 4.dp, height = 36.dp)
                    .background(accent, RoundedCornerShape(2.dp)),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    event.title.ifBlank { stringResource(R.string.calendar_event_untitled) },
                    style = BliskoText.CardTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    Modifier.padding(top = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val time = if (event.allDay) {
                        stringResource(R.string.calendar_event_all_day)
                    } else {
                        "${event.start.toLocalTime().hhmm()}–${event.end.toLocalTime().hhmm()}"
                    }
                    MetaItem(Icons.Outlined.Schedule, time)
                    if (event.location.isNotBlank()) {
                        MetaItem(Icons.Outlined.LocationOn, event.location)
                    }
                }
                // Główny kalendarz konta Google nazywa się adresem e-mail – tego nie pokazujemy.
                val calendarLabel = event.calendarName.takeUnless { it.isBlank() || '@' in it }
                if (calendarLabel != null) {
                    Text(
                        calendarLabel,
                        Modifier.padding(top = 5.dp),
                        style = BliskoText.Small,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCheckbox(task: Task, accent: Color, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val label = if (task.done) {
        stringResource(R.string.cd_task_restore, task.title)
    } else {
        stringResource(R.string.cd_task_mark_done, task.title)
    }
    Box(
        Modifier
            .padding(top = 1.dp)
            .size(23.dp)
            .clip(shape)
            .background(if (task.done) accent else Color.Transparent)
            .border(1.5.dp, accent, shape)
            .clickable(role = Role.Checkbox, onClickLabel = label, onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        if (task.done) Icon(Icons.Outlined.Check, label, Modifier.size(15.dp), tint = Color.White)
    }
}

@Composable
private fun MetaItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(14.dp), tint = BliskoColors.MetaText)
        Spacer(Modifier.width(4.dp))
        Text(text, style = BliskoText.Meta)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6F1, widthDp = 412, heightDp = 860)
@Composable
private fun CalendarScreenPreview() {
    val today = LocalDate.now()
    val tasks = SampleData.tasks(today)
    BliskoTheme {
        CalendarScreen(
            state = CalendarUiState(
                today = today,
                selectedDate = today,
                datesWithTasks = tasks.mapNotNull { it.date }.toSet(),
                tasks = tasks.filter { it.date == today || it.date == null },
            ),
            onSelectDate = {}, onModeChange = {}, onPrevious = {}, onNext = {},
            onToggleDone = {}, onOpenTask = {}, onAddTask = {}, onOpenMenu = {},
        )
    }
}
