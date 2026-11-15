package com.example.apkmoblineprojekt.data

import com.example.apkmoblineprojekt.data.model.AppSettings
import com.example.apkmoblineprojekt.data.model.NearbyPlace
import com.example.apkmoblineprojekt.data.model.Note
import com.example.apkmoblineprojekt.data.model.Place
import com.example.apkmoblineprojekt.data.model.PlaceCategory
import com.example.apkmoblineprojekt.data.model.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong

// Implementacje tymczasowe – trzymają dane w pamięci procesu, żeby UI działało przed podpięciem Room.

class InMemoryTaskRepository(initial: List<Task>) : TaskRepository {
    private val tasks = MutableStateFlow(initial)
    private val nextId = AtomicLong((initial.maxOfOrNull { it.id } ?: 0) + 1)

    override fun observeTasks(): Flow<List<Task>> = tasks.asStateFlow()

    override suspend fun getTask(id: Long): Task? = tasks.value.firstOrNull { it.id == id }

    override suspend fun saveTask(task: Task): Long {
        if (task.id == 0L) {
            val id = nextId.getAndIncrement()
            tasks.update { it + task.copy(id = id) }
            return id
        }
        tasks.update { list -> list.map { if (it.id == task.id) task else it } }
        return task.id
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        tasks.update { list -> list.map { if (it.id == id) it.copy(done = done) else it } }
    }

    override suspend fun deleteTask(id: Long) {
        tasks.update { list -> list.filterNot { it.id == id } }
    }
}

class InMemoryNoteRepository(initial: List<Note>) : NoteRepository {
    private val notes = MutableStateFlow(initial)
    private val nextId = AtomicLong((initial.maxOfOrNull { it.id } ?: 0) + 1)

    override fun observeNotes(): Flow<List<Note>> = notes.asStateFlow()

    override suspend fun saveNote(note: Note): Long {
        if (note.id == 0L) {
            val id = nextId.getAndIncrement()
            notes.update { it + note.copy(id = id) }
            return id
        }
        notes.update { list -> list.map { if (it.id == note.id) note else it } }
        return note.id
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) {
        notes.update { list -> list.map { if (it.id == id) it.copy(pinned = pinned) else it } }
    }

    override suspend fun deleteNote(id: Long) {
        notes.update { list -> list.filterNot { it.id == id } }
    }
}

class InMemoryPlaceRepository(initial: List<Place>) : PlaceRepository {
    private val places = MutableStateFlow(initial)
    private val nextId = AtomicLong((initial.maxOfOrNull { it.id } ?: 0) + 1)

    override fun observePlaces(): Flow<List<Place>> = places.asStateFlow()

    override suspend fun savePlace(place: Place): Long {
        if (place.id == 0L) {
            val id = nextId.getAndIncrement()
            places.update { it + place.copy(id = id) }
            return id
        }
        places.update { list -> list.map { if (it.id == place.id) place else it } }
        return place.id
    }

    override suspend fun deletePlace(id: Long) {
        places.update { list -> list.filterNot { it.id == id } }
    }
}

class InMemorySettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(AppSettings())

    override val settings: Flow<AppSettings> = state.asStateFlow()

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        state.update(transform)
    }
}

/** Zwraca stałe wyniki z makiety, z opóźnieniem udającym zapytanie sieciowe. */
class FakePlaceSearchRepository : PlaceSearchRepository {
    override suspend fun searchNearby(category: PlaceCategory?, query: String): List<NearbyPlace> {
        if (category == null && query.isBlank()) return emptyList()
        delay(300)
        return SampleData.nearbyPlaces
            .filter { category == null || it.category == category }
            .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
            .sortedBy { it.distanceMeters }
    }
}
