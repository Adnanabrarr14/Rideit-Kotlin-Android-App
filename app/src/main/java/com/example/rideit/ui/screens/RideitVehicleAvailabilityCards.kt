package com.example.rideit.ui.screens.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

enum class RideitVehicleType {
    CAR,
    BIKE,
    AUTO,
    XL,
    PREMIUM,
    CUSTOM
}

enum class RideitDriverAvailabilityStatus {
    ONLINE,
    OFFLINE,
    BUSY,
    ON_TRIP,
    SUSPENDED,
    PENDING_VERIFICATION
}

enum class RideitVehicleVerificationStatus {
    VERIFIED,
    PENDING,
    REJECTED,
    MISSING,
    EXPIRED
}

enum class RideitVehicleCardStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

@Immutable
data class RideitVehicleDetailsUiModel(
    val vehicleId: String = "",
    val vehicleName: String = "Rideit Vehicle",
    val vehicleModel: String? = null,
    val vehicleNumber: String? = null,
    val vehicleColor: String? = null,
    val vehicleType: RideitVehicleType = RideitVehicleType.CAR,
    val seatsText: String? = null,
    val verificationStatus: RideitVehicleVerificationStatus = RideitVehicleVerificationStatus.PENDING,
    val isPrimary: Boolean = true
)

@Immutable
data class RideitDriverAvailabilityUiModel(
    val status: RideitDriverAvailabilityStatus = RideitDriverAvailabilityStatus.OFFLINE,
    val isOnline: Boolean = false,
    val statusTitle: String? = null,
    val statusMessage: String? = null,
    val activeAreaText: String? = null,
    val lastOnlineText: String? = null
)

@Composable
fun RideitVehicleDetailsCard(
    vehicle: RideitVehicleDetailsUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 16.dp else 22.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = Color.Black.copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
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
                        colors = listOf(Color.White, Color(0xFFF8FAFC))
                    )
                )
                .padding(if (compact) 16.dp else 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitVehicleIcon(
                    vehicleType = vehicle.vehicleType,
                    compact = compact,
                    pulse = vehicle.verificationStatus == RideitVehicleVerificationStatus.VERIFIED
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
                            text = vehicle.vehicleName,
                            color = Color(0xFF0F172A),
                            fontSize = if (compact) 16.sp else 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (vehicle.isPrimary) {
                            Spacer(modifier = Modifier.width(7.dp))

                            RideitVehicleMiniBadge(
                                text = "Primary",
                                style = RideitVehicleCardStyle.PRIMARY,
                                compact = compact
                            )
                        }
                    }

                    Text(
                        text = listOfNotNull(
                            vehicle.vehicleModel?.takeIf { it.isNotBlank() },
                            vehicle.vehicleColor?.takeIf { it.isNotBlank() }
                        ).joinToString(" • ").ifBlank {
                            vehicle.vehicleType.defaultSubtitle()
                        },
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                RideitVehicleVerificationBadge(
                    status = vehicle.verificationStatus,
                    compact = compact
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 14.dp else 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RideitVehicleInfoTile(
                    label = "Plate",
                    value = vehicle.vehicleNumber?.takeIf { it.isNotBlank() } ?: "Not set",
                    style = RideitVehicleCardStyle.PRIMARY,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )

                RideitVehicleInfoTile(
                    label = "Seats",
                    value = vehicle.seatsText?.takeIf { it.isNotBlank() } ?: vehicle.vehicleType.defaultSeats(),
                    style = RideitVehicleCardStyle.SUCCESS,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )

                RideitVehicleInfoTile(
                    label = "Type",
                    value = vehicle.vehicleType.shortLabel(),
                    style = RideitVehicleCardStyle.PREMIUM,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )
            }
        }
    }
}

