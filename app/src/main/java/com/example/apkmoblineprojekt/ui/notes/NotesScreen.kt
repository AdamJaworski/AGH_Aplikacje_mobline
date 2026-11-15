package com.example.apkmoblineprojekt.ui.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.data.SampleData
import com.example.apkmoblineprojekt.data.model.Note
import com.example.apkmoblineprojekt.ui.components.CircleIconButton
import com.example.apkmoblineprojekt.ui.components.PillChip
import com.example.apkmoblineprojekt.ui.components.ScreenHeader
import com.example.apkmoblineprojekt.ui.components.SearchField
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText
import com.example.apkmoblineprojekt.ui.theme.BliskoTheme
import com.example.apkmoblineprojekt.ui.util.appViewModel
import com.example.apkmoblineprojekt.ui.util.dayMonth
import com.example.apkmoblineprojekt.ui.util.hhmm
import java.time.LocalDate

/**
 * [onAddNote] / [onOpenNote] – edytor notatki nie jest jeszcze w makiecie; do podpięcia
 * jako kolejny cel nawigacji.
 */
@Composable
fun NotesRoute(onAddNote: () -> Unit, onOpenNote: (Note) -> Unit) {
    val viewModel = appViewModel { NotesViewModel(it.noteRepository) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    NotesScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onAddNote = onAddNote,
        onOpenNote = onOpenNote,
        onTogglePinned = viewModel::togglePinned,
    )
}

@Composable
fun NotesScreen(
    state: NotesUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (NotesFilter) -> Unit,
    onAddNote: () -> Unit,
    onOpenNote: (Note) -> Unit,
    onTogglePinned: (Note) -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 19.dp, end = 19.dp, top = 25.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        item {
            ScreenHeader(
                eyebrow = stringResource(R.string.notes_eyebrow),
                title = stringResource(R.string.notes_title),
            ) {
                CircleIconButton(Icons.Outlined.Add, stringResource(R.string.cd_add_note), onAddNote, filled = true)
            }
        }
        item {
            Column {
                SearchField(state.query, onQueryChange, stringResource(R.string.notes_search_placeholder))
                Row(
                    Modifier.padding(top = 13.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    listOf(
                        NotesFilter.ALL to R.string.notes_filter_all,
                        NotesFilter.PINNED to R.string.notes_filter_pinned,
                        NotesFilter.LINKED to R.string.notes_filter_linked,
                    ).forEach { (filter, label) ->
                        PillChip(stringResource(label), state.filter == filter, { onFilterChange(filter) })
                    }
                }
            }
        }
        if (state.notes.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.notes_empty),
                    Modifier.padding(horizontal = 2.dp),
                    style = BliskoText.Caption,
                )
            }
        }
        items(state.notes, key = { it.id }) { note ->
            NoteCard(note, onClick = { onOpenNote(note) }, onLongClick = { onTogglePinned(note) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onLongClick: () -> Unit) {
    val shape = RoundedCornerShape(19.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BliskoColors.Card)
            .border(1.dp, BliskoColors.Line, shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(
                    if (note.pinned) R.string.cd_unpin_note else R.string.cd_pin_note,
                ),
            ),
    ) {
        Column(Modifier.padding(19.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(note.title, Modifier.weight(1f), style = BliskoText.NoteTitle)
                if (note.pinned) {
                    Icon(
                        Icons.Outlined.PushPin,
                        stringResource(R.string.cd_pinned),
                        Modifier.size(17.dp),
                        tint = BliskoColors.Coral,
                    )
                }
            }
            Text(note.content, Modifier.padding(top = 9.dp, bottom = 14.dp), style = BliskoText.Body)
            Text(noteMeta(note), style = BliskoText.Small.copy(color = BliskoColors.Faint))
        }
        if (note.pinned) {
            Box(
                Modifier
                    .matchParentSize(),
            ) {
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(BliskoColors.Coral),
                )
            }
        }
    }
}

@Composable
private fun noteMeta(note: Note): String {
    val today = LocalDate.now()
    val date = note.updatedAt.toLocalDate()
    val day = when (date) {
        today -> stringResource(R.string.date_today)
        today.minusDays(1) -> stringResource(R.string.date_yesterday)
        else -> date.dayMonth()
    }
    val detail = if (note.taskId != null) {
        stringResource(R.string.notes_linked_to_task)
    } else {
        note.updatedAt.toLocalTime().hhmm()
    }
    return "$day · $detail"
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6F1, widthDp = 412, heightDp = 760)
@Composable
private fun NotesScreenPreview() {
    BliskoTheme {
        NotesScreen(NotesUiState(notes = SampleData.notes()), {}, {}, {}, {}, {})
    }
}
