package com.example.rideit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitMapControlType {
    RECENTER,
    LOCATION,
    GPS,
    LAYERS,
    SAVED_PLACES,
    ROUTE,
    ZOOM_IN,
    ZOOM_OUT,
    TRAFFIC,
    CLEAR_ROUTE,
    CUSTOM
}

enum class RideitMapControlStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

@Immutable
data class RideitMapControlUiModel(
    val type: RideitMapControlType,
    val label: String,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val active: Boolean = false,
    val style: RideitMapControlStyle = RideitMapControlStyle.NEUTRAL
)

@Composable
fun RideitFloatingMapControlButton(
    control: RideitMapControlUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    showLabel: Boolean = false,
    onClick: () -> Unit = {}
) {
    val actualStyle = if (control.active) {
        when (control.style) {
            RideitMapControlStyle.NEUTRAL -> RideitMapControlStyle.PRIMARY
            else -> control.style
        }
    } else {
        control.style
    }

    val alpha = if (control.enabled) 1f else 0.55f

    Row(
        modifier = modifier
            .shadow(
                elevation = if (compact) 12.dp else 16.dp,
                shape = RoundedCornerShape(50),
                spotColor = Color.Black.copy(alpha = 0.16f)
            )
            .background(
                color = Color.White.copy(alpha = 0.96f),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        actualStyle.borderColor().copy(alpha = 0.75f)
                    )
                ),
                shape = RoundedCornerShape(50)
            )
            .clickable(enabled = control.enabled && !control.loading) {
                onClick()
            }
            .padding(
                horizontal = if (showLabel) 11.dp else 9.dp,
                vertical = 9.dp
            )
            .graphicsLayer {
                this.alpha = alpha
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitMapControlIcon(
            type = control.type,
            style = actualStyle,
            active = control.active,
            loading = control.loading,
            compact = compact
        )

        AnimatedVisibility(
            visible = showLabel,
            enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(140)) + shrinkHorizontally(animationSpec = tween(160))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = control.label,
                    color = actualStyle.textColor(),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RideitVerticalMapControls(
    controls: List<RideitMapControlUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    showLabels: Boolean = false,
    onControlClick: (RideitMapControlUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        controls.forEach { control ->
            RideitFloatingMapControlButton(
                control = control,
                compact = compact,
                showLabel = showLabels,
                onClick = {
                    onControlClick(control)
                }
            )
        }
    }
}

@Composable
fun RideitHorizontalMapControls(
    controls: List<RideitMapControlUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    showLabels: Boolean = false,
    onControlClick: (RideitMapControlUiModel) -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        controls.forEach { control ->
            RideitFloatingMapControlButton(
                control = control,
                compact = compact,
                showLabel = showLabels,
                onClick = {
                    onControlClick(control)
                }
            )
        }
    }
}

@Composable
fun RideitMapControlsCluster(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    isLocationLoading: Boolean = false,
    isTrafficEnabled: Boolean = false,
    isRouteVisible: Boolean = false,
    showLabels: Boolean = false,
    onRecenterClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onTrafficClick: () -> Unit = {},
    onLayersClick: () -> Unit = {},
    onClearRouteClick: () -> Unit = {}
) {
    val controls = buildList {
        add(
            RideitMapControlUiModel(
                type = RideitMapControlType.RECENTER,
                label = "Recenter",
                style = RideitMapControlStyle.PRIMARY
            )
        )

        add(
            RideitMapControlUiModel(
                type = RideitMapControlType.LOCATION,
                label = "Location",
                loading = isLocationLoading,
                style = RideitMapControlStyle.SUCCESS
            )
        )

        add(
            RideitMapControlUiModel(
                type = RideitMapControlType.TRAFFIC,
                label = "Traffic",
                active = isTrafficEnabled,
                style = RideitMapControlStyle.WARNING
            )
        )

        add(
            RideitMapControlUiModel(
                type = RideitMapControlType.LAYERS,
                label = "Layers",
                style = RideitMapControlStyle.PREMIUM
            )
        )

        if (isRouteVisible) {
            add(
                RideitMapControlUiModel(
                    type = RideitMapControlType.CLEAR_ROUTE,
                    label = "Clear",
                    style = RideitMapControlStyle.DANGER
                )
            )
        }
    }

    RideitVerticalMapControls(
        controls = controls,
        modifier = modifier,
        compact = compact,
        showLabels = showLabels,
        onControlClick = { control ->
            when (control.type) {
                RideitMapControlType.RECENTER -> onRecenterClick()
                RideitMapControlType.LOCATION -> onLocationClick()
                RideitMapControlType.TRAFFIC -> onTrafficClick()
                RideitMapControlType.LAYERS -> onLayersClick()
                RideitMapControlType.CLEAR_ROUTE -> onClearRouteClick()
                else -> Unit
            }
        }
    )
}

