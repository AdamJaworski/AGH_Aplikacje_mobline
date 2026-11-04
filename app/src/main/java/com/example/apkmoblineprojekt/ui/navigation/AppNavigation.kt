package com.example.apkmoblineprojekt.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.apkmoblineprojekt.data.MockData
import com.example.apkmoblineprojekt.data.NoteFilter
import com.example.apkmoblineprojekt.data.PlaceCategory
import com.example.apkmoblineprojekt.ui.calendar.CalendarScreen
import com.example.apkmoblineprojekt.ui.notes.NotesScreen
import com.example.apkmoblineprojekt.ui.placepicker.PlacePickerScreen
import com.example.apkmoblineprojekt.ui.places.PlacesScreen
import com.example.apkmoblineprojekt.ui.settings.SettingsScreen
import com.example.apkmoblineprojekt.ui.taskedit.TaskEditScreen
import com.example.apkmoblineprojekt.ui.theme.*

/**
 * Ręczny kontener DI (zgodnie z HLD - zamiast Hilta).
 */
class AppContainer {
    // Miejsce na repozytoria Room, GeofenceHelper, AlarmScheduler, AiClient
}

sealed class ScreenRoute(
    val route: String,
    val label: String,
    val outlinedIcon: ImageVector? = null,
    val filledIcon: ImageVector? = null
) {
    data object Calendar : ScreenRoute(
        route = "calendar",
        label = "Kalendarz",
        outlinedIcon = Icons.Outlined.CalendarMonth,
        filledIcon = Icons.Filled.CalendarMonth
    )
    data object Notes : ScreenRoute(
        route = "notes",
        label = "Notatki",
        outlinedIcon = Icons.Outlined.EditNote,
        filledIcon = Icons.Filled.EditNote
    )
    data object Places : ScreenRoute(
        route = "places",
        label = "Miejsca",
        outlinedIcon = Icons.Outlined.LocationOn,
        filledIcon = Icons.Filled.LocationOn
    )
    data object Settings : ScreenRoute(
        route = "settings",
        label = "Ustawienia",
        outlinedIcon = Icons.Outlined.Settings,
        filledIcon = Icons.Filled.Settings
    )
    data object TaskEdit : ScreenRoute("task_edit", "Nowe zadanie")
    data object PlacePicker : ScreenRoute("place_picker", "Wybierz miejsce")
}

private val bottomNavDestinations = listOf(
    ScreenRoute.Calendar,
    ScreenRoute.Notes,
    ScreenRoute.Places,
    ScreenRoute.Settings
)

@Composable
fun GeoPlanerApp(
    appContainer: AppContainer = remember { AppContainer() }
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = bottomNavDestinations.any { it.route == currentRoute }

    // Przykładowy stan trzymany na poziomie nawigacji (w docelowej aplikacji w ViewModelach)
    var tasks by remember { mutableStateOf(MockData.todayTasks) }
    var isWeekView by remember { mutableStateOf(true) }
    var selectedLocationName by remember { mutableStateOf("Sklep budowlany") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                GeoPlanerBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Calendar.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(ScreenRoute.Calendar.route) {
                CalendarScreen(
                    monthYearText = "MAJ 2025",
                    selectedDateTitle = "Środa, 21 maja",
                    weekRangeText = "19–25 maja",
                    isWeekView = isWeekView,
                    days = MockData.weekDays,
                    tasks = tasks,
                    onToggleCalendarView = { isWeekView = it },
                    onDayClick = { /* Zmiana wybranego dnia */ },
                    onTaskCheckedChange = { taskId, checked ->
                        tasks = tasks.map {
                            if (it.id == taskId) it.copy(isDone = checked) else it
                        }
                    },
                    onTaskClick = { navController.navigate(ScreenRoute.TaskEdit.route) },
                    onAddTaskClick = { navController.navigate(ScreenRoute.TaskEdit.route) },
                    onMenuClick = {}
                )
            }

            composable(ScreenRoute.Notes.route) {
                var searchQuery by remember { mutableStateOf("") }
                var selectedFilter by remember { mutableStateOf(NoteFilter.ALL) }

                NotesScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedFilter = selectedFilter,
                    onFilterSelect = { selectedFilter = it },
                    notes = MockData.notes,
                    onAddNoteClick = {},
                    onNoteClick = {}
                )
            }

            composable(ScreenRoute.Places.route) {
                PlacesScreen(
                    savedPlaces = MockData.savedPlaces,
                    onAddPlaceClick = { navController.navigate(ScreenRoute.PlacePicker.route) },
                    onPlaceClick = { navController.navigate(ScreenRoute.PlacePicker.route) },
                    onMyLocationClick = {}
                )
            }

            composable(ScreenRoute.Settings.route) {
                var quietHoursEnabled by remember { mutableStateOf(true) }
                SettingsScreen(
                    isQuietHoursEnabled = quietHoursEnabled,
                    onQuietHoursChanged = { quietHoursEnabled = it },
                    onNotificationPermissionClick = {},
                    onLocationPermissionClick = {},
                    onDefaultRadiusClick = {},
                    onDefaultLeadTimeClick = {}
                )
            }

            composable(ScreenRoute.TaskEdit.route) {
                var title by remember { mutableStateOf("") }
                var description by remember { mutableStateOf("") }
                var subtasks by remember { mutableStateOf(MockData.defaultSubtasks) }

                TaskEditScreen(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    dateText = "21 . 05 . 2025",
                    timeText = "18 : 00",
                    reminderText = "15 min wcześniej",
                    locationTitle = selectedLocationName,
                    locationSubtitle = "300 m od lokalizacji",
                    subtasks = subtasks,
                    onCancelClick = { navController.popBackStack() },
                    onSaveClick = { navController.popBackStack() },
                    onAiSuggestClick = {},
                    onDateClick = {},
                    onTimeClick = {},
                    onReminderClick = {},
                    onLocationClick = { navController.navigate(ScreenRoute.PlacePicker.route) },
                    onAddSubtaskClick = {
                        subtasks = subtasks + com.example.apkmoblineprojekt.data.SubtaskUi(
                            id = "s_${subtasks.size + 1}",
                            title = "Nowe podzadanie",
                            isDone = false
                        )
                    },
                    onSubtaskCheckedChange = { id, isDone ->
                        subtasks = subtasks.map { if (it.id == id) it.copy(isDone = isDone) else it }
                    }
                )
            }

            composable(ScreenRoute.PlacePicker.route) {
                var searchQuery by remember { mutableStateOf("") }
                var selectedCategory by remember { mutableStateOf(PlaceCategory.SHOP) }

                PlacePickerScreen(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it },
                    nearbyPlaces = MockData.nearbyPlaces,
                    onCloseClick = { navController.popBackStack() },
                    onDoneClick = { navController.popBackStack() },
                    onNearbyPlaceClick = { place ->
                        selectedLocationName = place.name
                        navController.popBackStack()
                    },
                    onMyLocationClick = {}
                )
            }
        }
    }
}

@Composable
fun GeoPlanerBottomBar(
    currentRoute: String?,
    onNavigate: (ScreenRoute) -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp
        ) {
            bottomNavDestinations.forEach { screen ->
                val isSelected = currentRoute == screen.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(screen) },
                    icon = {
                        Icon(
                            imageVector = (if (isSelected) screen.filledIcon else screen.outlinedIcon)!!,
                            contentDescription = screen.label
                        )
                    },
                    label = {
                        Text(
                            text = screen.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ForestGreen,
                        selectedTextColor = ForestGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GeoPlanerAppPreview() {
    GeoPlanerTheme {
        GeoPlanerApp()
    }
}