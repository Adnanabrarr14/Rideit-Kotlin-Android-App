package com.example.rideit.map

data class MapUiState(
    val pickupText: String = "",
    val dropoffText: String = "",
    val pickupSuggestions: List<LocationSuggestion> = emptyList(),
    val dropoffSuggestions: List<LocationSuggestion> = emptyList(),
    val selectedPickupPlaceId: String = "",
    val selectedDropoffPlaceId: String = "",
    val searchMessage: String? = null
) {
    val isSearchEnabled: Boolean
        get() = selectedPickupPlaceId.isNotBlank() && selectedDropoffPlaceId.isNotBlank()
}