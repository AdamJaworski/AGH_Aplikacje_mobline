package com.example.apkmoblineprojekt.ui.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.apkmoblineprojekt.ui.util.pluralPl
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.data.model.LocationType
import com.example.apkmoblineprojekt.data.model.PlaceCategory
import com.example.apkmoblineprojekt.data.model.TaskLocation
import com.example.apkmoblineprojekt.ui.components.BliskoCard
import com.example.apkmoblineprojekt.ui.components.ChevronRight
import com.example.apkmoblineprojekt.ui.components.FieldLabel
import com.example.apkmoblineprojekt.ui.components.IconTile
import com.example.apkmoblineprojekt.ui.components.SheetHeader
import com.example.apkmoblineprojekt.ui.components.SheetPrimaryButton
import com.example.apkmoblineprojekt.ui.placepicker.PlacePickerSheet
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText
import com.example.apkmoblineprojekt.ui.theme.BliskoTheme
import com.example.apkmoblineprojekt.ui.util.ReminderOptions
import com.example.apkmoblineprojekt.ui.util.appViewModel
import com.example.apkmoblineprojekt.ui.util.dayMonth
import com.example.apkmoblineprojekt.ui.util.displayName
import com.example.apkmoblineprojekt.ui.util.hhmm
import com.example.apkmoblineprojekt.ui.util.reminderLabel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@Composable
fun TaskEditorRoute(onClose: () -> Unit) {
    val viewModel = appViewModel {
        TaskEditorViewModel(createSavedStateHandle(), it.taskRepository, it.settingsRepository, it.aiAssistant)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var placePickerOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.finished) {
        if (state.finished) onClose()
    }

    TaskEditorScreen(
        state = state,
        actions = TaskEditorActions(
            onCancel = onClose,
            onSave = viewModel::save,
            onDelete = viewModel::delete,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onAskAi = viewModel::requestAiSuggestion,
            onDateChange = viewModel::onDateChange,
            onTimeChange = viewModel::onTimeChange,
            onReminderChange = viewModel::onReminderChange,
            onOpenPlacePicker = { placePickerOpen = true },
            onClearLocation = { viewModel.onLocationChange(null) },
            onAddSubtask = viewModel::addSubtask,
            onSubtaskChange = viewModel::onSubtaskChange,
            onToggleSubtask = viewModel::toggleSubtask,
            onRemoveSubtask = viewModel::removeSubtask,
        ),
    )

    if (placePickerOpen) {
        PlacePickerSheet(
            initial = state.location,
            onDismiss = { placePickerOpen = false },
            onConfirm = {
                viewModel.onLocationChange(it)
                placePickerOpen = false
            },
        )
    }
}

class TaskEditorActions(
    val onCancel: () -> Unit,
    val onSave: () -> Unit,
    val onDelete: () -> Unit,
    val onTitleChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onAskAi: () -> Unit,
    val onDateChange: (LocalDate?) -> Unit,
    val onTimeChange: (LocalTime?) -> Unit,
    val onReminderChange: (Int?) -> Unit,
    val onOpenPlacePicker: () -> Unit,
    val onClearLocation: () -> Unit,
    val onAddSubtask: () -> Unit,
    val onSubtaskChange: (Long, String) -> Unit,
    val onToggleSubtask: (Long) -> Unit,
    val onRemoveSubtask: (Long) -> Unit,
)

