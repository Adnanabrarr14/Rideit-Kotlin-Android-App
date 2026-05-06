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
import androidx.compose.ui.graphics.Brush
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(),
    onLogout: () -> Unit = {}
) {
    val uiState by mapViewModel.uiState.collectAsState()
    var showPanel by remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(
        uiState.pickupLatLng,
        uiState.dropoffLatLng,
        uiState.driverLatLng,
        uiState.routePoints,
        uiState.rideRequestStatus
    ) {
        delay(300)

        val builder = LatLngBounds.Builder()
        var hasPoint = false

        uiState.pickupLatLng?.let {
            builder.include(it)
            hasPoint = true
        }

        uiState.dropoffLatLng?.let {
            builder.include(it)
            hasPoint = true
        }

        uiState.driverLatLng?.let {
            builder.include(it)
            hasPoint = true
        }

        uiState.routePoints.forEach {
            builder.include(it)
            hasPoint = true
        }

        if (hasPoint) {
            try {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(builder.build(), 160),
                    durationMs = 900
                )
            } catch (_: Exception) {
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RideitDrawer(
                onClose = {
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onLogout()
                }
            )
        }
    ) {
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

            Button(
                onClick = {
                    scope.launch { drawerState.open() }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 46.dp, start = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111827),
                    contentColor = Color.White
                )
            ) {
                Text("☰", fontWeight = FontWeight.Bold)
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
}

@Composable
private fun RideitDrawer(
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 310.dp),
        drawerContainerColor = Color(0xFF111113)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF111113),
                            Color(0xFF1A1018),
                            Color(0xFF090909)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8A35F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Rideit",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Premium Ride App",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            DrawerItem("👤", "Profile", "Manage your account", onClose)
            DrawerItem("🧾", "Trip History", "View your previous rides", onClose)
            DrawerItem("💳", "Payment Methods", "Cards, cash and wallet", onClose)
            DrawerItem("⭐", "Ratings", "Rate your rides", onClose)
            DrawerItem("⚙️", "Settings", "App preferences", onClose)

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A1111)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚪", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Logout",
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1D1D21)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2138)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionSelected(suggestion) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", modifier = Modifier.width(36.dp))

                    Column {
                        Text(suggestion.title, fontWeight = FontWeight.Bold)

                        Text(
                            text = suggestion.fullAddress,
                            color = Color(0xFF777777),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(it, color = Color.Red)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onSearchClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = purple)
    ) {
        Text("Search Ride", fontWeight = FontWeight.Bold)
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
        colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
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

    Text(
        text = when (uiState.rideRequestStatus) {
            RideRequestStatus.SEARCHING_DRIVER -> "Finding your driver"
            RideRequestStatus.DRIVER_FOUND -> "Driver found"
            RideRequestStatus.DRIVER_ARRIVING -> "Driver is arriving"
            RideRequestStatus.RIDE_STARTED -> "Ride started"
            else -> "Ride status"
        },
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(12.dp))

    LinearProgressIndicator(
        progress = {
            when (uiState.rideRequestStatus) {
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
        color = statusColor,
        trackColor = Color(0xFFE5E7EB)
    )

    Spacer(modifier = Modifier.height(16.dp))

    uiState.driver?.let { driver ->
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF8FAFC)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👨")
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(driver.name, fontWeight = FontWeight.Bold)
                    Text("${driver.vehicleName} • ${driver.vehicleNumber}", color = Color.Gray)
                    Text("⭐ ${driver.rating} • ${driver.arrivalTime}", color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedButton(
        onClick = onCancelRide,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
    ) {
        Text("Cancel Ride", fontWeight = FontWeight.Bold)
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
        color = if (selected) lightColor else Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                    fontWeight = FontWeight.Bold,
                    color = if (selected) mainColor else Color.Black
                )

                Text(
                    text = option.subtitle,
                    color = Color.Gray,
                    maxLines = 1
                )

                Text(
                    text = "⏱ ${option.estimatedTime} • 👤 ${seatsForRide(option.title)} seats",
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = option.estimatedFare,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) mainColor else Color.Black
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

        drawCircle(
            color = Color(0xFF29245C),
            radius = h * 0.13f,
            center = Offset(w * 0.28f, h * 0.69f)
        )

        drawCircle(
            color = Color(0xFF29245C),
            radius = h * 0.13f,
            center = Offset(w * 0.70f, h * 0.69f)
        )
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