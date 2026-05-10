package com.example.rideit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RideitDriverActiveRideConnector(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    rideStatus: String?,
    riderName: String? = null,
    pickupText: String? = null,
    destinationText: String? = null,
    etaText: String? = null,
    fareText: String? = null,
    distanceText: String? = null,
    showPrimaryButton: Boolean = true,
    primaryButtonText: String? = null,
    showSecondaryButton: Boolean = false,
    secondaryButtonText: String = "Cancel ride",
    onPrimaryButtonClick: () -> Unit = {},
    onSecondaryButtonClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = isVisible && !rideStatus.isNullOrBlank(),
        enter = fadeIn(animationSpec = tween(240)) +
                expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) +
                shrinkVertically(animationSpec = tween(200))
    ) {
        RideitDriverActiveRidePanel(
            modifier = modifier,
            firebaseStatus = rideStatus,
            riderName = riderName,
            pickupText = pickupText,
            destinationText = destinationText,
            etaText = etaText,
            fareText = fareText,
            distanceText = distanceText,
            showPrimaryButton = showPrimaryButton,
            primaryButtonText = primaryButtonText,
            showSecondaryButton = showSecondaryButton,
            secondaryButtonText = secondaryButtonText,
            onPrimaryButtonClick = onPrimaryButtonClick,
            onSecondaryButtonClick = onSecondaryButtonClick
        )
    }
}

