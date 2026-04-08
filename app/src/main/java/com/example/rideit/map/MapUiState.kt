package com.example.rideit.ui.map

data class MapUiState(
    val pickupText: String = "",
    val dropoffText: String = "",
    val pickupSuggestions: List<LocationSuggestion> = emptyList(),
    val dropoffSuggestions: List<LocationSuggestion> = emptyList()
)