package com.example.rideit.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rideit.map.model.LocationSuggestion
import com.example.rideit.map.model.MapUiState
import com.example.rideit.map.model.RideOption
import com.example.rideit.map.model.RideRequestStatus
import com.example.rideit.map.repository.MapRepository
import com.example.rideit.model.Driver
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val repository = MapRepository()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    private var searchJob: Job? = null
    private var driverJob: Job? = null

    fun onPickupTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(pickupText = text)
    }

    fun onDropoffTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(
            dropoffText = text,
            showRideOptions = false,
            selectedRideOption = null,
            errorMessage = null
        )

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)

            val suggestions = repository.searchLocationSuggestions(text)

            _uiState.value = _uiState.value.copy(
                locationSuggestions = suggestions,
                errorMessage = if (text.trim().length >= 2 && suggestions.isEmpty()) {
                    "No locations found"
                } else {
                    null
                }
            )
        }
    }

    fun onSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.value = _uiState.value.copy(
            dropoffText = suggestion.title,
            dropoffLatLng = LatLng(suggestion.latitude, suggestion.longitude),
            locationSuggestions = emptyList(),
            errorMessage = null
        )
    }

    fun onSearchClicked() {
        val pickup = _uiState.value.pickupLatLng ?: LatLng(33.6844, 73.0479)
        val dropoff = _uiState.value.dropoffLatLng

        if (dropoff == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Select a dropoff location from suggestions"
            )
            return
        }

        val rides = listOf(
            RideOption("1", "Mini", "Suzuki Alto / Mira", "Rs. 180", "2 min"),
            RideOption("2", "Comfort", "Corolla / Civic / Elantra", "Rs. 320", "4 min"),
            RideOption("3", "Business", "Fortuner / Prado / Tucson", "Rs. 580", "6 min")
        )

        _uiState.value = _uiState.value.copy(
            pickupLatLng = pickup,
            routePoints = repository.getRoutePoints(pickup, dropoff),
            showRideOptions = true,
            rideOptions = rides,
            selectedRideOption = rides.first(),
            rideRequestStatus = RideRequestStatus.IDLE,
            driver = null,
            driverLatLng = null,
            rideConfirmedMessage = null,
            errorMessage = null
        )
    }

    fun onRideOptionSelected(option: RideOption) {
        _uiState.value = _uiState.value.copy(selectedRideOption = option)
    }

    fun onConfirmRideClicked() {
        val selectedRide = _uiState.value.selectedRideOption ?: return
        val pickup = _uiState.value.pickupLatLng ?: return

        driverJob?.cancel()

        _uiState.value = _uiState.value.copy(
            rideRequestStatus = RideRequestStatus.SEARCHING_DRIVER,
            showRideOptions = false,
            rideConfirmedMessage = "Searching for ${selectedRide.title} driver..."
        )

        driverJob = viewModelScope.launch {
            delay(2200)

            val driver = Driver(
                id = "1",
                name = "Ali Khan",
                vehicleName = when (selectedRide.title) {
                    "Mini" -> "Suzuki Alto"
                    "Comfort" -> "Toyota Corolla"
                    "Business" -> "Toyota Fortuner"
                    else -> "Suzuki Alto"
                },
                vehicleNumber = "LEA-1234",
                rating = 4.8,
                arrivalTime = selectedRide.estimatedTime
            )

            val driverStart = LatLng(
                pickup.latitude + 0.018,
                pickup.longitude - 0.018
            )

            val driverRoute = repository.getRoutePoints(driverStart, pickup)

            _uiState.value = _uiState.value.copy(
                rideRequestStatus = RideRequestStatus.DRIVER_FOUND,
                driver = driver,
                driverLatLng = driverStart,
                rideConfirmedMessage = "Driver found"
            )

            delay(1000)

            _uiState.value = _uiState.value.copy(
                rideRequestStatus = RideRequestStatus.DRIVER_ARRIVING,
                rideConfirmedMessage = "Driver is arriving..."
            )

            driverRoute.forEach { point ->
                delay(180)
                _uiState.value = _uiState.value.copy(driverLatLng = point)
            }

            _uiState.value = _uiState.value.copy(
                rideRequestStatus = RideRequestStatus.RIDE_STARTED,
                driverLatLng = pickup,
                rideConfirmedMessage = "Driver arrived. Ride started!"
            )
        }
    }

    fun onCancelRideClicked() {
        driverJob?.cancel()

        _uiState.value = _uiState.value.copy(
            rideRequestStatus = RideRequestStatus.CANCELLED,
            showRideOptions = false,
            driver = null,
            driverLatLng = null,
            rideConfirmedMessage = "Ride cancelled"
        )
    }
}