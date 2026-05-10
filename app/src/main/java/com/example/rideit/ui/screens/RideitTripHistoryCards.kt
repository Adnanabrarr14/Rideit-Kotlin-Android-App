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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitTripHistoryStatus {
    COMPLETED,
    CANCELLED,
    IN_PROGRESS,
    UNKNOWN
}

data class RideitTripHistoryUiModel(
    val tripId: String = "",
    val status: String? = null,
    val pickupText: String? = null,
    val destinationText: String? = null,
    val fareText: String? = null,
    val distanceText: String? = null,
    val durationText: String? = null,
    val dateText: String? = null,
    val timeText: String? = null,
    val riderName: String? = null,
    val driverName: String? = null,
    val ratingText: String? = null,
    val paymentMethodText: String? = null
)

@Composable
fun RideitTripHistoryCard(
    trip: RideitTripHistoryUiModel,
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    val historyStatus = remember(trip.status) {
        trip.status.toRideitTripHistoryStatus()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 12.dp else 18.dp,
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
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
                .padding(if (compact) 15.dp else 17.dp)
        ) {
            RideitTripHistoryHeader(
                trip = trip,
                status = historyStatus,
                isDriverMode = isDriverMode,
                compact = compact
            )

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 14.dp))

            RideitTripHistoryRouteBlock(
                pickupText = trip.pickupText,
                destinationText = trip.destinationText,
                compact = compact
            )

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 14.dp))

            RideitTripHistoryStatsRow(
                fareText = trip.fareText,
                distanceText = trip.distanceText,
                durationText = trip.durationText,
                ratingText = trip.ratingText,
                compact = compact
            )

            if (!trip.paymentMethodText.isNullOrBlank() || trip.tripId.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                RideitTripHistoryFooter(
                    tripId = trip.tripId,
                    paymentMethodText = trip.paymentMethodText,
                    compact = compact
                )
            }
        }
    }
}

@Composable
fun RideitTripHistoryList(
    trips: List<RideitTripHistoryUiModel>,
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    compact: Boolean = false,
    emptyTitle: String = "No trip history yet",
    emptyMessage: String = "Your completed Rideit trips will appear here.",
    onTripClick: (RideitTripHistoryUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)
    ) {
        if (trips.isEmpty()) {
            RideitTripHistoryEmptyState(
                title = emptyTitle,
                message = emptyMessage,
                compact = compact
            )
        } else {
            trips.forEach { trip ->
                RideitTripHistoryCard(
                    trip = trip,
                    isDriverMode = isDriverMode,
                    compact = compact,
                    onClick = {
                        onTripClick(trip)
                    }
                )
            }
        }
    }
}