@Composable
fun TaskEditorScreen(state: TaskEditorUiState, actions: TaskEditorActions) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BliskoColors.Surface)
            .imePadding(),
    ) {
        SheetHeader(
            title = stringResource(if (state.isNew) R.string.editor_title_new else R.string.editor_title_edit),
            leading = {
                Text(
                    stringResource(R.string.action_cancel),
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = actions.onCancel)
                        .padding(vertical = 5.dp, horizontal = 2.dp),
                    style = BliskoText.Button.copy(color = BliskoColors.Muted),
                )
            },
            trailing = {
                SheetPrimaryButton(stringResource(R.string.action_save), actions.onSave, enabled = state.canSave)
            },
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 19.dp, end = 19.dp, top = 18.dp, bottom = 42.dp),
        ) {
            TitleInputs(state, actions)
            AiButton(state, actions.onAskAi)
            AnimatedVisibility(
                visible = state.ai is AiState.Ready || state.ai is AiState.Failed,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                AiResultBanner(state.ai)
            }

            FieldLabel(stringResource(R.string.editor_section_when))
            WhenCard(state, actions)

            FieldLabel(stringResource(R.string.editor_section_where))
            WhereCard(state.location, actions.onOpenPlacePicker, actions.onClearLocation)

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FieldLabel(stringResource(R.string.editor_section_subtasks))
                Row(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Button, onClick = actions.onAddSubtask)
                        .padding(start = 3.dp, end = 3.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Add, null, Modifier.size(15.dp), tint = BliskoColors.Coral)
                    Spacer(Modifier.width(3.dp))
                    Text(
                        stringResource(R.string.action_add),
                        style = BliskoText.FieldLabel.copy(color = BliskoColors.Coral, letterSpacing = 0.sp),
                    )
                }
            }
            SubtasksCard(state.subtasks, actions)

            if (!state.isNew) {
                Text(
                    stringResource(R.string.editor_delete),
                    Modifier
                        .padding(top = 28.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = actions.onDelete)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = BliskoText.Button.copy(color = BliskoColors.Coral),
                )
            }
        }
    }
}

@Composable
private fun TitleInputs(state: TaskEditorUiState, actions: TaskEditorActions) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (state.isNew) focusRequester.requestFocus()
    }
    BliskoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            PlainTextField(
                value = state.title,
                onValueChange = actions.onTitleChange,
                placeholder = stringResource(R.string.editor_title_placeholder),
                style = BliskoText.SectionTitleSmall.copy(letterSpacing = 0.sp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(bottom = 9.dp),
            )
            RowDivider()
            PlainTextField(
                value = state.description,
                onValueChange = actions.onDescriptionChange,
                placeholder = stringResource(R.string.editor_description_placeholder),
                style = BliskoText.Detail.copy(fontSize = 11.sp, lineHeight = 16.sp),
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun AiButton(state: TaskEditorUiState, onClick: () -> Unit) {
    BliskoCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .alpha(if (state.title.isBlank()) 0.6f else 1f),
        cornerRadius = 17.dp,
        color = Color(0xFFFFF8F4),
        borderColor = Color(0xFFEDD8CF),
        onClick = { if (state.canAskAi) onClick() },
    ) {
        Row(
            Modifier
                .heightIn(min = 61.dp)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            IconTile(
                Icons.Outlined.AutoAwesome,
                background = Color(0xFFF8E3DA),
                tint = BliskoColors.Coral,
                size = 36.dp,
                cornerRadius = 12.dp,
            )
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.editor_ai_title), style = BliskoText.Button)
                Text(
                    stringResource(R.string.editor_ai_subtitle),
                    Modifier.padding(top = 2.dp),
                    style = BliskoText.Small,
                )
            }
            if (state.ai == AiState.Loading) {
                CircularProgressIndicator(Modifier.size(18.dp), color = BliskoColors.Coral, strokeWidth = 2.dp)
            } else {
                ChevronRight(tint = Color(0xFFAAAEAD))
            }
        }
    }
}

@Composable
private fun AiResultBanner(ai: AiState) {
    val failed = ai is AiState.Failed
    Column(
        Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .background(if (failed) BliskoColors.CoralTint else Color(0xFFE9F1EB), RoundedCornerShape(14.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        val color = if (failed) BliskoColors.Coral else Color(0xFF38654F)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(16.dp), tint = color)
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(if (failed) R.string.editor_ai_failed_title else R.string.editor_ai_ready_title),
                style = BliskoText.Button.copy(fontSize = 10.sp, color = color),
            )
        }
        Text(
            if (ai is AiState.Ready) aiResultMessage(ai) else stringResource(R.string.editor_ai_failed_message),
            Modifier.padding(top = 5.dp),
            style = BliskoText.Small.copy(color = color),
        )
    }
}

