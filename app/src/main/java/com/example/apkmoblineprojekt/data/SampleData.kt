package com.example.apkmoblineprojekt.data

import com.example.apkmoblineprojekt.data.model.LocationType
import com.example.apkmoblineprojekt.data.model.NearbyPlace
import com.example.apkmoblineprojekt.data.model.Note
import com.example.apkmoblineprojekt.data.model.Place
import com.example.apkmoblineprojekt.data.model.PlaceCategory
import com.example.apkmoblineprojekt.data.model.PlaceKind
import com.example.apkmoblineprojekt.data.model.Subtask
import com.example.apkmoblineprojekt.data.model.Task
import com.example.apkmoblineprojekt.data.model.TaskColor
import com.example.apkmoblineprojekt.data.model.TaskLocation
import java.time.LocalDate
import java.time.LocalTime

/** Dane z makiety, przesunięte względem dzisiejszej daty. Do usunięcia po podpięciu Room. */
object SampleData {
    const val HOME_ID = 1L
    const val WORK_ID = 2L
    const val MOM_ID = 3L

    fun tasks(today: LocalDate = LocalDate.now()): List<Task> = listOf(
        Task(
            id = 1,
            title = "Kupić żarówki",
            description = "4 × LED E27, ciepłe światło",
            location = TaskLocation(LocationType.CATEGORY, category = PlaceCategory.HARDWARE_STORE),
            subtasks = listOf(Subtask(1, "Sprawdź gwint w lampie")),
            color = TaskColor.CORAL,
        ),
        Task(
            id = 2,
            title = "Wysłać dokumenty",
            description = "Umowa i załącznik dla księgowości",
            date = today,
            time = LocalTime.of(14, 30),
            remindMinutesBefore = 15,
            color = TaskColor.BLUE,
        ),
        Task(
            id = 3,
            title = "Odebrać paczkę",
            description = "Kod odbioru w wiadomości",
            date = today,
            location = TaskLocation(LocationType.CATEGORY, category = PlaceCategory.PARCEL_LOCKER),
            done = true,
            color = TaskColor.GREEN,
        ),
        Task(
            id = 4,
            title = "Zadzwonić do mamy",
            description = "Zapytać o niedzielny obiad",
            date = today.minusDays(1),
            location = TaskLocation(LocationType.SAVED, name = "Mama", placeId = MOM_ID),
            color = TaskColor.GREEN,
        ),
        Task(
            id = 5,
            title = "Podlać kwiaty",
            description = "Storczyk tylko odrobinę",
            date = today.plusDays(2),
            location = TaskLocation(LocationType.HOME, name = "Dom", placeId = HOME_ID),
        ),
        Task(
            id = 6,
            title = "Oddać laptopa do IT",
            description = "Wymiana baterii",
            date = today.plusDays(3),
            time = LocalTime.of(9, 0),
            remindMinutesBefore = 30,
            location = TaskLocation(LocationType.WORK, name = "Praca", placeId = WORK_ID),
            color = TaskColor.BLUE,
        ),
    ).map { it.copy(uid = "sample-${it.id}") } // stałe uid – po restarcie trafiają w te same wydarzenia

    fun notes(today: LocalDate = LocalDate.now()): List<Note> = listOf(
        Note(
            id = 1,
            title = "Pomysły do mieszkania",
            content = "Półka nad biurkiem, cieplejsze światło w salonie, rośliny na parapet.",
            pinned = true,
            updatedAt = today.atTime(8, 12),
        ),
        Note(
            id = 2,
            title = "Lista na wyjazd",
            content = "Ładowarka, książka, dokumenty, kurtka przeciwdeszczowa.",
            updatedAt = today.minusDays(1).atTime(19, 40),
            taskId = 2,
        ),
        Note(
            id = 3,
            title = "Przepis od Ani",
            content = "Pomidory, czosnek, bazylia i dobrej jakości oliwa.",
            updatedAt = today.minusDays(3).atTime(12, 5),
        ),
    )

    fun places(): List<Place> = listOf(
        Place(HOME_ID, "Dom", "ul. Słoneczna 18", PlaceKind.HOME),
        Place(WORK_ID, "Praca", "ul. Domaniewska 37", PlaceKind.WORK),
        Place(MOM_ID, "Mama", "ul. Lipowa 4", PlaceKind.SAVED),
    )

    val nearbyPlaces: List<NearbyPlace> = listOf(
        NearbyPlace("h1", "Dom i ogród", PlaceCategory.HARDWARE_STORE, 220, at(21)),
        NearbyPlace("h2", "Majster Market", PlaceCategory.HARDWARE_STORE, 480, at(20)),
        NearbyPlace("h3", "Centrum budowlane", PlaceCategory.HARDWARE_STORE, 1200, at(22)),
        NearbyPlace("h4", "Hurtownia Budmat", PlaceCategory.HARDWARE_STORE, 1900, at(18)),
        NearbyPlace("h5", "Sklep żelazny", PlaceCategory.HARDWARE_STORE, 2300),
        NearbyPlace("h6", "Market Remont", PlaceCategory.HARDWARE_STORE, 3100, at(21)),
        NearbyPlace("g1", "Delikatesy Pod Lipami", PlaceCategory.GROCERY, 150, at(23)),
        NearbyPlace("g2", "Warzywniak u Basi", PlaceCategory.GROCERY, 340, at(19)),
        NearbyPlace("g3", "Supermarket Osiedlowy", PlaceCategory.GROCERY, 800, at(22)),
        NearbyPlace("p1", "Apteka Pod Orłem", PlaceCategory.PHARMACY, 300, at(20)),
        NearbyPlace("p2", "Apteka Całodobowa", PlaceCategory.PHARMACY, 1100, alwaysOpen = true),
        NearbyPlace("l1", "Paczkomat WAW123", PlaceCategory.PARCEL_LOCKER, 120, alwaysOpen = true),
        NearbyPlace("l2", "Paczkomat WAW311", PlaceCategory.PARCEL_LOCKER, 650, alwaysOpen = true),
        NearbyPlace("o1", "Urząd pocztowy nr 12", PlaceCategory.POST_OFFICE, 900, at(19)),
        NearbyPlace("a1", "Bankomat przy rynku", PlaceCategory.ATM, 210, alwaysOpen = true),
    )

    private fun at(hour: Int) = LocalTime.of(hour, 0)
}
