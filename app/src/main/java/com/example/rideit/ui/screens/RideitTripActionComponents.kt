package com.example.rideit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

enum class RideitTripActionStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    GHOST
}

enum class RideitTripChipStyle {
    NEUTRAL,
    ACTIVE,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM
}

@Composable
fun RideitPremiumTripActionButton(
    text: String,
    modifier: Modifier = Modifier,
    style: RideitTripActionStyle = RideitTripActionStyle.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false,
    leadingText: String? = null,
    onClick: () -> Unit
) {
    val height = if (compact) 48.dp else 54.dp
    val shape = RoundedCornerShape(if (compact) 17.dp else 19.dp)

    Button(
        onClick = {
            if (!loading) onClick()
        },
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = if (style == RideitTripActionStyle.GHOST) 0.dp else 10.dp,
                shape = shape,
                spotColor = style.shadowColor()
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.containerColor(),
            contentColor = style.contentColor(),
            disabledContainerColor = Color(0xFFE2E8F0),
            disabledContentColor = Color(0xFF94A3B8)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (compact) 18.dp else 20.dp),
                strokeWidth = 2.dp,
                color = style.contentColor()
            )

            Spacer(modifier = Modifier.width(10.dp))
        } else if (!leadingText.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(if (compact) 24.dp else 26.dp)
                    .background(
                        color = style.contentColor().copy(alpha = 0.16f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = leadingText.take(2).uppercase(),
                    color = style.contentColor(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
            text = if (loading) "Please wait..." else text,
            fontSize = if (compact) 13.sp else 14.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitTripSecondaryTextAction(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            color = if (danger) Color(0xFFB91C1C) else Color(0xFF2563EB),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun RideitTripStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    style: RideitTripChipStyle = RideitTripChipStyle.NEUTRAL,
    compact: Boolean = true,
    showPulse: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_status_chip_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_status_chip_pulse_value"
    )

    Row(
        modifier = modifier
            .background(
                color = style.backgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = style.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 7.dp else 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 8.dp else 9.dp)
                .graphicsLayer {
                    scaleX = if (showPulse) pulse else 1f
                    scaleY = if (showPulse) pulse else 1f
                }
                .background(
                    color = style.dotColor(),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = text,
            color = style.contentColor(),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitTripStageChip(
    stage: RideitTripStage,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    isDriverMode: Boolean = false
) {
    val text = when (stage) {
        RideitTripStage.SEARCHING -> if (isDriverMode) "Waiting" else "Searching"
        RideitTripStage.DRIVER_ASSIGNED -> if (isDriverMode) "Accepted" else "Assigned"
        RideitTripStage.ARRIVING -> "Arriving"
        RideitTripStage.IN_PROGRESS -> "Live"
        RideitTripStage.COMPLETED -> "Done"
        RideitTripStage.CANCELLED -> "Cancelled"
    }

    val chipStyle = when (stage) {
        RideitTripStage.SEARCHING -> RideitTripChipStyle.ACTIVE
        RideitTripStage.DRIVER_ASSIGNED -> RideitTripChipStyle.PREMIUM
        RideitTripStage.ARRIVING -> RideitTripChipStyle.WARNING
        RideitTripStage.IN_PROGRESS -> RideitTripChipStyle.SUCCESS
        RideitTripStage.COMPLETED -> RideitTripChipStyle.SUCCESS
        RideitTripStage.CANCELLED -> RideitTripChipStyle.DANGER
    }

    RideitTripStatusChip(
        text = text,
        modifier = modifier,
        style = chipStyle,
        compact = compact,
        showPulse = stage == RideitTripStage.SEARCHING ||
                stage == RideitTripStage.ARRIVING ||
                stage == RideitTripStage.IN_PROGRESS
    )
}

@Composable
fun RideitTripActionRow(
    modifier: Modifier = Modifier,
    primaryText: String,
    secondaryText: String? = null,
    primaryStyle: RideitTripActionStyle = RideitTripActionStyle.PRIMARY,
    secondaryDanger: Boolean = false,
    primaryLoading: Boolean = false,
    primaryEnabled: Boolean = true,
    showSecondary: Boolean = false,
    compact: Boolean = false,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
    ) {
        RideitPremiumTripActionButton(
            text = primaryText,
            style = primaryStyle,
            loading = primaryLoading,
            enabled = primaryEnabled,
            compact = compact,
            onClick = onPrimaryClick
        )

        AnimatedVisibility(
            visible = showSecondary && !secondaryText.isNullOrBlank(),
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(140))
        ) {
            RideitTripSecondaryTextAction(
                text = secondaryText ?: "",
                danger = secondaryDanger,
                onClick = onSecondaryClick
            )
        }
    }
}

@Composable
fun RideitTripInfoPillRow(
    modifier: Modifier = Modifier,
    etaText: String? = null,
    fareText: String? = null,
    distanceText: String? = null,
    compact: Boolean = true
) {
    AnimatedVisibility(
        visible = !etaText.isNullOrBlank() || !fareText.isNullOrBlank() || !distanceText.isNullOrBlank(),
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!etaText.isNullOrBlank()) {
                RideitTripInfoPill(
                    label = "ETA",
                    value = etaText,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )
            }

            if (!fareText.isNullOrBlank()) {
                RideitTripInfoPill(
                    label = "Fare",
                    value = fareText,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )
            }

            if (!distanceText.isNullOrBlank()) {
                RideitTripInfoPill(
                    label = "Distance",
                    value = distanceText,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )
            }
        }
    }
}

@Composable
fun RideitTripInfoPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    compact: Boolean = true
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
            .padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 9.dp else 11.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            color = Color(0xFF0F172A),
            fontSize = if (compact) 12.sp else 13.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitTripLiveIndicator(
    modifier: Modifier = Modifier,
    text: String = "Live",
    compact: Boolean = true
) {
    RideitTripStatusChip(
        text = text,
        modifier = modifier,
        style = RideitTripChipStyle.SUCCESS,
        compact = compact,
        showPulse = true
    )
}

private fun RideitTripActionStyle.containerColor(): Color {
    return when (this) {
        RideitTripActionStyle.PRIMARY -> Color(0xFF0F172A)
        RideitTripActionStyle.SUCCESS -> Color(0xFF16A34A)
        RideitTripActionStyle.WARNING -> Color(0xFFF59E0B)
        RideitTripActionStyle.DANGER -> Color(0xFFEF4444)
        RideitTripActionStyle.GHOST -> Color(0xFFF8FAFC)
    }
}

private fun RideitTripActionStyle.contentColor(): Color {
    return when (this) {
        RideitTripActionStyle.PRIMARY -> Color.White
        RideitTripActionStyle.SUCCESS -> Color.White
        RideitTripActionStyle.WARNING -> Color.White
        RideitTripActionStyle.DANGER -> Color.White
        RideitTripActionStyle.GHOST -> Color(0xFF0F172A)
    }
}

private fun RideitTripActionStyle.shadowColor(): Color {
    return when (this) {
        RideitTripActionStyle.PRIMARY -> Color(0xFF0F172A).copy(alpha = 0.26f)
        RideitTripActionStyle.SUCCESS -> Color(0xFF16A34A).copy(alpha = 0.24f)
        RideitTripActionStyle.WARNING -> Color(0xFFF59E0B).copy(alpha = 0.24f)
        RideitTripActionStyle.DANGER -> Color(0xFFEF4444).copy(alpha = 0.22f)
        RideitTripActionStyle.GHOST -> Color.Transparent
    }
}

private fun RideitTripChipStyle.backgroundColor(): Color {
    return when (this) {
        RideitTripChipStyle.NEUTRAL -> Color(0xFFF8FAFC)
        RideitTripChipStyle.ACTIVE -> Color(0xFFEFF6FF)
        RideitTripChipStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitTripChipStyle.WARNING -> Color(0xFFFEF3C7)
        RideitTripChipStyle.DANGER -> Color(0xFFFEE2E2)
        RideitTripChipStyle.PREMIUM -> Color(0xFFF3E8FF)
    }
}

private fun RideitTripChipStyle.borderColor(): Color {
    return when (this) {
        RideitTripChipStyle.NEUTRAL -> Color(0xFFE2E8F0)
        RideitTripChipStyle.ACTIVE -> Color(0xFFBFDBFE)
        RideitTripChipStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitTripChipStyle.WARNING -> Color(0xFFFDE68A)
        RideitTripChipStyle.DANGER -> Color(0xFFFCA5A5)
        RideitTripChipStyle.PREMIUM -> Color(0xFFD8B4FE)
    }
}

private fun RideitTripChipStyle.dotColor(): Color {
    return when (this) {
        RideitTripChipStyle.NEUTRAL -> Color(0xFF94A3B8)
        RideitTripChipStyle.ACTIVE -> Color(0xFF2563EB)
        RideitTripChipStyle.SUCCESS -> Color(0xFF22C55E)
        RideitTripChipStyle.WARNING -> Color(0xFFF59E0B)
        RideitTripChipStyle.DANGER -> Color(0xFFEF4444)
        RideitTripChipStyle.PREMIUM -> Color(0xFF7C3AED)
    }
}

private fun RideitTripChipStyle.contentColor(): Color {
    return when (this) {
        RideitTripChipStyle.NEUTRAL -> Color(0xFF475569)
        RideitTripChipStyle.ACTIVE -> Color(0xFF2563EB)
        RideitTripChipStyle.SUCCESS -> Color(0xFF166534)
        RideitTripChipStyle.WARNING -> Color(0xFF92400E)
        RideitTripChipStyle.DANGER -> Color(0xFFB91C1C)
        RideitTripChipStyle.PREMIUM -> Color(0xFF6D28D9)
    }
}