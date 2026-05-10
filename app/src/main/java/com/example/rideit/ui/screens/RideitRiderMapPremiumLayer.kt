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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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

@Immutable
data class RideitRiderMapPremiumUiState(
    val pickupText: String = "",
    val destinationText: String = "",
    val distanceText: String = "",
    val durationText: String = "",
    val etaText: String = "",
    val fareText: String = "",
    val paymentMethodText: String = "Cash",
    val hasLocationPermission: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val isFindingLocation: Boolean = false,
    val isBookingRide: Boolean = false,
    val isRouteVisible: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val showTopChrome: Boolean = true,
    val showMapControls: Boolean = true,
    val showRouteFloatingBar: Boolean = true,
    val showLocationCard: Boolean = false,
    val showPaymentSummary: Boolean = true,
    val errorMessage: String? = null,
    val selectedVehicleId: String = "rideit_go",
    val selectedPaymentMethodId: String = "cash"
)

@Composable
fun BoxScope.RideitRiderMapPremiumLayer(
    state: RideitRiderMapPremiumUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    onMenuClick: () -> Unit = {},
    onSavedPlacesClick: () -> Unit = {},
    onLocationStatusClick: () -> Unit = {},
    onGrantPermissionClick: () -> Unit = {},
    onEnableGpsClick: () -> Unit = {},
    onRetryLocationClick: () -> Unit = {},
    onManualLocationClick: () -> Unit = {},
    onRecenterClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onTrafficClick: () -> Unit = {},
    onLayersClick: () -> Unit = {},
    onClearRouteClick: () -> Unit = {},
    onRoutePreviewClick: () -> Unit = {},
    onVehicleClick: (String) -> Unit = {},
    onBookClick: () -> Unit = {},
    onChangeRouteClick: () -> Unit = {},
    onPaymentMethodClick: (String) -> Unit = {},
    onPaymentSummaryClick: () -> Unit = {},
    onErrorRetryClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = state.showTopChrome,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
        ) {
            RideitPremiumMapTopBar(
                pickupText = state.pickupText.ifBlank { "Premium rider map" },
                isFindingLocation = state.isFindingLocation,
                hasLocationPermission = state.hasLocationPermission,
                isGpsEnabled = state.isGpsEnabled,
                onMenuClick = onMenuClick,
                onLocationClick = onLocationStatusClick,
                onSavedPlacesClick = onSavedPlacesClick
            )
        }

        AnimatedVisibility(
            visible = !state.errorMessage.isNullOrBlank(),
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
        ) {
            RideitPremiumInlineErrorBanner(
                message = state.errorMessage ?: "Something went wrong.",
                onRetryClick = onErrorRetryClick
            )
        }

        AnimatedVisibility(
            visible = state.showLocationCard || !state.hasLocationPermission || !state.isGpsEnabled,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
        ) {
            RideitPremiumLocationCard(
                pickupText = state.pickupText,
                hasLocationPermission = state.hasLocationPermission,
                isGpsEnabled = state.isGpsEnabled,
                isFindingLocation = state.isFindingLocation,
                onGrantPermissionClick = onGrantPermissionClick,
                onEnableGpsClick = onEnableGpsClick,
                onRetryLocationClick = onRetryLocationClick,
                onManualLocationClick = onManualLocationClick
            )
        }
    }

    AnimatedVisibility(
        visible = state.showMapControls,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(160))
    ) {
        RideitPremiumMapControls(
            isFindingLocation = state.isFindingLocation,
            isTrafficEnabled = state.isTrafficEnabled,
            isRouteVisible = state.isRouteVisible,
            onRecenterClick = onRecenterClick,
            onLocationClick = onLocationClick,
            onTrafficClick = onTrafficClick,
            onLayersClick = onLayersClick,
            onClearRouteClick = onClearRouteClick
        )
    }

    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = state.showRouteFloatingBar && state.isRouteVisible,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
        ) {
            RideitPremiumRouteFloatingBar(
                distanceText = state.distanceText,
                durationText = state.durationText,
                fareText = state.fareText,
                onPreviewClick = onRoutePreviewClick,
                onClearClick = onClearRouteClick
            )
        }

        AnimatedVisibility(
            visible = state.showPaymentSummary,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
        ) {
            RideitPremiumPaymentSummaryCard(
                fareText = state.fareText.ifBlank { "Rs 0" },
                paymentMethodText = state.paymentMethodText.ifBlank { "Cash" },
                onClick = onPaymentSummaryClick
            )
        }

        AnimatedVisibility(
            visible = state.pickupText.isNotBlank() && state.destinationText.isNotBlank(),
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(260)),
            exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
        ) {
            RideitPremiumBookingPanel(
                pickupText = state.pickupText,
                destinationText = state.destinationText,
                distanceText = state.distanceText,
                durationText = state.durationText,
                etaText = state.etaText,
                fareText = state.fareText,
                paymentMethodText = state.paymentMethodText,
                selectedVehicleId = state.selectedVehicleId,
                selectedPaymentMethodId = state.selectedPaymentMethodId,
                isBookingRide = state.isBookingRide,
                onVehicleClick = onVehicleClick,
                onPaymentMethodClick = onPaymentMethodClick,
                onBookClick = onBookClick,
                onChangeRouteClick = onChangeRouteClick
            )
        }
    }
}

