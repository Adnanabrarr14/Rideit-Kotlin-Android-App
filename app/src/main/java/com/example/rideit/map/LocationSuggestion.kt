package com.example.rideit.map

import com.google.android.gms.maps.model.LatLng

data class LocationSuggestion(
    val placeId: String,
    val title: String,
    val subtitle: String = "",
    val latLng: LatLng? = null
)