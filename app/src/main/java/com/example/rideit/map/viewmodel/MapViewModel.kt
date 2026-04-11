package com.example.rideit.map.viewmodel

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rideit.map.model.LocationSuggestion
import com.example.rideit.map.model.MapUiState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val allowedCities = listOf(
        "Islamabad",
        "Rawalpindi",
        "Lahore"
    )

    fun onPickupTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            pickupText = value,
            selectedPickup = null,
            pickupLatLng = null,
            pickupSuggestions = emptyList(),
            routePoints = emptyList(),
            errorMessage = null
        )

        if (value.trim().length < 2) return
        loadSuggestions(query = value, isPickup = true)
    }

    fun onDropoffTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            dropoffText = value,
            selectedDropoff = null,
            dropoffLatLng = null,
            dropoffSuggestions = emptyList(),
            routePoints = emptyList(),
            errorMessage = null
        )

        if (value.trim().length < 2) return
        loadSuggestions(query = value, isPickup = false)
    }

    fun onPickupSuggestionSelected(suggestion: LocationSuggestion) {
        val pickupLatLng = LatLng(suggestion.latitude, suggestion.longitude)

        _uiState.value = _uiState.value.copy(
            pickupText = suggestion.title,
            pickupSuggestions = emptyList(),
            selectedPickup = suggestion,
            pickupLatLng = pickupLatLng,
            errorMessage = null
        )

        buildSimpleRouteIfPossible()
    }

    fun onDropoffSuggestionSelected(suggestion: LocationSuggestion) {
        val dropoffLatLng = LatLng(suggestion.latitude, suggestion.longitude)

        _uiState.value = _uiState.value.copy(
            dropoffText = suggestion.title,
            dropoffSuggestions = emptyList(),
            selectedDropoff = suggestion,
            dropoffLatLng = dropoffLatLng,
            errorMessage = null
        )

        buildSimpleRouteIfPossible()
    }

    fun onSearchClicked() {
        val state = _uiState.value

        if (state.pickupText.isBlank() || state.dropoffText.isBlank()) {
            _uiState.value = state.copy(
                errorMessage = "Please enter both pickup and dropoff locations."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val pickup = geocodeSingleAddress(state.pickupText)
            val dropoff = geocodeSingleAddress(state.dropoffText)

            if (pickup == null || dropoff == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Could not find one or both locations in Islamabad, Rawalpindi, or Lahore."
                )
                return@launch
            }

            val pickupLatLng = LatLng(pickup.latitude, pickup.longitude)
            val dropoffLatLng = LatLng(dropoff.latitude, dropoff.longitude)

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
                routePoints = listOf(pickupLatLng, dropoffLatLng),
                errorMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun buildSimpleRouteIfPossible() {
        val pickup = _uiState.value.pickupLatLng
        val dropoff = _uiState.value.dropoffLatLng

        if (pickup != null && dropoff != null) {
            _uiState.value = _uiState.value.copy(
                routePoints = listOf(pickup, dropoff)
            )
        }
    }

    private fun loadSuggestions(query: String, isPickup: Boolean) {
        viewModelScope.launch {
            val results = searchLocationSuggestions(query)

            if (isPickup) {
                _uiState.value = _uiState.value.copy(
                    pickupSuggestions = results
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    dropoffSuggestions = results
                )
            }
        }
    }

    private suspend fun searchLocationSuggestions(query: String): List<LocationSuggestion> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())

                val addresses = getAddressesFromName(geocoder, query, 10)

                addresses
                    .filter { address -> isAllowedCity(address) }
                    .mapNotNull { address ->
                        val title = buildSuggestionTitle(address)
                        val fullAddress = address.getAddressLine(0) ?: title

                        if (title.isBlank()) {
                            null
                        } else {
                            LocationSuggestion(
                                title = title,
                                fullAddress = fullAddress,
                                latitude = address.latitude,
                                longitude = address.longitude
                            )
                        }
                    }
                    .distinctBy { it.fullAddress.lowercase() }
                    .take(6)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private suspend fun geocodeSingleAddress(query: String): LocationSuggestion? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                val addresses = getAddressesFromName(geocoder, query, 5)

                val validAddress = addresses.firstOrNull { address ->
                    isAllowedCity(address)
                } ?: return@withContext null

                val title = buildSuggestionTitle(validAddress)
                val fullAddress = validAddress.getAddressLine(0) ?: title

                LocationSuggestion(
                    title = title,
                    fullAddress = fullAddress,
                    latitude = validAddress.latitude,
                    longitude = validAddress.longitude
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun isAllowedCity(address: Address): Boolean {
        val cityValues = listOfNotNull(
            address.locality,
            address.subAdminArea,
            address.adminArea
        )

        return cityValues.any { city ->
            allowedCities.any { allowed ->
                city.contains(allowed, ignoreCase = true)
            }
        }
    }

    private fun buildSuggestionTitle(address: Address): String {
        return when {
            !address.featureName.isNullOrBlank() && !address.locality.isNullOrBlank() ->
                "${address.featureName}, ${address.locality}"

            !address.thoroughfare.isNullOrBlank() && !address.locality.isNullOrBlank() ->
                "${address.thoroughfare}, ${address.locality}"

            !address.subLocality.isNullOrBlank() && !address.locality.isNullOrBlank() ->
                "${address.subLocality}, ${address.locality}"

            !address.locality.isNullOrBlank() ->
                address.locality ?: ""

            else ->
                address.getAddressLine(0) ?: ""
        }
    }

    private suspend fun getAddressesFromName(
        geocoder: Geocoder,
        query: String,
        maxResults: Int
    ): List<Address> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(query, maxResults) { addresses ->
                    continuation.resume(addresses ?: emptyList())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(query, maxResults) ?: emptyList()
        }
    }
}