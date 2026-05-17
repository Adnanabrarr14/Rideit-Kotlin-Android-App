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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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

enum class RideitLocationStatusType {
    PERMISSION_NEEDED,
    GPS_OFF,
    SEARCHING_LOCATION,
    LOCATION_READY,
    LOCATION_ERROR,
    APPROXIMATE_LOCATION
}

@Immutable
data class RideitLocationStatusUiModel(
    val type: RideitLocationStatusType,
    val title: String? = null,
    val message: String? = null,
    val primaryActionText: String? = null,
    val secondaryActionText: String? = null,
    val showPrimaryAction: Boolean = true,
    val showSecondaryAction: Boolean = false
)

@Composable
fun RideitLocationStatusCard(
    status: RideitLocationStatusUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    loading: Boolean = false,
    onPrimaryActionClick: () -> Unit = {},
    onSecondaryActionClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 16.dp else 22.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = Color.Black.copy(alpha = 0.18f)
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
            ),
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
                .padding(if (compact) 16.dp else 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitLocationStatusIcon(
                type = status.type,
                compact = compact,
                loading = loading
            )

            Spacer(modifier = Modifier.height(if (compact) 13.dp else 16.dp))

            Text(
                text = status.title ?: status.type.defaultTitle(),
                color = Color(0xFF0F172A),
                fontSize = if (compact) 17.sp else 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = status.message ?: status.type.defaultMessage(),
                color = Color(0xFF64748B),
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) 17.sp else 19.sp,
                modifier = Modifier.padding(top = 6.dp, start = 6.dp, end = 6.dp),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (status.showPrimaryAction) {
                Spacer(modifier = Modifier.height(if (compact) 16.dp else 20.dp))

                Button(
                    onClick = {
                        if (!loading) onPrimaryActionClick()
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 50.dp else 54.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = status.type.primaryActionColor(),
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
                            text = "Checking...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Text(
                            text = status.primaryActionText ?: status.type.defaultPrimaryAction(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            if (status.showSecondaryAction) {
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        if (!loading) onSecondaryActionClick()
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = status.secondaryActionText ?: "Not Now",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun RideitAnimatedLocationStatusCard(
    visible: Boolean,
    status: RideitLocationStatusUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    loading: Boolean = false,
    onPrimaryActionClick: () -> Unit = {},
    onSecondaryActionClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(200))
    ) {
        RideitLocationStatusCard(
            status = status,
            modifier = modifier,
            compact = compact,
            loading = loading,
            onPrimaryActionClick = onPrimaryActionClick,
            onSecondaryActionClick = onSecondaryActionClick
        )
    }
}

@Composable
fun RideitLocationPermissionCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    loading: Boolean = false,
    onGrantPermissionClick: () -> Unit = {},
    onUseManualLocationClick: () -> Unit = {}
) {
    RideitLocationStatusCard(
        modifier = modifier,
        compact = compact,
        loading = loading,
        status = RideitLocationStatusUiModel(
            type = RideitLocationStatusType.PERMISSION_NEEDED,
            showPrimaryAction = true,
            showSecondaryAction = true,
            primaryActionText = "Allow location",
            secondaryActionText = "Enter manually"
        ),
        onPrimaryActionClick = onGrantPermissionClick,
        onSecondaryActionClick = onUseManualLocationClick
    )
}

@Composable
fun RideitGpsOffCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onEnableGpsClick: () -> Unit = {},
    onUseManualLocationClick: () -> Unit = {}
) {
    RideitLocationStatusCard(
        modifier = modifier,
        compact = compact,
        status = RideitLocationStatusUiModel(
            type = RideitLocationStatusType.GPS_OFF,
            showPrimaryAction = true,
            showSecondaryAction = true,
            primaryActionText = "Turn on GPS",
            secondaryActionText = "Use manual pickup"
        ),
        onPrimaryActionClick = onEnableGpsClick,
        onSecondaryActionClick = onUseManualLocationClick
    )
}

@Composable
fun RideitLocationSearchingCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    RideitLocationStatusCard(
        modifier = modifier,
        compact = compact,
        loading = true,
        status = RideitLocationStatusUiModel(
            type = RideitLocationStatusType.SEARCHING_LOCATION,
            showPrimaryAction = false,
            showSecondaryAction = false
        )
    )
}

@Composable
fun RideitLocationReadyBanner(
    modifier: Modifier = Modifier,
    locationText: String? = null,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    RideitInlineLocationStatusBanner(
        modifier = modifier,
        type = RideitLocationStatusType.LOCATION_READY,
        title = "Location ready",
        message = locationText?.takeIf { it.isNotBlank() } ?: "Your pickup location is ready.",
        compact = compact,
        onClick = onClick
    )
}

@Composable
fun RideitLocationErrorBanner(
    modifier: Modifier = Modifier,
    message: String = "Could not get your current location.",
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    RideitInlineLocationStatusBanner(
        modifier = modifier,
        type = RideitLocationStatusType.LOCATION_ERROR,
        title = "Location issue",
        message = message,
        compact = compact,
        onClick = onClick
    )
}

@Composable
fun RideitInlineLocationStatusBanner(
    type: RideitLocationStatusType,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = type.softBackgroundColor(),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .border(
                width = 1.dp,
                color = type.borderColor(),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable { onClick() }
            .padding(if (compact) 13.dp else 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitLocationStatusIcon(
            type = type,
            compact = true,
            loading = type == RideitLocationStatusType.SEARCHING_LOCATION,
            inline = true
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = message,
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = type.shortStatusText(),
            color = type.textColor(),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitLocationStatusChip(
    type: RideitLocationStatusType,
    modifier: Modifier = Modifier,
    text: String? = null,
    compact: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(
                color = type.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = type.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 8.dp else 9.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                strokeWidth = 2.dp,
                color = type.dotColor()
            )
        } else {
            Box(
                modifier = Modifier
                    .size(if (compact) 9.dp else 10.dp)
                    .background(
                        color = type.dotColor(),
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = text ?: type.shortStatusText(),
            color = type.textColor(),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitLocationControlPanel(
    modifier: Modifier = Modifier,
    hasPermission: Boolean,
    isGpsEnabled: Boolean,
    isSearchingLocation: Boolean,
    currentLocationText: String? = null,
    compact: Boolean = true,
    onGrantPermissionClick: () -> Unit = {},
    onEnableGpsClick: () -> Unit = {},
    onRetryLocationClick: () -> Unit = {},
    onManualLocationClick: () -> Unit = {}
) {
    val status = when {
        !hasPermission -> RideitLocationStatusUiModel(
            type = RideitLocationStatusType.PERMISSION_NEEDED,
            showPrimaryAction = true,
            showSecondaryAction = true,
            primaryActionText = "Allow location",
            secondaryActionText = "Enter manually"
        )

        !isGpsEnabled -> RideitLocationStatusUiModel(
            type = RideitLocationStatusType.GPS_OFF,
            showPrimaryAction = true,
            showSecondaryAction = true,
            primaryActionText = "Turn on GPS",
            secondaryActionText = "Use manual pickup"
        )

        isSearchingLocation -> RideitLocationStatusUiModel(
            type = RideitLocationStatusType.SEARCHING_LOCATION,
            showPrimaryAction = false,
            showSecondaryAction = false
        )

        else -> RideitLocationStatusUiModel(
            type = RideitLocationStatusType.LOCATION_READY,
            title = "Pickup location ready",
            message = currentLocationText?.takeIf { it.isNotBlank() }
                ?: "Your current pickup location is ready for booking.",
            showPrimaryAction = true,
            showSecondaryAction = true,
            primaryActionText = "Refresh location",
            secondaryActionText = "Change manually"
        )
    }

    RideitLocationStatusCard(
        modifier = modifier,
        status = status,
        compact = compact,
        loading = isSearchingLocation,
        onPrimaryActionClick = {
            when {
                !hasPermission -> onGrantPermissionClick()
                !isGpsEnabled -> onEnableGpsClick()
                else -> onRetryLocationClick()
            }
        },
        onSecondaryActionClick = onManualLocationClick
    )
}

@Composable
private fun RideitLocationStatusIcon(
    type: RideitLocationStatusType,
    compact: Boolean,
    loading: Boolean,
    inline: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_location_status_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_location_status_icon_pulse_value"
    )

    val outerSize = when {
        inline -> 38.dp
        compact -> 58.dp
        else -> 68.dp
    }

    val innerSize = when {
        inline -> 16.dp
        compact -> 25.dp
        else -> 30.dp
    }

    Box(
        modifier = Modifier
            .size(outerSize)
            .background(
                color = type.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(innerSize),
                strokeWidth = if (inline) 2.dp else 3.dp,
                color = type.dotColor()
            )
        } else {
            Box(
                modifier = Modifier
                    .size(innerSize)
                    .graphicsLayer {
                        scaleX = if (type.shouldPulse()) scale else 1f
                        scaleY = if (type.shouldPulse()) scale else 1f
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
                    fontSize = when {
                        inline -> 8.sp
                        compact -> 11.sp
                        else -> 13.sp
                    },
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun RideitLocationStatusType.defaultTitle(): String {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> "Allow location access"
        RideitLocationStatusType.GPS_OFF -> "Turn on GPS"
        RideitLocationStatusType.SEARCHING_LOCATION -> "Finding your location"
        RideitLocationStatusType.LOCATION_READY -> "Location ready"
        RideitLocationStatusType.LOCATION_ERROR -> "Location issue"
        RideitLocationStatusType.APPROXIMATE_LOCATION -> "Approximate location"
    }
}

private fun RideitLocationStatusType.defaultMessage(): String {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> "Rideit needs location permission to show nearby rides, pickup points, and accurate map movement."
        RideitLocationStatusType.GPS_OFF -> "GPS is turned off. Enable location services for accurate pickup and driver tracking."
        RideitLocationStatusType.SEARCHING_LOCATION -> "Rideit is checking your current location. This usually takes a moment."
        RideitLocationStatusType.LOCATION_READY -> "Your current pickup location is ready for booking."
        RideitLocationStatusType.LOCATION_ERROR -> "We could not read your current location. Please retry or enter pickup manually."
        RideitLocationStatusType.APPROXIMATE_LOCATION -> "Rideit is using an approximate location. You can adjust pickup manually."
    }
}

private fun RideitLocationStatusType.defaultPrimaryAction(): String {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> "Allow location"
        RideitLocationStatusType.GPS_OFF -> "Turn on GPS"
        RideitLocationStatusType.SEARCHING_LOCATION -> "Checking..."
        RideitLocationStatusType.LOCATION_READY -> "Refresh location"
        RideitLocationStatusType.LOCATION_ERROR -> "Retry location"
        RideitLocationStatusType.APPROXIMATE_LOCATION -> "Adjust pickup"
    }
}

private fun RideitLocationStatusType.shortStatusText(): String {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> "Permission"
        RideitLocationStatusType.GPS_OFF -> "GPS off"
        RideitLocationStatusType.SEARCHING_LOCATION -> "Searching"
        RideitLocationStatusType.LOCATION_READY -> "Ready"
        RideitLocationStatusType.LOCATION_ERROR -> "Issue"
        RideitLocationStatusType.APPROXIMATE_LOCATION -> "Approx"
    }
}

private fun RideitLocationStatusType.softBackgroundColor(): Color {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> Color(0xFFEFF6FF)
        RideitLocationStatusType.GPS_OFF -> Color(0xFFFEF3C7)
        RideitLocationStatusType.SEARCHING_LOCATION -> Color(0xFFEFF6FF)
        RideitLocationStatusType.LOCATION_READY -> Color(0xFFDCFCE7)
        RideitLocationStatusType.LOCATION_ERROR -> Color(0xFFFEE2E2)
        RideitLocationStatusType.APPROXIMATE_LOCATION -> Color(0xFFFFFBEB)
    }
}

private fun RideitLocationStatusType.borderColor(): Color {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> Color(0xFFBFDBFE)
        RideitLocationStatusType.GPS_OFF -> Color(0xFFFDE68A)
        RideitLocationStatusType.SEARCHING_LOCATION -> Color(0xFFBFDBFE)
        RideitLocationStatusType.LOCATION_READY -> Color(0xFFBBF7D0)
        RideitLocationStatusType.LOCATION_ERROR -> Color(0xFFFCA5A5)
        RideitLocationStatusType.APPROXIMATE_LOCATION -> Color(0xFFFDE68A)
    }
}

private fun RideitLocationStatusType.dotColor(): Color {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> Color(0xFF2563EB)
        RideitLocationStatusType.GPS_OFF -> Color(0xFFF59E0B)
        RideitLocationStatusType.SEARCHING_LOCATION -> Color(0xFF2563EB)
        RideitLocationStatusType.LOCATION_READY -> Color(0xFF22C55E)
        RideitLocationStatusType.LOCATION_ERROR -> Color(0xFFEF4444)
        RideitLocationStatusType.APPROXIMATE_LOCATION -> Color(0xFFF59E0B)
    }
}

private fun RideitLocationStatusType.textColor(): Color {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> Color(0xFF2563EB)
        RideitLocationStatusType.GPS_OFF -> Color(0xFF92400E)
        RideitLocationStatusType.SEARCHING_LOCATION -> Color(0xFF2563EB)
        RideitLocationStatusType.LOCATION_READY -> Color(0xFF166534)
        RideitLocationStatusType.LOCATION_ERROR -> Color(0xFFB91C1C)
        RideitLocationStatusType.APPROXIMATE_LOCATION -> Color(0xFF92400E)
    }
}

private fun RideitLocationStatusType.primaryActionColor(): Color {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> Color(0xFF2563EB)
        RideitLocationStatusType.GPS_OFF -> Color(0xFFF59E0B)
        RideitLocationStatusType.SEARCHING_LOCATION -> Color(0xFF2563EB)
        RideitLocationStatusType.LOCATION_READY -> Color(0xFF0F172A)
        RideitLocationStatusType.LOCATION_ERROR -> Color(0xFFEF4444)
        RideitLocationStatusType.APPROXIMATE_LOCATION -> Color(0xFFF59E0B)
    }
}

private fun RideitLocationStatusType.iconText(): String {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> "L"
        RideitLocationStatusType.GPS_OFF -> "!"
        RideitLocationStatusType.SEARCHING_LOCATION -> "•"
        RideitLocationStatusType.LOCATION_READY -> "✓"
        RideitLocationStatusType.LOCATION_ERROR -> "!"
        RideitLocationStatusType.APPROXIMATE_LOCATION -> "~"
    }
}

private fun RideitLocationStatusType.shouldPulse(): Boolean {
    return when (this) {
        RideitLocationStatusType.PERMISSION_NEEDED -> true
        RideitLocationStatusType.GPS_OFF -> true
        RideitLocationStatusType.SEARCHING_LOCATION -> true
        RideitLocationStatusType.LOCATION_READY -> false
        RideitLocationStatusType.LOCATION_ERROR -> true
        RideitLocationStatusType.APPROXIMATE_LOCATION -> true
    }
}
