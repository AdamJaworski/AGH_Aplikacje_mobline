package com.example.apkmoblineprojekt.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.ui.calendar.CalendarRoute
import com.example.apkmoblineprojekt.ui.notes.NotesRoute
import com.example.apkmoblineprojekt.ui.places.PlacesRoute
import com.example.apkmoblineprojekt.ui.settings.SettingsRoute
import com.example.apkmoblineprojekt.ui.task.TaskEditorRoute
import com.example.apkmoblineprojekt.ui.task.TaskEditorViewModel
import com.example.apkmoblineprojekt.ui.theme.BliskoColors
import com.example.apkmoblineprojekt.ui.theme.BliskoText
import java.time.LocalDate

enum class TopLevelDestination(
    val route: String,
    @param:StringRes val label: Int,
    val icon: ImageVector,
) {
    CALENDAR("calendar", R.string.nav_calendar, Icons.Outlined.CalendarMonth),
    NOTES("notes", R.string.nav_notes, Icons.Outlined.EditNote),
    PLACES("places", R.string.nav_places, Icons.Outlined.LocationOn),
    SETTINGS("settings", R.string.nav_settings, Icons.Outlined.Settings),
}

object Routes {
    private const val TASK_EDITOR_BASE = "task"
    const val TASK_EDITOR =
        "$TASK_EDITOR_BASE?${TaskEditorViewModel.ARG_TASK_ID}={${TaskEditorViewModel.ARG_TASK_ID}}" +
            "&${TaskEditorViewModel.ARG_DATE}={${TaskEditorViewModel.ARG_DATE}}"

    fun newTask(date: LocalDate) = "$TASK_EDITOR_BASE?${TaskEditorViewModel.ARG_DATE}=$date"

    fun editTask(id: Long) = "$TASK_EDITOR_BASE?${TaskEditorViewModel.ARG_TASK_ID}=$id"
}

@Composable
fun BliskoApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentTab = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }

    Scaffold(
        containerColor = BliskoColors.Surface,
        bottomBar = {
            if (currentTab != null) {
                BottomBar(current = currentTab, onSelect = { navController.navigateToTab(it) })
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.CALENDAR.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            composable(TopLevelDestination.CALENDAR.route) {
                CalendarRoute(
                    onAddTask = { navController.navigate(Routes.newTask(it)) },
                    onOpenTask = { navController.navigate(Routes.editTask(it.id)) },
                    onOpenMenu = { /* TODO: menu (np. widok wszystkich zadań, wyszukiwanie) */ },
                )
            }
            composable(TopLevelDestination.NOTES.route) {
                NotesRoute(
                    onAddNote = { /* TODO: edytor notatki */ },
                    onOpenNote = { /* TODO: edytor notatki */ },
                )
            }
            composable(TopLevelDestination.PLACES.route) {
                PlacesRoute(onOpenPlace = { /* TODO: szczegóły miejsca */ })
            }
            composable(TopLevelDestination.SETTINGS.route) {
                SettingsRoute()
            }
            composable(
                route = Routes.TASK_EDITOR,
                arguments = listOf(
                    navArgument(TaskEditorViewModel.ARG_TASK_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(TaskEditorViewModel.ARG_DATE) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) {
                TaskEditorRoute(onClose = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateToTab(tab: TopLevelDestination) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomBar(current: TopLevelDestination, onSelect: (TopLevelDestination) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BliskoColors.NavBar.copy(alpha = 0.97f))
            .navigationBarsPadding(),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFDADBD4).copy(alpha = 0.9f)),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(start = 8.dp, end = 8.dp, top = 7.dp, bottom = 10.dp),
        ) {
            TopLevelDestination.entries.forEach { tab ->
                val active = tab == current
                val color = if (active) BliskoColors.Forest else BliskoColors.NavInactive
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(role = Role.Tab) { onSelect(tab) }
                        .semantics { selected = active },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                ) {
                    Icon(
                        tab.icon,
                        null,
                        Modifier
                            .offset(y = if (active) (-1).dp else 0.dp)
                            .size(21.dp),
                        tint = color,
                    )
                    Text(
                        stringResource(tab.label),
                        style = BliskoText.Button.copy(
                            fontSize = 9.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                            color = color,
                        ),
                    )
                }
            }
        }
    }
}
