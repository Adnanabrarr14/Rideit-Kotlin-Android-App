package com.example.rideit.map.model

enum class RideRequestStatus {
    IDLE,
    SEARCHING_DRIVER,
    DRIVER_FOUND,
    DRIVER_ARRIVING,
    RIDE_STARTED,
    CANCELLED
}