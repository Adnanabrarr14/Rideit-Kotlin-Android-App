package com.example.rideit.map

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val fallbackLocations = listOf(
        LocationSuggestion("local_1", "Saddar", "Rawalpindi"),
        LocationSuggestion("local_2", "Saddar", "Karachi"),
        LocationSuggestion("local_3", "DHA Phase 1", "Rawalpindi"),
        LocationSuggestion("local_4", "DHA Phase 2", "Islamabad"),
        LocationSuggestion("local_5", "DHA Phase 3", "Lahore"),
        LocationSuggestion("local_6", "DHA Phase 4", "Lahore"),
        LocationSuggestion("local_7", "DHA Phase 5", "Lahore"),
        LocationSuggestion("local_8", "DHA Phase 6", "Lahore"),
        LocationSuggestion("local_9", "Bahria Town Phase 7", "Rawalpindi"),
        LocationSuggestion("local_10", "Bahria Town Phase 8", "Rawalpindi"),
        LocationSuggestion("local_11", "Blue Area", "Islamabad"),
        LocationSuggestion("local_12", "F-10 Markaz", "Islamabad"),
        LocationSuggestion("local_13", "F-11 Markaz", "Islamabad"),
        LocationSuggestion("local_14", "Johar Town", "Lahore"),
        LocationSuggestion("local_15", "Gulberg", "Lahore"),
        LocationSuggestion("local_16", "Model Town", "Lahore"),
        LocationSuggestion("local_17", "Committee Chowk", "Rawalpindi"),
        LocationSuggestion("local_18", "Commercial Market", "Rawalpindi")
    )

    private val placesClient: PlacesClient? by lazy {
        try {
            val context = getApplication<Application>().applicationContext
            val apiKey = getApiKeyFromManifest()

            if (apiKey.isBlank()) {
                Log.e("RideitPlaces", "API key not found in manifest")
                null
            } else {
                if (!Places.isInitialized()) {
                    Places.initialize(context, apiKey)
                }
                Places.createClient(context)
            }
        } catch (e: Exception) {
            Log.e("RideitPlaces", "Places init failed: ${e.message}")
            null
        }
    }

    private val rideitBiasBounds = LatLngBounds(
        LatLng(31.15, 72.70),
        LatLng(33.95, 74.10)
    )

    fun onPickupTextChanged(text: String) {
        _uiState.update {
            it.copy(
                pickupText = text,
                selectedPickupPlaceId = "",
                searchMessage = null
            )
        }

        if (text.length < 2) {
            _uiState.update { it.copy(pickupSuggestions = emptyList()) }
            return
        }

        fetchPredictions(query = text, isPickup = true)
    }

    fun onDropoffTextChanged(text: String) {
        _uiState.update {
            it.copy(
                dropoffText = text,
                selectedDropoffPlaceId = "",
                searchMessage = null
            )
        }

        if (text.length < 2) {
            _uiState.update { it.copy(dropoffSuggestions = emptyList()) }
            return
        }

        fetchPredictions(query = text, isPickup = false)
    }

    fun onPickupSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.update {
            it.copy(
                pickupText = buildFullText(suggestion),
                selectedPickupPlaceId = suggestion.placeId,
                pickupSuggestions = emptyList(),
                searchMessage = null
            )
        }
    }

    fun onDropoffSuggestionSelected(suggestion: LocationSuggestion) {
        _uiState.update {
            it.copy(
                dropoffText = buildFullText(suggestion),
                selectedDropoffPlaceId = suggestion.placeId,
                dropoffSuggestions = emptyList(),
                searchMessage = null
            )
        }
    }

    fun onSearchClicked() {
        val state = _uiState.value

        if (!state.isSearchEnabled) {
            _uiState.update {
                it.copy(searchMessage = "Please select both Pickup and Dropoff")
            }
            return
        }

        _uiState.update {
            it.copy(
                pickupSuggestions = emptyList(),
                dropoffSuggestions = emptyList(),
                searchMessage = "Searching route from ${state.pickupText} to ${state.dropoffText}"
            )
        }
    }

    fun consumeSearchMessage() {
        _uiState.update {
            it.copy(searchMessage = null)
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

    private fun fetchPredictions(query: String, isPickup: Boolean) {
        val client = placesClient

        if (client == null) {
            showFallbackSuggestions(query, isPickup)
            return
        }

        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .setCountries(listOf("PK"))
            .setLocationBias(RectangularBounds.newInstance(rideitBiasBounds))
            .build()

        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val realItems = response.autocompletePredictions.map { prediction ->
                    LocationSuggestion(
                        placeId = prediction.placeId,
                        title = prediction.getPrimaryText(null).toString(),
                        subtitle = prediction.getSecondaryText(null)?.toString().orEmpty()
                    )
                }

                val finalItems = if (realItems.isNotEmpty()) {
                    realItems
                } else {
                    filterFallbackSuggestions(query)
                }

                if (isPickup) {
                    if (_uiState.value.pickupText == query) {
                        _uiState.update { it.copy(pickupSuggestions = finalItems) }
                    }
                } else {
                    if (_uiState.value.dropoffText == query) {
                        _uiState.update { it.copy(dropoffSuggestions = finalItems) }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RideitPlaces", "Autocomplete failed: ${e.message}")
                showFallbackSuggestions(query, isPickup)
            }
    }

    private fun showFallbackSuggestions(query: String, isPickup: Boolean) {
        val items = filterFallbackSuggestions(query)

        if (isPickup) {
            if (_uiState.value.pickupText == query) {
                _uiState.update { it.copy(pickupSuggestions = items) }
            }
        } else {
            if (_uiState.value.dropoffText == query) {
                _uiState.update { it.copy(dropoffSuggestions = items) }
            }
        }
    }

    private fun filterFallbackSuggestions(query: String): List<LocationSuggestion> {
        return fallbackLocations.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.subtitle.contains(query, ignoreCase = true)
        }.take(8)
    }

    private fun buildFullText(suggestion: LocationSuggestion): String {
        return if (suggestion.subtitle.isNotBlank()) {
            "${suggestion.title}, ${suggestion.subtitle}"
        } else {
            suggestion.title
        }
    }

    private fun getApiKeyFromManifest(): String {
        return try {
            val context = getApplication<Application>().applicationContext
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
        } catch (e: Exception) {
            Log.e("RideitPlaces", "Manifest key read failed: ${e.message}")
            ""
        }
    }
}