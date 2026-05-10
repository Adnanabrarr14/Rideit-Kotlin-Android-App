package com.example.rideit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun RideitRiderActiveTripConnector(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    rideStatus: String?,
    driverName: String? = null,
    vehicleName: String? = null,
    vehicleNumber: String? = null,
    pickupText: String? = null,
    destinationText: String? = null,
    etaText: String? = null,
    fareText: String? = null,
    showCancelButton: Boolean = true,
    onCancelRideClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(240)) +
                expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) +
                shrinkVertically(animationSpec = tween(200))
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            RideitRiderActiveTripTimelinePanel(
                firebaseStatus = rideStatus,
                driverName = driverName,
                vehicleName = vehicleName,
                vehicleNumber = vehicleNumber,
                pickupText = pickupText,
                destinationText = destinationText,
                etaText = etaText,
                fareText = fareText,
                showCancelButton = showCancelButton,
                onCancelRideClick = onCancelRideClick
            )
        }
    }
}

@Composable
fun RideitRiderActiveTripConnectorFromMap(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    activeRide: Map<String, Any?>?,
    showCancelButton: Boolean = true,
    onCancelRideClick: () -> Unit = {}
) {
    val status = remember(activeRide) {
        activeRide.getRideitString(
            "status",
            "rideStatus",
            "tripStatus"
        )
    }

    val driverName = remember(activeRide) {
        activeRide.getRideitString(
            "driverName",
            "driver_name",
            "driverFullName",
            "driver_full_name"
        )
    }

    val vehicleName = remember(activeRide) {
        activeRide.getRideitString(
            "vehicleName",
            "vehicle_name",
            "carName",
            "car_name",
            "carModel",
            "car_model",
            "vehicleModel",
            "vehicle_model"
        )
    }

    val vehicleNumber = remember(activeRide) {
        activeRide.getRideitString(
            "vehicleNumber",
            "vehicle_number",
            "carNumber",
            "car_number",
            "plateNumber",
            "plate_number",
            "licensePlate",
            "license_plate"
        )
    }

    val pickupText = remember(activeRide) {
        activeRide.getRideitString(
            "pickupText",
            "pickup_text",
            "pickup",
            "pickupAddress",
            "pickup_address",
            "from",
            "fromAddress",
            "from_address"
        )
    }

    val destinationText = remember(activeRide) {
        activeRide.getRideitString(
            "destinationText",
            "destination_text",
            "destination",
            "destinationAddress",
            "destination_address",
            "dropoff",
            "dropOff",
            "dropoffAddress",
            "drop_off_address",
            "to",
            "toAddress",
            "to_address"
        )
    }

    val etaText = remember(activeRide) {
        activeRide.getRideitString(
            "etaText",
            "eta_text",
            "eta",
            "estimatedTime",
            "estimated_time",
            "driverEta",
            "driver_eta"
        )
    }

    val fareText = remember(activeRide) {
        activeRide.getRideitFareText()
    }

    RideitRiderActiveTripConnector(
        modifier = modifier,
        isVisible = isVisible && !status.isNullOrBlank(),
        rideStatus = status,
        driverName = driverName,
        vehicleName = vehicleName,
        vehicleNumber = vehicleNumber,
        pickupText = pickupText,
        destinationText = destinationText,
        etaText = etaText,
        fareText = fareText,
        showCancelButton = showCancelButton,
        onCancelRideClick = onCancelRideClick
    )
}

private fun Map<String, Any?>?.getRideitString(
    vararg keys: String
): String? {
    if (this == null) return null

    keys.forEach { key ->
        val directValue = this[key]
        val cleanDirectValue = directValue.toCleanRideitString()
        if (!cleanDirectValue.isNullOrBlank()) return cleanDirectValue
    }

    return null
}

private fun Map<String, Any?>?.getRideitFareText(): String? {
    if (this == null) return null

    val textFare = getRideitString(
        "fareText",
        "fare_text",
        "priceText",
        "price_text",
        "totalFareText",
        "total_fare_text"
    )

    if (!textFare.isNullOrBlank()) return textFare

    val numericFare = getRideitString(
        "fare",
        "price",
        "totalFare",
        "total_fare",
        "estimatedFare",
        "estimated_fare"
    )

    return numericFare?.let { fare ->
        when {
            fare.startsWith("Rs", ignoreCase = true) -> fare
            fare.startsWith("PKR", ignoreCase = true) -> fare
            fare.startsWith("$") -> fare
            else -> "Rs $fare"
        }
    }
}

private fun Any?.toCleanRideitString(): String? {
    return when (this) {
        null -> null
        is String -> this.trim().takeIf { it.isNotBlank() }
        is Int -> this.toString()
        is Long -> this.toString()
        is Float -> {
            if (this % 1f == 0f) {
                this.toInt().toString()
            } else {
                this.toString()
            }
        }
        is Double -> {
            if (this % 1.0 == 0.0) {
                this.toInt().toString()
            } else {
                this.toString()
            }
        }
        is Boolean -> this.toString()
        else -> this.toString().trim().takeIf { it.isNotBlank() }
    }
}