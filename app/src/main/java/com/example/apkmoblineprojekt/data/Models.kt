package com.example.apkmoblineprojekt.data

import androidx.compose.ui.graphics.Color
import com.example.apkmoblineprojekt.ui.theme.AccentBlue
import com.example.apkmoblineprojekt.ui.theme.AccentCoral
import com.example.apkmoblineprojekt.ui.theme.ForestGreen

data class CalendarDayUi(
    val dayOfWeek: String,
    val dayNumber: String,
    val hasTasks: Boolean = false,
    val isSelected: Boolean = false
)

data class SubtaskUi(
    val id: String,
    val title: String,
    val isDone: Boolean
)

data class TaskUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val locationName: String? = null,
    val timeText: String? = null,
    val isDone: Boolean = false,
    val accentColor: Color = AccentCoral
)

enum class NoteFilter(val label: String) {
    ALL("Wszystkie"),
    PINNED("Przypięte"),
    LINKED("Powiązane")
}

data class NoteUi(
    val id: String,
    val title: String,
    val content: String,
    val footerText: String,
    val isPinnedWithLocation: Boolean = false
)

enum class PlaceType { HOME, WORK, OTHER }

data class SavedPlaceUi(
    val id: String,
    val name: String,
    val address: String,
    val taskCount: Int,
    val type: PlaceType
)

enum class PlaceCategory(val label: String) {
    SHOP("Sklep"),
    PHARMACY("Apteka"),
    PARCEL("Paczkomat"),
    OTHER("Inne")
}

data class NearbyPlaceUi(
    val index: Int,
    val name: String,
    val openHours: String,
    val distance: String
)

object MockData {
    val weekDays = listOf(
        CalendarDayUi("PON", "19", hasTasks = false),
        CalendarDayUi("WT", "20", hasTasks = true),
        CalendarDayUi("ŚR", "21", hasTasks = true, isSelected = true),
        CalendarDayUi("CZW", "22", hasTasks = false),
        CalendarDayUi("PT", "23", hasTasks = true),
        CalendarDayUi("SOB", "24", hasTasks = true),
        CalendarDayUi("ND", "25", hasTasks = false)
    )

    val todayTasks = listOf(
        TaskUi(
            id = "1",
            title = "Kupić żarówki",
            subtitle = "4 × LED E27, ciepłe światło",
            locationName = "Sklep budowlany",
            isDone = false,
            accentColor = AccentCoral
        ),
        TaskUi(
            id = "2",
            title = "Wysłać dokumenty",
            subtitle = "Umowa i załącznik dla księgowości",
            timeText = "14:30",
            isDone = false,
            accentColor = AccentBlue
        )
    )

    val defaultSubtasks = listOf(
        SubtaskUi("s1", "Sprawdź gwint w lampie", false)
    )

    val nearbyPlaces = listOf(
        NearbyPlaceUi(1, "Dom i ogród", "Otwarte do 21:00", "220 m"),
        NearbyPlaceUi(2, "Majster Market", "Otwarte do 20:00", "480 m"),
        NearbyPlaceUi(3, "Centrum budowlane", "Otwarte do 22:00", "1,2 km")
    )

    val notes = listOf(
        NoteUi(
            id = "n1",
            title = "Pomysły do mieszkania",
            content = "Półka nad biurkiem, cieplejsze światło w salonie, rośliny na parapet.",
            footerText = "Dzisiaj · 08:12",
            isPinnedWithLocation = true
        ),
        NoteUi(
            id = "n2",
            title = "Lista na wyjazd",
            content = "Ładowarka, książka, dokumenty, kurtka przeciwdeszczowa.",
            footerText = "20 maja · Powiązana z zadaniem",
            isPinnedWithLocation = false
        ),
        NoteUi(
            id = "n3",
            title = "Przepis od Ani",
            content = "Pomidory, czosnek, bazylia i dobrej jakości oliwa.",
            footerText = "18 maja · 3 min czytania",
            isPinnedWithLocation = false
        )
    )

    val savedPlaces = listOf(
        SavedPlaceUi("p1", "Dom", "ul. Słoneczna 18", 3, PlaceType.HOME),
        SavedPlaceUi("p2", "Praca", "ul. Domaniewska 37", 2, PlaceType.WORK),
        SavedPlaceUi("p3", "Mama", "ul. Lipowa 4", 0, PlaceType.OTHER)
    )
}