@Composable
fun RideitDriverAvailabilityCard(
    availability: RideitDriverAvailabilityUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    loading: Boolean = false,
    onOnlineChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}
) {
    val style = availability.status.toCardStyle()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 16.dp else 22.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = style.dotColor().copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        style.borderColor().copy(alpha = 0.85f)
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
                        colors = listOf(style.softBackgroundColor(), Color.White)
                    )
                )
                .padding(if (compact) 16.dp else 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitAvailabilityIcon(
                    status = availability.status,
                    compact = compact,
                    loading = loading
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 13.dp)
                ) {
                    Text(
                        text = availability.statusTitle?.takeIf { it.isNotBlank() }
                            ?: availability.status.defaultTitle(),
                        color = Color(0xFF0F172A),
                        fontSize = if (compact) 16.sp else 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = availability.statusMessage?.takeIf { it.isNotBlank() }
                            ?: availability.status.defaultMessage(),
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = availability.isOnline,
                    onCheckedChange = {
                        if (!loading && availability.status != RideitDriverAvailabilityStatus.SUSPENDED) {
                            onOnlineChange(it)
                        }
                    },
                    enabled = !loading && availability.status != RideitDriverAvailabilityStatus.SUSPENDED,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF22C55E),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1),
                        uncheckedBorderColor = Color(0xFFE2E8F0)
                    )
                )
            }

            if (!availability.activeAreaText.isNullOrBlank() || !availability.lastOnlineText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(if (compact) 14.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RideitVehicleInfoTile(
                        label = "Area",
                        value = availability.activeAreaText?.takeIf { it.isNotBlank() } ?: "Not set",
                        style = RideitVehicleCardStyle.PRIMARY,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )

                    RideitVehicleInfoTile(
                        label = "Last online",
                        value = availability.lastOnlineText?.takeIf { it.isNotBlank() } ?: "Now",
                        style = RideitVehicleCardStyle.SUCCESS,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                }
            }
        }
    }
}

