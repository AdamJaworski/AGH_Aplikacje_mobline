package com.example.apkmoblineprojekt.ui.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkmoblineprojekt.data.PlaceRepository
import com.example.apkmoblineprojekt.data.TaskRepository
import com.example.apkmoblineprojekt.data.model.Place
import com.example.apkmoblineprojekt.data.model.PlaceKind
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavedPlaceItem(val place: Place, val openTaskCount: Int)

data class PlacesUiState(val places: List<SavedPlaceItem> = emptyList())

class PlacesViewModel(
    private val placeRepository: PlaceRepository,
    taskRepository: TaskRepository,
) : ViewModel() {

    val uiState: StateFlow<PlacesUiState> =
        combine(placeRepository.observePlaces(), taskRepository.observeTasks()) { places, tasks ->
            val counts = tasks
                .filter { !it.done }
                .mapNotNull { it.location?.placeId }
                .groupingBy { it }
                .eachCount()
            PlacesUiState(
                places = places
                    .sortedBy { it.kind.ordinal }
                    .map { SavedPlaceItem(it, counts[it.id] ?: 0) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlacesUiState())

    /** TODO: współrzędne z Geocodera / pinezki na mapie, gdy będzie prawdziwa mapa. */
    fun addPlace(name: String) {
        viewModelScope.launch {
            placeRepository.savePlace(Place(name = name, address = "", kind = PlaceKind.SAVED))
        }
    }
}
