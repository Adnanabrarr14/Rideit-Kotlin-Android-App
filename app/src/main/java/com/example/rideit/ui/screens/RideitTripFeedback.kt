package com.example.rideit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class RideitTripFeedbackType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    LIVE
}

@Stable
class RideitTripFeedbackController internal constructor(
    val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) {
    fun showSuccess(
        message: String,
        actionLabel: String? = null
    ) {
        show(
            type = RideitTripFeedbackType.SUCCESS,
            message = message,
            actionLabel = actionLabel
        )
    }

    fun showError(
        message: String,
        actionLabel: String? = null
    ) {
        show(
            type = RideitTripFeedbackType.ERROR,
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long
        )
    }

    fun showWarning(
        message: String,
        actionLabel: String? = null
    ) {
        show(
            type = RideitTripFeedbackType.WARNING,
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long
        )
    }

    fun showInfo(
        message: String,
        actionLabel: String? = null
    ) {
        show(
            type = RideitTripFeedbackType.INFO,
            message = message,
            actionLabel = actionLabel
        )
    }

    fun showLive(
        message: String,
        actionLabel: String? = null
    ) {
        show(
            type = RideitTripFeedbackType.LIVE,
            message = message,
            actionLabel = actionLabel
        )
    }

    fun showRideBooked() {
        showSuccess("Ride booked successfully")
    }

    fun showRideAccepted() {
        showSuccess("Ride accepted")
    }

    fun showDriverArriving() {
        showLive("Driver is arriving")
    }

    fun showTripStarted() {
        showLive("Trip started")
    }

    fun showTripCompleted() {
        showSuccess("Trip completed")
    }

    fun showRideCancelled() {
        showWarning("Ride cancelled")
    }

    fun showNetworkError() {
        showError("Connection issue. Please try again.", actionLabel = "Retry")
    }

    fun show(
        type: RideitTripFeedbackType,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        val cleanMessage = message.trim()
        if (cleanMessage.isBlank()) return

        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = "${type.name}|$cleanMessage",
                actionLabel = actionLabel,
                withDismissAction = false,
                duration = duration
            )
        }
    }
}

@Composable
fun rememberRideitTripFeedbackController(): RideitTripFeedbackController {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    return remember(snackbarHostState, coroutineScope) {
        RideitTripFeedbackController(
            snackbarHostState = snackbarHostState,
            coroutineScope = coroutineScope
        )
    }
}

@Composable
fun RideitTripFeedbackHost(
    controller: RideitTripFeedbackController,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomCenter
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = alignment
    ) {
        SnackbarHost(
            hostState = controller.snackbarHostState,
            snackbar = { snackbarData ->
                RideitPremiumTripSnackbar(
                    snackbarData = snackbarData
                )
            }
        )
    }
}

@Composable
fun RideitTopTripFeedbackHost(
    controller: RideitTripFeedbackController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        SnackbarHost(
            hostState = controller.snackbarHostState,
            snackbar = { snackbarData ->
                RideitPremiumTripSnackbar(
                    snackbarData = snackbarData
                )
            }
        )
    }
}

