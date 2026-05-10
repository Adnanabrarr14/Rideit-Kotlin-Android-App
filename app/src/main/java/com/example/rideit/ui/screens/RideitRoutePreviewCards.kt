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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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

enum class RideitVehicleEstimateType {
    BIKE,
    RIDEIT_GO,
    RIDEIT_PREMIUM,
    RIDEIT_XL,
    AUTO,
    CUSTOM
}

@Immutable
data class RideitRoutePreviewUiModel(
    val pickupText: String? = null,
    val destinationText: String? = null,
    val distanceText: String? = null,
    val durationText: String? = null,
    val etaText: String? = null,
    val fareText: String? = null,
    val paymentMethodText: String? = null,
    val noteText: String? = null
)

@Immutable
data class RideitVehicleEstimateUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val fareText: String? = null,
    val etaText: String? = null,
    val capacityText: String? = null,
    val type: RideitVehicleEstimateType = RideitVehicleEstimateType.RIDEIT_GO,
    val selected: Boolean = false,
    val recommended: Boolean = false
)

@Composable
fun RideitRoutePreviewCard(
    route: RideitRoutePreviewUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showFareBlock: Boolean = true,
    showPaymentBlock: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 14.dp else 20.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = Color.Black.copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.76f),
                        Color.White.copy(alpha = 0.22f)
                    )
                ),
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
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
                .padding(if (compact) 16.dp else 20.dp)
        ) {
            RideitRoutePreviewHeader(
                distanceText = route.distanceText,
                durationText = route.durationText,
                etaText = route.etaText,
                compact = compact
            )

            Spacer(modifier = Modifier.height(if (compact) 14.dp else 18.dp))

            RideitRoutePreviewRouteBlock(
                pickupText = route.pickupText,
                destinationText = route.destinationText,
                compact = compact
            )

            if (showFareBlock || showPaymentBlock || !route.noteText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(if (compact) 14.dp else 16.dp))
            }

            if (showFareBlock || showPaymentBlock) {
                RideitRoutePreviewFareRow(
                    fareText = route.fareText,
                    paymentMethodText = route.paymentMethodText,
                    showFareBlock = showFareBlock,
                    showPaymentBlock = showPaymentBlock,
                    compact = compact
                )
            }

            if (!route.noteText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                RideitRoutePreviewNote(
                    text = route.noteText,
                    compact = compact
                )
            }
        }
    }
}

