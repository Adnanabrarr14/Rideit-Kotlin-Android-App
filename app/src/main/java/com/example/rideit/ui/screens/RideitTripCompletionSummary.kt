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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun RideitTripCompletionSummarySheet(
    visible: Boolean,
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    riderName: String? = null,
    driverName: String? = null,
    pickupText: String? = null,
    destinationText: String? = null,
    fareText: String? = null,
    distanceText: String? = null,
    durationText: String? = null,
    paymentMethodText: String? = null,
    tripIdText: String? = null,
    primaryButtonText: String? = null,
    secondaryButtonText: String? = null,
    showPrimaryButton: Boolean = true,
    showSecondaryButton: Boolean = true,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(260)) +
                expandVertically(animationSpec = tween(280)),
        exit = fadeOut(animationSpec = tween(180)) +
                shrinkVertically(animationSpec = tween(220))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.22f)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.80f),
                            Color.White.copy(alpha = 0.22f)
                        )
                    ),
                    shape = RoundedCornerShape(36.dp)
                ),
            shape = RoundedCornerShape(36.dp),
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RideitCompletionSuccessIcon()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isDriverMode) "Ride completed" else "Trip completed",
                    color = Color(0xFF0F172A),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isDriverMode) {
                        "Great job. Your Rideit trip has been finished successfully."
                    } else {
                        "Thanks for riding with Rideit. Your trip summary is ready."
                    },
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                RideitTripCompletionAmountBlock(
                    fareText = fareText,
                    paymentMethodText = paymentMethodText,
                    isDriverMode = isDriverMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                RideitTripCompletionRouteBlock(
                    pickupText = pickupText,
                    destinationText = destinationText
                )

                Spacer(modifier = Modifier.height(14.dp))

                RideitTripCompletionDetailsGrid(
                    isDriverMode = isDriverMode,
                    riderName = riderName,
                    driverName = driverName,
                    distanceText = distanceText,
                    durationText = durationText,
                    tripIdText = tripIdText
                )

                if (showPrimaryButton) {
                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onPrimaryClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(19.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = primaryButtonText ?: if (isDriverMode) "Back to driver home" else "Rate this ride",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (showSecondaryButton) {
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onSecondaryClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = secondaryButtonText ?: if (isDriverMode) "View earnings" else "View receipt",
                            color = Color(0xFF2563EB),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RideitCompactTripCompletionCard(
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    fareText: String? = null,
    destinationText: String? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitCompletionMiniIcon()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = if (isDriverMode) "Ride completed" else "Trip completed",
                    color = Color(0xFF0F172A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = destinationText?.takeIf { it.isNotBlank() }
                        ?: if (isDriverMode) "Your trip summary is ready" else "Receipt and rating are ready",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!fareText.isNullOrBlank()) {
                Text(
                    text = fareText,
                    color = Color(0xFF16A34A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RideitCompletionSuccessIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_completion_success_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_completion_success_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .background(
                color = Color(0xFFDCFCE7),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .background(
                    color = Color(0xFF22C55E),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun RideitCompletionMiniIcon() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .background(
                color = Color(0xFFDCFCE7),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = Color(0xFF22C55E),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun RideitTripCompletionAmountBlock(
    fareText: String?,
    paymentMethodText: String?,
    isDriverMode: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF0FDF4),
                shape = RoundedCornerShape(26.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFBBF7D0),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isDriverMode) "Trip earning" else "Total fare",
            color = Color(0xFF166534),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = fareText?.takeIf { it.isNotBlank() } ?: "Rs 0",
            color = Color(0xFF0F172A),
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 2.dp)
        )

        Text(
            text = paymentMethodText?.takeIf { it.isNotBlank() } ?: "Payment method not available",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun RideitTripCompletionRouteBlock(
    pickupText: String?,
    destinationText: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(14.dp)
    ) {
        RideitCompletionRouteLine(
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

        RideitCompletionRouteLine(
            dotColor = Color(0xFFEF4444),
            label = "Destination",
            value = destinationText?.takeIf { it.isNotBlank() } ?: "Destination location"
        )
    }
}

@Composable
private fun RideitCompletionRouteLine(
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
private fun RideitTripCompletionDetailsGrid(
    isDriverMode: Boolean,
    riderName: String?,
    driverName: String?,
    distanceText: String?,
    durationText: String?,
    tripIdText: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RideitCompletionDetailTile(
                label = if (isDriverMode) "Rider" else "Driver",
                value = if (isDriverMode) {
                    riderName?.takeIf { it.isNotBlank() } ?: "Rider"
                } else {
                    driverName?.takeIf { it.isNotBlank() } ?: "Driver"
                },
                modifier = Modifier.weight(1f)
            )

            RideitCompletionDetailTile(
                label = "Distance",
                value = distanceText?.takeIf { it.isNotBlank() } ?: "—",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RideitCompletionDetailTile(
                label = "Duration",
                value = durationText?.takeIf { it.isNotBlank() } ?: "—",
                modifier = Modifier.weight(1f)
            )

            RideitCompletionDetailTile(
                label = "Trip ID",
                value = tripIdText?.takeIf { it.isNotBlank() } ?: "Rideit",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RideitCompletionDetailTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
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
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}