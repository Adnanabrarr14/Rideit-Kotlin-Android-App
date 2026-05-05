package com.example.rideit.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rideit.map.model.LocationSuggestion
import com.example.rideit.map.model.MapUiState
import com.example.rideit.map.model.RideOption
import com.example.rideit.map.model.RideRequestStatus
import com.example.rideit.map.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by mapViewModel.uiState.collectAsState()
    var showPanel by remember { mutableStateOf(true) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(33.6844, 73.0479), 14f)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving) {
            showPanel = false
        } else {
            delay(500)
            showPanel = true
        }
    }

    LaunchedEffect(uiState.dropoffLatLng) {
        uiState.dropoffLatLng?.let {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(it, 15.5f),
                durationMs = 800
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            uiState.pickupLatLng?.let {
                Marker(state = MarkerState(it), title = "Pickup")
            }

            uiState.dropoffLatLng?.let {
                Marker(state = MarkerState(it), title = "Dropoff")
            }

            uiState.driverLatLng?.let {
                Marker(
                    state = MarkerState(it),
                    title = "Driver",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            if (uiState.routePoints.size >= 2) {
                Polyline(
                    points = uiState.routePoints,
                    width = 8f,
                    color = Color(0xFF8A35F2)
                )
            }
        }

        AnimatedVisibility(
            visible = showPanel,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            RideitBottomPanel(
                uiState = uiState,
                mapViewModel = mapViewModel
            )
        }
    }
}

@Composable
private fun RideitBottomPanel(
    uiState: MapUiState,
    mapViewModel: MapViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 18.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD1D5DB))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER ||
                        uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND ||
                        uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING ||
                        uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED -> {
                    PremiumRideStatusContent(
                        uiState = uiState,
                        onCancelRide = mapViewModel::onCancelRideClicked
                    )
                }

                uiState.showRideOptions && uiState.rideOptions.isNotEmpty() -> {
                    RideSelectionContent(
                        uiState = uiState,
                        onRideSelected = mapViewModel::onRideOptionSelected,
                        onConfirmRide = mapViewModel::onConfirmRideClicked
                    )
                }

                else -> {
                    SearchContent(
                        uiState = uiState,
                        onPickupChanged = mapViewModel::onPickupTextChanged,
                        onDropoffChanged = mapViewModel::onDropoffTextChanged,
                        onSuggestionSelected = mapViewModel::onSuggestionSelected,
                        onSearchClick = mapViewModel::onSearchClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchContent(
    uiState: MapUiState,
    onPickupChanged: (String) -> Unit,
    onDropoffChanged: (String) -> Unit,
    onSuggestionSelected: (LocationSuggestion) -> Unit,
    onSearchClick: () -> Unit
) {
    val purple = Color(0xFF8A35F2)

    Text(
        text = "Where are you going?",
        color = Color(0xFF111827),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(18.dp))

    OutlinedTextField(
        value = uiState.pickupText,
        onValueChange = onPickupChanged,
        label = { Text("Pickup location") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = uiState.dropoffText,
        onValueChange = onDropoffChanged,
        label = { Text("Dropoff location") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = purple,
            focusedLabelColor = purple
        )
    )

    if (uiState.locationSuggestions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.heightIn(max = 190.dp)) {
            items(uiState.locationSuggestions) { suggestion ->
                SuggestionRow(
                    suggestion = suggestion,
                    onClick = { onSuggestionSelected(suggestion) }
                )
            }
        }
    }

    uiState.errorMessage?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = it, color = Color.Red)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onSearchClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = purple,
            contentColor = Color.White
        )
    ) {
        Text("Search Ride", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SuggestionRow(
    suggestion: LocationSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("📍", modifier = Modifier.width(36.dp))

        Column {
            Text(
                text = suggestion.title,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold
            )

            Text(
                text = suggestion.fullAddress,
                color = Color(0xFF777777),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RideSelectionContent(
    uiState: MapUiState,
    onRideSelected: (RideOption) -> Unit,
    onConfirmRide: () -> Unit
) {
    val selectedRide = uiState.selectedRideOption ?: uiState.rideOptions.first()
    val selectedColor = rideColor(selectedRide.title)

    Text(
        text = "Choose your ride",
        color = Color(0xFF111827),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(12.dp))

    LazyColumn(
        modifier = Modifier.heightIn(max = 330.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(uiState.rideOptions) { option ->
            CompactRideOptionCard(
                option = option,
                selected = option == selectedRide,
                onClick = { onRideSelected(option) }
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Button(
        onClick = onConfirmRide,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = selectedColor,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Book ${selectedRide.title} — ${selectedRide.estimatedFare}",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PremiumRideStatusContent(
    uiState: MapUiState,
    onCancelRide: () -> Unit
) {
    val statusColor = when (uiState.rideRequestStatus) {
        RideRequestStatus.SEARCHING_DRIVER -> Color(0xFF8A35F2)
        RideRequestStatus.DRIVER_FOUND -> Color(0xFF2563EB)
        RideRequestStatus.DRIVER_ARRIVING -> Color(0xFFE17A00)
        RideRequestStatus.RIDE_STARTED -> Color(0xFF16A34A)
        else -> Color(0xFF8A35F2)
    }

    val title = when (uiState.rideRequestStatus) {
        RideRequestStatus.SEARCHING_DRIVER -> "Finding your driver"
        RideRequestStatus.DRIVER_FOUND -> "Driver found"
        RideRequestStatus.DRIVER_ARRIVING -> "Driver is arriving"
        RideRequestStatus.RIDE_STARTED -> "Ride started"
        else -> "Ride status"
    }

    val subtitle = when (uiState.rideRequestStatus) {
        RideRequestStatus.SEARCHING_DRIVER -> "We are matching you with a nearby driver."
        RideRequestStatus.DRIVER_FOUND -> "Your driver accepted the ride."
        RideRequestStatus.DRIVER_ARRIVING -> "Driver is moving toward your pickup point."
        RideRequestStatus.RIDE_STARTED -> "Enjoy your ride with Rideit."
        else -> uiState.rideConfirmedMessage ?: ""
    }

    Text(
        text = title,
        color = Color(0xFF111827),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = subtitle,
        color = Color(0xFF777777),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

    RideProgressHeader(
        status = uiState.rideRequestStatus,
        color = statusColor
    )

    Spacer(modifier = Modifier.height(16.dp))

    uiState.driver?.let { driver ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF8FAFC),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DriverAvatar(color = statusColor)

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driver.name,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "${driver.vehicleName} • ${driver.vehicleNumber}",
                        color = Color(0xFF777777),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "⭐ ${driver.rating} • Arrives in ${driver.arrivalTime}",
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("☎", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
    }

    uiState.rideConfirmedMessage?.let {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = statusColor.copy(alpha = 0.10f)
        ) {
            Text(
                text = it,
                modifier = Modifier.padding(14.dp),
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
    }

    OutlinedButton(
        onClick = onCancelRide,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFFEF4444)
        )
    ) {
        Text("Cancel Ride", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RideProgressHeader(
    status: RideRequestStatus,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ride_loading")
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF7F2FF)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusDot(
                    active = true,
                    color = color,
                    alpha = if (status == RideRequestStatus.SEARCHING_DRIVER) animatedAlpha else 1f
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = statusText(status),
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = {
                    when (status) {
                        RideRequestStatus.SEARCHING_DRIVER -> 0.25f
                        RideRequestStatus.DRIVER_FOUND -> 0.50f
                        RideRequestStatus.DRIVER_ARRIVING -> 0.78f
                        RideRequestStatus.RIDE_STARTED -> 1f
                        else -> 0f
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = color,
                trackColor = Color(0xFFE5E7EB)
            )
        }
    }
}

@Composable
private fun StatusDot(
    active: Boolean,
    color: Color,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(if (active) color else Color(0xFFD1D5DB))
    )
}

@Composable
private fun DriverAvatar(
    color: Color
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👨",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

private fun statusText(status: RideRequestStatus): String {
    return when (status) {
        RideRequestStatus.SEARCHING_DRIVER -> "Searching nearby drivers"
        RideRequestStatus.DRIVER_FOUND -> "Driver confirmed"
        RideRequestStatus.DRIVER_ARRIVING -> "Driver approaching pickup"
        RideRequestStatus.RIDE_STARTED -> "Ride in progress"
        else -> "Preparing ride"
    }
}

@Composable
private fun CompactRideOptionCard(
    option: RideOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val mainColor = rideColor(option.title)
    val lightColor = rideLightColor(option.title)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .clickable { onClick() }
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) mainColor else Color(0xFFE7E7E7),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) lightColor else Color.White,
        shadowElevation = if (selected) 7.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VehicleDrawing(
                color = if (selected) mainColor else Color(0xFFB8B8B8),
                modifier = Modifier
                    .width(92.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    color = if (selected) mainColor else Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = option.subtitle,
                    color = Color(0xFF8A8A8A),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )

                Text(
                    text = "⏱ ${option.estimatedTime} • 👤 ${seatsForRide(option.title)} seats",
                    color = Color(0xFF999999),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = option.estimatedFare,
                    color = if (selected) mainColor else Color(0xFF111827),
                    fontWeight = FontWeight.Bold
                )

                if (selected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(mainColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleDrawing(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.12f, h * 0.40f),
            size = Size(w * 0.74f, h * 0.25f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.30f, h * 0.24f),
            size = Size(w * 0.36f, h * 0.25f),
            cornerRadius = CornerRadius(14f, 14f)
        )

        drawCircle(Color(0xFF29245C), h * 0.13f, Offset(w * 0.28f, h * 0.69f))
        drawCircle(Color(0xFF29245C), h * 0.13f, Offset(w * 0.70f, h * 0.69f))
    }
}

private fun rideColor(title: String): Color {
    return when (title.lowercase()) {
        "mini" -> Color(0xFF8A35F2)
        "comfort" -> Color(0xFF2563EB)
        "business" -> Color(0xFFE17A00)
        else -> Color(0xFF8A35F2)
    }
}

private fun rideLightColor(title: String): Color {
    return when (title.lowercase()) {
        "mini" -> Color(0xFFF1E8FF)
        "comfort" -> Color(0xFFE4F0FF)
        "business" -> Color(0xFFFFF1C9)
        else -> Color(0xFFF1E8FF)
    }
}

private fun seatsForRide(title: String): Int {
    return when (title.lowercase()) {
        "mini" -> 3
        "comfort" -> 4
        "business" -> 6
        else -> 4
    }
}