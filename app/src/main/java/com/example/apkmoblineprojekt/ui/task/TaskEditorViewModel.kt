package com.example.apkmoblineprojekt.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkmoblineprojekt.ai.AiAssistant
import com.example.apkmoblineprojekt.data.SettingsRepository
import com.example.apkmoblineprojekt.data.TaskRepository
import com.example.apkmoblineprojekt.data.model.DEFAULT_RADIUS_METERS
import com.example.apkmoblineprojekt.data.model.Subtask
import com.example.apkmoblineprojekt.data.model.Task
import com.example.apkmoblineprojekt.data.model.TaskColor
import com.example.apkmoblineprojekt.data.model.TaskLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/** Podzadanie w trakcie edycji; [key] identyfikuje wiersz także przed zapisem. */
data class SubtaskDraft(
    val key: Long,
    val title: String,
    val done: Boolean = false,
    val id: Long = 0,
)

sealed interface AiState {
    data object Idle : AiState
    data object Loading : AiState

    /** [location] – miejsce ustawione przez AI (null, jeśli użytkownik miał już własne). */
    data class Ready(val addedSubtasks: Int, val location: TaskLocation?) : AiState
    data object Failed : AiState
}

data class TaskEditorUiState(
    val isNew: Boolean = true,
    val loading: Boolean = false,
    val title: String = "",
    val description: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val remindMinutesBefore: Int? = null,
    val location: TaskLocation? = null,
    val subtasks: List<SubtaskDraft> = emptyList(),
    val ai: AiState = AiState.Idle,
    val finished: Boolean = false,
) {
    val canSave: Boolean get() = title.isNotBlank() && !loading
    val canAskAi: Boolean get() = title.isNotBlank() && ai != AiState.Loading
}

class TaskEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val aiAssistant: AiAssistant,
) : ViewModel() {

    private val taskId: Long? = savedStateHandle.get<Long>(ARG_TASK_ID)?.takeIf { it > 0 }
    private val initialDate: LocalDate =
        savedStateHandle.get<String>(ARG_DATE)?.let(LocalDate::parse) ?: LocalDate.now()

    private var existing: Task? = null
    private var defaultRadius = DEFAULT_RADIUS_METERS
    private var nextKey = 0L

    private val _uiState = MutableStateFlow(
        TaskEditorUiState(isNew = taskId == null, loading = taskId != null, date = initialDate),
    )
    val uiState: StateFlow<TaskEditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            defaultRadius = settings.defaultRadiusMeters
            val task = taskId?.let { taskRepository.getTask(it) }
            existing = task
            _uiState.update { state ->
                if (task == null) {
                    state.copy(loading = false, remindMinutesBefore = settings.defaultRemindMinutes)
                } else {
                    state.copy(
                        loading = false,
                        title = task.title,
                        description = task.description,
                        date = task.date,
                        time = task.time,
                        remindMinutesBefore = task.remindMinutesBefore ?: settings.defaultRemindMinutes,
                        location = task.location,
                        subtasks = task.subtasks.map { SubtaskDraft(nextKey++, it.title, it.done, it.id) },
                    )
                }
            }
        }
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun onDateChange(value: LocalDate?) = _uiState.update { it.copy(date = value) }

    fun onTimeChange(value: LocalTime?) = _uiState.update { it.copy(time = value) }

    fun onReminderChange(minutes: Int?) = _uiState.update { it.copy(remindMinutesBefore = minutes) }

    fun onLocationChange(location: TaskLocation?) = _uiState.update { it.copy(location = location) }

    fun addSubtask() = _uiState.update { it.copy(subtasks = it.subtasks + SubtaskDraft(nextKey++, "")) }

    fun onSubtaskChange(key: Long, title: String) = updateSubtask(key) { it.copy(title = title) }

    fun toggleSubtask(key: Long) = updateSubtask(key) { it.copy(done = !it.done) }

    fun removeSubtask(key: Long) = _uiState.update { state ->
        state.copy(subtasks = state.subtasks.filterNot { it.key == key })
    }

    private fun updateSubtask(key: Long, transform: (SubtaskDraft) -> SubtaskDraft) = _uiState.update { state ->
        state.copy(subtasks = state.subtasks.map { if (it.key == key) transform(it) else it })
    }

    /** AI tylko proponuje: dopisuje brakujące podzadania i ustawia miejsce, jeśli go nie było. */
    fun requestAiSuggestion() {
        val request = _uiState.value
        if (!request.canAskAi) return
        _uiState.update { it.copy(ai = AiState.Loading) }
        viewModelScope.launch {
            val suggestion = try {
                aiAssistant.suggest(request.title.trim(), request.description.trim())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(ai = AiState.Failed) }
                return@launch
            }
            val current = _uiState.value
            val known = current.subtasks.map { it.title.trim().lowercase() }.toSet()
            val added = suggestion.subtasks
                .filter { it.trim().lowercase() !in known }
                .map { SubtaskDraft(nextKey++, it) }
            val suggestedLocation = suggestion.location
                ?.copy(radiusMeters = defaultRadius)
                ?.takeIf { current.location == null }
            _uiState.value = current.copy(
                subtasks = current.subtasks + added,
                location = current.location ?: suggestedLocation,
                ai = AiState.Ready(added.size, suggestedLocation),
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            val base = existing
            // Kopia zapisanej wersji – zachowuje pola, których edytor nie zna (uid, powiązanie z kalendarzem).
            val task = (base ?: Task(title = "")).copy(
                id = base?.id ?: 0,
                title = state.title.trim(),
                description = state.description.trim(),
                date = state.date,
                time = state.time,
                remindMinutesBefore = state.remindMinutesBefore.takeIf { state.time != null },
                location = state.location,
                subtasks = state.subtasks
                    .filter { it.title.isNotBlank() }
                    .map { Subtask(id = it.id, title = it.title.trim(), done = it.done) },
                done = base?.done ?: false,
                color = base?.color ?: if (state.location != null) TaskColor.CORAL else TaskColor.BLUE,
            )
            taskRepository.saveTask(task)
            _uiState.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        val id = existing?.id ?: return
        viewModelScope.launch {
            taskRepository.deleteTask(id)
            _uiState.update { it.copy(finished = true) }
        }
    }

    companion object {
        const val ARG_TASK_ID = "taskId"
        const val ARG_DATE = "date"
    }
}
