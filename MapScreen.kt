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
import com.example.rideit.map.model.*
import com.example.rideit.map.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.components.PremiumDriverMiniCard
import ui.components.PremiumMapPolishLayer
import ui.components.PremiumRideCompletionSheet
import ui.components.PremiumRouteSummaryCard
import ui.components.PremiumSafetyQuickActions
import ui.components.PremiumTripReceiptPreviewSheet
import ui.components.PremiumTripStatusBanner
import ui.components.RideitTripStatus

@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by mapViewModel.uiState.collectAsState()

    var showPanel by remember { mutableStateOf(true) }
    var showRideCompletionSheet by remember { mutableStateOf(false) }
    var showReceiptPreviewSheet by remember { mutableStateOf(false) }
    var submittedRating by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val defaultLocation = LatLng(33.6844, 73.0479)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    val currentTripStatus = when (uiState.rideRequestStatus) {
        RideRequestStatus.SEARCHING_DRIVER -> RideitTripStatus.SearchingDriver
        RideRequestStatus.DRIVER_FOUND -> RideitTripStatus.DriverFound
        RideRequestStatus.DRIVER_ARRIVING -> RideitTripStatus.DriverArriving
        RideRequestStatus.RIDE_STARTED -> RideitTripStatus.TripInProgress
        else -> RideitTripStatus.Idle
    }

    val selectedRide = uiState.selectedRideOption ?: uiState.rideOptions.firstOrNull()

    val overlayVisible = showRideCompletionSheet || showReceiptPreviewSheet

    val hasPickupOrDropoffText =
        uiState.pickupText.isNotBlank() || uiState.dropoffText.isNotBlank()

    val hasPickupOrDropoffLocation =
        uiState.pickupLatLng != null || uiState.dropoffLatLng != null

    val hasActiveRoute =
        uiState.routePoints.size >= 2

    val hasActiveRideFlow =
        currentTripStatus != RideitTripStatus.Idle

    val shouldShowRouteSummary =
        !overlayVisible &&
                (
                        hasPickupOrDropoffText ||
                                hasPickupOrDropoffLocation ||
                                hasActiveRoute ||
                                hasActiveRideFlow ||
                                uiState.selectedRideOption != null
                        )

    val driverMiniStatusText = when (uiState.rideRequestStatus) {
        RideRequestStatus.SEARCHING_DRIVER -> "Matching you with a nearby driver"
        RideRequestStatus.DRIVER_FOUND -> "Driver accepted your ride"
        RideRequestStatus.DRIVER_ARRIVING -> "Driver is heading to pickup"
        RideRequestStatus.RIDE_STARTED -> "Ride is currently in progress"
        else -> "Driver status"
    }

    val driverMiniProgress = when (uiState.rideRequestStatus) {
        RideRequestStatus.SEARCHING_DRIVER -> 0.25f
        RideRequestStatus.DRIVER_FOUND -> 0.50f
        RideRequestStatus.DRIVER_ARRIVING -> 0.78f
        RideRequestStatus.RIDE_STARTED -> 1f
        else -> 0f
    }

    val shouldShowDriverMiniCard =
        uiState.driver != null &&
                hasActiveRideFlow &&
                !showPanel &&
                !overlayVisible

    val shouldShowSafetyActions =
        !overlayVisible &&
                (
                        currentTripStatus == RideitTripStatus.DriverFound ||
                                currentTripStatus == RideitTripStatus.DriverArriving ||
                                currentTripStatus == RideitTripStatus.TripInProgress
                        )

    val premiumMapStatusText = when (currentTripStatus) {
        RideitTripStatus.SearchingDriver -> "Finding driver"
        RideitTripStatus.DriverFound -> "Driver assigned"
        RideitTripStatus.DriverArriving -> "Driver arriving"
        RideitTripStatus.TripInProgress -> "Trip tracking"
        RideitTripStatus.TripCompleted -> "Trip completed"
        RideitTripStatus.Cancelled -> "Ride cancelled"
        RideitTripStatus.Idle -> "Map ready"
    }

    LaunchedEffect(cameraPositionState.isMoving, overlayVisible) {
        if (overlayVisible) {
            showPanel = false
        } else {
            if (cameraPositionState.isMoving) {
                showPanel = false
            } else {
                delay(500)
                showPanel = true
            }
        }
    }

    LaunchedEffect(
        uiState.pickupLatLng,
        uiState.dropoffLatLng,
        uiState.driverLatLng,
        uiState.routePoints,
        uiState.rideRequestStatus,
        overlayVisible
    ) {
        if (overlayVisible) return@LaunchedEffect

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

        PremiumMapPolishLayer(
            visible = !overlayVisible,
            isMapMoving = cameraPositionState.isMoving,
            hasRoute = hasActiveRoute,
            hasDriver = uiState.driver != null,
            tripStatusText = premiumMapStatusText,
            modifier = Modifier.fillMaxSize()
        )

        PremiumTripStatusBanner(
            status = currentTripStatus,
            driverName = uiState.driver?.name,
            etaText = uiState.driver?.arrivalTime,
            vehicleText = uiState.driver?.vehicleName,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
        )

        PremiumRouteSummaryCard(
            visible = shouldShowRouteSummary,
            pickupText = uiState.pickupText,
            dropoffText = uiState.dropoffText,
            rideTitle = selectedRide?.title,
            estimatedFare = selectedRide?.estimatedFare,
            estimatedTime = selectedRide?.estimatedTime,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(
                    top = if (currentTripStatus == RideitTripStatus.Idle) 18.dp else 150.dp,
                    start = 16.dp,
                    end = 104.dp
                )
        )

        PremiumSafetyQuickActions(
            visible = shouldShowSafetyActions,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            onSafetyClick = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Safety center ready. Driver and trip are being monitored."
                    )
                }
            },
            onShareTripClick = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Share trip feature prepared. Real sharing can be connected later."
                    )
                }
            },
            onSupportClick = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Rideit support is ready to help during your trip."
                    )
                }
            },
            onEmergencyInfoClick = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Emergency info opened. Real emergency contact can be added later."
                    )
                }
            }
        )

        uiState.driver?.let { driver ->
            PremiumDriverMiniCard(
                visible = shouldShowDriverMiniCard,
                driverName = driver.name,
                vehicleName = driver.vehicleName,
                vehicleNumber = driver.vehicleNumber,
                rating = driver.rating,
                arrivalTime = driver.arrivalTime,
                statusText = driverMiniStatusText,
                progress = driverMiniProgress,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            )
        }

        PremiumMapControls(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 46.dp, end = 16.dp),
            onRecenterClick = {
                scope.launch {
                    val target = uiState.driverLatLng
                        ?: uiState.dropoffLatLng
                        ?: uiState.pickupLatLng
                        ?: defaultLocation

                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(target, 15.5f),
                        durationMs = 700
                    )
                }
            },
            onRouteFocusClick = {
                scope.launch {
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
                                durationMs = 850
                            )
                        } catch (_: Exception) {
                        }
                    } else {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f),
                            durationMs = 700
                        )
                    }
                }
            }
        )

        AnimatedVisibility(
            visible = showPanel && !overlayVisible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            RideitBottomPanel(
                uiState = uiState,
                mapViewModel = mapViewModel,
                onCompleteRideClicked = {
                    showRideCompletionSheet = true
                }
            )
        }

        PremiumRideCompletionSheet(
            visible = showRideCompletionSheet,
            driverName = uiState.driver?.name,
            rideTitle = selectedRide?.title,
            fareText = selectedRide?.estimatedFare,
            modifier = Modifier.align(Alignment.Center),
            onDismiss = {
                showRideCompletionSheet = false
            },
            onSubmitRating = { rating, tags, feedback ->
                submittedRating = rating
                showRideCompletionSheet = false
                showReceiptPreviewSheet = true

                scope.launch {
                    val tagText = if (tags.isNotEmpty()) {
                        " Tags: ${tags.joinToString(", ")}."
                    } else {
                        ""
                    }

                    val feedbackText = if (feedback.isNotBlank()) {
                        " Feedback saved."
                    } else {
                        ""
                    }

                    snackbarHostState.showSnackbar(
                        message = "Thanks! $rating-star rating submitted.$tagText$feedbackText"
                    )
                }
            }
        )

        PremiumTripReceiptPreviewSheet(
            visible = showReceiptPreviewSheet,
            driverName = uiState.driver?.name,
            pickupText = uiState.pickupText,
            dropoffText = uiState.dropoffText,
            rideTitle = selectedRide?.title,
            fareText = selectedRide?.estimatedFare,
            rating = submittedRating,
            modifier = Modifier.align(Alignment.Center),
            onDoneClick = {
                showReceiptPreviewSheet = false
            },
            onViewHistoryClick = {
                showReceiptPreviewSheet = false

                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Trip History screen is already available. Real receipt navigation can be connected later."
                    )
                }
            }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 120.dp)
        )
    }
}

@Composable
private fun PremiumMapControls(
    modifier: Modifier = Modifier,
    onRecenterClick: () -> Unit,
    onRouteFocusClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        PremiumMapButton(
            icon = "⌖",
            label = "Recenter",
            onClick = onRecenterClick
        )

        PremiumMapButton(
            icon = "⛶",
            label = "Route",
            onClick = onRouteFocusClick
        )
    }
}

@Composable
private fun PremiumMapButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                color = Color(0xFF8A35F2),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RideitBottomPanel(
    uiState: MapUiState,
    mapViewModel: MapViewModel,
    onCompleteRideClicked: () -> Unit
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
                        onCancelRide = mapViewModel::onCancelRideClicked,
                        onCompleteRideClicked = onCompleteRideClicked
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
    onCancelRide: () -> Unit,
    onCompleteRideClicked: () -> Unit
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
                    Text(
                        "⭐ ${driver.rating} • ${driver.arrivalTime}",
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED) {
        Button(
            onClick = onCompleteRideClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF16A34A)
            )
        ) {
            Text(
                text = "Complete Ride Demo",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

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