@Composable
fun RideitDriverAvailabilityStatusChip(
    status: RideitDriverAvailabilityStatus,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit = {}
) {
    val style = status.toCardStyle()

    Row(
        modifier = modifier
            .background(
                color = style.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = style.borderColor(),
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
                .size(if (compact) 9.dp else 10.dp)
                .background(
                    color = style.dotColor(),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = if (loading) "Updating" else status.shortLabel(),
            color = style.textColor(),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitVehicleListSection(
    vehicles: List<RideitVehicleDetailsUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    title: String = "Vehicle details",
    subtitle: String = "Manage your Rideit driver vehicle",
    onVehicleClick: (RideitVehicleDetailsUiModel) -> Unit = {},
    onAddVehicleClick: () -> Unit = {}
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
                    fontSize = if (compact) 16.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Add",
                color = Color(0xFF2563EB),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
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
                    .clickable { onAddVehicleClick() }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        if (vehicles.isEmpty()) {
            RideitEmptyVehicleCard(
                compact = compact,
                onClick = onAddVehicleClick
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
            ) {
                vehicles.forEach { vehicle ->
                    RideitVehicleDetailsCard(
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
fun RideitDriverVehicleAvailabilityPanel(
    visible: Boolean,
    vehicle: RideitVehicleDetailsUiModel,
    availability: RideitDriverAvailabilityUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    loadingAvailability: Boolean = false,
    primaryButtonText: String = "Edit vehicle",
    secondaryButtonText: String = "Driver settings",
    onOnlineChange: (Boolean) -> Unit = {},
    onVehicleClick: () -> Unit = {},
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(200))
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 16.dp)
        ) {
            RideitDriverAvailabilityCard(
                availability = availability,
                compact = compact,
                loading = loadingAvailability,
                onOnlineChange = onOnlineChange
            )

            RideitVehicleDetailsCard(
                vehicle = vehicle,
                compact = compact,
                onClick = onVehicleClick
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = primaryButtonText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onSecondaryClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEFF6FF),
                        contentColor = Color(0xFF2563EB)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = secondaryButtonText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun RideitCompactVehicleStatusCard(
    vehicle: RideitVehicleDetailsUiModel,
    availability: RideitDriverAvailabilityUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 12.dp else 16.dp,
                shape = RoundedCornerShape(if (compact) 24.dp else 28.dp),
                spotColor = Color.Black.copy(alpha = 0.13f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.62f),
                shape = RoundedCornerShape(if (compact) 24.dp else 28.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 24.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFF8FAFC))
                    )
                )
                .padding(if (compact) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitVehicleIcon(
                vehicleType = vehicle.vehicleType,
                compact = compact,
                pulse = availability.isOnline
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = vehicle.vehicleName,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 14.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = listOfNotNull(
                        vehicle.vehicleNumber?.takeIf { it.isNotBlank() },
                        availability.status.shortLabel()
                    ).joinToString(" • "),
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            RideitDriverAvailabilityStatusChip(
                status = availability.status,
                compact = true
            )
        }
    }
}

@Composable
private fun RideitVehicleInfoTile(
    label: String,
    value: String,
    style: RideitVehicleCardStyle,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    Column(
        modifier = modifier
            .background(
                color = style.softBackgroundColor(),
                shape = RoundedCornerShape(if (compact) 18.dp else 20.dp)
            )
            .border(
                width = 1.dp,
                color = style.borderColor(),
                shape = RoundedCornerShape(if (compact) 18.dp else 20.dp)
            )
            .padding(horizontal = 8.dp, vertical = if (compact) 9.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = value,
            color = style.textColor(),
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitVehicleIcon(
    vehicleType: RideitVehicleType,
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_vehicle_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_vehicle_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 46.dp else 54.dp)
            .background(
                color = vehicleType.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 20.dp else 24.dp)
                .graphicsLayer {
                    scaleX = if (pulse) scale else 1f
                    scaleY = if (pulse) scale else 1f
                }
                .background(
                    color = vehicleType.dotColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = vehicleType.iconText(),
                color = Color.White,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun RideitAvailabilityIcon(
    status: RideitDriverAvailabilityStatus,
    compact: Boolean,
    loading: Boolean
) {
    val style = status.toCardStyle()

    RideitVehicleStatusIcon(
        style = style,
        text = if (loading) "…" else status.iconText(),
        compact = compact,
        pulse = status == RideitDriverAvailabilityStatus.ONLINE ||
                status == RideitDriverAvailabilityStatus.ON_TRIP ||
                loading
    )
}

@Composable
private fun RideitVehicleStatusIcon(
    style: RideitVehicleCardStyle,
    text: String,
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_vehicle_status_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_vehicle_status_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 46.dp else 54.dp)
            .background(
                color = style.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 20.dp else 24.dp)
                .graphicsLayer {
                    scaleX = if (pulse) scale else 1f
                    scaleY = if (pulse) scale else 1f
                }
                .background(
                    color = style.dotColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun RideitVehicleVerificationBadge(
    status: RideitVehicleVerificationStatus,
    compact: Boolean
) {
    val style = status.toCardStyle()

    Box(
        modifier = Modifier
            .background(
                color = style.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = style.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = if (compact) 8.dp else 9.dp,
                vertical = if (compact) 5.dp else 6.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label(),
            color = style.textColor(),
            fontSize = if (compact) 9.sp else 10.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitVehicleMiniBadge(
    text: String,
    style: RideitVehicleCardStyle,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = style.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = style.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = if (compact) 7.dp else 8.dp,
                vertical = if (compact) 4.dp else 5.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = style.textColor(),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitEmptyVehicleCard(
    compact: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFBFDBFE),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitVehicleIcon(
                vehicleType = RideitVehicleType.CAR,
                compact = compact,
                pulse = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No vehicle added",
                color = Color(0xFF0F172A),
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Add your vehicle details before accepting Rideit trips.",
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) 16.sp else 18.sp,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

fun rideitDefaultVehicleDetails(): RideitVehicleDetailsUiModel {
    return RideitVehicleDetailsUiModel(
        vehicleId = "default_vehicle",
        vehicleName = "Rideit Vehicle",
        vehicleModel = "Add vehicle model",
        vehicleNumber = "Not set",
        vehicleColor = "Not set",
        vehicleType = RideitVehicleType.CAR,
        seatsText = "4 seats",
        verificationStatus = RideitVehicleVerificationStatus.PENDING,
        isPrimary = true
    )
}

fun rideitDefaultDriverAvailability(
    isOnline: Boolean = false
): RideitDriverAvailabilityUiModel {
    return RideitDriverAvailabilityUiModel(
        status = if (isOnline) RideitDriverAvailabilityStatus.ONLINE else RideitDriverAvailabilityStatus.OFFLINE,
        isOnline = isOnline,
        activeAreaText = "Current city",
        lastOnlineText = if (isOnline) "Now" else "Offline"
    )
}

private fun RideitVehicleType.defaultSubtitle(): String {
    return when (this) {
        RideitVehicleType.CAR -> "Standard Rideit car"
        RideitVehicleType.BIKE -> "Fast bike ride"
        RideitVehicleType.AUTO -> "Compact auto ride"
        RideitVehicleType.XL -> "Larger vehicle"
        RideitVehicleType.PREMIUM -> "Premium vehicle"
        RideitVehicleType.CUSTOM -> "Custom vehicle"
    }
}

private fun RideitVehicleType.defaultSeats(): String {
    return when (this) {
        RideitVehicleType.BIKE -> "1 seat"
        RideitVehicleType.AUTO -> "3 seats"
        RideitVehicleType.XL -> "6 seats"
        else -> "4 seats"
    }
}

private fun RideitVehicleType.shortLabel(): String {
    return when (this) {
        RideitVehicleType.CAR -> "Car"
        RideitVehicleType.BIKE -> "Bike"
        RideitVehicleType.AUTO -> "Auto"
        RideitVehicleType.XL -> "XL"
        RideitVehicleType.PREMIUM -> "Premium"
        RideitVehicleType.CUSTOM -> "Custom"
    }
}

private fun RideitVehicleType.iconText(): String {
    return when (this) {
        RideitVehicleType.CAR -> "C"
        RideitVehicleType.BIKE -> "B"
        RideitVehicleType.AUTO -> "A"
        RideitVehicleType.XL -> "XL"
        RideitVehicleType.PREMIUM -> "P"
        RideitVehicleType.CUSTOM -> "•"
    }
}

private fun RideitVehicleType.softBackgroundColor(): Color {
    return when (this) {
        RideitVehicleType.CAR -> Color(0xFFEFF6FF)
        RideitVehicleType.BIKE -> Color(0xFFFEF3C7)
        RideitVehicleType.AUTO -> Color(0xFFF0FDF4)
        RideitVehicleType.XL -> Color(0xFFECFEFF)
        RideitVehicleType.PREMIUM -> Color(0xFFF3E8FF)
        RideitVehicleType.CUSTOM -> Color(0xFFF8FAFC)
    }
}

private fun RideitVehicleType.dotColor(): Color {
    return when (this) {
        RideitVehicleType.CAR -> Color(0xFF2563EB)
        RideitVehicleType.BIKE -> Color(0xFFF59E0B)
        RideitVehicleType.AUTO -> Color(0xFF16A34A)
        RideitVehicleType.XL -> Color(0xFF0891B2)
        RideitVehicleType.PREMIUM -> Color(0xFF7C3AED)
        RideitVehicleType.CUSTOM -> Color(0xFF64748B)
    }
}

private fun RideitDriverAvailabilityStatus.defaultTitle(): String {
    return when (this) {
        RideitDriverAvailabilityStatus.ONLINE -> "You are online"
        RideitDriverAvailabilityStatus.OFFLINE -> "You are offline"
        RideitDriverAvailabilityStatus.BUSY -> "You are busy"
        RideitDriverAvailabilityStatus.ON_TRIP -> "Trip in progress"
        RideitDriverAvailabilityStatus.SUSPENDED -> "Account restricted"
        RideitDriverAvailabilityStatus.PENDING_VERIFICATION -> "Verification pending"
    }
}

private fun RideitDriverAvailabilityStatus.defaultMessage(): String {
    return when (this) {
        RideitDriverAvailabilityStatus.ONLINE -> "You can receive new Rideit ride requests."
        RideitDriverAvailabilityStatus.OFFLINE -> "Go online when you are ready to accept rides."
        RideitDriverAvailabilityStatus.BUSY -> "Ride requests are paused while you are busy."
        RideitDriverAvailabilityStatus.ON_TRIP -> "You are currently handling an active trip."
        RideitDriverAvailabilityStatus.SUSPENDED -> "Contact support to restore driver availability."
        RideitDriverAvailabilityStatus.PENDING_VERIFICATION -> "Complete verification before going online."
    }
}

private fun RideitDriverAvailabilityStatus.shortLabel(): String {
    return when (this) {
        RideitDriverAvailabilityStatus.ONLINE -> "Online"
        RideitDriverAvailabilityStatus.OFFLINE -> "Offline"
        RideitDriverAvailabilityStatus.BUSY -> "Busy"
        RideitDriverAvailabilityStatus.ON_TRIP -> "On trip"
        RideitDriverAvailabilityStatus.SUSPENDED -> "Restricted"
        RideitDriverAvailabilityStatus.PENDING_VERIFICATION -> "Pending"
    }
}

private fun RideitDriverAvailabilityStatus.iconText(): String {
    return when (this) {
        RideitDriverAvailabilityStatus.ONLINE -> "✓"
        RideitDriverAvailabilityStatus.OFFLINE -> "•"
        RideitDriverAvailabilityStatus.BUSY -> "!"
        RideitDriverAvailabilityStatus.ON_TRIP -> "R"
        RideitDriverAvailabilityStatus.SUSPENDED -> "!"
        RideitDriverAvailabilityStatus.PENDING_VERIFICATION -> "?"
    }
}

private fun RideitDriverAvailabilityStatus.toCardStyle(): RideitVehicleCardStyle {
    return when (this) {
        RideitDriverAvailabilityStatus.ONLINE -> RideitVehicleCardStyle.SUCCESS
        RideitDriverAvailabilityStatus.OFFLINE -> RideitVehicleCardStyle.NEUTRAL
        RideitDriverAvailabilityStatus.BUSY -> RideitVehicleCardStyle.WARNING
        RideitDriverAvailabilityStatus.ON_TRIP -> RideitVehicleCardStyle.PRIMARY
        RideitDriverAvailabilityStatus.SUSPENDED -> RideitVehicleCardStyle.DANGER
        RideitDriverAvailabilityStatus.PENDING_VERIFICATION -> RideitVehicleCardStyle.PREMIUM
    }
}

private fun RideitVehicleVerificationStatus.label(): String {
    return when (this) {
        RideitVehicleVerificationStatus.VERIFIED -> "Verified"
        RideitVehicleVerificationStatus.PENDING -> "Pending"
        RideitVehicleVerificationStatus.REJECTED -> "Rejected"
        RideitVehicleVerificationStatus.MISSING -> "Missing"
        RideitVehicleVerificationStatus.EXPIRED -> "Expired"
    }
}

private fun RideitVehicleVerificationStatus.toCardStyle(): RideitVehicleCardStyle {
    return when (this) {
        RideitVehicleVerificationStatus.VERIFIED -> RideitVehicleCardStyle.SUCCESS
        RideitVehicleVerificationStatus.PENDING -> RideitVehicleCardStyle.WARNING
        RideitVehicleVerificationStatus.REJECTED -> RideitVehicleCardStyle.DANGER
        RideitVehicleVerificationStatus.MISSING -> RideitVehicleCardStyle.NEUTRAL
        RideitVehicleVerificationStatus.EXPIRED -> RideitVehicleCardStyle.DANGER
    }
}

private fun RideitVehicleCardStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitVehicleCardStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitVehicleCardStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitVehicleCardStyle.WARNING -> Color(0xFFFEF3C7)
        RideitVehicleCardStyle.DANGER -> Color(0xFFFEE2E2)
        RideitVehicleCardStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitVehicleCardStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitVehicleCardStyle.borderColor(): Color {
    return when (this) {
        RideitVehicleCardStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitVehicleCardStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitVehicleCardStyle.WARNING -> Color(0xFFFDE68A)
        RideitVehicleCardStyle.DANGER -> Color(0xFFFCA5A5)
        RideitVehicleCardStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitVehicleCardStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitVehicleCardStyle.dotColor(): Color {
    return when (this) {
        RideitVehicleCardStyle.PRIMARY -> Color(0xFF2563EB)
        RideitVehicleCardStyle.SUCCESS -> Color(0xFF22C55E)
        RideitVehicleCardStyle.WARNING -> Color(0xFFF59E0B)
        RideitVehicleCardStyle.DANGER -> Color(0xFFEF4444)
        RideitVehicleCardStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitVehicleCardStyle.NEUTRAL -> Color(0xFF64748B)
    }
}

private fun RideitVehicleCardStyle.textColor(): Color {
    return when (this) {
        RideitVehicleCardStyle.PRIMARY -> Color(0xFF2563EB)
        RideitVehicleCardStyle.SUCCESS -> Color(0xFF166534)
        RideitVehicleCardStyle.WARNING -> Color(0xFF92400E)
        RideitVehicleCardStyle.DANGER -> Color(0xFFB91C1C)
        RideitVehicleCardStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitVehicleCardStyle.NEUTRAL -> Color(0xFF475569)
    }
}