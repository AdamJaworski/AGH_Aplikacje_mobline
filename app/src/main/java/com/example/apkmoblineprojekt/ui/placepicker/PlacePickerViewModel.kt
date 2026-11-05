package com.example.apkmoblineprojekt.ui.placepicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkmoblineprojekt.data.PlaceSearchRepository
import com.example.apkmoblineprojekt.data.SettingsRepository
import com.example.apkmoblineprojekt.data.model.DEFAULT_RADIUS_METERS
import com.example.apkmoblineprojekt.data.model.LocationType
import com.example.apkmoblineprojekt.data.model.NearbyPlace
import com.example.apkmoblineprojekt.data.model.PlaceCategory
import com.example.apkmoblineprojekt.data.model.TaskLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlacePickerUiState(
    val query: String = "",
    val category: PlaceCategory? = null,
    val results: List<NearbyPlace> = emptyList(),
    val searching: Boolean = false,
    val selection: TaskLocation? = null,
    val radiusMeters: Int = DEFAULT_RADIUS_METERS,
)

class PlacePickerViewModel(
    private val placeSearchRepository: PlaceSearchRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacePickerUiState())
    val uiState: StateFlow<PlacePickerUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    /** Wywoływane przy każdym otwarciu arkusza. */
    fun start(initial: TaskLocation?) {
        viewModelScope.launch {
            val radius = initial?.radiusMeters ?: settingsRepository.settings.first().defaultRadiusMeters
            _uiState.value = PlacePickerUiState(
                category = initial?.category,
                selection = initial,
                radiusMeters = radius,
            )
            search(debounce = false)
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        search(debounce = true)
    }

    fun selectCategory(category: PlaceCategory) {
        _uiState.update {
            it.copy(
                category = category,
                selection = TaskLocation(LocationType.CATEGORY, category = category, radiusMeters = it.radiusMeters),
            )
        }
        search(debounce = false)
    }

    fun selectNearby(place: NearbyPlace) {
        _uiState.update {
            it.copy(
                selection = TaskLocation(
                    LocationType.POINT,
                    name = place.name,
                    category = place.category,
                    radiusMeters = it.radiusMeters,
                ),
            )
        }
    }

    /** TODO: pobrać bieżącą pozycję (FusedLocationProvider) i wycentrować mapę. */
    fun locateMe() = Unit

    private fun search(debounce: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounce) delay(300)
            val (category, query) = _uiState.value.let { it.category to it.query }
            _uiState.update { it.copy(searching = true) }
            val results = placeSearchRepository.searchNearby(category, query)
            _uiState.update { it.copy(results = results, searching = false) }
        }
    }
}
