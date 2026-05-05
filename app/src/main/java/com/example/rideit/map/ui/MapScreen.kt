package com.example.rideit.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rideit.driver.ui.DriverFoundCard
import com.example.rideit.map.model.*
import com.example.rideit.map.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
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
                Polyline(points = uiState.routePoints, width = 8f)
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
                    RideStatusContent(
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
private fun RideStatusContent(
    uiState: MapUiState,
    onCancelRide: () -> Unit
) {
    Text(
        text = when (uiState.rideRequestStatus) {
            RideRequestStatus.SEARCHING_DRIVER -> "Finding your driver..."
            RideRequestStatus.DRIVER_FOUND -> "Driver found"
            RideRequestStatus.DRIVER_ARRIVING -> "Driver is arriving"
            RideRequestStatus.RIDE_STARTED -> "Ride started"
            else -> "Ride status"
        },
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(12.dp))

    uiState.rideConfirmedMessage?.let {
        Text(text = it, color = Color(0xFF666666))
    }

    Spacer(modifier = Modifier.height(12.dp))

    uiState.driver?.let {
        DriverFoundCard(
            driver = it,
            onCancelClick = onCancelRide
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = onCancelRide,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEF4444),
            contentColor = Color.White
        )
    ) {
        Text("Cancel Ride")
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