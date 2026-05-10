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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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

enum class RideitPanelStateType {
    EMPTY,
    LOADING,
    ERROR,
    SUCCESS
}

@Composable
fun RideitTripPanelStateCard(
    state: RideitPanelStateType,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    actionText: String? = null,
    showAction: Boolean = false,
    compact: Boolean = false,
    onActionClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = state != RideitPanelStateType.SUCCESS,
        enter = fadeIn(animationSpec = tween(240)) +
                expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) +
                shrinkVertically(animationSpec = tween(200))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (compact) 14.dp else 22.dp,
                    shape = RoundedCornerShape(if (compact) 26.dp else 32.dp),
                    spotColor = Color.Black.copy(alpha = 0.16f)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.72f),
                            Color.White.copy(alpha = 0.22f)
                        )
                    ),
                    shape = RoundedCornerShape(if (compact) 26.dp else 32.dp)
                ),
            shape = RoundedCornerShape(if (compact) 26.dp else 32.dp),
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
                    .padding(if (compact) 16.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RideitTripPanelStateIcon(
                    state = state,
                    compact = compact
                )

                Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

                Text(
                    text = title ?: state.defaultTitle(),
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 16.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message ?: state.defaultMessage(),
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = if (compact) 17.sp else 19.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                if (showAction && !actionText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(if (compact) 14.dp else 18.dp))

                    Button(
                        onClick = onActionClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (compact) 48.dp else 52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = state.actionColor(),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = actionText,
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
fun RideitRiderEmptyTripState(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showAction: Boolean = true,
    onBookRideClick: () -> Unit = {}
) {
    RideitTripPanelStateCard(
        modifier = modifier,
        state = RideitPanelStateType.EMPTY,
        title = "Ready for your next ride?",
        message = "Search your destination and book a premium Rideit trip.",
        actionText = "Book a ride",
        showAction = showAction,
        compact = compact,
        onActionClick = onBookRideClick
    )
}

@Composable
fun RideitDriverEmptyRideState(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showAction: Boolean = false,
    onGoOnlineClick: () -> Unit = {}
) {
    RideitTripPanelStateCard(
        modifier = modifier,
        state = RideitPanelStateType.EMPTY,
        title = "No active ride",
        message = "Stay online and new rider requests will appear here.",
        actionText = "Go online",
        showAction = showAction,
        compact = compact,
        onActionClick = onGoOnlineClick
    )
}

@Composable
fun RideitTripLoadingState(
    modifier: Modifier = Modifier,
    title: String = "Loading ride details",
    message: String = "Please wait while Rideit refreshes your active trip.",
    compact: Boolean = false
) {
    RideitTripPanelStateCard(
        modifier = modifier,
        state = RideitPanelStateType.LOADING,
        title = title,
        message = message,
        showAction = false,
        compact = compact
    )
}

@Composable
fun RideitTripErrorState(
    modifier: Modifier = Modifier,
    title: String = "Couldn’t load ride",
    message: String = "Check your connection and try again.",
    actionText: String = "Retry",
    compact: Boolean = false,
    onRetryClick: () -> Unit = {}
) {
    RideitTripPanelStateCard(
        modifier = modifier,
        state = RideitPanelStateType.ERROR,
        title = title,
        message = message,
        actionText = actionText,
        showAction = true,
        compact = compact,
        onActionClick = onRetryClick
    )
}

@Composable
fun RideitInlineTripStateRow(
    state: RideitPanelStateType,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    compact: Boolean = true
) {
    if (state == RideitPanelStateType.SUCCESS) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = state.softBackgroundColor(),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = state.softBorderColor(),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 11.dp else 13.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitTripPanelStateIcon(
            state = state,
            compact = true,
            inline = true
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title ?: state.defaultTitle(),
                color = Color(0xFF0F172A),
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = message ?: state.defaultMessage(),
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RideitTripPanelStateIcon(
    state: RideitPanelStateType,
    compact: Boolean,
    inline: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_panel_state_icon_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_panel_state_icon_pulse_value"
    )

    val outerSize = when {
        inline -> 38.dp
        compact -> 54.dp
        else -> 64.dp
    }

    val innerSize = when {
        inline -> 16.dp
        compact -> 22.dp
        else -> 26.dp
    }

    Box(
        modifier = Modifier
            .size(outerSize)
            .background(
                color = state.iconBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            RideitPanelStateType.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(innerSize),
                    strokeWidth = if (inline) 2.dp else 3.dp,
                    color = Color(0xFF2563EB)
                )
            }

            RideitPanelStateType.EMPTY,
            RideitPanelStateType.ERROR,
            RideitPanelStateType.SUCCESS -> {
                Box(
                    modifier = Modifier
                        .size(innerSize)
                        .graphicsLayer {
                            scaleX = if (state == RideitPanelStateType.EMPTY) pulse else 1f
                            scaleY = if (state == RideitPanelStateType.EMPTY) pulse else 1f
                        }
                        .background(
                            color = state.iconDotColor(),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (state == RideitPanelStateType.ERROR) {
                        Text(
                            text = "!",
                            color = Color.White,
                            fontSize = if (inline) 11.sp else 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

private fun RideitPanelStateType.defaultTitle(): String {
    return when (this) {
        RideitPanelStateType.EMPTY -> "No active trip"
        RideitPanelStateType.LOADING -> "Loading ride details"
        RideitPanelStateType.ERROR -> "Something went wrong"
        RideitPanelStateType.SUCCESS -> "Ride ready"
    }
}

private fun RideitPanelStateType.defaultMessage(): String {
    return when (this) {
        RideitPanelStateType.EMPTY -> "Your active ride information will appear here."
        RideitPanelStateType.LOADING -> "Rideit is refreshing your latest trip status."
        RideitPanelStateType.ERROR -> "Please check your connection and try again."
        RideitPanelStateType.SUCCESS -> "Your ride details are ready."
    }
}

private fun RideitPanelStateType.iconBackgroundColor(): Color {
    return when (this) {
        RideitPanelStateType.EMPTY -> Color(0xFFEFF6FF)
        RideitPanelStateType.LOADING -> Color(0xFFEFF6FF)
        RideitPanelStateType.ERROR -> Color(0xFFFEE2E2)
        RideitPanelStateType.SUCCESS -> Color(0xFFDCFCE7)
    }
}

private fun RideitPanelStateType.iconDotColor(): Color {
    return when (this) {
        RideitPanelStateType.EMPTY -> Color(0xFF2563EB)
        RideitPanelStateType.LOADING -> Color(0xFF2563EB)
        RideitPanelStateType.ERROR -> Color(0xFFEF4444)
        RideitPanelStateType.SUCCESS -> Color(0xFF22C55E)
    }
}

private fun RideitPanelStateType.actionColor(): Color {
    return when (this) {
        RideitPanelStateType.EMPTY -> Color(0xFF0F172A)
        RideitPanelStateType.LOADING -> Color(0xFF2563EB)
        RideitPanelStateType.ERROR -> Color(0xFFEF4444)
        RideitPanelStateType.SUCCESS -> Color(0xFF16A34A)
    }
}

private fun RideitPanelStateType.softBackgroundColor(): Color {
    return when (this) {
        RideitPanelStateType.EMPTY -> Color(0xFFF8FAFC)
        RideitPanelStateType.LOADING -> Color(0xFFEFF6FF)
        RideitPanelStateType.ERROR -> Color(0xFFFEF2F2)
        RideitPanelStateType.SUCCESS -> Color(0xFFF0FDF4)
    }
}

private fun RideitPanelStateType.softBorderColor(): Color {
    return when (this) {
        RideitPanelStateType.EMPTY -> Color(0xFFE2E8F0)
        RideitPanelStateType.LOADING -> Color(0xFFBFDBFE)
        RideitPanelStateType.ERROR -> Color(0xFFFCA5A5)
        RideitPanelStateType.SUCCESS -> Color(0xFFBBF7D0)
    }
}