package com.example.rideit.map.model

import com.example.rideit.model.Driver
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val pickupText: String = "Current location",
    val dropoffText: String = "",
    val pickupLatLng: LatLng? = LatLng(33.6844, 73.0479),
    val dropoffLatLng: LatLng? = null,
    val driverLatLng: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val locationSuggestions: List<LocationSuggestion> = emptyList(),
    val showRideOptions: Boolean = false,
    val rideOptions: List<RideOption> = emptyList(),
    val selectedRideOption: RideOption? = null,
    val rideRequestStatus: RideRequestStatus = RideRequestStatus.IDLE,
    val driver: Driver? = null,
    val rideConfirmedMessage: String? = null,
    val errorMessage: String? = null
)