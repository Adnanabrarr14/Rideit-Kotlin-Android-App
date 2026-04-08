package com.example.rideit.map

data class LocationSuggestion(
    val placeId: String,
    val title: String,
    val subtitle: String = ""
)