@Composable
fun RideitTripHistorySection(
    title: String,
    trips: List<RideitTripHistoryUiModel>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isDriverMode: Boolean = false,
    compact: Boolean = false,
    onTripClick: (RideitTripHistoryUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 18.sp else 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 12.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            RideitTripHistoryCountChip(
                count = trips.size,
                compact = compact
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

        RideitTripHistoryList(
            trips = trips,
            isDriverMode = isDriverMode,
            compact = compact,
            emptyTitle = if (isDriverMode) "No driver trips yet" else "No rider trips yet",
            emptyMessage = if (isDriverMode) {
                "Completed driver trips and earnings will appear here."
            } else {
                "Completed rides and receipts will appear here."
            },
            onTripClick = onTripClick
        )
    }
}

@Composable
fun RideitCompactRecentTripCard(
    trip: RideitTripHistoryUiModel,
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    onClick: () -> Unit = {}
) {
    val status = remember(trip.status) {
        trip.status.toRideitTripHistoryStatus()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.13f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.55f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitTripHistoryStatusIcon(
                status = status,
                compact = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = trip.destinationText?.takeIf { it.isNotBlank() }
                        ?: if (isDriverMode) "Driver trip" else "Rideit trip",
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = listOfNotNull(
                        trip.dateText?.takeIf { it.isNotBlank() },
                        trip.timeText?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank {
                        status.label()
                    },
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!trip.fareText.isNullOrBlank()) {
                Text(
                    text = trip.fareText,
                    color = if (status == RideitTripHistoryStatus.CANCELLED) {
                        Color(0xFF94A3B8)
                    } else {
                        Color(0xFF16A34A)
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RideitTripHistoryHeader(
    trip: RideitTripHistoryUiModel,
    status: RideitTripHistoryStatus,
    isDriverMode: Boolean,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitTripHistoryStatusIcon(
            status = status,
            compact = compact
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = when {
                    isDriverMode && !trip.riderName.isNullOrBlank() -> "Trip with ${trip.riderName}"
                    !isDriverMode && !trip.driverName.isNullOrBlank() -> "Trip with ${trip.driverName}"
                    isDriverMode -> "Driver trip"
                    else -> "Rideit trip"
                },
                color = Color(0xFF0F172A),
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = listOfNotNull(
                    trip.dateText?.takeIf { it.isNotBlank() },
                    trip.timeText?.takeIf { it.isNotBlank() }
                ).joinToString(" • ").ifBlank {
                    "Trip details"
                },
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        RideitTripHistoryStatusChip(
            status = status,
            compact = compact
        )
    }
}

@Composable
private fun RideitTripHistoryRouteBlock(
    pickupText: String?,
    destinationText: String?,
    compact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(if (compact) 20.dp else 23.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(if (compact) 20.dp else 23.dp)
            )
            .padding(if (compact) 12.dp else 14.dp)
    ) {
        RideitHistoryRouteLine(
            dotColor = Color(0xFF22C55E),
            label = "Pickup",
            value = pickupText?.takeIf { it.isNotBlank() } ?: "Pickup location",
            compact = compact
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (compact) 8.dp else 10.dp),
            color = Color(0xFFE2E8F0)
        )

        RideitHistoryRouteLine(
            dotColor = Color(0xFFEF4444),
            label = "Destination",
            value = destinationText?.takeIf { it.isNotBlank() } ?: "Destination location",
            compact = compact
        )
    }
}

@Composable
private fun RideitHistoryRouteLine(
    dotColor: Color,
    label: String,
    value: String,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(if (compact) 8.dp else 10.dp)
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
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RideitTripHistoryStatsRow(
    fareText: String?,
    distanceText: String?,
    durationText: String?,
    ratingText: String?,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RideitHistoryStatTile(
            label = "Fare",
            value = fareText?.takeIf { it.isNotBlank() } ?: "—",
            modifier = Modifier.weight(1f),
            compact = compact
        )

        RideitHistoryStatTile(
            label = "Distance",
            value = distanceText?.takeIf { it.isNotBlank() } ?: "—",
            modifier = Modifier.weight(1f),
            compact = compact
        )

        RideitHistoryStatTile(
            label = if (!ratingText.isNullOrBlank()) "Rating" else "Duration",
            value = ratingText?.takeIf { it.isNotBlank() }
                ?: durationText?.takeIf { it.isNotBlank() }
                ?: "—",
            modifier = Modifier.weight(1f),
            compact = compact
        )
    }
}

@Composable
private fun RideitHistoryStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    Column(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(if (compact) 17.dp else 19.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(if (compact) 17.dp else 19.dp)
            )
            .padding(horizontal = 8.dp, vertical = if (compact) 9.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = value,
            color = Color(0xFF0F172A),
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitTripHistoryFooter(
    tripId: String,
    paymentMethodText: String?,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = paymentMethodText?.takeIf { it.isNotBlank() } ?: "Rideit payment",
            color = Color(0xFF64748B),
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (tripId.isNotBlank()) {
            Text(
                text = "ID ${tripId.takeLast(6).uppercase()}",
                color = Color(0xFF94A3B8),
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RideitTripHistoryStatusIcon(
    status: RideitTripHistoryStatus,
    compact: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_history_status_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_history_status_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 42.dp else 48.dp)
            .background(
                color = status.backgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 18.dp else 21.dp)
                .graphicsLayer {
                    scaleX = if (status == RideitTripHistoryStatus.IN_PROGRESS) pulse else 1f
                    scaleY = if (status == RideitTripHistoryStatus.IN_PROGRESS) pulse else 1f
                }
                .background(
                    color = status.dotColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = status.iconText(),
                color = Color.White,
                fontSize = if (compact) 9.sp else 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun RideitTripHistoryStatusChip(
    status: RideitTripHistoryStatus,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = status.backgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = status.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = if (compact) 9.dp else 10.dp,
                vertical = if (compact) 6.dp else 7.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label(),
            color = status.textColor(),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitTripHistoryCountChip(
    count: Int,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFBFDBFE),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 7.dp else 8.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count trips",
            color = Color(0xFF2563EB),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RideitTripHistoryEmptyState(
    title: String,
    message: String,
    compact: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.60f),
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp)
            ),
        shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(if (compact) 18.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 52.dp else 60.dp)
                    .background(
                        color = Color(0xFFEFF6FF),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 22.dp else 26.dp)
                        .background(
                            color = Color(0xFF2563EB),
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = message,
                color = Color(0xFF64748B),
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) 17.sp else 19.sp,
                modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
}

fun String?.toRideitTripHistoryStatus(): RideitTripHistoryStatus {
    return when (this?.trim()?.lowercase()) {
        "completed",
        "complete",
        "trip_completed",
        "ride_completed",
        "done",
        "finished" -> RideitTripHistoryStatus.COMPLETED

        "cancelled",
        "canceled",
        "cancelled_by_rider",
        "cancelled_by_driver",
        "canceled_by_rider",
        "canceled_by_driver" -> RideitTripHistoryStatus.CANCELLED

        "in_progress",
        "ongoing",
        "ride_started",
        "trip_started",
        "started",
        "driver_arriving",
        "arriving",
        "accepted",
        "driver_accepted" -> RideitTripHistoryStatus.IN_PROGRESS

        else -> RideitTripHistoryStatus.UNKNOWN
    }
}

private fun RideitTripHistoryStatus.label(): String {
    return when (this) {
        RideitTripHistoryStatus.COMPLETED -> "Completed"
        RideitTripHistoryStatus.CANCELLED -> "Cancelled"
        RideitTripHistoryStatus.IN_PROGRESS -> "Live"
        RideitTripHistoryStatus.UNKNOWN -> "Rideit"
    }
}

private fun RideitTripHistoryStatus.iconText(): String {
    return when (this) {
        RideitTripHistoryStatus.COMPLETED -> "✓"
        RideitTripHistoryStatus.CANCELLED -> "!"
        RideitTripHistoryStatus.IN_PROGRESS -> "•"
        RideitTripHistoryStatus.UNKNOWN -> "R"
    }
}

private fun RideitTripHistoryStatus.backgroundColor(): Color {
    return when (this) {
        RideitTripHistoryStatus.COMPLETED -> Color(0xFFDCFCE7)
        RideitTripHistoryStatus.CANCELLED -> Color(0xFFFEE2E2)
        RideitTripHistoryStatus.IN_PROGRESS -> Color(0xFFEFF6FF)
        RideitTripHistoryStatus.UNKNOWN -> Color(0xFFF8FAFC)
    }
}

private fun RideitTripHistoryStatus.borderColor(): Color {
    return when (this) {
        RideitTripHistoryStatus.COMPLETED -> Color(0xFFBBF7D0)
        RideitTripHistoryStatus.CANCELLED -> Color(0xFFFCA5A5)
        RideitTripHistoryStatus.IN_PROGRESS -> Color(0xFFBFDBFE)
        RideitTripHistoryStatus.UNKNOWN -> Color(0xFFE2E8F0)
    }
}

private fun RideitTripHistoryStatus.dotColor(): Color {
    return when (this) {
        RideitTripHistoryStatus.COMPLETED -> Color(0xFF22C55E)
        RideitTripHistoryStatus.CANCELLED -> Color(0xFFEF4444)
        RideitTripHistoryStatus.IN_PROGRESS -> Color(0xFF2563EB)
        RideitTripHistoryStatus.UNKNOWN -> Color(0xFF94A3B8)
    }
}

private fun RideitTripHistoryStatus.textColor(): Color {
    return when (this) {
        RideitTripHistoryStatus.COMPLETED -> Color(0xFF166534)
        RideitTripHistoryStatus.CANCELLED -> Color(0xFFB91C1C)
        RideitTripHistoryStatus.IN_PROGRESS -> Color(0xFF2563EB)
        RideitTripHistoryStatus.UNKNOWN -> Color(0xFF64748B)
    }
}