@Composable
fun RideitMapTopControlBar(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    title: String = "Rideit Map",
    subtitle: String? = null,
    locationStatusType: RideitLocationStatusType = RideitLocationStatusType.LOCATION_READY,
    isLocationLoading: Boolean = false,
    onLocationStatusClick: () -> Unit = {},
    onSavedPlacesClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
                spotColor = Color.Black.copy(alpha = 0.14f)
            )
            .background(
                color = Color.White.copy(alpha = 0.96f),
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.72f),
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp)
            )
            .padding(horizontal = if (compact) 14.dp else 16.dp, vertical = if (compact) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitMapControlIcon(
            type = RideitMapControlType.ROUTE,
            style = RideitMapControlStyle.PRIMARY,
            active = true,
            loading = false,
            compact = compact
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!subtitle.isNullOrBlank()) {
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
        }

        RideitLocationStatusChip(
            type = locationStatusType,
            loading = isLocationLoading,
            compact = true,
            onClick = onLocationStatusClick
        )

        Spacer(modifier = Modifier.width(8.dp))

        RideitFloatingMapControlButton(
            control = RideitMapControlUiModel(
                type = RideitMapControlType.SAVED_PLACES,
                label = "Saved",
                style = RideitMapControlStyle.PREMIUM
            ),
            compact = true,
            showLabel = false,
            onClick = onSavedPlacesClick
        )
    }
}

@Composable
fun RideitRouteFloatingActionBar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    distanceText: String? = null,
    durationText: String? = null,
    fareText: String? = null,
    onPreviewClick: () -> Unit = {},
    onClearClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(200))
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(if (compact) 28.dp else 32.dp),
                    spotColor = Color.Black.copy(alpha = 0.16f)
                )
                .background(
                    color = Color.White.copy(alpha = 0.97f),
                    shape = RoundedCornerShape(if (compact) 28.dp else 32.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(if (compact) 28.dp else 32.dp)
                )
                .clickable { onPreviewClick() }
                .padding(if (compact) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitMapControlIcon(
                type = RideitMapControlType.ROUTE,
                style = RideitMapControlStyle.SUCCESS,
                active = true,
                loading = false,
                compact = compact
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = listOfNotNull(
                        distanceText?.takeIf { it.isNotBlank() },
                        durationText?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank {
                        "Route selected"
                    },
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 14.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = fareText?.takeIf { it.isNotBlank() } ?: "Tap to preview fare and ride options",
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            RideitFloatingMapControlButton(
                control = RideitMapControlUiModel(
                    type = RideitMapControlType.CLEAR_ROUTE,
                    label = "Clear",
                    style = RideitMapControlStyle.DANGER
                ),
                compact = true,
                showLabel = false,
                onClick = onClearClick
            )
        }
    }
}

@Composable
fun RideitMapZoomControls(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onZoomInClick: () -> Unit = {},
    onZoomOutClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(50),
                spotColor = Color.Black.copy(alpha = 0.14f)
            )
            .background(
                color = Color.White.copy(alpha = 0.96f),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.72f),
                shape = RoundedCornerShape(50)
            )
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RideitFloatingMapControlButton(
            control = RideitMapControlUiModel(
                type = RideitMapControlType.ZOOM_IN,
                label = "Zoom in",
                style = RideitMapControlStyle.NEUTRAL
            ),
            compact = compact,
            showLabel = false,
            onClick = onZoomInClick
        )

        RideitFloatingMapControlButton(
            control = RideitMapControlUiModel(
                type = RideitMapControlType.ZOOM_OUT,
                label = "Zoom out",
                style = RideitMapControlStyle.NEUTRAL
            ),
            compact = compact,
            showLabel = false,
            onClick = onZoomOutClick
        )
    }
}

