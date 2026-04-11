package com.example.rideit.map.repository

import android.content.Context
import com.example.rideit.map.model.LocationSuggestion
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MapRepository(
    @Suppress("UNUSED_PARAMETER")
    private val context: Context
) {

    private val safePlaces = listOf(
        LocationSuggestion(
            title = "Bahria Town, Rawalpindi",
            fullAddress = "Bahria Town, Rawalpindi, Pakistan",
            latitude = 33.5349,
            longitude = 73.1152
        ),
        LocationSuggestion(
            title = "DHA Phase 1, Islamabad",
            fullAddress = "DHA Phase 1, Islamabad, Pakistan",
            latitude = 33.5527,
            longitude = 73.1570
        ),
        LocationSuggestion(
            title = "DHA Phase 2, Islamabad",
            fullAddress = "DHA Phase 2, Islamabad, Pakistan",
            latitude = 33.5213,
            longitude = 73.1230
        ),
        LocationSuggestion(
            title = "Giga Mall, Islamabad",
            fullAddress = "Giga Mall, DHA Phase 2, Islamabad, Pakistan",
            latitude = 33.5206,
            longitude = 73.1207
        ),
        LocationSuggestion(
            title = "Saddar, Rawalpindi",
            fullAddress = "Saddar, Rawalpindi, Pakistan",
            latitude = 33.5969,
            longitude = 73.0479
        ),
        LocationSuggestion(
            title = "Blue Area, Islamabad",
            fullAddress = "Blue Area, Islamabad, Pakistan",
            latitude = 33.7090,
            longitude = 73.0498
        ),
        LocationSuggestion(
            title = "F-10 Markaz, Islamabad",
            fullAddress = "F-10 Markaz, Islamabad, Pakistan",
            latitude = 33.6969,
            longitude = 73.0138
        ),
        LocationSuggestion(
            title = "G-11 Markaz, Islamabad",
            fullAddress = "G-11 Markaz, Islamabad, Pakistan",
            latitude = 33.6840,
            longitude = 72.9882
        ),
        LocationSuggestion(
            title = "Centaurus Mall, Islamabad",
            fullAddress = "Centaurus Mall, Islamabad, Pakistan",
            latitude = 33.7076,
            longitude = 73.0498
        ),
        LocationSuggestion(
            title = "Faizabad, Islamabad",
            fullAddress = "Faizabad Interchange, Islamabad, Pakistan",
            latitude = 33.6602,
            longitude = 73.0736
        ),
        LocationSuggestion(
            title = "Johar Town, Lahore",
            fullAddress = "Johar Town, Lahore, Pakistan",
            latitude = 31.4697,
            longitude = 74.2728
        ),
        LocationSuggestion(
            title = "DHA Phase 5, Lahore",
            fullAddress = "DHA Phase 5, Lahore, Pakistan",
            latitude = 31.4690,
            longitude = 74.3850
        ),
        LocationSuggestion(
            title = "Liberty Market, Lahore",
            fullAddress = "Liberty Market, Gulberg, Lahore, Pakistan",
            latitude = 31.5204,
            longitude = 74.3463
        ),
        LocationSuggestion(
            title = "Allama Iqbal Airport, Lahore",
            fullAddress = "Allama Iqbal International Airport, Lahore, Pakistan",
            latitude = 31.5216,
            longitude = 74.4036
        ),
        LocationSuggestion(
            title = "Packages Mall, Lahore",
            fullAddress = "Packages Mall, Lahore, Pakistan",
            latitude = 31.4698,
            longitude = 74.3572
        )
    )

    fun searchLocationSuggestions(query: String): List<LocationSuggestion> {
        val cleaned = query.trim()
        if (cleaned.length < 2) return emptyList()

        return safePlaces
            .filter {
                it.title.contains(cleaned, ignoreCase = true) ||
                        it.fullAddress.contains(cleaned, ignoreCase = true)
            }
            .take(6)
    }

    fun findPlace(query: String): LocationSuggestion? {
        val cleaned = query.trim()
        if (cleaned.isBlank()) return null

        val exact = safePlaces.firstOrNull {
            it.title.equals(cleaned, ignoreCase = true) ||
                    it.fullAddress.equals(cleaned, ignoreCase = true)
        }

        if (exact != null) return exact

        return safePlaces.firstOrNull {
            it.title.contains(cleaned, ignoreCase = true) ||
                    it.fullAddress.contains(cleaned, ignoreCase = true)
        }
    }

    fun getRoutePoints(origin: LatLng, destination: LatLng): List<LatLng> {
        return listOf(origin, destination)
    }

    fun calculateDistanceKm(start: LatLng, end: LatLng): Double {
        val earthRadiusKm = 6371.0

        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLng = Math.toRadians(end.longitude - start.longitude)
        val startLat = Math.toRadians(start.latitude)
        val endLat = Math.toRadians(end.latitude)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                sin(dLng / 2) * sin(dLng / 2) * cos(startLat) * cos(endLat)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}