@Composable
private fun aiResultMessage(ai: AiState.Ready): String {
    val subtasks = pluralPl(ai.addedSubtasks, R.string.ai_subtasks_one, R.string.ai_subtasks_few, R.string.ai_subtasks_many)
    val location = ai.location ?: return if (ai.addedSubtasks > 0) {
        stringResource(R.string.editor_ai_only_subtasks, subtasks)
    } else {
        stringResource(R.string.editor_ai_nothing_new)
    }
    val place = location.displayName()
    val placeText = if (location.type == LocationType.CATEGORY) {
        stringResource(R.string.editor_ai_category, place)
    } else {
        stringResource(R.string.editor_ai_place, place)
    }
    return if (ai.addedSubtasks > 0) {
        stringResource(R.string.editor_ai_subtasks_and_place, subtasks, placeText)
    } else {
        stringResource(R.string.editor_ai_only_place, placeText)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhenCard(state: TaskEditorUiState, actions: TaskEditorActions) {
    var dateDialog by rememberSaveable { mutableStateOf(false) }
    var timeDialog by rememberSaveable { mutableStateOf(false) }
    var reminderMenu by remember { mutableStateOf(false) }

    BliskoCard(Modifier.fillMaxWidth(), cornerRadius = 17.dp) {
        Column {
            EditorRow(
                icon = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.editor_date),
                value = state.date?.dayMonth() ?: stringResource(R.string.editor_no_date),
                onClick = { dateDialog = true },
            )
            RowDivider()
            EditorRow(
                icon = Icons.Outlined.Schedule,
                label = stringResource(R.string.editor_time),
                value = state.time?.hhmm() ?: stringResource(R.string.editor_no_time),
                onClick = { timeDialog = true },
            )
            RowDivider()
            Box {
                val enabled = state.time != null
                EditorRow(
                    icon = Icons.Outlined.Notifications,
                    label = stringResource(R.string.editor_reminder),
                    value = if (enabled) {
                        reminderLabel(state.remindMinutesBefore)
                    } else {
                        stringResource(R.string.editor_reminder_needs_time)
                    },
                    onClick = { reminderMenu = true },
                    enabled = enabled,
                    trailingIcon = Icons.Outlined.KeyboardArrowDown,
                )
                DropdownMenu(
                    expanded = reminderMenu,
                    onDismissRequest = { reminderMenu = false },
                    containerColor = BliskoColors.Card,
                ) {
                    ReminderOptions.forEach { minutes ->
                        DropdownMenuItem(
                            text = { Text(reminderLabel(minutes)) },
                            onClick = {
                                actions.onReminderChange(minutes)
                                reminderMenu = false
                            },
                            trailingIcon = if (minutes == state.remindMinutesBefore) {
                                { Icon(Icons.Outlined.Check, null, tint = BliskoColors.Forest) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }
    }

    if (dateDialog) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (state.date ?: LocalDate.now())
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { dateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        actions.onDateChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    dateDialog = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        actions.onDateChange(null)
                        dateDialog = false
                    }) { Text(stringResource(R.string.editor_no_date)) }
                    TextButton(onClick = { dateDialog = false }) { Text(stringResource(R.string.action_cancel)) }
                }
            },
        ) {
            DatePicker(pickerState)
        }
    }

    if (timeDialog) {
        val initial = state.time ?: LocalTime.of(18, 0)
        val pickerState = rememberTimePickerState(initial.hour, initial.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { timeDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    actions.onTimeChange(LocalTime.of(pickerState.hour, pickerState.minute))
                    timeDialog = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        actions.onTimeChange(null)
                        timeDialog = false
                    }) { Text(stringResource(R.string.editor_no_time)) }
                    TextButton(onClick = { timeDialog = false }) { Text(stringResource(R.string.action_cancel)) }
                }
            },
            text = { TimePicker(pickerState) },
            containerColor = BliskoColors.Card,
        )
    }
}

@Composable
private fun WhereCard(location: TaskLocation?, onOpen: () -> Unit, onClear: () -> Unit) {
    BliskoCard(Modifier.fillMaxWidth(), cornerRadius = 17.dp, onClick = onOpen) {
        Row(
            Modifier
                .heightIn(min = 51.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconTile(
                Icons.Outlined.LocationOn,
                background = BliskoColors.CoralTint,
                tint = BliskoColors.Coral,
                size = 31.dp,
                cornerRadius = 10.dp,
                iconSize = 18.dp,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    location?.displayName() ?: stringResource(R.string.editor_pick_place),
                    style = BliskoText.RowLabel,
                )
                Text(
                    if (location != null) {
                        stringResource(R.string.editor_place_radius, location.radiusMeters)
                    } else {
                        stringResource(R.string.editor_place_optional)
                    },
                    Modifier.padding(top = 2.dp),
                    style = BliskoText.Small,
                )
            }
            if (location != null) {
                Icon(
                    Icons.Outlined.Close,
                    stringResource(R.string.cd_clear_place),
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onClear)
                        .padding(4.dp)
                        .size(16.dp),
                    tint = BliskoColors.Chevron,
                )
            }
            ChevronRight(tint = Color(0xFFB0B5B2))
        }
    }
}

