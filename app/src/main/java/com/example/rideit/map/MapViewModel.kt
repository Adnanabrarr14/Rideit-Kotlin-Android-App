package com.example.rideit.map

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val httpClient = OkHttpClient()

    private val serviceAreaBounds = LatLngBounds(
        LatLng(31.1500, 72.7000),
        LatLng(33.9500, 74.1000)
    )

    private val placesClient: PlacesClient? by lazy {
        try {
            val context = getApplication<Application>().applicationContext
            val apiKey = getMapsApiKeyFromManifest()

            if (apiKey.isBlank()) {
                Log.e("RideitPlaces", "Maps API key missing in manifest")
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

    private val localSuggestions = listOf(
        LocationSuggestion("local_i_1", "Blue Area", "Islamabad", LatLng(33.7101, 73.0498)),
        LocationSuggestion("local_i_2", "F-10", "Islamabad", LatLng(33.6969, 73.0138)),
        LocationSuggestion("local_i_3", "F-10 Markaz", "Islamabad", LatLng(33.6969, 73.0138)),
        LocationSuggestion("local_i_4", "F-11", "Islamabad", LatLng(33.6994, 72.9892)),
        LocationSuggestion("local_i_5", "F-11 Markaz", "Islamabad", LatLng(33.6994, 72.9892)),
        LocationSuggestion("local_i_6", "G-9", "Islamabad", LatLng(33.6846, 73.0322)),
        LocationSuggestion("local_i_7", "G-9 Markaz", "Islamabad", LatLng(33.6846, 73.0322)),
        LocationSuggestion("local_i_8", "G-10", "Islamabad", LatLng(33.6757, 73.0167)),
        LocationSuggestion("local_i_9", "G-10 Markaz", "Islamabad", LatLng(33.6757, 73.0167)),
        LocationSuggestion("local_i_10", "G-11", "Islamabad", LatLng(33.6718, 72.9917)),
        LocationSuggestion("local_i_11", "G-11 Markaz", "Islamabad", LatLng(33.6718, 72.9917)),
        LocationSuggestion("local_i_12", "I-8", "Islamabad", LatLng(33.6646, 73.0731)),
        LocationSuggestion("local_i_13", "I-8 Markaz", "Islamabad", LatLng(33.6646, 73.0731)),
        LocationSuggestion("local_i_14", "DHA Phase 2", "Islamabad", LatLng(33.5306, 73.1107)),
        LocationSuggestion("local_i_15", "DHA Phase 2 Sector E", "Islamabad", LatLng(33.5267, 73.1220)),
        LocationSuggestion("local_i_16", "Bahria Enclave", "Islamabad", LatLng(33.5715, 73.1856)),
        LocationSuggestion("local_i_17", "Centaurus Mall", "Islamabad", LatLng(33.7077, 73.0498)),
        LocationSuggestion("local_i_18", "Faisal Masjid", "Islamabad", LatLng(33.7295, 73.0372)),
        LocationSuggestion("local_r_1", "Saddar", "Rawalpindi", LatLng(33.5973, 73.0479)),
        LocationSuggestion("local_r_2", "Committee Chowk", "Rawalpindi", LatLng(33.6151, 73.0715)),
        LocationSuggestion("local_r_3", "Commercial Market", "Rawalpindi", LatLng(33.6265, 73.0711)),
        LocationSuggestion("local_r_4", "Bahria Town Phase 7", "Rawalpindi", LatLng(33.5339, 73.1607)),
        LocationSuggestion("local_r_5", "Bahria Town Phase 8", "Rawalpindi", LatLng(33.5268, 73.1702)),
        LocationSuggestion("local_r_6", "DHA Phase 1", "Rawalpindi", LatLng(33.5489, 73.1140)),
        LocationSuggestion("local_l_1", "Gulberg", "Lahore", LatLng(31.5204, 74.3587)),
        LocationSuggestion("local_l_2", "Johar Town", "Lahore", LatLng(31.4697, 74.2728)),
        LocationSuggestion("local_l_3", "Emporium Mall", "Lahore", LatLng(31.4674, 74.2654)),
        LocationSuggestion("local_l_4", "DHA Phase 5", "Lahore", LatLng(31.4479, 74.4443)),
        LocationSuggestion("local_l_5", "DHA Phase 6", "Lahore", LatLng(31.4404, 74.4586))
    )

    fun onPickupTextChanged(text: String) {
        _uiState.update {
            it.copy(
                pickupText = text,
                selectedPickupPlaceId = "",
                pickupLatLng = null,
                showSearchMarkers = false,
                routePoints = emptyList(),
                searchMessage = null
            )
        }

        if (text.length < 2) {
            _uiState.update { state -> state.copy(pickupSuggestions = emptyList()) }
            return
        }

        fetchPredictions(query = text, isPickup = true)
    }

    fun onDropoffTextChanged(text: String) {
        _uiState.update {
            it.copy(
                dropoffText = text,
                selectedDropoffPlaceId = "",
                dropoffLatLng = null,
                showSearchMarkers = false,
                routePoints = emptyList(),
                searchMessage = null
            )
        }

        if (text.length < 2) {
            _uiState.update { state -> state.copy(dropoffSuggestions = emptyList()) }
            return
        }

        fetchPredictions(query = text, isPickup = false)
    }

    fun onPickupSuggestionSelected(suggestion: LocationSuggestion) {
        if (suggestion.placeId.startsWith("local_")) {
            _uiState.update {
                it.copy(
                    pickupText = buildFullText(suggestion),
                    selectedPickupPlaceId = suggestion.placeId,
                    pickupSuggestions = emptyList(),
                    pickupLatLng = suggestion.latLng,
                    showSearchMarkers = false,
                    routePoints = emptyList(),
                    searchMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    pickupText = buildFullText(suggestion),
                    selectedPickupPlaceId = suggestion.placeId,
                    pickupSuggestions = emptyList(),
                    pickupLatLng = null,
                    showSearchMarkers = false,
                    routePoints = emptyList(),
                    searchMessage = null
                )
            }
            fetchPlaceLatLng(suggestion.placeId, true)
        }
    }

    fun onDropoffSuggestionSelected(suggestion: LocationSuggestion) {
        if (suggestion.placeId.startsWith("local_")) {
            _uiState.update {
                it.copy(
                    dropoffText = buildFullText(suggestion),
                    selectedDropoffPlaceId = suggestion.placeId,
                    dropoffSuggestions = emptyList(),
                    dropoffLatLng = suggestion.latLng,
                    showSearchMarkers = false,
                    routePoints = emptyList(),
                    searchMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    dropoffText = buildFullText(suggestion),
                    selectedDropoffPlaceId = suggestion.placeId,
                    dropoffSuggestions = emptyList(),
                    dropoffLatLng = null,
                    showSearchMarkers = false,
                    routePoints = emptyList(),
                    searchMessage = null
                )
            }
            fetchPlaceLatLng(suggestion.placeId, false)
        }
    }

    fun onSearchClicked() {
        val state = _uiState.value
        val pickup = state.pickupLatLng
        val dropoff = state.dropoffLatLng

        if (state.pickupText.isBlank() || state.dropoffText.isBlank()) {
            _uiState.update { it.copy(searchMessage = "Please select both Pickup and Dropoff") }
            return
        }

        if (pickup == null || dropoff == null) {
            _uiState.update { it.copy(searchMessage = "Please select locations from suggestions first") }
            return
        }

        _uiState.update {
            it.copy(
                pickupSuggestions = emptyList(),
                dropoffSuggestions = emptyList(),
                showSearchMarkers = true,
                routePoints = emptyList(),
                isRouteLoading = true,
                searchMessage = "Finding route..."
            )
        }

        fetchRoute(pickup, dropoff)
    }

    fun consumeSearchMessage() {
        _uiState.update { it.copy(searchMessage = null) }
    }

    private fun fetchPredictions(query: String, isPickup: Boolean) {
        val localMatched = filterLocalSuggestions(query)
        val client = placesClient

        if (client == null) {
            updateSuggestions(localMatched, isPickup, query)
            return
        }

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(AutocompleteSessionToken.newInstance())
            .setQuery(query)
            .setCountries(listOf("PK"))
            .setLocationRestriction(RectangularBounds.newInstance(serviceAreaBounds))
            .build()

        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val googleResults = response.autocompletePredictions.map { prediction ->
                    LocationSuggestion(
                        placeId = prediction.placeId,
                        title = prediction.getPrimaryText(null).toString(),
                        subtitle = prediction.getSecondaryText(null)?.toString().orEmpty()
                    )
                }

                val merged = mergeSuggestions(googleResults, localMatched)
                updateSuggestions(merged, isPickup, query)
            }
            .addOnFailureListener { e ->
                Log.e("RideitPlaces", "Autocomplete failed: ${e.message}")
                updateSuggestions(localMatched, isPickup, query)
            }
    }

    private fun fetchPlaceLatLng(placeId: String, isPickup: Boolean) {
        val client = placesClient ?: return

        val request = FetchPlaceRequest.builder(
            placeId,
            listOf(Place.Field.LAT_LNG)
        ).build()

        client.fetchPlace(request)
            .addOnSuccessListener { response ->
                val latLng = response.place.latLng
                if (latLng != null) {
                    if (isPickup) {
                        _uiState.update { it.copy(pickupLatLng = latLng) }
                    } else {
                        _uiState.update { it.copy(dropoffLatLng = latLng) }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RideitPlaces", "LatLng fetch failed: ${e.message}")
            }
    }

    private fun fetchRoute(origin: LatLng, destination: LatLng) {
        val routesKey = getRoutesApiKeyFromManifest()
        if (routesKey.isBlank()) {
            _uiState.update {
                it.copy(
                    isRouteLoading = false,
                    searchMessage = "Routes API key missing"
                )
            }
            return
        }

        val url = "https://routes.googleapis.com/directions/v2:computeRoutes"

        val bodyJson = JSONObject().apply {
            put("origin", JSONObject().apply {
                put("location", JSONObject().apply {
                    put("latLng", JSONObject().apply {
                        put("latitude", origin.latitude)
                        put("longitude", origin.longitude)
                    })
                })
            })
            put("destination", JSONObject().apply {
                put("location", JSONObject().apply {
                    put("latLng", JSONObject().apply {
                        put("latitude", destination.latitude)
                        put("longitude", destination.longitude)
                    })
                })
            })
            put("travelMode", "DRIVE")
            put("routingPreference", "TRAFFIC_AWARE")
            put("polylineQuality", "OVERVIEW")
            put("polylineEncoding", "ENCODED_POLYLINE")
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Goog-Api-Key", routesKey)
            .addHeader(
                "X-Goog-FieldMask",
                "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"
            )
            .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                _uiState.update {
                    it.copy(
                        isRouteLoading = false,
                        searchMessage = "Route request failed"
                    )
                }
                Log.e("RideitRoutes", "HTTP failure: ${e.message}")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val bodyString = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isRouteLoading = false,
                            searchMessage = "Route API error ${response.code}"
                        )
                    }
                    Log.e("RideitRoutes", "API error ${response.code}: $bodyString")
                    return
                }

                try {
                    val root = JSONObject(bodyString)
                    val routesArray: JSONArray = root.optJSONArray("routes") ?: JSONArray()

                    if (routesArray.length() == 0) {
                        _uiState.update {
                            it.copy(
                                isRouteLoading = false,
                                searchMessage = "No route found"
                            )
                        }
                        return
                    }

                    val firstRoute = routesArray.getJSONObject(0)
                    val polylineObj = firstRoute.optJSONObject("polyline")
                    val encodedPolyline = polylineObj?.optString("encodedPolyline").orEmpty()

                    if (encodedPolyline.isBlank()) {
                        _uiState.update {
                            it.copy(
                                isRouteLoading = false,
                                searchMessage = "Route polyline missing"
                            )
                        }
                        return
                    }

                    val decodedPoints = decodePolyline(encodedPolyline)

                    _uiState.update {
                        it.copy(
                            routePoints = decodedPoints,
                            isRouteLoading = false,
                            showSearchMarkers = true,
                            searchRequestId = System.currentTimeMillis(),
                            searchMessage = "Route found"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isRouteLoading = false,
                            searchMessage = "Route parse error"
                        )
                    }
                    Log.e("RideitRoutes", "Parse error: ${e.message}")
                }
            }
        })
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val polyline = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var shift = 0
            var result = 0
            var b: Int

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)

            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            polyline.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return polyline
    }

    private fun filterLocalSuggestions(query: String): List<LocationSuggestion> {
        val normalizedQuery = query.trim().lowercase()

        val scored = localSuggestions.mapNotNull { item ->
            val title = item.title.lowercase()
            val subtitle = item.subtitle.lowercase()
            val full = "$title $subtitle"

            val score = when {
                title == normalizedQuery -> 140
                title.startsWith(normalizedQuery) -> 120
                full.startsWith(normalizedQuery) -> 110
                title.contains(normalizedQuery) -> 95
                subtitle.contains(normalizedQuery) -> 80
                full.contains(normalizedQuery) -> 70
                else -> -1
            }

            if (score >= 0) Pair(item, score) else null
        }

        return scored.sortedByDescending { it.second }.map { it.first }.take(15)
    }

    private fun mergeSuggestions(
        googleResults: List<LocationSuggestion>,
        localResults: List<LocationSuggestion>
    ): List<LocationSuggestion> {
        val merged = mutableListOf<LocationSuggestion>()
        val seen = mutableSetOf<String>()

        localResults.forEach { item ->
            val key = "${item.title.trim().lowercase()}|${item.subtitle.trim().lowercase()}"
            if (seen.add(key)) merged.add(item)
        }

        googleResults.forEach { item ->
            val key = "${item.title.trim().lowercase()}|${item.subtitle.trim().lowercase()}"
            if (seen.add(key)) merged.add(item)
        }

        return merged.take(15)
    }

    private fun updateSuggestions(
        suggestions: List<LocationSuggestion>,
        isPickup: Boolean,
        query: String
    ) {
        if (isPickup) {
            if (_uiState.value.pickupText == query) {
                _uiState.update { it.copy(pickupSuggestions = suggestions) }
            }
        } else {
            if (_uiState.value.dropoffText == query) {
                _uiState.update { it.copy(dropoffSuggestions = suggestions) }
            }
        }
    }

    private fun buildFullText(suggestion: LocationSuggestion): String {
        return if (suggestion.subtitle.isNotBlank()) {
            "${suggestion.title}, ${suggestion.subtitle}"
        } else {
            suggestion.title
        }
    }

    private fun getMapsApiKeyFromManifest(): String {
        return try {
            val context = getApplication<Application>().applicationContext
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
        } catch (e: Exception) {
            Log.e("RideitPlaces", "Maps key read failed: ${e.message}")
            ""
        }
    }

    private fun getRoutesApiKeyFromManifest(): String {
        return try {
            val context = getApplication<Application>().applicationContext
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.example.rideit.ROUTES_API_KEY").orEmpty()
        } catch (e: Exception) {
            Log.e("RideitRoutes", "Routes key read failed: ${e.message}")
            ""
        }
    }
}