@Composable
private fun RideitMapControlIcon(
    type: RideitMapControlType,
    style: RideitMapControlStyle,
    active: Boolean,
    loading: Boolean,
    compact: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_map_control_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_map_control_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 34.dp else 40.dp)
            .background(
                color = style.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                strokeWidth = 2.dp,
                color = style.dotColor()
            )
        } else {
            Box(
                modifier = Modifier
                    .size(if (compact) 15.dp else 18.dp)
                    .graphicsLayer {
                        scaleX = if (active || type.shouldPulse()) scale else 1f
                        scaleY = if (active || type.shouldPulse()) scale else 1f
                    }
                    .background(
                        color = style.dotColor(),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.iconText(),
                    color = Color.White,
                    fontSize = if (compact) 8.sp else 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

fun rideitDefaultMapControls(
    isLocationLoading: Boolean = false,
    isTrafficEnabled: Boolean = false,
    isRouteVisible: Boolean = false
): List<RideitMapControlUiModel> {
    return buildList {
        add(
            RideitMapControlUiModel(
                type = RideitMapControlType.RECENTER,
                label = "Recenter",
                style = RideitMapControlStyle.PRIMARY
            )
        )

        add(
            RideitMapControlUiModel(
                type = RideitMapControlType.LOCATION,
                label = "Location",
                loading = isLocationLoading,
                style = RideitMapControlStyle.SUCCESS
            )
        )

        add(
            RideitMapControlUiModel(
                type = RideitMapControlType.TRAFFIC,
                label = "Traffic",
                active = isTrafficEnabled,
                style = RideitMapControlStyle.WARNING
            )
        )

        add(
            RideitMapControlUiModel(
                type = RideitMapControlType.SAVED_PLACES,
                label = "Saved",
                style = RideitMapControlStyle.PREMIUM
            )
        )

        if (isRouteVisible) {
            add(
                RideitMapControlUiModel(
                    type = RideitMapControlType.CLEAR_ROUTE,
                    label = "Clear",
                    style = RideitMapControlStyle.DANGER
                )
            )
        }
    }
}

private fun RideitMapControlType.iconText(): String {
    return when (this) {
        RideitMapControlType.RECENTER -> "⌖"
        RideitMapControlType.LOCATION -> "L"
        RideitMapControlType.GPS -> "G"
        RideitMapControlType.LAYERS -> "▣"
        RideitMapControlType.SAVED_PLACES -> "★"
        RideitMapControlType.ROUTE -> "R"
        RideitMapControlType.ZOOM_IN -> "+"
        RideitMapControlType.ZOOM_OUT -> "−"
        RideitMapControlType.TRAFFIC -> "T"
        RideitMapControlType.CLEAR_ROUTE -> "×"
        RideitMapControlType.CUSTOM -> "•"
    }
}

private fun RideitMapControlType.shouldPulse(): Boolean {
    return when (this) {
        RideitMapControlType.LOCATION,
        RideitMapControlType.GPS,
        RideitMapControlType.ROUTE -> true

        else -> false
    }
}

private fun RideitMapControlStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitMapControlStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitMapControlStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitMapControlStyle.WARNING -> Color(0xFFFEF3C7)
        RideitMapControlStyle.DANGER -> Color(0xFFFEE2E2)
        RideitMapControlStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitMapControlStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitMapControlStyle.borderColor(): Color {
    return when (this) {
        RideitMapControlStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitMapControlStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitMapControlStyle.WARNING -> Color(0xFFFDE68A)
        RideitMapControlStyle.DANGER -> Color(0xFFFCA5A5)
        RideitMapControlStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitMapControlStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitMapControlStyle.dotColor(): Color {
    return when (this) {
        RideitMapControlStyle.PRIMARY -> Color(0xFF2563EB)
        RideitMapControlStyle.SUCCESS -> Color(0xFF22C55E)
        RideitMapControlStyle.WARNING -> Color(0xFFF59E0B)
        RideitMapControlStyle.DANGER -> Color(0xFFEF4444)
        RideitMapControlStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitMapControlStyle.NEUTRAL -> Color(0xFF64748B)
    }
}

private fun RideitMapControlStyle.textColor(): Color {
    return when (this) {
        RideitMapControlStyle.PRIMARY -> Color(0xFF2563EB)
        RideitMapControlStyle.SUCCESS -> Color(0xFF166534)
        RideitMapControlStyle.WARNING -> Color(0xFF92400E)
        RideitMapControlStyle.DANGER -> Color(0xFFB91C1C)
        RideitMapControlStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitMapControlStyle.NEUTRAL -> Color(0xFF475569)
    }
}