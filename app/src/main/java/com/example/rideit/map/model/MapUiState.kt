package com.example.rideit.map.model

import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val pickupText: String = "",
    val dropoffText: String = "",
    val pickupSuggestions: List<LocationSuggestion> = emptyList(),
    val dropoffSuggestions: List<LocationSuggestion> = emptyList(),
    val selectedPickup: LocationSuggestion? = null,
    val selectedDropoff: LocationSuggestion? = null,
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)