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
fun RideitRiderActiveTripTimelinePanel(
    firebaseStatus: String?,
    modifier: Modifier = Modifier,
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
    val stage = remember(firebaseStatus) {
        firebaseStatus.toRideitTripStage()
    }

    val isCancelled = stage == RideitTripStage.CANCELLED
    val isCompleted = stage == RideitTripStage.COMPLETED
    val showDriverBlock = stage != RideitTripStage.SEARCHING && !isCancelled

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
            RideitRiderTripHeader(
                stage = stage,
                etaText = etaText,
                fareText = fareText
            )

            Spacer(modifier = Modifier.height(16.dp))

            RideitTripTimeline(
                currentStage = stage,
                isDriverMode = false,
                compact = true
            )

            AnimatedVisibility(
                visible = showDriverBlock,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    RideitDriverSummaryBlock(
                        driverName = driverName,
                        vehicleName = vehicleName,
                        vehicleNumber = vehicleNumber,
                        stage = stage
                    )
                }
            }

            AnimatedVisibility(
                visible = !pickupText.isNullOrBlank() || !destinationText.isNullOrBlank(),
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    RideitRouteSummaryBlock(
                        pickupText = pickupText,
                        destinationText = destinationText
                    )
                }
            }

            AnimatedVisibility(
                visible = showCancelButton && !isCancelled && !isCompleted,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(180))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onCancelRideClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFEE2E2),
                            contentColor = Color(0xFFB91C1C)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Cancel ride",
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
private fun RideitRiderTripHeader(
    stage: RideitTripStage,
    etaText: String?,
    fareText: String?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rider_active_trip_header_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rider_active_trip_header_pulse_value"
    )

    val title = when (stage) {
        RideitTripStage.SEARCHING -> "Finding your driver"
        RideitTripStage.DRIVER_ASSIGNED -> "Driver assigned"
        RideitTripStage.ARRIVING -> "Driver is arriving"
        RideitTripStage.IN_PROGRESS -> "Trip in progress"
        RideitTripStage.COMPLETED -> "Trip completed"
        RideitTripStage.CANCELLED -> "Ride cancelled"
    }

    val subtitle = when (stage) {
        RideitTripStage.SEARCHING -> "Rideit is matching you with a nearby driver."
        RideitTripStage.DRIVER_ASSIGNED -> "Your driver has accepted the ride."
        RideitTripStage.ARRIVING -> "Please stay near your pickup location."
        RideitTripStage.IN_PROGRESS -> "You are on your way to the destination."
        RideitTripStage.COMPLETED -> "Thanks for riding with Rideit."
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
                        scaleX = if (stage == RideitTripStage.SEARCHING || stage == RideitTripStage.ARRIVING) pulse else 1f
                        scaleY = if (stage == RideitTripStage.SEARCHING || stage == RideitTripStage.ARRIVING) pulse else 1f
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
                text = subtitle,
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
private fun RideitDriverSummaryBlock(
    driverName: String?,
    vehicleName: String?,
    vehicleNumber: String?,
    stage: RideitTripStage
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2563EB),
                                Color(0xFF7C3AED)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driverName.firstInitialOrRideit(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Text(
                    text = driverName?.takeIf { it.isNotBlank() } ?: "Rideit driver",
                    color = Color(0xFF0F172A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = buildVehicleLine(vehicleName, vehicleNumber),
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            RideitSmallStageBadge(stage = stage)
        }
    }
}

@Composable
private fun RideitRouteSummaryBlock(
    pickupText: String?,
    destinationText: String?
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
            RideitRouteLine(
                dotColor = Color(0xFF22C55E),
                label = "Pickup",
                value = pickupText?.takeIf { it.isNotBlank() } ?: "Current pickup location"
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                color = Color(0xFFE2E8F0)
            )

            RideitRouteLine(
                dotColor = Color(0xFFEF4444),
                label = "Destination",
                value = destinationText?.takeIf { it.isNotBlank() } ?: "Selected destination"
            )
        }
    }
}

@Composable
private fun RideitRouteLine(
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

@Composable
private fun RideitSmallStageBadge(
    stage: RideitTripStage
) {
    val text = when (stage) {
        RideitTripStage.SEARCHING -> "Searching"
        RideitTripStage.DRIVER_ASSIGNED -> "Accepted"
        RideitTripStage.ARRIVING -> "Arriving"
        RideitTripStage.IN_PROGRESS -> "Live"
        RideitTripStage.COMPLETED -> "Done"
        RideitTripStage.CANCELLED -> "Cancelled"
    }

    val background = when (stage) {
        RideitTripStage.CANCELLED -> Color(0xFFFEE2E2)
        RideitTripStage.COMPLETED -> Color(0xFFDCFCE7)
        RideitTripStage.IN_PROGRESS -> Color(0xFFDCFCE7)
        RideitTripStage.ARRIVING -> Color(0xFFFEF3C7)
        else -> Color(0xFFEFF6FF)
    }

    val content = when (stage) {
        RideitTripStage.CANCELLED -> Color(0xFFB91C1C)
        RideitTripStage.COMPLETED -> Color(0xFF166534)
        RideitTripStage.IN_PROGRESS -> Color(0xFF166534)
        RideitTripStage.ARRIVING -> Color(0xFF92400E)
        else -> Color(0xFF2563EB)
    }

    Box(
        modifier = Modifier
            .background(
                color = background,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = content,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

fun String?.toRideitTripStage(): RideitTripStage {
    return when (this?.trim()?.lowercase()) {
        "pending",
        "requested",
        "searching",
        "searching_driver",
        "finding_driver" -> RideitTripStage.SEARCHING

        "accepted",
        "driver_accepted",
        "driver_assigned",
        "driver_found" -> RideitTripStage.DRIVER_ASSIGNED

        "driver_arriving",
        "arriving",
        "on_the_way",
        "reached_pickup",
        "arrived" -> RideitTripStage.ARRIVING

        "ride_started",
        "started",
        "in_progress",
        "ongoing",
        "trip_started" -> RideitTripStage.IN_PROGRESS

        "completed",
        "complete",
        "trip_completed",
        "ride_completed" -> RideitTripStage.COMPLETED

        "cancelled",
        "canceled",
        "cancelled_by_rider",
        "cancelled_by_driver",
        "canceled_by_rider",
        "canceled_by_driver" -> RideitTripStage.CANCELLED

        else -> RideitTripStage.SEARCHING
    }
}

private fun String?.firstInitialOrRideit(): String {
    val cleanName = this?.trim().orEmpty()
    return if (cleanName.isNotBlank()) {
        cleanName.first().uppercase()
    } else {
        "R"
    }
}

private fun buildVehicleLine(
    vehicleName: String?,
    vehicleNumber: String?
): String {
    val cleanVehicle = vehicleName?.trim().orEmpty()
    val cleanNumber = vehicleNumber?.trim().orEmpty()

    return when {
        cleanVehicle.isNotBlank() && cleanNumber.isNotBlank() -> "$cleanVehicle • $cleanNumber"
        cleanVehicle.isNotBlank() -> cleanVehicle
        cleanNumber.isNotBlank() -> cleanNumber
        else -> "Vehicle details will appear here"
    }
}