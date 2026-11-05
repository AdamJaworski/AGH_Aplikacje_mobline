package com.example.apkmoblineprojekt.data

import com.example.apkmoblineprojekt.data.model.AppSettings
import com.example.apkmoblineprojekt.data.model.NearbyPlace
import com.example.apkmoblineprojekt.data.model.Note
import com.example.apkmoblineprojekt.data.model.Place
import com.example.apkmoblineprojekt.data.model.PlaceCategory
import com.example.apkmoblineprojekt.data.model.Task
import kotlinx.coroutines.flow.Flow

/** Docelowo: Room + GeofenceHelper/AlarmScheduler przy zapisie (HLD §5). */
interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun getTask(id: Long): Task?

    /** Wstawia zadanie z `id == 0` albo nadpisuje istniejące. Zwraca id. */
    suspend fun saveTask(task: Task): Long
    suspend fun setDone(id: Long, done: Boolean)
    suspend fun deleteTask(id: Long)
}

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun saveNote(note: Note): Long
    suspend fun setPinned(id: Long, pinned: Boolean)
    suspend fun deleteNote(id: Long)
}

/** Zapisane miejsca użytkownika: DOM, PRACA i inne. */
interface PlaceRepository {
    fun observePlaces(): Flow<List<Place>>
    suspend fun savePlace(place: Place): Long
    suspend fun deletePlace(id: Long)
}

/** Docelowo: Overpass API (kategorie) + Geocoder (adresy). */
interface PlaceSearchRepository {
    /** [category] == null i pusty [query] → pusta lista. */
    suspend fun searchNearby(category: PlaceCategory?, query: String): List<NearbyPlace>
}

/** Docelowo: DataStore. */
interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
