package com.example.rideit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitTripStage {
    SEARCHING,
    DRIVER_ASSIGNED,
    ARRIVING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

private data class RideitTimelineItem(
    val stage: RideitTripStage,
    val riderTitle: String,
    val driverTitle: String,
    val riderSubtitle: String,
    val driverSubtitle: String
)

@Composable
fun RideitTripTimeline(
    currentStage: RideitTripStage,
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    compact: Boolean = false
) {
    val timelineItems = remember {
        listOf(
            RideitTimelineItem(
                stage = RideitTripStage.SEARCHING,
                riderTitle = "Finding driver",
                driverTitle = "Waiting request",
                riderSubtitle = "Looking for the best nearby driver",
                driverSubtitle = "New ride request will appear here"
            ),
            RideitTimelineItem(
                stage = RideitTripStage.DRIVER_ASSIGNED,
                riderTitle = "Driver assigned",
                driverTitle = "Ride accepted",
                riderSubtitle = "Your driver is ready for pickup",
                driverSubtitle = "You accepted this ride request"
            ),
            RideitTimelineItem(
                stage = RideitTripStage.ARRIVING,
                riderTitle = "Driver arriving",
                driverTitle = "Go to pickup",
                riderSubtitle = "Driver is heading to your location",
                driverSubtitle = "Navigate safely to the rider"
            ),
            RideitTimelineItem(
                stage = RideitTripStage.IN_PROGRESS,
                riderTitle = "Trip in progress",
                driverTitle = "Trip started",
                riderSubtitle = "Enjoy your Rideit journey",
                driverSubtitle = "Take rider safely to destination"
            ),
            RideitTimelineItem(
                stage = RideitTripStage.COMPLETED,
                riderTitle = "Trip completed",
                driverTitle = "Ride completed",
                riderSubtitle = "Receipt and rating are ready",
                driverSubtitle = "Great job. Trip finished"
            )
        )
    }

    val activeIndex = timelineItems.indexOfFirst { it.stage == currentStage }.coerceAtLeast(0)
    val isCancelled = currentStage == RideitTripStage.CANCELLED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
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
                .padding(
                    horizontal = if (compact) 16.dp else 18.dp,
                    vertical = if (compact) 14.dp else 18.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isDriverMode) "Driver trip status" else "Your ride status",
                        color = Color(0xFF0F172A),
                        fontSize = if (compact) 15.sp else 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = if (isCancelled) {
                            "This ride has been cancelled"
                        } else {
                            "Live progress updated as the ride moves"
                        },
                        color = if (isCancelled) Color(0xFFB91C1C) else Color(0xFF64748B),
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                RideitStatusBadge(
                    text = when (currentStage) {
                        RideitTripStage.SEARCHING -> "Searching"
                        RideitTripStage.DRIVER_ASSIGNED -> "Assigned"
                        RideitTripStage.ARRIVING -> "Arriving"
                        RideitTripStage.IN_PROGRESS -> "Live"
                        RideitTripStage.COMPLETED -> "Done"
                        RideitTripStage.CANCELLED -> "Cancelled"
                    },
                    isCancelled = isCancelled
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 14.dp else 18.dp))

            if (isCancelled) {
                RideitCancelledTimelineItem(
                    isDriverMode = isDriverMode,
                    compact = compact
                )
            } else {
                timelineItems.forEachIndexed { index, item ->
                    val isCompleted = index < activeIndex
                    val isActive = index == activeIndex
                    val isLast = index == timelineItems.lastIndex

                    RideitTimelineRow(
                        item = item,
                        isDriverMode = isDriverMode,
                        isCompleted = isCompleted,
                        isActive = isActive,
                        isLast = isLast,
                        compact = compact
                    )
                }
            }
        }
    }
}

@Composable
private fun RideitTimelineRow(
    item: RideitTimelineItem,
    isDriverMode: Boolean,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitTimelineDot(
                isCompleted = isCompleted,
                isActive = isActive,
                isCancelled = false,
                compact = compact
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (compact) 22.dp else 28.dp)
                        .background(
                            color = if (isCompleted) {
                                Color(0xFF22C55E).copy(alpha = 0.55f)
                            } else {
                                Color(0xFFE2E8F0)
                            },
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else if (compact) 10.dp else 14.dp)
        ) {
            Text(
                text = if (isDriverMode) item.driverTitle else item.riderTitle,
                color = when {
                    isActive -> Color(0xFF0F172A)
                    isCompleted -> Color(0xFF166534)
                    else -> Color(0xFF94A3B8)
                },
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = if (isActive || isCompleted) FontWeight.ExtraBold else FontWeight.SemiBold
            )

            Text(
                text = if (isDriverMode) item.driverSubtitle else item.riderSubtitle,
                color = when {
                    isActive -> Color(0xFF475569)
                    isCompleted -> Color(0xFF64748B)
                    else -> Color(0xFFCBD5E1)
                },
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun RideitTimelineDot(
    isCompleted: Boolean,
    isActive: Boolean,
    isCancelled: Boolean,
    compact: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_timeline_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_timeline_pulse_scale"
    )

    val dotColor by animateColorAsState(
        targetValue = when {
            isCancelled -> Color(0xFFEF4444)
            isActive -> Color(0xFF2563EB)
            isCompleted -> Color(0xFF22C55E)
            else -> Color(0xFFCBD5E1)
        },
        animationSpec = tween(durationMillis = 250),
        label = "rideit_timeline_dot_color"
    )

    val ringColor by animateColorAsState(
        targetValue = when {
            isCancelled -> Color(0xFFFEE2E2)
            isActive -> Color(0xFFDBEAFE)
            isCompleted -> Color(0xFFDCFCE7)
            else -> Color(0xFFF1F5F9)
        },
        animationSpec = tween(durationMillis = 250),
        label = "rideit_timeline_ring_color"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 25.dp else 29.dp)
            .graphicsLayer {
                scaleX = if (isActive) pulse else 1f
                scaleY = if (isActive) pulse else 1f
            }
            .background(ringColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 11.dp else 13.dp)
                .background(dotColor, CircleShape)
        )
    }
}

@Composable
private fun RideitStatusBadge(
    text: String,
    isCancelled: Boolean
) {
    val backgroundColor = if (isCancelled) Color(0xFFFEE2E2) else Color(0xFFEFF6FF)
    val textColor = if (isCancelled) Color(0xFFB91C1C) else Color(0xFF2563EB)
    val borderColor = if (isCancelled) Color(0xFFFCA5A5) else Color(0xFFBFDBFE)

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RideitCancelledTimelineItem(
    isDriverMode: Boolean,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        RideitTimelineDot(
            isCompleted = false,
            isActive = true,
            isCancelled = true,
            compact = compact
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (isDriverMode) "Ride cancelled" else "Your ride was cancelled",
                color = Color(0xFFB91C1C),
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = if (isDriverMode) {
                    "This request is no longer active"
                } else {
                    "You can book another Rideit trip anytime"
                },
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun RideitTripTimelinePreviewCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RideitTripTimeline(
            currentStage = RideitTripStage.ARRIVING,
            isDriverMode = false
        )

        RideitTripTimeline(
            currentStage = RideitTripStage.IN_PROGRESS,
            isDriverMode = true,
            compact = true
        )
    }
}