package com.example.apkmoblineprojekt.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkmoblineprojekt.data.SettingsRepository
import com.example.apkmoblineprojekt.data.TaskRepository
import com.example.apkmoblineprojekt.data.model.ExternalEvent
import com.example.apkmoblineprojekt.data.model.Task
import com.example.apkmoblineprojekt.system.DeviceCalendar
import com.example.apkmoblineprojekt.ui.util.startOfWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

enum class CalendarMode { WEEK, MONTH }

data class CalendarUiState(
    val today: LocalDate,
    val selectedDate: LocalDate,
    val mode: CalendarMode = CalendarMode.WEEK,
    val visibleWeekStart: LocalDate = selectedDate.startOfWeek(),
    val visibleMonth: YearMonth = YearMonth.from(selectedDate),
    val datesWithTasks: Set<LocalDate> = emptySet(),
    val datesWithEvents: Set<LocalDate> = emptySet(),
    val tasks: List<Task> = emptyList(),
    /** Wydarzenia z kalendarza telefonu w wybranym dniu (bez tych, które są zadaniami z aplikacji). */
    val events: List<ExternalEvent> = emptyList(),
    val showCalendarPrompt: Boolean = false,
) {
    val openTaskCount: Int get() = tasks.count { !it.done }

    /** Zadania i wydarzenia wybranego dnia na jednej osi czasu; zrobione zadania na końcu. */
    val dayItems: List<DayItem> by lazy {
        (tasks.map(DayItem::TaskItem) + events.map(DayItem::EventItem))
            .sortedWith(compareBy<DayItem> { it.done }.thenBy(nullsFirst()) { it.time })
    }
}

sealed interface DayItem {
    val key: String
    val done: Boolean
    val time: LocalTime?

    data class TaskItem(val task: Task) : DayItem {
        override val key get() = "task-${task.id}"
        override val done get() = task.done
        override val time get() = task.time
    }

    data class EventItem(val event: ExternalEvent) : DayItem {
        override val key get() = "event-${event.eventId}-${event.beginMillis}"
        override val done get() = false
        override val time: LocalTime? get() = if (event.allDay) null else event.start.toLocalTime()
    }
}

private data class CalendarSelection(
    val selectedDate: LocalDate,
    val mode: CalendarMode,
    val weekStart: LocalDate,
    val month: YearMonth,
) {
    /** Zakres dni widoczny w kalendarzu (plus wybrany dzień), dla którego pobieramy wydarzenia. */
    val eventRange: Pair<LocalDate, LocalDate>
        get() {
            val (from, to) = when (mode) {
                CalendarMode.WEEK -> weekStart to weekStart.plusDays(7)
                CalendarMode.MONTH -> month.atDay(1) to month.atEndOfMonth().plusDays(1)
            }
            return minOf(from, selectedDate) to maxOf(to, selectedDate.plusDays(1))
        }
}

private data class CalendarSyncState(val enabled: Boolean, val access: Boolean) {
    val active: Boolean get() = enabled && access
}

class CalendarViewModel(
    private val taskRepository: TaskRepository,
    private val deviceCalendar: DeviceCalendar,
    private val settingsRepository: SettingsRepository,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {

    private val selection = MutableStateFlow(
        CalendarSelection(today, CalendarMode.WEEK, today.startOfWeek(), YearMonth.from(today)),
    )
    private val promptDismissed = MutableStateFlow(false)

    private val syncState = combine(settingsRepository.settings, deviceCalendar.access) { settings, access ->
        CalendarSyncState(settings.calendarSyncEnabled, access)
    }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val events = combine(selection.map { it.eventRange }.distinctUntilChanged(), syncState) { range, sync ->
        range to sync.active
    }.flatMapLatest { (range, active) ->
        if (active) deviceCalendar.observeEvents(range.first, range.second) else flowOf(emptyList())
    }

    private val prompt = combine(syncState, promptDismissed) { sync, dismissed -> !sync.active && !dismissed }

    val uiState: StateFlow<CalendarUiState> =
        combine(taskRepository.observeTasks(), selection, events, prompt) { tasks, sel, events, prompt ->
            val linkedEventIds = tasks.mapNotNullTo(mutableSetOf()) { it.calendarEventId }
            val external = events.filterNot { it.eventId in linkedEventIds }
            CalendarUiState(
                today = today,
                selectedDate = sel.selectedDate,
                mode = sel.mode,
                visibleWeekStart = sel.weekStart,
                visibleMonth = sel.month,
                datesWithTasks = tasks.mapNotNullTo(mutableSetOf()) { it.date },
                datesWithEvents = external.flatMapTo(mutableSetOf()) { it.coveredDates(sel.eventRange) },
                tasks = tasks.filter { it.isOn(sel.selectedDate) }
                    .sortedWith(compareBy<Task> { it.done }.thenBy { it.time }),
                events = external.filter { it.occursOn(sel.selectedDate) }
                    .sortedWith(compareByDescending<ExternalEvent> { it.allDay }.thenBy { it.start }),
                showCalendarPrompt = prompt,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            CalendarUiState(today = today, selectedDate = today),
        )

    /** Zadania bez daty (np. tylko z miejscem) pokazujemy w dniu dzisiejszym. */
    private fun Task.isOn(day: LocalDate) = date == day || (date == null && day == today)

    private fun ExternalEvent.coveredDates(range: Pair<LocalDate, LocalDate>): Sequence<LocalDate> =
        generateSequence(maxOf(start.toLocalDate(), range.first)) { it.plusDays(1) }
            .takeWhile { it < range.second }
            .takeWhile { occursOn(it) }

    fun selectDate(date: LocalDate) {
        selection.update {
            it.copy(selectedDate = date, weekStart = date.startOfWeek(), month = YearMonth.from(date))
        }
    }

    fun setMode(mode: CalendarMode) {
        selection.update {
            it.copy(
                mode = mode,
                weekStart = it.selectedDate.startOfWeek(),
                month = YearMonth.from(it.selectedDate),
            )
        }
    }

    fun showPrevious() = shift(-1)

    fun showNext() = shift(1)

    private fun shift(step: Long) {
        selection.update {
            when (it.mode) {
                CalendarMode.WEEK -> it.copy(weekStart = it.weekStart.plusWeeks(step))
                CalendarMode.MONTH -> it.copy(month = it.month.plusMonths(step))
            }
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch { taskRepository.setDone(task.id, !task.done) }
    }

    /** true → trzeba najpierw poprosić o uprawnienia; false → wystarczyło włączyć synchronizację. */
    fun needsCalendarPermission(): Boolean = !deviceCalendar.access.value

    /** Wynik prośby o uprawnienia (albo od razu true, gdy już je mamy). */
    fun onCalendarPermissionResult(granted: Boolean) {
        deviceCalendar.refresh()
        if (!granted) {
            promptDismissed.value = true
            return
        }
        viewModelScope.launch { settingsRepository.update { it.copy(calendarSyncEnabled = true) } }
    }

    fun dismissCalendarPrompt() {
        promptDismissed.value = true
    }
}
