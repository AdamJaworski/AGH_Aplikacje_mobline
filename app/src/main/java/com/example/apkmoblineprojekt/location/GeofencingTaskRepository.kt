package com.example.apkmoblineprojekt.location

import android.util.Log
import com.example.apkmoblineprojekt.data.PlaceRepository
import com.example.apkmoblineprojekt.data.TaskRepository
import com.example.apkmoblineprojekt.data.model.LocationType
import com.example.apkmoblineprojekt.data.model.Place
import com.example.apkmoblineprojekt.data.model.PlaceKind
import com.example.apkmoblineprojekt.data.model.Task
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.ZoneId

/**
 * Dekorator [TaskRepository], który po zapisie, oznaczeniu jako zrobione albo usunięciu zadania
 * aktualizuje jego strefy geofence (HLD §5 p. 1, 2 i 4).
 *
 * Punkty strefy:
 * - POINT → współrzędne wybrane w „Wybierz miejsce” (geokodowanie adresu),
 * - HOME / WORK / SAVED → współrzędne zapisanego miejsca; jeśli ich brak, geokodujemy jego adres,
 * - CATEGORY → jeszcze nie: wymaga wyszukiwania przez Overpass (HLD §11, etap 6).
 */
class GeofencingTaskRepository(
    private val delegate: TaskRepository,
    private val placeRepository: PlaceRepository,
    private val geocodingRepository: GeocodingRepository,
    private val store: GeofenceStore,
    private val helper: GeofenceHelper,
) : TaskRepository by delegate {

    override suspend fun saveTask(task: Task): Long {
        val id = delegate.saveTask(task)
        updateZones(delegate.getTask(id) ?: task.copy(id = id))
        return id
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        delegate.setDone(id, done)
        delegate.getTask(id)?.let { updateZones(it) }
    }

    override suspend fun deleteTask(id: Long) {
        val task = delegate.getTask(id)
        delegate.deleteTask(id)
        if (task != null) {
            store.removeForTask(task.uid)
            helper.sync()
        }
    }

    private suspend fun updateZones(task: Task) {
        val zones = if (task.done) emptyList() else zonesFor(task)
        if (zones.isEmpty()) store.removeForTask(task.uid) else store.replaceForTask(task.uid, zones)
        helper.sync()
    }

    private suspend fun zonesFor(task: Task): List<GeofenceZone> {
        val location = task.location ?: return emptyList()
        val point: Triple<String, Double, Double>? = when (location.type) {
            LocationType.POINT -> {
                val lat = location.latitude
                val lng = location.longitude
                if (lat != null && lng != null) Triple(location.name.ifBlank { location.address.orEmpty() }, lat, lng) else null
            }
            LocationType.HOME, LocationType.WORK, LocationType.SAVED ->
                findPlace(location.type, location.placeId)?.let { place -> coordinatesOf(place) }
            LocationType.CATEGORY -> null // TODO etap 6: do 5 najbliższych miejsc wokół DOM i PRACA (Overpass).
        }
        if (point == null) {
            Log.i(TAG, "Zadanie ${task.id}: brak współrzędnych dla ${location.type} – bez strefy")
            return emptyList()
        }
        val (name, lat, lng) = point
        return listOf(
            GeofenceZone(
                requestId = "task:${task.uid}:${location.placeId ?: "point"}",
                taskUid = task.uid,
                taskId = task.id,
                taskTitle = task.title,
                placeName = name,
                latitude = lat,
                longitude = lng,
                radiusMeters = location.radiusMeters,
                dueAt = task.date?.atTime(task.time ?: LocalTime.MAX)
                    ?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            ),
        )
    }

    private suspend fun findPlace(type: LocationType, placeId: Long?): Place? {
        val places = placeRepository.observePlaces().first()
        placeId?.let { id -> places.firstOrNull { it.id == id }?.let { return it } }
        val kind = when (type) {
            LocationType.HOME -> PlaceKind.HOME
            LocationType.WORK -> PlaceKind.WORK
            else -> return null
        }
        return places.firstOrNull { it.kind == kind }
    }

    private suspend fun coordinatesOf(place: Place): Triple<String, Double, Double>? {
        val lat = place.latitude
        val lng = place.longitude
        if (lat != null && lng != null) return Triple(place.name, lat, lng)
        val geocoded = geocodingRepository.geocode(place.address) ?: return null
        return Triple(place.name, geocoded.lat, geocoded.lng)
    }

    private companion object {
        const val TAG = "GeofencingTaskRepo"
    }
}
