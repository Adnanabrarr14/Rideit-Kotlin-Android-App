package com.example.rideit.map.repository

import com.example.rideit.map.model.LocationSuggestion
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class MapRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val verifiedPlaces = listOf(
        LocationSuggestion("G-10 Markaz", "G-10 Markaz, Islamabad, Pakistan", 33.6763, 73.0130),
        LocationSuggestion("G-11 Markaz", "G-11 Markaz, Islamabad, Pakistan", 33.6840, 72.9882),
        LocationSuggestion("F-10 Markaz", "F-10 Markaz, Islamabad, Pakistan", 33.6969, 73.0138),
        LocationSuggestion("F-11 Markaz", "F-11 Markaz, Islamabad, Pakistan", 33.6844, 72.9887),
        LocationSuggestion("Blue Area", "Blue Area, Islamabad, Pakistan", 33.7090, 73.0498),
        LocationSuggestion("Centaurus Mall", "Centaurus Mall, Islamabad, Pakistan", 33.7076, 73.0498),
        LocationSuggestion("Safa Gold Mall", "Safa Gold Mall, F-7 Markaz, Islamabad, Pakistan", 33.7205, 73.0562),
        LocationSuggestion("Giga Mall", "Giga Mall, DHA Phase 2, Islamabad, Pakistan", 33.5206, 73.1207),
        LocationSuggestion("Amazon Mall", "Amazon Mall, Islamabad Expressway, Islamabad, Pakistan", 33.5998, 73.1356),
        LocationSuggestion("Faisal Mosque", "Faisal Mosque, Islamabad, Pakistan", 33.7294, 73.0379),
        LocationSuggestion("Faizabad", "Faizabad Interchange, Islamabad, Pakistan", 33.6602, 73.0736),
        LocationSuggestion("I-8 Markaz", "I-8 Markaz, Islamabad, Pakistan", 33.6677, 73.0740),
        LocationSuggestion("I-9 Markaz", "I-9 Markaz, Islamabad, Pakistan", 33.6588, 73.0572),
        LocationSuggestion("I-10 Markaz", "I-10 Markaz, Islamabad, Pakistan", 33.6499, 73.0346),
        LocationSuggestion("DHA Phase 2", "DHA Phase 2, Islamabad, Pakistan", 33.5213, 73.1230),
        LocationSuggestion("PWD Housing Society", "PWD Housing Society, Islamabad, Pakistan", 33.5621, 73.1389),
        LocationSuggestion("Saddar Rawalpindi", "Saddar, Rawalpindi, Pakistan", 33.5969, 73.0479),
        LocationSuggestion("Raja Bazaar", "Raja Bazaar, Rawalpindi, Pakistan", 33.6167, 73.0606),
        LocationSuggestion("Commercial Market", "Commercial Market, Rawalpindi, Pakistan", 33.6428, 73.0656),
        LocationSuggestion("6th Road", "6th Road, Rawalpindi, Pakistan", 33.6421, 73.0763),
        LocationSuggestion("Bahria Town Rawalpindi", "Bahria Town, Rawalpindi, Pakistan", 33.5349, 73.1152),
        LocationSuggestion("Islamabad International Airport", "Islamabad International Airport, Islamabad, Pakistan", 33.5490, 72.8257),
        LocationSuggestion("Rawalpindi Railway Station", "Rawalpindi Railway Station, Rawalpindi, Pakistan", 33.6007, 73.0479)
    )

    private val mallPlaces = listOf(
        LocationSuggestion("Centaurus Mall", "Centaurus Mall, Islamabad, Pakistan", 33.7076, 73.0498),
        LocationSuggestion("Safa Gold Mall", "Safa Gold Mall, F-7 Markaz, Islamabad, Pakistan", 33.7205, 73.0562),
        LocationSuggestion("Giga Mall", "Giga Mall, DHA Phase 2, Islamabad, Pakistan", 33.5206, 73.1207),
        LocationSuggestion("Amazon Mall", "Amazon Mall, Islamabad Expressway, Islamabad, Pakistan", 33.5998, 73.1356)
    )

    private val airportPlaces = listOf(
        LocationSuggestion("Islamabad International Airport", "Islamabad International Airport, Islamabad, Pakistan", 33.5490, 72.8257)
    )

    private val workPlaces = listOf(
        LocationSuggestion("Blue Area", "Blue Area, Islamabad, Pakistan", 33.7090, 73.0498),
        LocationSuggestion("I-8 Markaz", "I-8 Markaz, Islamabad, Pakistan", 33.6677, 73.0740),
        LocationSuggestion("F-10 Markaz", "F-10 Markaz, Islamabad, Pakistan", 33.6969, 73.0138),
        LocationSuggestion("Saddar Rawalpindi", "Saddar, Rawalpindi, Pakistan", 33.5969, 73.0479)
    )

    private val restaurantPlaces = listOf(
        LocationSuggestion("Monal Restaurant", "Monal Restaurant, Pir Sohawa Road, Islamabad, Pakistan", 33.7487, 73.0678),
        LocationSuggestion("Howdy F-7", "Howdy, F-7 Markaz, Islamabad, Pakistan", 33.7204, 73.0565),
        LocationSuggestion("Tuscany Courtyard", "Tuscany Courtyard, Kohsar Market, Islamabad, Pakistan", 33.7296, 73.0792),
        LocationSuggestion("Chaaye Khana F-6", "Chaaye Khana, F-6 Markaz, Islamabad, Pakistan", 33.7298, 73.0757),
        LocationSuggestion("KFC F-10", "KFC, F-10 Markaz, Islamabad, Pakistan", 33.6968, 73.0140),
        LocationSuggestion("McDonald's Centaurus", "McDonald's, Centaurus Mall, Islamabad, Pakistan", 33.7076, 73.0498),
        LocationSuggestion("Savour Foods Blue Area", "Savour Foods, Blue Area, Islamabad, Pakistan", 33.7097, 73.0522),
        LocationSuggestion("Ranchers I-8", "Ranchers, I-8 Markaz, Islamabad, Pakistan", 33.6677, 73.0740)
    )

    suspend fun searchLocationSuggestions(query: String): List<LocationSuggestion> {
        val cleanedQuery = normalizeQuery(query.trim())
        if (cleanedQuery.length < 2) return emptyList()

        val localResults = verifiedPlaces
            .filter {
                it.title.contains(cleanedQuery, ignoreCase = true) ||
                        it.fullAddress.contains(cleanedQuery, ignoreCase = true)
            }
            .take(8)

        if (localResults.isNotEmpty()) return localResults

        return withContext(Dispatchers.IO) {
            searchNominatim("$cleanedQuery Islamabad Rawalpindi Pakistan")
                .take(8)
        }
    }

    suspend fun searchQuickPlaceSuggestions(
        quickPlaceType: String,
        currentLocation: LatLng?
    ): List<LocationSuggestion> {
        val normalizedType = quickPlaceType.lowercase().trim()

        return when (normalizedType) {
            "home" -> {
                currentLocation?.let {
                    listOf(
                        LocationSuggestion(
                            title = "Home",
                            fullAddress = "Detected from your current device GPS location",
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    )
                } ?: emptyList()
            }

            "work" -> workPlaces

            "mall" -> {
                val remoteResults = withContext(Dispatchers.IO) {
                    val searchText = if (currentLocation != null) {
                        "shopping mall near ${currentLocation.latitude}, ${currentLocation.longitude}"
                    } else {
                        "shopping mall Islamabad Rawalpindi Pakistan"
                    }

                    searchNominatim(searchText)
                }

                (mallPlaces + remoteResults)
                    .filter {
                        it.title.contains("mall", ignoreCase = true) ||
                                it.fullAddress.contains("mall", ignoreCase = true) ||
                                it.fullAddress.contains("shopping", ignoreCase = true)
                    }
                    .distinctBy { it.title.lowercase() }
                    .take(8)
            }

            "airport" -> {
                val remoteResults = withContext(Dispatchers.IO) {
                    val searchText = if (currentLocation != null) {
                        "airport near ${currentLocation.latitude}, ${currentLocation.longitude}"
                    } else {
                        "airport Islamabad Rawalpindi Pakistan"
                    }

                    searchNominatim(searchText)
                }

                (airportPlaces + remoteResults)
                    .filter {
                        val text = "${it.title} ${it.fullAddress}".lowercase()
                        text.contains("airport") &&
                                !text.contains("restaurant") &&
                                !text.contains("hotel") &&
                                !text.contains("cafe") &&
                                !text.contains("food")
                    }
                    .distinctBy { it.title.lowercase() }
                    .take(8)
            }

            "restaurant" -> {
                val remoteResults = withContext(Dispatchers.IO) {
                    val searchText = if (currentLocation != null) {
                        "restaurant near ${currentLocation.latitude}, ${currentLocation.longitude}"
                    } else {
                        "restaurant Islamabad Rawalpindi Pakistan"
                    }

                    searchNominatim(searchText)
                }

                (restaurantPlaces + remoteResults)
                    .filter {
                        val text = "${it.title} ${it.fullAddress}".lowercase()
                        text.contains("restaurant") ||
                                text.contains("food") ||
                                text.contains("cafe") ||
                                text.contains("kfc") ||
                                text.contains("mcdonald") ||
                                text.contains("hotel") ||
                                text.contains("kitchen")
                    }
                    .distinctBy { it.title.lowercase() }
                    .take(8)
            }

            else -> emptyList()
        }
    }

    private fun normalizeQuery(query: String): String {
        return query
            .replace("G10", "G-10", ignoreCase = true)
            .replace("G 10", "G-10", ignoreCase = true)
            .replace("G11", "G-11", ignoreCase = true)
            .replace("G 11", "G-11", ignoreCase = true)
            .replace("F10", "F-10", ignoreCase = true)
            .replace("F 10", "F-10", ignoreCase = true)
            .replace("F11", "F-11", ignoreCase = true)
            .replace("F 11", "F-11", ignoreCase = true)
            .replace("I8", "I-8", ignoreCase = true)
            .replace("I 8", "I-8", ignoreCase = true)
            .replace("I9", "I-9", ignoreCase = true)
            .replace("I 9", "I-9", ignoreCase = true)
            .replace("I10", "I-10", ignoreCase = true)
            .replace("I 10", "I-10", ignoreCase = true)
            .replace("DHA2", "DHA Phase 2", ignoreCase = true)
            .replace("DHA 2", "DHA Phase 2", ignoreCase = true)
            .replace("pwd", "PWD Housing Society", ignoreCase = true)
    }

    private fun searchNominatim(searchText: String): List<LocationSuggestion> {
        return try {
            val encodedQuery = URLEncoder.encode(searchText, "UTF-8")

            val url =
                "https://nominatim.openstreetmap.org/search" +
                        "?q=$encodedQuery" +
                        "&format=json" +
                        "&addressdetails=1" +
                        "&limit=8" +
                        "&countrycodes=pk" +
                        "&accept-language=en"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Rideit-Android-App/1.0")
                .header("Accept-Language", "en")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful || body.isBlank()) return emptyList()

            val jsonArray = JSONArray(body)

            List(jsonArray.length()) { index ->
                val item = jsonArray.getJSONObject(index)
                val displayName = item.optString("display_name")
                val lat = item.optDouble("lat")
                val lon = item.optDouble("lon")
                val title = displayName.split(",").firstOrNull()?.trim().orEmpty()

                LocationSuggestion(
                    title = title,
                    fullAddress = displayName,
                    latitude = lat,
                    longitude = lon
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getRoutePoints(origin: LatLng, destination: LatLng): List<LatLng> {
        val latDiff = destination.latitude - origin.latitude
        val lngDiff = destination.longitude - origin.longitude

        if (abs(latDiff) < 0.00001 && abs(lngDiff) < 0.00001) {
            return listOf(origin, destination)
        }

        return if (abs(lngDiff) >= abs(latDiff)) {
            listOf(
                origin,
                LatLng(origin.latitude, origin.longitude + lngDiff * 0.25),
                LatLng(origin.latitude + latDiff * 0.28, origin.longitude + lngDiff * 0.45),
                LatLng(origin.latitude + latDiff * 0.62, origin.longitude + lngDiff * 0.72),
                LatLng(destination.latitude, origin.longitude + lngDiff * 0.88),
                destination
            )
        } else {
            listOf(
                origin,
                LatLng(origin.latitude + latDiff * 0.22, origin.longitude),
                LatLng(origin.latitude + latDiff * 0.42, origin.longitude + lngDiff * 0.26),
                LatLng(origin.latitude + latDiff * 0.70, origin.longitude + lngDiff * 0.58),
                LatLng(origin.latitude + latDiff * 0.88, destination.longitude),
                destination
            )
        }
    }
}