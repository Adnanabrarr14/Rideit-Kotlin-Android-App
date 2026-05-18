package com.example.rideit.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rideit.RideitFareConstants
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
    private val fallbackPickupLatLng = LatLng(33.6844, 73.0479)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    private var searchJob: Job? = null
    private var driverJob: Job? = null
    private var quickPlaceJob: Job? = null

    fun onPickupTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(pickupText = text)
    }

    fun onCurrentLocationDetected(latLng: LatLng) {
        val currentState = _uiState.value

        _uiState.value = currentState.copy(
            pickupText = "Current location",
            pickupLatLng = latLng,
            routePoints = buildRoutePreview(
                pickup = latLng,
                dropoff = currentState.dropoffLatLng
            ),
            errorMessage = null
        )
    }

    fun onDropoffTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(
            dropoffText = text,
            dropoffLatLng = null,
            routePoints = emptyList(),
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

    fun onQuickPlaceSelected(type: String) {
        val cleanType = type.lowercase().trim()
        val currentLocation = _uiState.value.pickupLatLng

        quickPlaceJob?.cancel()
        quickPlaceJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showRideOptions = false,
                selectedRideOption = null,
                routePoints = emptyList(),
                dropoffLatLng = null,
                locationSuggestions = emptyList(),
                errorMessage = when (cleanType) {
                    "home" -> "Detecting your home/current area..."
                    "work" -> "Select or add your work location"
                    "mall" -> "Searching nearby malls..."
                    "airport" -> "Searching nearby airports..."
                    "restaurant" -> "Searching nearby restaurants..."
                    else -> null
                }
            )

            val suggestions = repository.searchQuickPlaceSuggestions(
                quickPlaceType = cleanType,
                currentLocation = currentLocation
            )

            when (cleanType) {
                "home" -> {
                    val home = suggestions.firstOrNull()
                    if (home != null) {
                        val dropoff = LatLng(home.latitude, home.longitude)
                        _uiState.value = _uiState.value.copy(
                            dropoffText = home.title,
                            dropoffLatLng = dropoff,
                            routePoints = buildRoutePreview(
                                pickup = _uiState.value.pickupLatLng,
                                dropoff = dropoff
                            ),
                            locationSuggestions = emptyList(),
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            dropoffText = "",
                            dropoffLatLng = null,
                            locationSuggestions = emptyList(),
                            errorMessage = "Turn on location services to detect your home/current area"
                        )
                    }
                }

                "work" -> {
                    _uiState.value = _uiState.value.copy(
                        dropoffText = "Work",
                        dropoffLatLng = null,
                        locationSuggestions = suggestions,
                        errorMessage = if (suggestions.isEmpty()) {
                            "Search and select your work location"
                        } else {
                            "Select your work location from suggestions"
                        }
                    )
                }

                "mall" -> {
                    _uiState.value = _uiState.value.copy(
                        dropoffText = "Mall",
                        dropoffLatLng = null,
                        locationSuggestions = suggestions,
                        errorMessage = if (suggestions.isEmpty()) {
                            "No nearby malls found. Try typing mall name."
                        } else {
                            "Select a nearby mall"
                        }
                    )
                }

                "airport" -> {
                    _uiState.value = _uiState.value.copy(
                        dropoffText = "Airport",
                        dropoffLatLng = null,
                        locationSuggestions = suggestions,
                        errorMessage = if (suggestions.isEmpty()) {
                            "No nearby airports found. Try typing airport name."
                        } else {
                            "Select a nearby airport"
                        }
                    )
                }

                "restaurant" -> {
                    _uiState.value = _uiState.value.copy(
                        dropoffText = "Restaurant",
                        dropoffLatLng = null,
                        locationSuggestions = suggestions,
                        errorMessage = if (suggestions.isEmpty()) {
                            "No nearby restaurants found. Try typing restaurant name."
                        } else {
                            "Select a nearby restaurant"
                        }
                    )
                }
            }
        }
    }

    fun onSuggestionSelected(suggestion: LocationSuggestion) {
        val dropoff = LatLng(suggestion.latitude, suggestion.longitude)

        _uiState.value = _uiState.value.copy(
            dropoffText = suggestion.title,
            dropoffLatLng = dropoff,
            routePoints = buildRoutePreview(
                pickup = _uiState.value.pickupLatLng,
                dropoff = dropoff
            ),
            locationSuggestions = emptyList(),
            errorMessage = null
        )
    }

    fun onSearchClicked() {
        val pickup = _uiState.value.pickupLatLng ?: fallbackPickupLatLng
        val dropoff = _uiState.value.dropoffLatLng

        if (dropoff == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Select a dropoff location from suggestions"
            )
            return
        }

        if (
            pickup.latitude == dropoff.latitude &&
            pickup.longitude == dropoff.longitude
        ) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Pickup and dropoff cannot be the same location"
            )
            return
        }

        val rides = createRideOptions()

        _uiState.value = _uiState.value.copy(
            pickupLatLng = pickup,
            routePoints = buildRoutePreview(pickup, dropoff),
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
                    RideitFareConstants.MINI_TITLE -> "Suzuki Alto"
                    RideitFareConstants.COMFORT_TITLE -> "Toyota Corolla"
                    RideitFareConstants.BUSINESS_TITLE -> "Toyota Fortuner"
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
            dropoffText = "",
            dropoffLatLng = null,
            routePoints = emptyList(),
            selectedRideOption = null,
            driver = null,
            driverLatLng = null,
            rideConfirmedMessage = "Ride cancelled"
        )
    }

    private fun createRideOptions(): List<RideOption> {
        return listOf(
            RideOption(
                id = RideitFareConstants.MINI_ID,
                title = RideitFareConstants.MINI_TITLE,
                subtitle = RideitFareConstants.MINI_SUBTITLE,
                estimatedFare = RideitFareConstants.formatFare(RideitFareConstants.MINI_FARE),
                estimatedTime = RideitFareConstants.MINI_TIME
            ),
            RideOption(
                id = RideitFareConstants.COMFORT_ID,
                title = RideitFareConstants.COMFORT_TITLE,
                subtitle = RideitFareConstants.COMFORT_SUBTITLE,
                estimatedFare = RideitFareConstants.formatFare(RideitFareConstants.COMFORT_FARE),
                estimatedTime = RideitFareConstants.COMFORT_TIME
            ),
            RideOption(
                id = RideitFareConstants.BUSINESS_ID,
                title = RideitFareConstants.BUSINESS_TITLE,
                subtitle = RideitFareConstants.BUSINESS_SUBTITLE,
                estimatedFare = RideitFareConstants.formatFare(RideitFareConstants.BUSINESS_FARE),
                estimatedTime = RideitFareConstants.BUSINESS_TIME
            )
        )
    }

    private fun buildRoutePreview(
        pickup: LatLng?,
        dropoff: LatLng?
    ): List<LatLng> {
        val destination = dropoff ?: return emptyList()
        val origin = pickup ?: fallbackPickupLatLng

        return repository.getRoutePoints(origin, destination)
            .ifEmpty { listOf(origin, destination) }
    }
}
