package com.example.apkmoblineprojekt.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apkmoblineprojekt.data.CalendarDayUi
import com.example.apkmoblineprojekt.data.MockData
import com.example.apkmoblineprojekt.data.TaskUi
import com.example.apkmoblineprojekt.ui.components.CircularIconActionButton
import com.example.apkmoblineprojekt.ui.components.ScreenTopHeader
import com.example.apkmoblineprojekt.ui.theme.*

@Composable
fun CalendarScreen(
    monthYearText: String,
    selectedDateTitle: String,
    weekRangeText: String,
    isWeekView: Boolean,
    days: List<CalendarDayUi>,
    tasks: List<TaskUi>,
    onToggleCalendarView: (Boolean) -> Unit,
    onDayClick: (CalendarDayUi) -> Unit,
    onTaskCheckedChange: (String, Boolean) -> Unit,
    onTaskClick: (TaskUi) -> Unit,
    onAddTaskClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                ScreenTopHeader(
                    overlineText = monthYearText,
                    title = selectedDateTitle,
                    actionButton = {
                        CircularIconActionButton(
                            icon = Icons.Default.Menu,
                            contentDescription = "Menu",
                            onClick = onMenuClick
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                WeekCalendarCard(
                    weekRangeText = weekRangeText,
                    isWeekView = isWeekView,
                    days = days,
                    onToggleView = onToggleCalendarView,
                    onDayClick = onDayClick,
                    onPrevWeek = {},
                    onNextWeek = {}
                )
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = "DZISIAJ",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentCoral
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Do zrobienia",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${tasks.size} zadania",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            items(tasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onCheckedChange = { checked -> onTaskCheckedChange(task.id, checked) },
                    onClick = { onTaskClick(task) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Pływający przycisk "+ Nowe zadanie"
        ExtendedFloatingActionButton(
            onClick = onAddTaskClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = ForestGreen,
            contentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            },
            text = {
                Text(
                    text = "Nowe zadanie",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        )
    }
}

@Composable
fun WeekCalendarCard(
    weekRangeText: String,
    isWeekView: Boolean,
    days: List<CalendarDayUi>,
    onToggleView: (Boolean) -> Unit,
    onDayClick: (CalendarDayUi) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrevWeek,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Poprzedni tydzień",
                            tint = TextSecondary
                        )
                    }
                    Text(
                        text = weekRangeText,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = onNextWeek,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Następny tydzień",
                            tint = TextSecondary
                        )
                    }
                }

                // Przełącznik T / M (Tydzień / Miesiąc)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfacePillGray)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CalendarModeSegment(
                        label = "T",
                        isSelected = isWeekView,
                        onClick = { onToggleView(true) }
                    )
                    CalendarModeSegment(
                        label = "M",
                        isSelected = !isWeekView,
                        onClick = { onToggleView(false) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    DayPillItem(
                        day = day,
                        onClick = { onDayClick(day) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarModeSegment(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) SurfaceWhite else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

@Composable
fun DayPillItem(
    day: CalendarDayUi,
    onClick: () -> Unit
) {
    val containerColor = if (day.isSelected) ForestGreen else Color.Transparent
    val dayNameColor = if (day.isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary
    val numberColor = if (day.isSelected) Color.White else TextPrimary

    Column(
        modifier = Modifier
            .width(42.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.dayOfWeek,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = dayNameColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = day.dayNumber,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = numberColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (day.hasTasks) AccentCoral else Color.Transparent)
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskUi,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Niestandardowy zaokrąglony Checkbox w kolorze kategorii
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(22.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (task.isDone) task.accentColor else Color.Transparent)
                        .border(1.5.dp, task.accentColor, RoundedCornerShape(7.dp))
                        .clickable { onCheckedChange(!task.isDone) },
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (task.locationName != null) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.locationName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        } else if (task.timeText != null) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.timeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Kolorowy pionowy znacznik na prawej krawędzi karty (jak w Figmie)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(4.dp)
                    .height(38.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                    .background(task.accentColor)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    GeoPlanerTheme {
        CalendarScreen(
            monthYearText = "MAJ 2025",
            selectedDateTitle = "Środa, 21 maja",
            weekRangeText = "19–25 maja",
            isWeekView = true,
            days = MockData.weekDays,
            tasks = MockData.todayTasks,
            onToggleCalendarView = {},
            onDayClick = {},
            onTaskCheckedChange = { _, _ -> },
            onTaskClick = {},
            onAddTaskClick = {},
            onMenuClick = {}
        )
    }
}