@Composable
fun RideitDriverActiveRideConnectorFromMap(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    activeRide: Map<String, Any?>?,
    showPrimaryButton: Boolean = true,
    primaryButtonText: String? = null,
    showSecondaryButton: Boolean = false,
    secondaryButtonText: String = "Cancel ride",
    onPrimaryButtonClick: () -> Unit = {},
    onSecondaryButtonClick: () -> Unit = {}
) {
    val status = remember(activeRide) {
        activeRide.getRideitDriverString(
            "status",
            "rideStatus",
            "tripStatus"
        )
    }

    val riderName = remember(activeRide) {
        activeRide.getRideitDriverString(
            "riderName",
            "rider_name",
            "passengerName",
            "passenger_name",
            "customerName",
            "customer_name",
            "userName",
            "user_name"
        )
    }

    val pickupText = remember(activeRide) {
        activeRide.getRideitDriverString(
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
        activeRide.getRideitDriverString(
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
        activeRide.getRideitDriverString(
            "etaText",
            "eta_text",
            "eta",
            "estimatedTime",
            "estimated_time",
            "pickupEta",
            "pickup_eta",
            "driverEta",
            "driver_eta"
        )
    }

    val distanceText = remember(activeRide) {
        activeRide.getRideitDriverString(
            "distanceText",
            "distance_text",
            "distance",
            "estimatedDistance",
            "estimated_distance",
            "tripDistance",
            "trip_distance"
        )
    }

    val fareText = remember(activeRide) {
        activeRide.getRideitDriverFareText()
    }

    RideitDriverActiveRideConnector(
        modifier = modifier,
        isVisible = isVisible,
        rideStatus = status,
        riderName = riderName,
        pickupText = pickupText,
        destinationText = destinationText,
        etaText = etaText,
        fareText = fareText,
        distanceText = distanceText,
        showPrimaryButton = showPrimaryButton,
        primaryButtonText = primaryButtonText,
        showSecondaryButton = showSecondaryButton,
        secondaryButtonText = secondaryButtonText,
        onPrimaryButtonClick = onPrimaryButtonClick,
        onSecondaryButtonClick = onSecondaryButtonClick
    )
}

@Composable
private fun RideitDriverActiveRidePanel(
    modifier: Modifier = Modifier,
    firebaseStatus: String?,
    riderName: String?,
    pickupText: String?,
    destinationText: String?,
    etaText: String?,
    fareText: String?,
    distanceText: String?,
    showPrimaryButton: Boolean,
    primaryButtonText: String?,
    showSecondaryButton: Boolean,
    secondaryButtonText: String,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit
) {
    val stage = remember(firebaseStatus) {
        firebaseStatus.toRideitTripStage()
    }

    val isCancelled = stage == RideitTripStage.CANCELLED
    val isCompleted = stage == RideitTripStage.COMPLETED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            RideitDriverRideHeader(
                stage = stage,
                riderName = riderName,
                etaText = etaText,
                fareText = fareText
            )

            Spacer(modifier = Modifier.height(16.dp))

            RideitTripTimeline(
                currentStage = stage,
                isDriverMode = true,
                compact = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            RideitDriverRideInfoBlock(
                pickupText = pickupText,
                destinationText = destinationText,
                distanceText = distanceText
            )

            AnimatedVisibility(
                visible = showPrimaryButton && !isCancelled && !isCompleted,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onPrimaryButtonClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = primaryButtonText ?: stage.defaultDriverPrimaryButtonText(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showSecondaryButton && !isCancelled && !isCompleted,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onSecondaryButtonClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFB91C1C)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = secondaryButtonText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RideitDriverRideHeader(
    stage: RideitTripStage,
    riderName: String?,
    etaText: String?,
    fareText: String?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "driver_active_ride_header_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driver_active_ride_header_pulse_value"
    )

    val title = when (stage) {
        RideitTripStage.SEARCHING -> "Waiting for ride"
        RideitTripStage.DRIVER_ASSIGNED -> "Ride accepted"
        RideitTripStage.ARRIVING -> "Go to pickup"
        RideitTripStage.IN_PROGRESS -> "Trip in progress"
        RideitTripStage.COMPLETED -> "Ride completed"
        RideitTripStage.CANCELLED -> "Ride cancelled"
    }

    val subtitle = when (stage) {
        RideitTripStage.SEARCHING -> "New requests will appear when available."
        RideitTripStage.DRIVER_ASSIGNED -> "Prepare to reach the rider safely."
        RideitTripStage.ARRIVING -> "Head to the pickup point now."
        RideitTripStage.IN_PROGRESS -> "Drive safely toward the destination."
        RideitTripStage.COMPLETED -> "Great job. This ride is complete."
        RideitTripStage.CANCELLED -> "This ride is no longer active."
    }

    val dotColor = when (stage) {
        RideitTripStage.SEARCHING -> Color(0xFF2563EB)
        RideitTripStage.DRIVER_ASSIGNED -> Color(0xFF7C3AED)
        RideitTripStage.ARRIVING -> Color(0xFFF59E0B)
        RideitTripStage.IN_PROGRESS -> Color(0xFF16A34A)
        RideitTripStage.COMPLETED -> Color(0xFF22C55E)
        RideitTripStage.CANCELLED -> Color(0xFFEF4444)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = dotColor.copy(alpha = 0.12f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        scaleX = if (stage == RideitTripStage.ARRIVING || stage == RideitTripStage.IN_PROGRESS) pulse else 1f
                        scaleY = if (stage == RideitTripStage.ARRIVING || stage == RideitTripStage.IN_PROGRESS) pulse else 1f
                    }
                    .background(
                        color = dotColor,
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 13.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = riderName?.takeIf { it.isNotBlank() }?.let { "$subtitle Rider: $it" } ?: subtitle,
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!etaText.isNullOrBlank() || !fareText.isNullOrBlank()) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (!etaText.isNullOrBlank()) {
                    Text(
                        text = etaText,
                        color = Color(0xFF0F172A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (!fareText.isNullOrBlank()) {
                    Text(
                        text = fareText,
                        color = Color(0xFF2563EB),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RideitDriverRideInfoBlock(
    pickupText: String?,
    destinationText: String?,
    distanceText: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(14.dp)
        ) {
            RideitDriverRouteLine(
                dotColor = Color(0xFF22C55E),
                label = "Pickup",
                value = pickupText?.takeIf { it.isNotBlank() } ?: "Pickup location"
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                color = Color(0xFFE2E8F0)
            )

            RideitDriverRouteLine(
                dotColor = Color(0xFFEF4444),
                label = "Destination",
                value = destinationText?.takeIf { it.isNotBlank() } ?: "Destination location"
            )

            if (!distanceText.isNullOrBlank()) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    color = Color(0xFFE2E8F0)
                )

                RideitDriverRouteLine(
                    dotColor = Color(0xFF2563EB),
                    label = "Distance",
                    value = distanceText
                )
            }
        }
    }
}

@Composable
private fun RideitDriverRouteLine(
    dotColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(10.dp)
                .background(dotColor, CircleShape)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun RideitTripStage.defaultDriverPrimaryButtonText(): String {
    return when (this) {
        RideitTripStage.SEARCHING -> "View request"
        RideitTripStage.DRIVER_ASSIGNED -> "Start pickup"
        RideitTripStage.ARRIVING -> "Arrived at pickup"
        RideitTripStage.IN_PROGRESS -> "Complete ride"
        RideitTripStage.COMPLETED -> "Completed"
        RideitTripStage.CANCELLED -> "Cancelled"
    }
}

private fun Map<String, Any?>?.getRideitDriverString(
    vararg keys: String
): String? {
    if (this == null) return null

    keys.forEach { key ->
        val directValue = this[key]
        val cleanDirectValue = directValue.toCleanRideitDriverString()
        if (!cleanDirectValue.isNullOrBlank()) return cleanDirectValue
    }

    return null
}

private fun Map<String, Any?>?.getRideitDriverFareText(): String? {
    if (this == null) return null

    val textFare = getRideitDriverString(
        "fareText",
        "fare_text",
        "priceText",
        "price_text",
        "totalFareText",
        "total_fare_text"
    )

    if (!textFare.isNullOrBlank()) return textFare

    val numericFare = getRideitDriverString(
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

private fun Any?.toCleanRideitDriverString(): String? {
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