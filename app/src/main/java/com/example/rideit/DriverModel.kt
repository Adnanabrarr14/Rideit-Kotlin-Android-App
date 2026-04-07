package com.example.rideit

data class DriverModel(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val vehicleType: String = "",
    val role: String = "driver"
)