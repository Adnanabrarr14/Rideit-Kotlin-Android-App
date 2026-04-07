package com.example.rideit

data class RideRequest(
    val userId: String = "",
    val userEmail: String = "",
    val pickup: String = "",
    val drop: String = "",
    val rideType: String = "Mini",
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis(),

    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val driverId: String = "",
    val driverLat: Double = 0.0,
    val driverLng: Double = 0.0
)