@Composable
fun RideitRiderMapPremiumBottomTools(
    state: RideitRiderMapPremiumUiState,
    modifier: Modifier = Modifier,
    onPaymentMethodClick: (String) -> Unit = {},
    onPaymentSummaryClick: () -> Unit = {},
    onSavedPlacesClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RideitPremiumPaymentMethodSelector(
            selectedPaymentMethodId = state.selectedPaymentMethodId,
            onPaymentMethodClick = onPaymentMethodClick,
            onAddPaymentClick = onPaymentSummaryClick
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitPremiumSmallInfoBanner(
                title = "Location ready",
                message = state.pickupText.ifBlank { "Pickup ready" },
                modifier = Modifier.weight(1f),
                onClick = onSavedPlacesClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            RideitPremiumStatusChip(
                text = if (state.errorMessage.isNullOrBlank()) "Ready" else "Issue",
                success = state.errorMessage.isNullOrBlank()
            )
        }
    }
}

@Composable
private fun RideitPremiumMapTopBar(
    pickupText: String,
    isFindingLocation: Boolean,
    hasLocationPermission: Boolean,
    isGpsEnabled: Boolean,
    onMenuClick: () -> Unit,
    onLocationClick: () -> Unit,
    onSavedPlacesClick: () -> Unit
) {
    RideitPremiumCard(
        corner = 26
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitPulseIcon(
                text = "☰",
                color = Color(0xFF2563EB),
                softColor = Color(0xFFEFF6FF),
                pulse = false,
                onClick = onMenuClick
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "Rideit",
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = pickupText,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            RideitMiniStatusChip(
                text = when {
                    !hasLocationPermission -> "Permission"
                    !isGpsEnabled -> "GPS off"
                    isFindingLocation -> "Finding"
                    else -> "Ready"
                },
                color = when {
                    !hasLocationPermission -> Color(0xFF2563EB)
                    !isGpsEnabled -> Color(0xFFF59E0B)
                    isFindingLocation -> Color(0xFF2563EB)
                    else -> Color(0xFF16A34A)
                },
                softColor = when {
                    !hasLocationPermission -> Color(0xFFEFF6FF)
                    !isGpsEnabled -> Color(0xFFFEF3C7)
                    isFindingLocation -> Color(0xFFEFF6FF)
                    else -> Color(0xFFDCFCE7)
                },
                onClick = onLocationClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            RideitPulseIcon(
                text = "★",
                color = Color(0xFF7C3AED),
                softColor = Color(0xFFF3E8FF),
                pulse = false,
                onClick = onSavedPlacesClick
            )
        }
    }
}

@Composable
private fun RideitPremiumInlineErrorBanner(
    message: String,
    onRetryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFEE2E2),
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFFCA5A5),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onRetryClick() }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitPulseIcon(
            text = "!",
            color = Color(0xFFEF4444),
            softColor = Color(0xFFFEE2E2),
            pulse = true,
            onClick = onRetryClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 11.dp)
        ) {
            Text(
                text = "Rideit needs attention",
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            Text(
                text = message,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Text(
            text = "Retry",
            color = Color(0xFFB91C1C),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(50))
                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(50))
                .padding(horizontal = 9.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun RideitPremiumLocationCard(
    pickupText: String,
    hasLocationPermission: Boolean,
    isGpsEnabled: Boolean,
    isFindingLocation: Boolean,
    onGrantPermissionClick: () -> Unit,
    onEnableGpsClick: () -> Unit,
    onRetryLocationClick: () -> Unit,
    onManualLocationClick: () -> Unit
) {
    val title = when {
        !hasLocationPermission -> "Allow location access"
        !isGpsEnabled -> "Turn on GPS"
        isFindingLocation -> "Finding your location"
        else -> "Pickup location ready"
    }

    val message = when {
        !hasLocationPermission -> "Rideit needs location permission for pickup, nearby drivers, and map movement."
        !isGpsEnabled -> "GPS is off. Enable location services for accurate pickup."
        isFindingLocation -> "Rideit is checking your current location."
        else -> pickupText.ifBlank { "Your pickup is ready for booking." }
    }

    val primaryText = when {
        !hasLocationPermission -> "Allow location"
        !isGpsEnabled -> "Turn on GPS"
        isFindingLocation -> "Checking..."
        else -> "Refresh location"
    }

    val color = when {
        !hasLocationPermission -> Color(0xFF2563EB)
        !isGpsEnabled -> Color(0xFFF59E0B)
        isFindingLocation -> Color(0xFF2563EB)
        else -> Color(0xFF16A34A)
    }

    val softColor = when {
        !hasLocationPermission -> Color(0xFFEFF6FF)
        !isGpsEnabled -> Color(0xFFFEF3C7)
        isFindingLocation -> Color(0xFFEFF6FF)
        else -> Color(0xFFDCFCE7)
    }

    RideitPremiumCard(corner = 28) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitPulseIcon(
                text = when {
                    isFindingLocation -> "•"
                    !isGpsEnabled -> "!"
                    !hasLocationPermission -> "L"
                    else -> "✓"
                },
                color = color,
                softColor = softColor,
                pulse = isFindingLocation || !hasLocationPermission || !isGpsEnabled,
                large = true
            )

            Spacer(modifier = Modifier.height(13.dp))

            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = message,
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 6.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        !hasLocationPermission -> onGrantPermissionClick()
                        !isGpsEnabled -> onEnableGpsClick()
                        else -> onRetryLocationClick()
                    }
                },
                enabled = !isFindingLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                if (isFindingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = primaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            TextButton(
                onClick = onManualLocationClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Enter manually",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun RideitPremiumMapControls(
    isFindingLocation: Boolean,
    isTrafficEnabled: Boolean,
    isRouteVisible: Boolean,
    onRecenterClick: () -> Unit,
    onLocationClick: () -> Unit,
    onTrafficClick: () -> Unit,
    onLayersClick: () -> Unit,
    onClearRouteClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ) {
        RideitFloatingControlButton(
            text = "⌖",
            color = Color(0xFF2563EB),
            softColor = Color(0xFFEFF6FF),
            onClick = onRecenterClick
        )

        RideitFloatingControlButton(
            text = if (isFindingLocation) "•" else "L",
            color = Color(0xFF16A34A),
            softColor = Color(0xFFDCFCE7),
            loading = isFindingLocation,
            onClick = onLocationClick
        )

        RideitFloatingControlButton(
            text = "T",
            color = if (isTrafficEnabled) Color(0xFFF59E0B) else Color(0xFF64748B),
            softColor = if (isTrafficEnabled) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
            onClick = onTrafficClick
        )

        RideitFloatingControlButton(
            text = "▣",
            color = Color(0xFF7C3AED),
            softColor = Color(0xFFF3E8FF),
            onClick = onLayersClick
        )

        if (isRouteVisible) {
            RideitFloatingControlButton(
                text = "×",
                color = Color(0xFFEF4444),
                softColor = Color(0xFFFEE2E2),
                onClick = onClearRouteClick
            )
        }
    }
}

@Composable
private fun RideitPremiumRouteFloatingBar(
    distanceText: String,
    durationText: String,
    fareText: String,
    onPreviewClick: () -> Unit,
    onClearClick: () -> Unit
) {
    RideitPremiumCard(
        corner = 28,
        modifier = Modifier.clickable { onPreviewClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitPulseIcon(
                text = "R",
                color = Color(0xFF16A34A),
                softColor = Color(0xFFDCFCE7),
                pulse = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = listOf(distanceText, durationText)
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                        .ifBlank { "Route selected" },
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = fareText.ifBlank { "Tap to preview fare and ride options" },
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            RideitFloatingControlButton(
                text = "×",
                color = Color(0xFFEF4444),
                softColor = Color(0xFFFEE2E2),
                onClick = onClearClick
            )
        }
    }
}

@Composable
private fun RideitPremiumPaymentSummaryCard(
    fareText: String,
    paymentMethodText: String,
    onClick: () -> Unit
) {
    RideitPremiumCard(
        corner = 24,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitPulseIcon(
                text = "Rs",
                color = Color(0xFF16A34A),
                softColor = Color(0xFFDCFCE7),
                pulse = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "Payment summary",
                    color = Color(0xFF0F172A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = paymentMethodText,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = fareText,
                color = Color(0xFF166534),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RideitPremiumBookingPanel(
    pickupText: String,
    destinationText: String,
    distanceText: String,
    durationText: String,
    etaText: String,
    fareText: String,
    paymentMethodText: String,
    selectedVehicleId: String,
    selectedPaymentMethodId: String,
    isBookingRide: Boolean,
    onVehicleClick: (String) -> Unit,
    onPaymentMethodClick: (String) -> Unit,
    onBookClick: () -> Unit,
    onChangeRouteClick: () -> Unit
) {
    RideitPremiumCard(corner = 34) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitPulseIcon(
                    text = "R",
                    color = Color(0xFF2563EB),
                    softColor = Color(0xFFEFF6FF),
                    pulse = true
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "Rideit route preview",
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )

                    Text(
                        text = listOf(distanceText, durationText, etaText)
                            .filter { it.isNotBlank() }
                            .joinToString(" • ")
                            .ifBlank { "Confirm pickup and destination" },
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(
                    text = fareText.ifBlank { "Rs 0" },
                    color = Color(0xFF166534),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            RideitRouteLine(
                title = "Pickup",
                value = pickupText,
                color = Color(0xFF16A34A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            RideitRouteLine(
                title = "Destination",
                value = destinationText,
                color = Color(0xFFEF4444)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RideitSelectableMiniCard(
                    title = "Rideit Go",
                    subtitle = "Affordable",
                    selected = selectedVehicleId == "rideit_go",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onVehicleClick("rideit_go")
                    }
                )

                RideitSelectableMiniCard(
                    title = "Comfort",
                    subtitle = "Premium",
                    selected = selectedVehicleId == "comfort",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onVehicleClick("comfort")
                    }
                )

                RideitSelectableMiniCard(
                    title = "XL",
                    subtitle = "Large",
                    selected = selectedVehicleId == "xl",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onVehicleClick("xl")
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            RideitSelectableMiniCard(
                title = paymentMethodText.ifBlank { "Cash" },
                subtitle = "Payment method",
                selected = selectedPaymentMethodId == "cash",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onPaymentMethodClick(selectedPaymentMethodId.ifBlank { "cash" })
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onBookClick,
                enabled = !isBookingRide,
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
                if (isBookingRide) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Booking...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Text(
                        text = "Book Rideit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            TextButton(
                onClick = onChangeRouteClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Change pickup or destination",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun RideitPremiumPaymentMethodSelector(
    selectedPaymentMethodId: String,
    onPaymentMethodClick: (String) -> Unit,
    onAddPaymentClick: () -> Unit
) {
    RideitPremiumCard(corner = 24) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = "Payment method",
                color = Color(0xFF0F172A),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Choose how you want to pay",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RideitSelectableMiniCard(
                    title = "Cash",
                    subtitle = "Driver",
                    selected = selectedPaymentMethodId == "cash",
                    modifier = Modifier.weight(1f),
                    onClick = { onPaymentMethodClick("cash") }
                )

                RideitSelectableMiniCard(
                    title = "Card",
                    subtitle = "Soon",
                    selected = selectedPaymentMethodId == "card",
                    modifier = Modifier.weight(1f),
                    onClick = { onPaymentMethodClick("card") }
                )

                RideitSelectableMiniCard(
                    title = "Wallet",
                    subtitle = "Soon",
                    selected = selectedPaymentMethodId == "wallet",
                    modifier = Modifier.weight(1f),
                    onClick = { onPaymentMethodClick("wallet") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Add payment method",
                color = Color(0xFF2563EB),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(50))
                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(50))
                    .clickable { onAddPaymentClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun RideitPremiumSmallInfoBanner(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .background(Color(0xFFDCFCE7), RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color(0xFF16A34A), CircleShape)
        )

        Column(
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )

            Text(
                text = message,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RideitPremiumStatusChip(
    text: String,
    success: Boolean
) {
    Row(
        modifier = Modifier
            .background(
                color = if (success) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = if (success) Color(0xFFBBF7D0) else Color(0xFFFCA5A5),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(
                    color = if (success) Color(0xFF16A34A) else Color(0xFFEF4444),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = text,
            color = if (success) Color(0xFF166534) else Color(0xFFB91C1C),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RideitRouteLine(
    title: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .background(color, CircleShape)
        )

        Column(
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value.ifBlank { "Not selected" },
                color = Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun RideitSelectableMiniCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = if (selected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color(0xFFBFDBFE) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = if (selected) Color(0xFF2563EB) else Color(0xFF0F172A),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun RideitFloatingControlButton(
    text: String,
    color: Color,
    softColor: Color,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.14f)
            )
            .background(Color.White.copy(alpha = 0.96f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.72f), CircleShape)
            .clickable(enabled = !loading) { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.dp,
                color = color
            )
        } else {
            RideitPulseIcon(
                text = text,
                color = color,
                softColor = softColor,
                pulse = false,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun RideitPulseIcon(
    text: String,
    color: Color,
    softColor: Color,
    pulse: Boolean,
    large: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_premium_layer_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_premium_layer_pulse_value"
    )

    val outerSize = if (large) 58.dp else 34.dp
    val innerSize = if (large) 26.dp else 15.dp

    Box(
        modifier = Modifier
            .size(outerSize)
            .background(softColor, CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .graphicsLayer {
                    scaleX = if (pulse) scale else 1f
                    scaleY = if (pulse) scale else 1f
                }
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = if (large) 11.sp else 8.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RideitMiniStatusChip(
    text: String,
    color: Color,
    softColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(softColor, RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitPremiumCard(
    modifier: Modifier = Modifier,
    corner: Int = 28,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(corner.dp),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color.White.copy(alpha = 0.22f)
                    )
                ),
                shape = RoundedCornerShape(corner.dp)
            ),
        shape = RoundedCornerShape(corner.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFF8FAFC))
                )
            )
        ) {
            content()
        }
    }
}