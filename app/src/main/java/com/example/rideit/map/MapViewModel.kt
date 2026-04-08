package com.example.rideit.ui.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MapViewModel : ViewModel() {

    private val dummyLocations = listOf(
        LocationSuggestion("1", "Saddar", "Rawalpindi"),
        LocationSuggestion("2", "Commercial Market", "Rawalpindi"),
        LocationSuggestion("3", "Raja Bazaar", "Rawalpindi"),
        LocationSuggestion("4", "Committee Chowk", "Rawalpindi"),
        LocationSuggestion("5", "Faizabad", "Islamabad"),
        LocationSuggestion("6", "Blue Area", "Islamabad"),
        LocationSuggestion("7", "F-10 Markaz", "Islamabad"),
        LocationSuggestion("8", "Giga Mall", "DHA Islamabad"),
        LocationSuggestion("9", "Bahria Town Phase 7", "Rawalpindi"),
        LocationSuggestion("10", "Rawalpindi Railway Station", "Rawalpindi"),
        LocationSuggestion("11", "Islamabad International Airport", "Islamabad"),
        LocationSuggestion("12", "Centaurus Mall", "Islamabad")
    )

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun onPickupTextChanged(text: String) {
        _uiState.update {
            it.copy(
                pickupText = text,
                pickupSuggestions = filterSuggestions(text)
            )
        }
    }

    fun onDropoffTextChanged(text: String) {
        _uiState.update {
            it.copy(
                dropoffText = text,
                dropoffSuggestions = filterSuggestions(text)
            )
        }
    }

    fun onPickupSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.update {
            it.copy(
                pickupText = suggestion.title,
                pickupSuggestions = emptyList()
            )
        }
    }

    fun onDropoffSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.update {
            it.copy(
                dropoffText = suggestion.title,
                dropoffSuggestions = emptyList()
            )
        }
    }

    fun clearSuggestions() {
        _uiState.update {
            it.copy(
                pickupSuggestions = emptyList(),
                dropoffSuggestions = emptyList()
            )
        }
    }

    private fun filterSuggestions(query: String): List<LocationSuggestion> {
        if (query.isBlank()) return emptyList()

        return dummyLocations.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
        }.take(6)
    }
}