@Composable
private fun RideitPremiumTripSnackbar(
    snackbarData: SnackbarData
) {
    val parsed = remember(snackbarData.visuals.message) {
        snackbarData.visuals.message.toRideitFeedbackPayload()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color.Black.copy(alpha = 0.20f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitTripFeedbackIcon(
                type = parsed.type
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = parsed.type.title(),
                    color = Color(0xFF0F172A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = parsed.message,
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val actionLabel = snackbarData.visuals.actionLabel

            if (!actionLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = {
                        snackbarData.performAction()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = parsed.type.contentColor()
                    )
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RideitTripFeedbackIcon(
    type: RideitTripFeedbackType
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_trip_feedback_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_trip_feedback_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                color = type.backgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer {
                    scaleX = if (type == RideitTripFeedbackType.LIVE) pulse else 1f
                    scaleY = if (type == RideitTripFeedbackType.LIVE) pulse else 1f
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
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun RideitInlineTripFeedbackBanner(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    type: RideitTripFeedbackType = RideitTripFeedbackType.INFO,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) +
                expandHorizontally(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(160)) +
                shrinkHorizontally(animationSpec = tween(180))
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = type.softBackgroundColor(),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(
                    width = 1.dp,
                    color = type.softBorderColor(),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitTripFeedbackMiniIcon(type = type)

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                color = Color(0xFF0F172A),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!actionText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = actionText,
                    color = type.contentColor(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun RideitTripFeedbackMiniIcon(
    type: RideitTripFeedbackType
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = type.backgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = type.dotColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.iconText(),
                color = Color.White,
                fontSize = 7.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private data class RideitFeedbackPayload(
    val type: RideitTripFeedbackType,
    val message: String
)

private fun String.toRideitFeedbackPayload(): RideitFeedbackPayload {
    val parts = split("|", limit = 2)

    if (parts.size != 2) {
        return RideitFeedbackPayload(
            type = RideitTripFeedbackType.INFO,
            message = this
        )
    }

    val type = runCatching {
        RideitTripFeedbackType.valueOf(parts[0])
    }.getOrDefault(RideitTripFeedbackType.INFO)

    return RideitFeedbackPayload(
        type = type,
        message = parts[1]
    )
}

private fun RideitTripFeedbackType.title(): String {
    return when (this) {
        RideitTripFeedbackType.SUCCESS -> "Success"
        RideitTripFeedbackType.ERROR -> "Action failed"
        RideitTripFeedbackType.WARNING -> "Ride update"
        RideitTripFeedbackType.INFO -> "Rideit"
        RideitTripFeedbackType.LIVE -> "Live update"
    }
}

private fun RideitTripFeedbackType.iconText(): String {
    return when (this) {
        RideitTripFeedbackType.SUCCESS -> "✓"
        RideitTripFeedbackType.ERROR -> "!"
        RideitTripFeedbackType.WARNING -> "!"
        RideitTripFeedbackType.INFO -> "i"
        RideitTripFeedbackType.LIVE -> "•"
    }
}

private fun RideitTripFeedbackType.backgroundColor(): Color {
    return when (this) {
        RideitTripFeedbackType.SUCCESS -> Color(0xFFDCFCE7)
        RideitTripFeedbackType.ERROR -> Color(0xFFFEE2E2)
        RideitTripFeedbackType.WARNING -> Color(0xFFFEF3C7)
        RideitTripFeedbackType.INFO -> Color(0xFFEFF6FF)
        RideitTripFeedbackType.LIVE -> Color(0xFFDCFCE7)
    }
}

private fun RideitTripFeedbackType.dotColor(): Color {
    return when (this) {
        RideitTripFeedbackType.SUCCESS -> Color(0xFF22C55E)
        RideitTripFeedbackType.ERROR -> Color(0xFFEF4444)
        RideitTripFeedbackType.WARNING -> Color(0xFFF59E0B)
        RideitTripFeedbackType.INFO -> Color(0xFF2563EB)
        RideitTripFeedbackType.LIVE -> Color(0xFF16A34A)
    }
}

private fun RideitTripFeedbackType.contentColor(): Color {
    return when (this) {
        RideitTripFeedbackType.SUCCESS -> Color(0xFF166534)
        RideitTripFeedbackType.ERROR -> Color(0xFFB91C1C)
        RideitTripFeedbackType.WARNING -> Color(0xFF92400E)
        RideitTripFeedbackType.INFO -> Color(0xFF2563EB)
        RideitTripFeedbackType.LIVE -> Color(0xFF166534)
    }
}

private fun RideitTripFeedbackType.softBackgroundColor(): Color {
    return when (this) {
        RideitTripFeedbackType.SUCCESS -> Color(0xFFF0FDF4)
        RideitTripFeedbackType.ERROR -> Color(0xFFFEF2F2)
        RideitTripFeedbackType.WARNING -> Color(0xFFFFFBEB)
        RideitTripFeedbackType.INFO -> Color(0xFFEFF6FF)
        RideitTripFeedbackType.LIVE -> Color(0xFFF0FDF4)
    }
}

private fun RideitTripFeedbackType.softBorderColor(): Color {
    return when (this) {
        RideitTripFeedbackType.SUCCESS -> Color(0xFFBBF7D0)
        RideitTripFeedbackType.ERROR -> Color(0xFFFCA5A5)
        RideitTripFeedbackType.WARNING -> Color(0xFFFDE68A)
        RideitTripFeedbackType.INFO -> Color(0xFFBFDBFE)
        RideitTripFeedbackType.LIVE -> Color(0xFFBBF7D0)
    }
}