@Composable
fun RideitRouteEstimateBookingPanel(
    visible: Boolean,
    route: RideitRoutePreviewUiModel,
    vehicles: List<RideitVehicleEstimateUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    loading: Boolean = false,
    primaryButtonText: String = "Book Rideit",
    onVehicleClick: (RideitVehicleEstimateUiModel) -> Unit = {},
    onBookClick: () -> Unit = {},
    onChangeRouteClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(200))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.20f)
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
                            colors = listOf(Color.White, Color(0xFFF8FAFC))
                        )
                    )
                    .padding(18.dp)
            ) {
                RideitRoutePreviewCard(
                    route = route,
                    compact = compact,
                    showFareBlock = false,
                    showPaymentBlock = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                RideitVehicleEstimateSection(
                    vehicles = vehicles,
                    compact = compact,
                    onVehicleClick = onVehicleClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!loading) onBookClick()
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE2E8F0),
                        disabledContentColor = Color(0xFF94A3B8)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Booking...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Text(
                            text = primaryButtonText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        if (!loading) onChangeRouteClick()
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Change pickup or destination",
                        color = Color(0xFF2563EB),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun RideitVehicleEstimateSection(
    vehicles: List<RideitVehicleEstimateUiModel>,
    modifier: Modifier = Modifier,
    title: String = "Choose your ride",
    subtitle: String = "Select the best Rideit option",
    compact: Boolean = true,
    onVehicleClick: (RideitVehicleEstimateUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 17.sp else 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

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
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${vehicles.size} options",
                    color = Color(0xFF2563EB),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (vehicles.isEmpty()) {
            RideitRoutePreviewNote(
                text = "Ride options will appear after selecting pickup and destination.",
                compact = compact
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                vehicles.forEach { vehicle ->
                    RideitVehicleEstimateCard(
                        vehicle = vehicle,
                        compact = compact,
                        onClick = {
                            onVehicleClick(vehicle)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RideitVehicleEstimateCard(
    vehicle: RideitVehicleEstimateUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    val borderColor = if (vehicle.selected) {
        vehicle.type.borderColor()
    } else {
        Color(0xFFE2E8F0)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (vehicle.selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(if (compact) 23.dp else 26.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 23.dp else 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (vehicle.selected) {
                vehicle.type.softBackgroundColor()
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitVehicleEstimateIcon(
                type = vehicle.type,
                selected = vehicle.selected,
                compact = compact
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = vehicle.title,
                        color = Color(0xFF0F172A),
                        fontSize = if (compact) 14.sp else 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (vehicle.recommended) {
                        Spacer(modifier = Modifier.width(7.dp))

                        RideitVehicleRecommendedBadge(compact = compact)
                    }
                }

                Text(
                    text = vehicle.subtitle?.takeIf { it.isNotBlank() }
                        ?: vehicle.type.defaultSubtitle(),
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!vehicle.capacityText.isNullOrBlank() || !vehicle.etaText.isNullOrBlank()) {
                    Text(
                        text = listOfNotNull(
                            vehicle.etaText?.takeIf { it.isNotBlank() },
                            vehicle.capacityText?.takeIf { it.isNotBlank() }
                        ).joinToString(" • "),
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 3.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = vehicle.fareText?.takeIf { it.isNotBlank() } ?: "—",
                color = if (vehicle.selected) vehicle.type.textColor() else Color(0xFF0F172A),
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RideitVehicleEstimateChips(
    vehicles: List<RideitVehicleEstimateUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onVehicleClick: (RideitVehicleEstimateUiModel) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        vehicles.forEach { vehicle ->
            RideitVehicleEstimateChip(
                vehicle = vehicle,
                compact = compact,
                onClick = {
                    onVehicleClick(vehicle)
                }
            )
        }
    }
}

@Composable
fun RideitVehicleEstimateChip(
    vehicle: RideitVehicleEstimateUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(
                color = if (vehicle.selected) vehicle.type.softBackgroundColor() else Color.White,
                shape = RoundedCornerShape(50)
            )
            .border(
                width = if (vehicle.selected) 2.dp else 1.dp,
                color = if (vehicle.selected) vehicle.type.borderColor() else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 8.dp else 9.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 22.dp else 24.dp)
                .background(
                    color = vehicle.type.dotColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = vehicle.type.iconText(),
                color = Color.White,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = vehicle.title,
                color = if (vehicle.selected) vehicle.type.textColor() else Color(0xFF0F172A),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!vehicle.fareText.isNullOrBlank()) {
                Text(
                    text = vehicle.fareText,
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RideitRoutePreviewHeader(
    distanceText: String?,
    durationText: String?,
    etaText: String?,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitRoutePreviewIcon(
            compact = compact,
            pulse = true
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 13.dp)
        ) {
            Text(
                text = "Route preview",
                color = Color(0xFF0F172A),
                fontSize = if (compact) 17.sp else 20.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = listOfNotNull(
                    distanceText?.takeIf { it.isNotBlank() },
                    durationText?.takeIf { it.isNotBlank() },
                    etaText?.takeIf { it.isNotBlank() }?.let { "ETA $it" }
                ).joinToString(" • ").ifBlank {
                    "Choose pickup and destination"
                },
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RideitRoutePreviewRouteBlock(
    pickupText: String?,
    destinationText: String?,
    compact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(if (compact) 22.dp else 25.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(if (compact) 22.dp else 25.dp)
            )
            .padding(if (compact) 13.dp else 15.dp)
    ) {
        RideitRoutePreviewLine(
            dotColor = Color(0xFF22C55E),
            label = "Pickup",
            value = pickupText?.takeIf { it.isNotBlank() } ?: "Current pickup location",
            compact = compact
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (compact) 9.dp else 11.dp),
            color = Color(0xFFE2E8F0)
        )

        RideitRoutePreviewLine(
            dotColor = Color(0xFFEF4444),
            label = "Destination",
            value = destinationText?.takeIf { it.isNotBlank() } ?: "Select destination",
            compact = compact
        )
    }
}

@Composable
private fun RideitRoutePreviewLine(
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
                .size(if (compact) 9.dp else 10.dp)
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
private fun RideitRoutePreviewFareRow(
    fareText: String?,
    paymentMethodText: String?,
    showFareBlock: Boolean,
    showPaymentBlock: Boolean,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showFareBlock) {
            RideitRoutePreviewInfoTile(
                label = "Estimated fare",
                value = fareText?.takeIf { it.isNotBlank() } ?: "Rs 0",
                modifier = Modifier.weight(1f),
                compact = compact,
                highlight = true
            )
        }

        if (showPaymentBlock) {
            RideitRoutePreviewInfoTile(
                label = "Payment",
                value = paymentMethodText?.takeIf { it.isNotBlank() } ?: "Cash",
                modifier = Modifier.weight(1f),
                compact = compact,
                highlight = false
            )
        }
    }
}

@Composable
private fun RideitRoutePreviewInfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    compact: Boolean,
    highlight: Boolean
) {
    Column(
        modifier = modifier
            .background(
                color = if (highlight) Color(0xFFF0FDF4) else Color.White,
                shape = RoundedCornerShape(if (compact) 20.dp else 22.dp)
            )
            .border(
                width = 1.dp,
                color = if (highlight) Color(0xFFBBF7D0) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(if (compact) 20.dp else 22.dp)
            )
            .padding(horizontal = 12.dp, vertical = if (compact) 11.dp else 13.dp),
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
            color = if (highlight) Color(0xFF166534) else Color(0xFF0F172A),
            fontSize = if (compact) 13.sp else 14.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitRoutePreviewNote(
    text: String,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(if (compact) 20.dp else 22.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFBFDBFE),
                shape = RoundedCornerShape(if (compact) 20.dp else 22.dp)
            )
            .padding(if (compact) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 28.dp else 32.dp)
                .background(Color(0xFF2563EB), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "i",
                color = Color.White,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = Color(0xFF2563EB),
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitRoutePreviewIcon(
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_route_preview_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_route_preview_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 48.dp else 56.dp)
            .background(
                color = Color(0xFFEFF6FF),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 21.dp else 25.dp)
                .graphicsLayer {
                    scaleX = if (pulse) scale else 1f
                    scaleY = if (pulse) scale else 1f
                }
                .background(
                    color = Color(0xFF2563EB),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "R",
                color = Color.White,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun RideitVehicleEstimateIcon(
    type: RideitVehicleEstimateType,
    selected: Boolean,
    compact: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_vehicle_estimate_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_vehicle_estimate_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 44.dp else 50.dp)
            .background(
                color = type.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 19.dp else 22.dp)
                .graphicsLayer {
                    scaleX = if (selected) scale else 1f
                    scaleY = if (selected) scale else 1f
                }
                .background(
                    color = type.dotColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.iconText(),
                color = Color.White,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RideitVehicleRecommendedBadge(
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFBBF7D0),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = if (compact) 7.dp else 8.dp,
                vertical = if (compact) 4.dp else 5.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Best",
            color = Color(0xFF166534),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

fun rideitDefaultVehicleEstimates(
    selectedId: String = "rideit_go"
): List<RideitVehicleEstimateUiModel> {
    return listOf(
        RideitVehicleEstimateUiModel(
            id = "bike",
            title = "Bike",
            subtitle = "Fast and budget friendly",
            fareText = "Rs 180",
            etaText = "3 min",
            capacityText = "1 seat",
            type = RideitVehicleEstimateType.BIKE,
            selected = selectedId == "bike"
        ),
        RideitVehicleEstimateUiModel(
            id = "rideit_go",
            title = "Rideit Go",
            subtitle = "Everyday affordable ride",
            fareText = "Rs 420",
            etaText = "5 min",
            capacityText = "4 seats",
            type = RideitVehicleEstimateType.RIDEIT_GO,
            selected = selectedId == "rideit_go",
            recommended = true
        ),
        RideitVehicleEstimateUiModel(
            id = "premium",
            title = "Premium",
            subtitle = "Comfortable premium car",
            fareText = "Rs 680",
            etaText = "7 min",
            capacityText = "4 seats",
            type = RideitVehicleEstimateType.RIDEIT_PREMIUM,
            selected = selectedId == "premium"
        )
    )
}

private fun RideitVehicleEstimateType.defaultSubtitle(): String {
    return when (this) {
        RideitVehicleEstimateType.BIKE -> "Fast and budget friendly"
        RideitVehicleEstimateType.RIDEIT_GO -> "Everyday affordable ride"
        RideitVehicleEstimateType.RIDEIT_PREMIUM -> "Comfortable premium car"
        RideitVehicleEstimateType.RIDEIT_XL -> "More space for groups"
        RideitVehicleEstimateType.AUTO -> "Compact local ride"
        RideitVehicleEstimateType.CUSTOM -> "Custom ride option"
    }
}

private fun RideitVehicleEstimateType.softBackgroundColor(): Color {
    return when (this) {
        RideitVehicleEstimateType.BIKE -> Color(0xFFFFFBEB)
        RideitVehicleEstimateType.RIDEIT_GO -> Color(0xFFEFF6FF)
        RideitVehicleEstimateType.RIDEIT_PREMIUM -> Color(0xFFF3E8FF)
        RideitVehicleEstimateType.RIDEIT_XL -> Color(0xFFECFEFF)
        RideitVehicleEstimateType.AUTO -> Color(0xFFF0FDF4)
        RideitVehicleEstimateType.CUSTOM -> Color(0xFFF8FAFC)
    }
}

private fun RideitVehicleEstimateType.borderColor(): Color {
    return when (this) {
        RideitVehicleEstimateType.BIKE -> Color(0xFFFDE68A)
        RideitVehicleEstimateType.RIDEIT_GO -> Color(0xFFBFDBFE)
        RideitVehicleEstimateType.RIDEIT_PREMIUM -> Color(0xFFD8B4FE)
        RideitVehicleEstimateType.RIDEIT_XL -> Color(0xFFA5F3FC)
        RideitVehicleEstimateType.AUTO -> Color(0xFFBBF7D0)
        RideitVehicleEstimateType.CUSTOM -> Color(0xFFE2E8F0)
    }
}

private fun RideitVehicleEstimateType.dotColor(): Color {
    return when (this) {
        RideitVehicleEstimateType.BIKE -> Color(0xFFF59E0B)
        RideitVehicleEstimateType.RIDEIT_GO -> Color(0xFF2563EB)
        RideitVehicleEstimateType.RIDEIT_PREMIUM -> Color(0xFF7C3AED)
        RideitVehicleEstimateType.RIDEIT_XL -> Color(0xFF0891B2)
        RideitVehicleEstimateType.AUTO -> Color(0xFF16A34A)
        RideitVehicleEstimateType.CUSTOM -> Color(0xFF0F172A)
    }
}

private fun RideitVehicleEstimateType.textColor(): Color {
    return when (this) {
        RideitVehicleEstimateType.BIKE -> Color(0xFF92400E)
        RideitVehicleEstimateType.RIDEIT_GO -> Color(0xFF2563EB)
        RideitVehicleEstimateType.RIDEIT_PREMIUM -> Color(0xFF6D28D9)
        RideitVehicleEstimateType.RIDEIT_XL -> Color(0xFF0E7490)
        RideitVehicleEstimateType.AUTO -> Color(0xFF166534)
        RideitVehicleEstimateType.CUSTOM -> Color(0xFF0F172A)
    }
}

private fun RideitVehicleEstimateType.iconText(): String {
    return when (this) {
        RideitVehicleEstimateType.BIKE -> "B"
        RideitVehicleEstimateType.RIDEIT_GO -> "R"
        RideitVehicleEstimateType.RIDEIT_PREMIUM -> "P"
        RideitVehicleEstimateType.RIDEIT_XL -> "XL"
        RideitVehicleEstimateType.AUTO -> "A"
        RideitVehicleEstimateType.CUSTOM -> "•"
    }
}