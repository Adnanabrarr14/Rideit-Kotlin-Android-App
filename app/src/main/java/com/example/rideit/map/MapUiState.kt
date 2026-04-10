package com.example.rideit.map

import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val pickupText: String = "",
    val dropoffText: String = "",
    val pickupSuggestions: List<LocationSuggestion> = emptyList(),
    val dropoffSuggestions: List<LocationSuggestion> = emptyList(),
    val selectedPickupPlaceId: String = "",
    val selectedDropoffPlaceId: String = "",
    val searchMessage: String? = null,
    val pickupLatLng: LatLng? = null,
    val dropoffLatLng: LatLng? = null,
    val showSearchMarkers: Boolean = false,
    val searchRequestId: Long = 0L,
    val routePoints: List<LatLng> = emptyList(),
    val isRouteLoading: Boolean = false
) {
    val isSearchEnabled: Boolean
        get() = pickupText.isNotBlank() && dropoffText.isNotBlank()
}