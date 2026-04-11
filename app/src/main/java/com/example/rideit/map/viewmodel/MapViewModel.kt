package com.example.rideit.map.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rideit.map.model.LocationSuggestion
import com.example.rideit.map.model.MapUiState
import com.example.rideit.map.model.RideOption
import com.example.rideit.map.repository.MapRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MapRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun onPickupTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            pickupText = value,
            selectedPickup = null,
            pickupLatLng = null,
            pickupSuggestions = repository.searchLocationSuggestions(value),
            routePoints = emptyList(),
            rideOptions = emptyList(),
            selectedRideOption = null,
            showRideOptions = false,
            rideConfirmedMessage = null,
            errorMessage = null
        )
    }

    fun onDropoffTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            dropoffText = value,
            selectedDropoff = null,
            dropoffLatLng = null,
            dropoffSuggestions = repository.searchLocationSuggestions(value),
            routePoints = emptyList(),
            rideOptions = emptyList(),
            selectedRideOption = null,
            showRideOptions = false,
            rideConfirmedMessage = null,
            errorMessage = null
        )
    }

    fun onPickupSuggestionSelected(suggestion: LocationSuggestion) {
        val pickupLatLng = LatLng(suggestion.latitude, suggestion.longitude)

        _uiState.value = _uiState.value.copy(
            pickupText = suggestion.title,
            selectedPickup = suggestion,
            pickupLatLng = pickupLatLng,
            pickupSuggestions = emptyList(),
            routePoints = emptyList(),
            rideOptions = emptyList(),
            selectedRideOption = null,
            showRideOptions = false,
            rideConfirmedMessage = null,
            errorMessage = null
        )

        buildPreviewRouteIfPossible()
    }

    fun onDropoffSuggestionSelected(suggestion: LocationSuggestion) {
        val dropoffLatLng = LatLng(suggestion.latitude, suggestion.longitude)

        _uiState.value = _uiState.value.copy(
            dropoffText = suggestion.title,
            selectedDropoff = suggestion,
            dropoffLatLng = dropoffLatLng,
            dropoffSuggestions = emptyList(),
            routePoints = emptyList(),
            rideOptions = emptyList(),
            selectedRideOption = null,
            showRideOptions = false,
            rideConfirmedMessage = null,
            errorMessage = null
        )

        buildPreviewRouteIfPossible()
    }

    fun onSearchClicked() {
        val state = _uiState.value

        if (state.pickupText.isBlank() || state.dropoffText.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Please enter both pickup and dropoff locations."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            rideConfirmedMessage = null
        )

        try {
            val pickup = repository.findPlace(state.pickupText)
            val dropoff = repository.findPlace(state.dropoffText)

            if (pickup == null || dropoff == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Select a valid place from suggestions."
                )
                return
            }

            val pickupLatLng = LatLng(pickup.latitude, pickup.longitude)
            val dropoffLatLng = LatLng(dropoff.latitude, dropoff.longitude)

            val routePoints = repository.getRoutePoints(
                origin = pickupLatLng,
                destination = dropoffLatLng
            )

            val distanceKm = repository.calculateDistanceKm(pickupLatLng, dropoffLatLng)
            val rideOptions = buildRideOptions(distanceKm)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                pickupText = pickup.title,
                dropoffText = dropoff.title,
                selectedPickup = pickup,
                selectedDropoff = dropoff,
                pickupLatLng = pickupLatLng,
                dropoffLatLng = dropoffLatLng,
                pickupSuggestions = emptyList(),
                dropoffSuggestions = emptyList(),
                routePoints = routePoints,
                rideOptions = rideOptions,
                selectedRideOption = rideOptions.firstOrNull(),
                showRideOptions = rideOptions.isNotEmpty(),
                rideConfirmedMessage = null,
                errorMessage = null
            )
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Search failed. Please try again."
            )
        }
    }

    fun onRideOptionSelected(option: RideOption) {
        _uiState.value = _uiState.value.copy(
            selectedRideOption = option,
            rideConfirmedMessage = null
        )
    }

    fun onConfirmRideClicked() {
        val selectedRide = _uiState.value.selectedRideOption ?: run {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select a ride option first."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            rideConfirmedMessage = "${selectedRide.title} ride confirmed.",
            errorMessage = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun buildPreviewRouteIfPossible() {
        val pickup = _uiState.value.pickupLatLng
        val dropoff = _uiState.value.dropoffLatLng

        if (pickup != null && dropoff != null) {
            _uiState.value = _uiState.value.copy(
                routePoints = listOf(pickup, dropoff),
                rideOptions = emptyList(),
                selectedRideOption = null,
                showRideOptions = false,
                rideConfirmedMessage = null
            )
        }
    }

    private fun buildRideOptions(distanceKm: Double): List<RideOption> {
        val safeDistance = distanceKm.coerceAtLeast(1.0)

        val bikeFare = (80 + safeDistance * 18).roundToInt()
        val miniFare = (140 + safeDistance * 24).roundToInt()
        val carFare = (220 + safeDistance * 32).roundToInt()

        val bikeTime = estimateTimeMinutes(safeDistance, 28.0)
        val miniTime = estimateTimeMinutes(safeDistance, 32.0)
        val carTime = estimateTimeMinutes(safeDistance, 35.0)

        return listOf(
            RideOption(
                id = "bike",
                title = "Bike",
                subtitle = "Affordable quick ride",
                estimatedFare = "PKR $bikeFare",
                estimatedTime = "$bikeTime min"
            ),
            RideOption(
                id = "mini",
                title = "Mini",
                subtitle = "Budget everyday ride",
                estimatedFare = "PKR $miniFare",
                estimatedTime = "$miniTime min"
            ),
            RideOption(
                id = "car",
                title = "Car",
                subtitle = "Comfort ride",
                estimatedFare = "PKR $carFare",
                estimatedTime = "$carTime min"
            )
        )
    }

    private fun estimateTimeMinutes(distanceKm: Double, averageSpeedKmH: Double): Int {
        return ((distanceKm / averageSpeedKmH) * 60).roundToInt().coerceAtLeast(3)
    }
}