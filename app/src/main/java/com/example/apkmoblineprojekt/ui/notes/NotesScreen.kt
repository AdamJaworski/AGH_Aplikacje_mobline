package com.example.apkmoblineprojekt.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apkmoblineprojekt.data.MockData
import com.example.apkmoblineprojekt.data.NoteFilter
import com.example.apkmoblineprojekt.data.NoteUi
import com.example.apkmoblineprojekt.ui.components.AppSearchBar
import com.example.apkmoblineprojekt.ui.components.CircularIconActionButton
import com.example.apkmoblineprojekt.ui.components.ScreenTopHeader
import com.example.apkmoblineprojekt.ui.theme.*

@Composable
fun NotesScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: NoteFilter,
    onFilterSelect: (NoteFilter) -> Unit,
    notes: List<NoteUi>,
    onAddNoteClick: () -> Unit,
    onNoteClick: (NoteUi) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            ScreenTopHeader(
                overlineText = "TWOJE ZAPISKI",
                title = "Notatki",
                actionButton = {
                    CircularIconActionButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Dodaj notatkę",
                        onClick = onAddNoteClick,
                        containerColor = ForestGreen,
                        contentColor = Color.White,
                        hasBorder = false
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Szukaj w notatkach"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Chipy filtrów: Wszystkie | Przypięte | Powiązane
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NoteFilter.entries.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) ForestGreen else MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ForestGreen else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onFilterSelect(filter) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        items(notes, key = { it.id }) { note ->
            NoteCardItem(
                note = note,
                onClick = { onNoteClick(note) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
fun NoteCardItem(
    note: NoteUi,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (note.isPinnedWithLocation) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .heightIn(min = 136.dp)
                        .background(AccentCoral)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (note.isPinnedWithLocation) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Przypięta lokalizacja",
                            tint = AccentCoral,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = note.footerText,
                    fontSize = 12.sp,
                    color = TextPlaceholder
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenPreview() {
    GeoPlanerTheme {
        NotesScreen(
            searchQuery = "",
            onSearchQueryChange = {},
            selectedFilter = NoteFilter.ALL,
            onFilterSelect = {},
            notes = MockData.notes,
            onAddNoteClick = {},
            onNoteClick = {}
        )
    }
}