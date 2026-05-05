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
import kotlin.math.sin

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
        LocationSuggestion("Faisal Mosque", "Faisal Mosque, Islamabad, Pakistan", 33.7294, 73.0379),
        LocationSuggestion("Faizabad", "Faizabad Interchange, Islamabad, Pakistan", 33.6602, 73.0736),
        LocationSuggestion("I-8 Markaz", "I-8 Markaz, Islamabad, Pakistan", 33.6677, 73.0740),
        LocationSuggestion("I-9 Markaz", "I-9 Markaz, Islamabad, Pakistan", 33.6588, 73.0572),
        LocationSuggestion("I-10 Markaz", "I-10 Markaz, Islamabad, Pakistan", 33.6499, 73.0346),
        LocationSuggestion("DHA Phase 2", "DHA Phase 2, Islamabad, Pakistan", 33.5213, 73.1230),
        LocationSuggestion("Giga Mall", "Giga Mall, DHA Phase 2, Islamabad, Pakistan", 33.5206, 73.1207),
        LocationSuggestion("PWD Housing Society", "PWD Housing Society, Islamabad, Pakistan", 33.5621, 73.1389),
        LocationSuggestion("Saddar Rawalpindi", "Saddar, Rawalpindi, Pakistan", 33.5969, 73.0479),
        LocationSuggestion("Raja Bazaar", "Raja Bazaar, Rawalpindi, Pakistan", 33.6167, 73.0606),
        LocationSuggestion("Commercial Market", "Commercial Market, Rawalpindi, Pakistan", 33.6428, 73.0656),
        LocationSuggestion("6th Road", "6th Road, Rawalpindi, Pakistan", 33.6421, 73.0763),
        LocationSuggestion("Bahria Town Rawalpindi", "Bahria Town, Rawalpindi, Pakistan", 33.5349, 73.1152)
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

    private fun normalizeQuery(query: String): String {
        return query
            .replace("G10", "G-10", ignoreCase = true)
            .replace("G 10", "G-10", ignoreCase = true)
            .replace("F10", "F-10", ignoreCase = true)
            .replace("F 10", "F-10", ignoreCase = true)
            .replace("I8", "I-8", ignoreCase = true)
            .replace("I 8", "I-8", ignoreCase = true)
            .replace("I9", "I-9", ignoreCase = true)
            .replace("I 9", "I-9", ignoreCase = true)
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

                LocationSuggestion(
                    title = displayName.split(",").firstOrNull()?.trim().orEmpty(),
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
        val points = mutableListOf<LatLng>()

        val steps = 40
        val latDiff = destination.latitude - origin.latitude
        val lngDiff = destination.longitude - origin.longitude

        for (i in 0..steps) {
            val progress = i.toDouble() / steps.toDouble()

            val curveOffset = sin(progress * Math.PI) * 0.008

            val lat = origin.latitude + latDiff * progress + curveOffset
            val lng = origin.longitude + lngDiff * progress - curveOffset * 0.45

            points.add(LatLng(lat, lng))
        }

        return points
    }
}