@Composable
private fun SubtasksCard(subtasks: List<SubtaskDraft>, actions: TaskEditorActions) {
    if (subtasks.isEmpty()) {
        Text(
            stringResource(R.string.editor_no_subtasks),
            Modifier.padding(horizontal = 3.dp),
            style = BliskoText.Small,
        )
        return
    }
    BliskoCard(Modifier.fillMaxWidth(), cornerRadius = 17.dp) {
        Column {
            subtasks.forEachIndexed { index, subtask ->
                if (index > 0) RowDivider()
                SubtaskRow(subtask, actions)
            }
        }
    }
}

@Composable
private fun SubtaskRow(subtask: SubtaskDraft, actions: TaskEditorActions) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(subtask.key) {
        if (subtask.title.isEmpty()) focusRequester.requestFocus()
    }
    Row(
        Modifier
            .heightIn(min = 45.dp)
            .padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val shape = RoundedCornerShape(6.dp)
        Box(
            Modifier
                .size(18.dp)
                .clip(shape)
                .background(if (subtask.done) BliskoColors.Forest else Color.Transparent)
                .border(1.5.dp, if (subtask.done) BliskoColors.Forest else Color(0xFFAEB5B0), shape)
                .clickable(role = Role.Checkbox) { actions.onToggleSubtask(subtask.key) },
            contentAlignment = Alignment.Center,
        ) {
            if (subtask.done) Icon(Icons.Outlined.Check, null, Modifier.size(12.dp), tint = Color.White)
        }
        PlainTextField(
            value = subtask.title,
            onValueChange = { actions.onSubtaskChange(subtask.key, it) },
            placeholder = stringResource(R.string.editor_subtask_placeholder),
            style = BliskoText.Detail.copy(color = BliskoColors.Ink),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
        )
        Icon(
            Icons.Outlined.Close,
            stringResource(R.string.cd_remove_subtask),
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(role = Role.Button) { actions.onRemoveSubtask(subtask.key) }
                .padding(2.dp)
                .size(16.dp),
            tint = BliskoColors.Chevron,
        )
    }
}

@Composable
private fun EditorRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.5f)
            .heightIn(min = 51.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconTile(icon, background = Color(0xFFEFF2EE), size = 31.dp, cornerRadius = 10.dp, iconSize = 18.dp)
        Text(label, Modifier.weight(1f), style = BliskoText.RowLabel)
        Text(value, style = BliskoText.Value)
        if (trailingIcon != null) {
            Icon(trailingIcon, null, Modifier.size(17.dp), tint = Color(0xFFB0B5B2))
        }
    }
}

@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BliskoColors.Line),
    )
}

@Composable
private fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = style,
        singleLine = singleLine,
        minLines = minLines,
        cursorBrush = SolidColor(BliskoColors.Forest),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        style = style.copy(color = Color(0xFFB0B5B2), fontWeight = style.fontWeight ?: FontWeight.Normal),
                    )
                }
                inner()
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 412, heightDp = 860)
@Composable
private fun TaskEditorPreview() {
    BliskoTheme {
        TaskEditorScreen(
            state = TaskEditorUiState(
                title = "Kupić żarówki",
                description = "4 × LED E27, ciepłe światło",
                date = LocalDate.now(),
                time = LocalTime.of(18, 0),
                remindMinutesBefore = 15,
                location = TaskLocation(LocationType.CATEGORY, category = PlaceCategory.HARDWARE_STORE),
                subtasks = listOf(SubtaskDraft(1, "Sprawdź gwint w lampie")),
                ai = AiState.Ready(2, TaskLocation(LocationType.CATEGORY, category = PlaceCategory.HARDWARE_STORE)),
            ),
            actions = TaskEditorActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, { _, _ -> }, {}, {}),
        )
    }
}
