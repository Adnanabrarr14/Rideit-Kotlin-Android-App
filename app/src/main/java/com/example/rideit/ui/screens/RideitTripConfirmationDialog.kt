package com.example.rideit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitTripDialogType {
    CANCEL_RIDE,
    START_TRIP,
    COMPLETE_RIDE,
    ARRIVED_PICKUP,
    ACCEPT_RIDE,
    CUSTOM
}

@Composable
fun RideitTripConfirmationDialog(
    visible: Boolean,
    type: RideitTripDialogType,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    confirmText: String? = null,
    dismissText: String = "Not Now",
    loading: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!visible) return

    Dialog(
        onDismissRequest = {
            if (!loading) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress && !loading,
            dismissOnClickOutside = dismissOnClickOutside && !loading
        )
    ) {
        RideitTripConfirmationDialogContent(
            modifier = modifier,
            type = type,
            title = title ?: type.defaultTitle(),
            message = message ?: type.defaultMessage(),
            confirmText = confirmText ?: type.defaultConfirmText(),
            dismissText = dismissText,
            loading = loading,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun RideitTripConfirmationDialogContent(
    modifier: Modifier = Modifier,
    type: RideitTripDialogType,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val danger = type == RideitTripDialogType.CANCEL_RIDE
    val success = type == RideitTripDialogType.COMPLETE_RIDE ||
            type == RideitTripDialogType.ACCEPT_RIDE

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(34.dp),
                spotColor = Color.Black.copy(alpha = 0.28f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color.White.copy(alpha = 0.24f)
                    )
                ),
                shape = RoundedCornerShape(34.dp)
            ),
        shape = RoundedCornerShape(34.dp),
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
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitTripDialogIcon(
                type = type,
                loading = loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    if (!loading) onConfirm()
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(19.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        danger -> Color(0xFFEF4444)
                        success -> Color(0xFF16A34A)
                        else -> Color(0xFF0F172A)
                    },
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
                        text = "Please wait...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Text(
                        text = confirmText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    if (!loading) onDismiss()
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = dismissText,
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun RideitTripDialogIcon(
    type: RideitTripDialogType,
    loading: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_trip_dialog_icon_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_trip_dialog_icon_pulse_value"
    )

    val backgroundColor = when (type) {
        RideitTripDialogType.CANCEL_RIDE -> Color(0xFFFEE2E2)
        RideitTripDialogType.START_TRIP -> Color(0xFFEFF6FF)
        RideitTripDialogType.COMPLETE_RIDE -> Color(0xFFDCFCE7)
        RideitTripDialogType.ARRIVED_PICKUP -> Color(0xFFFEF3C7)
        RideitTripDialogType.ACCEPT_RIDE -> Color(0xFFDCFCE7)
        RideitTripDialogType.CUSTOM -> Color(0xFFEFF6FF)
    }

    val dotColor = when (type) {
        RideitTripDialogType.CANCEL_RIDE -> Color(0xFFEF4444)
        RideitTripDialogType.START_TRIP -> Color(0xFF2563EB)
        RideitTripDialogType.COMPLETE_RIDE -> Color(0xFF22C55E)
        RideitTripDialogType.ARRIVED_PICKUP -> Color(0xFFF59E0B)
        RideitTripDialogType.ACCEPT_RIDE -> Color(0xFF16A34A)
        RideitTripDialogType.CUSTOM -> Color(0xFF2563EB)
    }

    Box(
        modifier = Modifier
            .size(68.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp,
                color = dotColor
            )
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .background(
                        color = dotColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.iconText(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun RideitCancelRideConfirmationDialog(
    visible: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmCancel: () -> Unit
) {
    RideitTripConfirmationDialog(
        visible = visible,
        type = RideitTripDialogType.CANCEL_RIDE,
        modifier = modifier,
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirmCancel
    )
}

@Composable
fun RideitCompleteRideConfirmationDialog(
    visible: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmComplete: () -> Unit
) {
    RideitTripConfirmationDialog(
        visible = visible,
        type = RideitTripDialogType.COMPLETE_RIDE,
        modifier = modifier,
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirmComplete
    )
}

@Composable
fun RideitStartTripConfirmationDialog(
    visible: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmStart: () -> Unit
) {
    RideitTripConfirmationDialog(
        visible = visible,
        type = RideitTripDialogType.START_TRIP,
        modifier = modifier,
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirmStart
    )
}

@Composable
fun RideitArrivedPickupConfirmationDialog(
    visible: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmArrived: () -> Unit
) {
    RideitTripConfirmationDialog(
        visible = visible,
        type = RideitTripDialogType.ARRIVED_PICKUP,
        modifier = modifier,
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirmArrived
    )
}

@Composable
fun RideitAcceptRideConfirmationDialog(
    visible: Boolean,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmAccept: () -> Unit
) {
    RideitTripConfirmationDialog(
        visible = visible,
        type = RideitTripDialogType.ACCEPT_RIDE,
        modifier = modifier,
        loading = loading,
        onDismiss = onDismiss,
        onConfirm = onConfirmAccept
    )
}

private fun RideitTripDialogType.defaultTitle(): String {
    return when (this) {
        RideitTripDialogType.CANCEL_RIDE -> "Cancel this ride?"
        RideitTripDialogType.START_TRIP -> "Start this trip?"
        RideitTripDialogType.COMPLETE_RIDE -> "Complete this ride?"
        RideitTripDialogType.ARRIVED_PICKUP -> "Mark as arrived?"
        RideitTripDialogType.ACCEPT_RIDE -> "Accept this ride?"
        RideitTripDialogType.CUSTOM -> "Confirm action"
    }
}

private fun RideitTripDialogType.defaultMessage(): String {
    return when (this) {
        RideitTripDialogType.CANCEL_RIDE -> "This ride will be cancelled and both rider and driver screens will update."
        RideitTripDialogType.START_TRIP -> "The ride status will move to trip in progress."
        RideitTripDialogType.COMPLETE_RIDE -> "This will finish the ride and continue to receipt or rating flow."
        RideitTripDialogType.ARRIVED_PICKUP -> "The rider will know that you have reached the pickup location."
        RideitTripDialogType.ACCEPT_RIDE -> "This ride will be assigned to you and removed from open requests."
        RideitTripDialogType.CUSTOM -> "Please confirm before continuing."
    }
}

private fun RideitTripDialogType.defaultConfirmText(): String {
    return when (this) {
        RideitTripDialogType.CANCEL_RIDE -> "Yes, cancel ride"
        RideitTripDialogType.START_TRIP -> "Start trip"
        RideitTripDialogType.COMPLETE_RIDE -> "Complete ride"
        RideitTripDialogType.ARRIVED_PICKUP -> "I have arrived"
        RideitTripDialogType.ACCEPT_RIDE -> "Accept ride"
        RideitTripDialogType.CUSTOM -> "Confirm"
    }
}

private fun RideitTripDialogType.iconText(): String {
    return when (this) {
        RideitTripDialogType.CANCEL_RIDE -> "!"
        RideitTripDialogType.START_TRIP -> "▶"
        RideitTripDialogType.COMPLETE_RIDE -> "✓"
        RideitTripDialogType.ARRIVED_PICKUP -> "•"
        RideitTripDialogType.ACCEPT_RIDE -> "✓"
        RideitTripDialogType.CUSTOM -> "?"
    }
}
