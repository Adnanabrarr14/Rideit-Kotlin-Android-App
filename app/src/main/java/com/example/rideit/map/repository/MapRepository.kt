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

class MapRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val verifiedPlaces = listOf(
        LocationSuggestion("G-10 Markaz", "G-10 Markaz, Islamabad, Pakistan", 33.6763, 73.0130),
        LocationSuggestion("G-11 Markaz", "G-11 Markaz, Islamabad, Pakistan", 33.6840, 72.9882),
        LocationSuggestion("G-9 Markaz", "G-9 Markaz, Islamabad, Pakistan", 33.6906, 73.0334),
        LocationSuggestion("F-10 Markaz", "F-10 Markaz, Islamabad, Pakistan", 33.6969, 73.0138),
        LocationSuggestion("F-11 Markaz", "F-11 Markaz, Islamabad, Pakistan", 33.6844, 72.9887),
        LocationSuggestion("F-8 Markaz", "F-8 Markaz, Islamabad, Pakistan", 33.7104, 73.0396),
        LocationSuggestion("F-7 Markaz", "F-7 Markaz, Islamabad, Pakistan", 33.7215, 73.0563),
        LocationSuggestion("Blue Area", "Blue Area, Islamabad, Pakistan", 33.7090, 73.0498),
        LocationSuggestion("Centaurus Mall", "Centaurus Mall, Islamabad, Pakistan", 33.7076, 73.0498),
        LocationSuggestion("Safa Gold Mall", "Safa Gold Mall, F-7 Markaz, Islamabad, Pakistan", 33.7224, 73.0557),
        LocationSuggestion("Jinnah Super Market", "Jinnah Super Market, F-7, Islamabad, Pakistan", 33.7208, 73.0566),
        LocationSuggestion("Super Market", "Super Market, F-6, Islamabad, Pakistan", 33.7304, 73.0760),
        LocationSuggestion("Faisal Mosque", "Faisal Mosque, Islamabad, Pakistan", 33.7294, 73.0379),
        LocationSuggestion("Daman-e-Koh", "Daman-e-Koh, Islamabad, Pakistan", 33.7380, 73.0551),
        LocationSuggestion("Pakistan Monument", "Pakistan Monument, Islamabad, Pakistan", 33.6931, 73.0689),
        LocationSuggestion("Lok Virsa Museum", "Lok Virsa Museum, Islamabad, Pakistan", 33.6886, 73.0723),
        LocationSuggestion("Lake View Park", "Lake View Park, Islamabad, Pakistan", 33.7115, 73.1328),
        LocationSuggestion("Rawal Lake", "Rawal Lake, Islamabad, Pakistan", 33.7008, 73.1202),
        LocationSuggestion("Bahria University", "Bahria University, E-8, Islamabad, Pakistan", 33.7156, 73.0256),
        LocationSuggestion("NUST", "National University of Sciences and Technology, H-12, Islamabad, Pakistan", 33.6416, 72.9899),
        LocationSuggestion("COMSATS University Islamabad", "COMSATS University, Islamabad, Pakistan", 33.6518, 73.1566),
        LocationSuggestion("Quaid-i-Azam University", "Quaid-i-Azam University, Islamabad, Pakistan", 33.7475, 73.1389),
        LocationSuggestion("Islamabad International Airport", "Islamabad International Airport, Islamabad, Pakistan", 33.5607, 72.8516),
        LocationSuggestion("Faizabad", "Faizabad Interchange, Islamabad, Pakistan", 33.6602, 73.0736),
        LocationSuggestion("I-8 Markaz", "I-8 Markaz, Islamabad, Pakistan", 33.6677, 73.0740),
        LocationSuggestion("I-9 Markaz", "I-9 Markaz, Islamabad, Pakistan", 33.6588, 73.0572),
        LocationSuggestion("I-10 Markaz", "I-10 Markaz, Islamabad, Pakistan", 33.6499, 73.0346),
        LocationSuggestion("H-13", "H-13, Islamabad, Pakistan", 33.6185, 72.9636),
        LocationSuggestion("E-11", "E-11, Islamabad, Pakistan", 33.6992, 72.9783),
        LocationSuggestion("DHA Phase 2", "DHA Phase 2, Islamabad, Pakistan", 33.5213, 73.1230),
        LocationSuggestion("Giga Mall", "Giga Mall, DHA Phase 2, Islamabad, Pakistan", 33.5206, 73.1207),
        LocationSuggestion("PWD Housing Society", "PWD Housing Society, Islamabad, Pakistan", 33.5621, 73.1389),
        LocationSuggestion("Pakistan Town", "Pakistan Town, Islamabad, Pakistan", 33.5565, 73.1297),
        LocationSuggestion("Saddar Rawalpindi", "Saddar, Rawalpindi, Pakistan", 33.5969, 73.0479),
        LocationSuggestion("Raja Bazaar", "Raja Bazaar, Rawalpindi, Pakistan", 33.6167, 73.0606),
        LocationSuggestion("Committee Chowk", "Committee Chowk, Rawalpindi, Pakistan", 33.6124, 73.0637),
        LocationSuggestion("Commercial Market", "Commercial Market, Satellite Town, Rawalpindi, Pakistan", 33.6428, 73.0656),
        LocationSuggestion("6th Road", "6th Road, Rawalpindi, Pakistan", 33.6421, 73.0763),
        LocationSuggestion("Murree Road", "Murree Road, Rawalpindi, Pakistan", 33.6186, 73.0714),
        LocationSuggestion("Chandni Chowk", "Chandni Chowk, Rawalpindi, Pakistan", 33.6282, 73.0718),
        LocationSuggestion("Rehmanabad", "Rehmanabad, Rawalpindi, Pakistan", 33.6351, 73.0741),
        LocationSuggestion("Shamsabad", "Shamsabad, Rawalpindi, Pakistan", 33.6256, 73.0787),
        LocationSuggestion("Moti Mahal", "Moti Mahal, Rawalpindi, Pakistan", 33.6051, 73.0678),
        LocationSuggestion("Ayub Park", "Ayub National Park, Rawalpindi, Pakistan", 33.5635, 73.0821),
        LocationSuggestion("Bahria Town Rawalpindi", "Bahria Town, Rawalpindi, Pakistan", 33.5349, 73.1152),
        LocationSuggestion("Phase 7 Bahria Town", "Phase 7, Bahria Town, Rawalpindi, Pakistan", 33.5227, 73.1017),
        LocationSuggestion("Scheme 3", "Chaklala Scheme 3, Rawalpindi, Pakistan", 33.5867, 73.0852),
        LocationSuggestion("Lalkurti", "Lalkurti, Rawalpindi, Pakistan", 33.5903, 73.0644),
        LocationSuggestion("Peshawar Road", "Peshawar Road, Rawalpindi, Pakistan", 33.6222, 73.0184),
        LocationSuggestion("Westridge", "Westridge, Rawalpindi, Pakistan", 33.6188, 73.0242),
        LocationSuggestion("Adiala Road", "Adiala Road, Rawalpindi, Pakistan", 33.5488, 73.0176)
    )

    suspend fun searchLocationSuggestions(query: String): List<LocationSuggestion> {
        val cleanedQuery = normalizeQuery(query.trim())

        if (cleanedQuery.length < 2) return emptyList()

        val localResults = searchVerifiedPlaces(cleanedQuery)

        if (localResults.isNotEmpty()) {
            return localResults.take(8)
        }

        return withContext(Dispatchers.IO) {
            val attempts = buildSearchAttempts(cleanedQuery)
            val networkResults = mutableListOf<LocationSuggestion>()

            for (attempt in attempts) {
                networkResults.addAll(searchNominatim(attempt))
            }

            networkResults
                .distinctBy { it.fullAddress }
                .sortedByDescending { scoreResult(it, cleanedQuery) }
                .take(8)
        }
    }

    private fun searchVerifiedPlaces(query: String): List<LocationSuggestion> {
        val q = query.lowercase()

        return verifiedPlaces
            .map { place -> place to scoreResult(place, q) }
            .filter { (_, score) -> score > 0 }
            .sortedByDescending { (_, score) -> score }
            .map { it.first }
    }

    private fun normalizeQuery(query: String): String {
        return query
            .replace("G10", "G-10", ignoreCase = true)
            .replace("G 10", "G-10", ignoreCase = true)
            .replace("G11", "G-11", ignoreCase = true)
            .replace("G 11", "G-11", ignoreCase = true)
            .replace("G9", "G-9", ignoreCase = true)
            .replace("G 9", "G-9", ignoreCase = true)
            .replace("F10", "F-10", ignoreCase = true)
            .replace("F 10", "F-10", ignoreCase = true)
            .replace("F11", "F-11", ignoreCase = true)
            .replace("F 11", "F-11", ignoreCase = true)
            .replace("F8", "F-8", ignoreCase = true)
            .replace("F 8", "F-8", ignoreCase = true)
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

    private fun buildSearchAttempts(query: String): List<String> {
        return listOf(
            "$query Islamabad Pakistan",
            "$query Rawalpindi Pakistan",
            "$query Markaz Islamabad Pakistan",
            "$query Saddar Rawalpindi Pakistan",
            "$query Pakistan",
            query
        )
    }

    private fun searchNominatim(searchText: String): List<LocationSuggestion> {
        return try {
            val encodedQuery = URLEncoder.encode(searchText, "UTF-8")

            val url =
                "https://nominatim.openstreetmap.org/search" +
                        "?q=$encodedQuery" +
                        "&format=json" +
                        "&addressdetails=1" +
                        "&limit=10" +
                        "&countrycodes=pk" +
                        "&accept-language=en"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Rideit-Android-App/1.0")
                .header("Accept-Language", "en")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful || body.isBlank()) {
                return emptyList()
            }

            val jsonArray = JSONArray(body)

            List(jsonArray.length()) { index ->
                val item = jsonArray.getJSONObject(index)

                val displayName = item.optString("display_name")
                val lat = item.optDouble("lat")
                val lon = item.optDouble("lon")

                val title = displayName
                    .split(",")
                    .firstOrNull()
                    ?.trim()
                    .orEmpty()

                LocationSuggestion(
                    title = title.ifBlank { "Selected location" },
                    fullAddress = displayName,
                    latitude = lat,
                    longitude = lon
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun scoreResult(
        suggestion: LocationSuggestion,
        query: String
    ): Int {
        val q = query.lowercase()
        val title = suggestion.title.lowercase()
        val address = suggestion.fullAddress.lowercase()

        var score = 0

        if (title == q) score += 120
        if (title.startsWith(q)) score += 100
        if (title.contains(q)) score += 80
        if (address.contains(q)) score += 60

        val words = q.split(" ").filter { it.isNotBlank() }
        words.forEach { word ->
            if (title.contains(word)) score += 20
            if (address.contains(word)) score += 10
        }

        if (address.contains("islamabad")) score += 30
        if (address.contains("rawalpindi")) score += 30
        if (address.contains("markaz")) score += 25
        if (address.contains("saddar")) score += 20
        if (address.contains("dha")) score += 20
        if (address.contains("bahria")) score += 20
        if (address.contains("blue area")) score += 20
        if (address.contains("pakistan")) score += 10

        return score
    }

    fun getRoutePoints(origin: LatLng, destination: LatLng): List<LatLng> {
        return listOf(origin, destination)
    }
}