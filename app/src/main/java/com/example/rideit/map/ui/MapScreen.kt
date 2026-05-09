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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rideit.FirebaseManager
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.components.PremiumDriverMiniCard
import ui.components.PremiumMapPolishLayer
import ui.components.PremiumRideCompletionSheet
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

    var activeRideRequestId by remember { mutableStateOf<String?>(null) }
    var completedRideRequestId by remember { mutableStateOf<String?>(null) }

    var isSavingRideRequest by remember { mutableStateOf(false) }
    var isCancellingRideRequest by remember { mutableStateOf(false) }
    var isSavingFeedback by remember { mutableStateOf(false) }

    var firebaseRideMessage by remember { mutableStateOf<String?>(null) }
    var firebaseRideError by remember { mutableStateOf<String?>(null) }
    var firebaseDriverName by remember { mutableStateOf<String?>(null) }
    var firebaseDriverEmail by remember { mutableStateOf<String?>(null) }

    var firebaseTripCompleted by remember { mutableStateOf(false) }
    var firebaseTripCancelledByDriver by remember { mutableStateOf(false) }
    var firebaseLiveTripStatus by remember { mutableStateOf<String?>(null) }

    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val defaultLocation = LatLng(33.6844, 73.0479)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    LaunchedEffect(Unit) {
        if (activeRideRequestId != null || completedRideRequestId != null) {
            return@LaunchedEffect
        }

        FirebaseManager.findLatestRestorableRiderRide(
            onSuccess = { requestId, status, driverName, driverEmail, feedbackSubmitted ->
                if (requestId.isNullOrBlank() || status.isNullOrBlank()) {
                    return@findLatestRestorableRiderRide
                }

                val cleanStatus = status.lowercase()

                if (cleanStatus == "completed" && feedbackSubmitted) {
                    return@findLatestRestorableRiderRide
                }

                if (
                    cleanStatus == "pending" ||
                    cleanStatus == "requested" ||
                    cleanStatus == "searching" ||
                    cleanStatus == "searching_driver" ||
                    cleanStatus == "waiting" ||
                    cleanStatus == "waiting_for_driver"
                ) {
                    activeRideRequestId = null
                    completedRideRequestId = null
                    firebaseTripCompleted = false
                    firebaseTripCancelledByDriver = false
                    firebaseLiveTripStatus = null
                    firebaseDriverName = null
                    firebaseDriverEmail = null
                    firebaseRideMessage = null
                    firebaseRideError = null
                    showPanel = true
                    mapViewModel.onCancelRideClicked()
                    return@findLatestRestorableRiderRide
                }

                firebaseDriverName = driverName
                firebaseDriverEmail = driverEmail
                firebaseLiveTripStatus = cleanStatus

                when (cleanStatus) {
                    "accepted" -> {
                        activeRideRequestId = requestId
                        completedRideRequestId = null
                        firebaseTripCompleted = false
                        firebaseTripCancelledByDriver = false
                        firebaseRideMessage = "${driverName ?: "Your driver"} accepted your ride."
                        firebaseRideError = null
                        showPanel = true
                        mapViewModel.onConfirmRideClicked()
                    }

                    "driver_arriving" -> {
                        activeRideRequestId = requestId
                        completedRideRequestId = null
                        firebaseTripCompleted = false
                        firebaseTripCancelledByDriver = false
                        firebaseRideMessage = "${driverName ?: "Your driver"} arrived at pickup."
                        firebaseRideError = null
                        showPanel = true
                        mapViewModel.onConfirmRideClicked()
                    }

                    "ride_started" -> {
                        activeRideRequestId = requestId
                        completedRideRequestId = null
                        firebaseTripCompleted = false
                        firebaseTripCancelledByDriver = false
                        firebaseRideMessage = "Trip in progress. Enjoy your Rideit ride."
                        firebaseRideError = null
                        showPanel = true
                        mapViewModel.onConfirmRideClicked()
                    }

                    "completed" -> {
                        activeRideRequestId = null
                        completedRideRequestId = requestId
                        firebaseTripCompleted = true
                        firebaseTripCancelledByDriver = false
                        firebaseLiveTripStatus = "completed"
                        firebaseRideMessage = "Trip completed. Please submit your rating."
                        firebaseRideError = null
                        showPanel = false
                        showRideCompletionSheet = true
                        showReceiptPreviewSheet = false
                        mapViewModel.onCancelRideClicked()
                    }
                }
            },
            onError = { error ->
                firebaseRideError = error
            }
        )
    }

    DisposableEffect(activeRideRequestId) {
        var listenerRegistration: ListenerRegistration? = null

        val requestId = activeRideRequestId

        if (!requestId.isNullOrBlank()) {
            listenerRegistration = firestore.collection("ride_requests")
                .document(requestId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        firebaseRideError = error.message ?: "Unable to listen for ride status."
                        return@addSnapshotListener
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        firebaseRideError = "Ride request not found."
                        return@addSnapshotListener
                    }

                    val status = snapshot.getString("status").orEmpty()
                    val cleanStatus = status.lowercase()
                    val driverName = snapshot.getString("driverName")
                    val driverEmail = snapshot.getString("driverEmail")

                    firebaseLiveTripStatus = cleanStatus

                    when (cleanStatus) {
                        "accepted" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = driverName ?: "Your driver"
                            firebaseDriverEmail = driverEmail
                            firebaseRideMessage = "${driverName ?: "Your driver"} accepted your ride."
                            firebaseRideError = null
                        }

                        "driver_arriving" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = driverName ?: firebaseDriverName ?: "Your driver"
                            firebaseDriverEmail = driverEmail ?: firebaseDriverEmail
                            firebaseRideMessage = "${driverName ?: firebaseDriverName ?: "Your driver"} arrived at pickup."
                            firebaseRideError = null
                        }

                        "ride_started" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = driverName ?: firebaseDriverName ?: "Your driver"
                            firebaseDriverEmail = driverEmail ?: firebaseDriverEmail
                            firebaseRideMessage = "Trip in progress. Enjoy your Rideit ride."
                            firebaseRideError = null
                        }

                        "completed" -> {
                            completedRideRequestId = requestId
                            firebaseTripCompleted = true
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = driverName ?: firebaseDriverName ?: "Your driver"
                            firebaseDriverEmail = driverEmail ?: firebaseDriverEmail
                            firebaseRideMessage = "Trip completed successfully."
                            firebaseRideError = null
                            activeRideRequestId = null
                            mapViewModel.onCancelRideClicked()
                            showPanel = false
                            showRideCompletionSheet = true
                            showReceiptPreviewSheet = false
                        }

                        "cancelled_by_driver" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = true
                            firebaseDriverName = driverName ?: firebaseDriverName
                            firebaseDriverEmail = driverEmail ?: firebaseDriverEmail
                            firebaseRideMessage = null
                            firebaseRideError = "Driver cancelled the trip. Please book another ride."
                            activeRideRequestId = null
                            completedRideRequestId = null
                            firebaseLiveTripStatus = "cancelled_by_driver"
                            mapViewModel.onCancelRideClicked()
                            showPanel = true
                            showRideCompletionSheet = false
                            showReceiptPreviewSheet = false
                        }

                        "declined" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = null
                            firebaseDriverEmail = null
                            firebaseRideMessage = "This request was declined. Please book another ride."
                            firebaseRideError = null
                            activeRideRequestId = null
                            completedRideRequestId = null
                            firebaseLiveTripStatus = "declined"
                            mapViewModel.onCancelRideClicked()
                            showPanel = true
                        }

                        "cancelled_by_rider" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = null
                            firebaseDriverEmail = null
                            firebaseRideMessage = "Ride cancelled successfully."
                            firebaseRideError = null
                            activeRideRequestId = null
                            completedRideRequestId = null
                            firebaseLiveTripStatus = "cancelled_by_rider"
                            mapViewModel.onCancelRideClicked()
                            showPanel = true
                        }

                        "pending", "requested", "searching", "searching_driver", "waiting", "waiting_for_driver" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = null
                            firebaseDriverEmail = null
                            firebaseRideMessage = "Ride request saved. Driver car is visible on the map."
                            firebaseRideError = null
                        }

                        else -> {
                            if (status.isNotBlank()) {
                                firebaseRideMessage = "Ride status: $status"
                            }
                        }
                    }
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    val localTripStatus = when (uiState.rideRequestStatus) {
        RideRequestStatus.SEARCHING_DRIVER -> RideitTripStatus.SearchingDriver
        RideRequestStatus.DRIVER_FOUND -> RideitTripStatus.DriverFound
        RideRequestStatus.DRIVER_ARRIVING -> RideitTripStatus.DriverArriving
        RideRequestStatus.RIDE_STARTED -> RideitTripStatus.TripInProgress
        else -> RideitTripStatus.Idle
    }

    val currentTripStatus = when {
        firebaseTripCompleted -> RideitTripStatus.TripCompleted
        firebaseTripCancelledByDriver -> RideitTripStatus.Cancelled
        firebaseLiveTripStatus == "pending" ||
                firebaseLiveTripStatus == "requested" ||
                firebaseLiveTripStatus == "searching" ||
                firebaseLiveTripStatus == "searching_driver" -> RideitTripStatus.SearchingDriver
        firebaseLiveTripStatus == "accepted" -> RideitTripStatus.DriverFound
        firebaseLiveTripStatus == "driver_arriving" -> RideitTripStatus.DriverArriving
        firebaseLiveTripStatus == "ride_started" -> RideitTripStatus.TripInProgress
        else -> localTripStatus
    }

    val visibleDriverLatLng = resolveVisibleDriverLatLng(
        firebaseLiveTripStatus = firebaseLiveTripStatus,
        localRideRequestStatus = uiState.rideRequestStatus,
        pickupLatLng = uiState.pickupLatLng,
        dropoffLatLng = uiState.dropoffLatLng,
        currentDriverLatLng = uiState.driverLatLng,
        fallbackLocation = defaultLocation
    )

    val selectedRide = uiState.selectedRideOption ?: uiState.rideOptions.firstOrNull()

    val overlayVisible = showRideCompletionSheet || showReceiptPreviewSheet
    val hasActiveRoute = uiState.routePoints.size >= 2
    val hasActiveRideFlow = currentTripStatus != RideitTripStatus.Idle

    val shouldShowCompactRouteChip =
        !overlayVisible &&
                !firebaseTripCompleted &&
                !firebaseTripCancelledByDriver &&
                (
                        hasActiveRoute ||
                                hasActiveRideFlow ||
                                uiState.showRideOptions ||
                                uiState.selectedRideOption != null
                        )

    val shouldShowMapControls =
        !overlayVisible &&
                !firebaseTripCompleted &&
                !firebaseTripCancelledByDriver &&
                (
                        hasActiveRoute ||
                                hasActiveRideFlow ||
                                uiState.pickupLatLng != null ||
                                uiState.dropoffLatLng != null ||
                                visibleDriverLatLng != null
                        )

    val driverMiniStatusText = when {
        firebaseTripCompleted -> "Trip completed successfully"
        firebaseTripCancelledByDriver -> "Driver cancelled the trip"
        firebaseLiveTripStatus == "pending" ||
                firebaseLiveTripStatus == "requested" ||
                firebaseLiveTripStatus == "searching" ||
                firebaseLiveTripStatus == "searching_driver" -> "Driver car is visible on the map"
        firebaseLiveTripStatus == "accepted" -> "${firebaseDriverName ?: "Your driver"} accepted your ride"
        firebaseLiveTripStatus == "driver_arriving" -> "Driver arrived at pickup"
        firebaseLiveTripStatus == "ride_started" -> "Trip is currently in progress"
        uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER -> "Driver car is visible on the map"
        uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND -> firebaseDriverName?.let { "$it accepted your ride" }
            ?: "Driver accepted your ride"
        uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> "Driver arrived at pickup"
        uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED -> "Ride is currently in progress"
        else -> "Driver status"
    }

    val driverMiniProgress = when {
        firebaseTripCompleted -> 1f
        firebaseTripCancelledByDriver -> 0f
        firebaseLiveTripStatus == "pending" ||
                firebaseLiveTripStatus == "requested" ||
                firebaseLiveTripStatus == "searching" ||
                firebaseLiveTripStatus == "searching_driver" -> 0.25f
        firebaseLiveTripStatus == "accepted" -> 0.50f
        firebaseLiveTripStatus == "driver_arriving" -> 0.80f
        firebaseLiveTripStatus == "ride_started" -> 0.92f
        uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER -> 0.25f
        uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND -> 0.50f
        uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> 0.80f
        uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED -> 1f
        else -> 0f
    }

    val shouldShowDriverMiniCard =
        hasActiveRideFlow &&
                !showPanel &&
                !overlayVisible &&
                !firebaseTripCancelledByDriver &&
                !firebaseTripCompleted &&
                visibleDriverLatLng != null

    val premiumMapStatusText = when {
        firebaseLiveTripStatus == "pending" ||
                firebaseLiveTripStatus == "requested" ||
                firebaseLiveTripStatus == "searching" ||
                firebaseLiveTripStatus == "searching_driver" -> "Driver nearby"
        firebaseLiveTripStatus == "driver_arriving" -> "Driver arrived"
        firebaseLiveTripStatus == "ride_started" -> "Trip in progress"
        else -> when (currentTripStatus) {
            RideitTripStatus.SearchingDriver -> "Driver nearby"
            RideitTripStatus.DriverFound -> "Driver assigned"
            RideitTripStatus.DriverArriving -> "Driver arrived"
            RideitTripStatus.TripInProgress -> "Trip tracking"
            RideitTripStatus.TripCompleted -> "Trip completed"
            RideitTripStatus.Cancelled -> "Ride cancelled"
            RideitTripStatus.Idle -> "Map ready"
        }
    }

    fun resetCompletedTripUi() {
        showReceiptPreviewSheet = false
        showRideCompletionSheet = false
        firebaseTripCompleted = false
        firebaseTripCancelledByDriver = false
        firebaseLiveTripStatus = null
        firebaseRideMessage = null
        firebaseRideError = null
        firebaseDriverName = null
        firebaseDriverEmail = null
        activeRideRequestId = null
        completedRideRequestId = null
        submittedRating = null
        isSavingFeedback = false
        mapViewModel.onCancelRideClicked()
        showPanel = true
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
        visibleDriverLatLng,
        uiState.routePoints,
        uiState.rideRequestStatus,
        firebaseLiveTripStatus,
        firebaseTripCompleted,
        firebaseTripCancelledByDriver,
        overlayVisible
    ) {
        if (overlayVisible) return@LaunchedEffect

        delay(300)

        if (firebaseTripCompleted || firebaseTripCancelledByDriver) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f),
                durationMs = 800
            )
            return@LaunchedEffect
        }

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

        visibleDriverLatLng?.let {
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
                    update = CameraUpdateFactory.newLatLngBounds(builder.build(), 190),
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
            if (!firebaseTripCompleted && !firebaseTripCancelledByDriver) {
                uiState.pickupLatLng?.let {
                    Marker(state = MarkerState(it), title = "Pickup")
                }

                uiState.dropoffLatLng?.let {
                    Marker(state = MarkerState(it), title = "Dropoff")
                }

                visibleDriverLatLng?.let {
                    Marker(
                        state = MarkerState(it),
                        title = "🚗 Driver car",
                        snippet = when (firebaseLiveTripStatus) {
                            "pending", "requested", "searching", "searching_driver" -> "Nearby driver car"
                            "accepted" -> "Driver accepted your ride"
                            "driver_arriving" -> "Driver arrived at pickup"
                            "ride_started" -> "Trip in progress"
                            else -> "Driver is on the way"
                        },
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                val driverRoute = buildDriverRoutePoints(
                    driverLatLng = visibleDriverLatLng,
                    pickupLatLng = uiState.pickupLatLng,
                    dropoffLatLng = uiState.dropoffLatLng,
                    status = firebaseLiveTripStatus,
                    localRideRequestStatus = uiState.rideRequestStatus
                )

                if (driverRoute.size >= 2) {
                    Polyline(
                        points = driverRoute,
                        width = 9f,
                        color = Color(0xFF2563EB)
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
        }

        PremiumMapPolishLayer(
            visible = !overlayVisible,
            isMapMoving = cameraPositionState.isMoving,
            hasRoute = hasActiveRoute && !firebaseTripCompleted && !firebaseTripCancelledByDriver,
            hasDriver = visibleDriverLatLng != null && !firebaseTripCompleted && !firebaseTripCancelledByDriver,
            tripStatusText = premiumMapStatusText,
            modifier = Modifier.fillMaxSize()
        )

        PremiumTripStatusBanner(
            status = currentTripStatus,
            driverName = firebaseDriverName ?: uiState.driver?.name,
            etaText = uiState.driver?.arrivalTime,
            vehicleText = uiState.driver?.vehicleName,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp)
        )

        CompactRouteChip(
            visible = shouldShowCompactRouteChip,
            pickupText = uiState.pickupText,
            dropoffText = uiState.dropoffText,
            rideTitle = selectedRide?.title,
            fareText = selectedRide?.estimatedFare,
            etaText = selectedRide?.estimatedTime,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(
                    top = if (currentTripStatus == RideitTripStatus.Idle) 76.dp else 116.dp,
                    start = 16.dp,
                    end = 120.dp
                )
        )

        PremiumDriverMiniCard(
            visible = shouldShowDriverMiniCard,
            driverName = firebaseDriverName ?: uiState.driver?.name ?: "Driver car",
            vehicleName = uiState.driver?.vehicleName ?: "Rideit vehicle",
            vehicleNumber = uiState.driver?.vehicleNumber ?: "On the map",
            rating = uiState.driver?.rating?.toString() ?: "5.0",
            arrivalTime = uiState.driver?.arrivalTime ?: "Coming",
            statusText = driverMiniStatusText,
            progress = driverMiniProgress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        )

        PremiumMapControls(
            visible = shouldShowMapControls,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 76.dp, end = 16.dp),
            onRecenterClick = {
                scope.launch {
                    val target = visibleDriverLatLng
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

                    visibleDriverLatLng?.let {
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
                                update = CameraUpdateFactory.newLatLngBounds(builder.build(), 190),
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
                isSavingRideRequest = isSavingRideRequest,
                isCancellingRideRequest = isCancellingRideRequest,
                firebaseRideMessage = firebaseRideMessage,
                firebaseRideError = firebaseRideError,
                firebaseDriverName = firebaseDriverName,
                firebaseDriverEmail = firebaseDriverEmail,
                firebaseTripCompleted = firebaseTripCompleted,
                firebaseTripCancelledByDriver = firebaseTripCancelledByDriver,
                firebaseLiveTripStatus = firebaseLiveTripStatus,
                onConfirmRideWithFirebase = {
                    if (isSavingRideRequest) return@RideitBottomPanel

                    val ride = uiState.selectedRideOption ?: uiState.rideOptions.firstOrNull()

                    if (ride == null) {
                        firebaseRideError = "Please select a ride first."
                        return@RideitBottomPanel
                    }

                    isSavingRideRequest = true
                    firebaseRideError = null
                    firebaseRideMessage = "Saving your ride request..."
                    firebaseTripCompleted = false
                    firebaseTripCancelledByDriver = false
                    completedRideRequestId = null
                    firebaseLiveTripStatus = null

                    FirebaseManager.saveRideRequest(
                        pickupAddress = uiState.pickupText.ifBlank { "Selected pickup location" },
                        dropoffAddress = uiState.dropoffText.ifBlank { "Selected dropoff location" },
                        rideType = ride.title,
                        fareEstimate = ride.estimatedFare,
                        onSuccess = { requestId ->
                            activeRideRequestId = requestId
                            completedRideRequestId = null
                            isSavingRideRequest = false
                            firebaseRideError = null
                            firebaseLiveTripStatus = "pending"
                            firebaseRideMessage = "Ride request saved. Driver car is visible on the map."
                            mapViewModel.onConfirmRideClicked()
                        },
                        onError = { error ->
                            isSavingRideRequest = false
                            firebaseRideError = error
                            firebaseRideMessage = null
                        }
                    )
                },
                onCancelRideWithFirebase = {
                    if (firebaseTripCompleted || firebaseTripCancelledByDriver) {
                        resetCompletedTripUi()
                        return@RideitBottomPanel
                    }

                    val requestId = activeRideRequestId

                    if (requestId.isNullOrBlank()) {
                        resetCompletedTripUi()
                        return@RideitBottomPanel
                    }

                    if (isCancellingRideRequest) return@RideitBottomPanel

                    isCancellingRideRequest = true
                    firebaseRideError = null
                    firebaseRideMessage = "Cancelling ride request..."

                    FirebaseManager.cancelRideRequest(
                        requestId = requestId,
                        onSuccess = {
                            isCancellingRideRequest = false
                            activeRideRequestId = null
                            completedRideRequestId = null
                            firebaseDriverName = null
                            firebaseDriverEmail = null
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseLiveTripStatus = "cancelled_by_rider"
                            firebaseRideMessage = "Ride cancelled successfully."
                            mapViewModel.onCancelRideClicked()

                            scope.launch {
                                snackbarHostState.showSnackbar("Ride cancelled successfully.")
                            }
                        },
                        onError = { error ->
                            isCancellingRideRequest = false
                            firebaseRideError = error
                        }
                    )
                },
                onCompleteRideClicked = {
                    showRideCompletionSheet = true
                }
            )
        }

        PremiumRideCompletionSheet(
            visible = showRideCompletionSheet,
            driverName = firebaseDriverName ?: uiState.driver?.name,
            rideTitle = selectedRide?.title,
            fareText = selectedRide?.estimatedFare,
            modifier = Modifier.align(Alignment.Center),
            onDismiss = {
                showRideCompletionSheet = false
            },
            onSubmitRating = { rating, tags, feedback ->
                if (isSavingFeedback) return@PremiumRideCompletionSheet

                val requestIdForFeedback = completedRideRequestId ?: activeRideRequestId

                if (requestIdForFeedback.isNullOrBlank()) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Completed ride not found. Feedback was not saved."
                        )
                    }
                    return@PremiumRideCompletionSheet
                }

                isSavingFeedback = true

                FirebaseManager.saveRiderTripFeedback(
                    requestId = requestIdForFeedback,
                    rating = rating,
                    tags = tags,
                    feedback = feedback,
                    onSuccess = {
                        isSavingFeedback = false
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
                                message = "Thanks! $rating-star rating saved to Firebase.$tagText$feedbackText"
                            )
                        }
                    },
                    onError = { error ->
                        isSavingFeedback = false

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = error
                            )
                        }
                    }
                )
            }
        )

        PremiumTripReceiptPreviewSheet(
            visible = showReceiptPreviewSheet,
            driverName = firebaseDriverName ?: uiState.driver?.name,
            pickupText = uiState.pickupText,
            dropoffText = uiState.dropoffText,
            rideTitle = selectedRide?.title,
            fareText = selectedRide?.estimatedFare,
            rating = submittedRating,
            modifier = Modifier.align(Alignment.Center),
            onDoneClick = {
                resetCompletedTripUi()
            },
            onViewHistoryClick = {
                resetCompletedTripUi()

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
private fun CompactRouteChip(
    visible: Boolean,
    pickupText: String,
    dropoffText: String,
    rideTitle: String?,
    fareText: String?,
    etaText: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White.copy(alpha = 0.96f),
            shadowElevation = 12.dp,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8A35F2).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↗",
                        color = Color(0xFF8A35F2),
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${pickupText.ifBlank { "Pickup" }} → ${dropoffText.ifBlank { "Dropoff" }}",
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = listOfNotNull(
                            rideTitle,
                            fareText,
                            etaText
                        ).joinToString(" • ").ifBlank { "Route ready" },
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumMapControls(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onRecenterClick: () -> Unit,
    onRouteFocusClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                color = Color(0xFF8A35F2),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
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
    isSavingRideRequest: Boolean,
    isCancellingRideRequest: Boolean,
    firebaseRideMessage: String?,
    firebaseRideError: String?,
    firebaseDriverName: String?,
    firebaseDriverEmail: String?,
    firebaseTripCompleted: Boolean,
    firebaseTripCancelledByDriver: Boolean,
    firebaseLiveTripStatus: String?,
    onConfirmRideWithFirebase: () -> Unit,
    onCancelRideWithFirebase: () -> Unit,
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
                .heightIn(max = 560.dp)
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
                        uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED ||
                        firebaseTripCompleted ||
                        firebaseTripCancelledByDriver ||
                        firebaseLiveTripStatus == "pending" ||
                        firebaseLiveTripStatus == "requested" ||
                        firebaseLiveTripStatus == "searching" ||
                        firebaseLiveTripStatus == "searching_driver" ||
                        firebaseLiveTripStatus == "accepted" ||
                        firebaseLiveTripStatus == "driver_arriving" ||
                        firebaseLiveTripStatus == "ride_started" -> {
                    PremiumRideStatusContent(
                        uiState = uiState,
                        isCancellingRideRequest = isCancellingRideRequest,
                        firebaseRideMessage = firebaseRideMessage,
                        firebaseRideError = firebaseRideError,
                        firebaseDriverName = firebaseDriverName,
                        firebaseDriverEmail = firebaseDriverEmail,
                        firebaseTripCompleted = firebaseTripCompleted,
                        firebaseTripCancelledByDriver = firebaseTripCancelledByDriver,
                        firebaseLiveTripStatus = firebaseLiveTripStatus,
                        onCancelRide = onCancelRideWithFirebase,
                        onCompleteRideClicked = onCompleteRideClicked
                    )
                }

                uiState.showRideOptions && uiState.rideOptions.isNotEmpty() -> {
                    RideSelectionContent(
                        uiState = uiState,
                        isSavingRideRequest = isSavingRideRequest,
                        firebaseRideMessage = firebaseRideMessage,
                        firebaseRideError = firebaseRideError,
                        onRideSelected = mapViewModel::onRideOptionSelected,
                        onConfirmRide = onConfirmRideWithFirebase
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
    isSavingRideRequest: Boolean,
    firebaseRideMessage: String?,
    firebaseRideError: String?,
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
        modifier = Modifier.heightIn(max = 300.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(uiState.rideOptions) { option ->
            CompactRideOptionCard(
                option = option,
                selected = option == selectedRide,
                onClick = {
                    if (!isSavingRideRequest) {
                        onRideSelected(option)
                    }
                }
            )
        }
    }

    firebaseRideMessage?.let {
        Spacer(modifier = Modifier.height(10.dp))
        FirebaseMessageCard(message = it, success = true)
    }

    firebaseRideError?.let {
        Spacer(modifier = Modifier.height(10.dp))
        FirebaseMessageCard(message = it, success = false)
    }

    if (isSavingRideRequest) {
        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50)),
            color = selectedColor,
            trackColor = Color(0xFFE5E7EB)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Saving your ride request...",
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Button(
        enabled = !isSavingRideRequest,
        onClick = onConfirmRide,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = selectedColor,
            disabledContainerColor = selectedColor.copy(alpha = 0.55f)
        )
    ) {
        Text(
            text = if (isSavingRideRequest) {
                "Saving Ride..."
            } else {
                "Book ${selectedRide.title} — ${selectedRide.estimatedFare}"
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PremiumRideStatusContent(
    uiState: MapUiState,
    isCancellingRideRequest: Boolean,
    firebaseRideMessage: String?,
    firebaseRideError: String?,
    firebaseDriverName: String?,
    firebaseDriverEmail: String?,
    firebaseTripCompleted: Boolean,
    firebaseTripCancelledByDriver: Boolean,
    firebaseLiveTripStatus: String?,
    onCancelRide: () -> Unit,
    onCompleteRideClicked: () -> Unit
) {
    val statusColor = when {
        firebaseTripCompleted -> Color(0xFF16A34A)
        firebaseTripCancelledByDriver -> Color(0xFFEF4444)
        firebaseLiveTripStatus == "accepted" -> Color(0xFF2563EB)
        firebaseLiveTripStatus == "driver_arriving" -> Color(0xFFE17A00)
        firebaseLiveTripStatus == "ride_started" -> Color(0xFF16A34A)
        uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER -> Color(0xFF8A35F2)
        uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND -> Color(0xFF2563EB)
        uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> Color(0xFFE17A00)
        uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED -> Color(0xFF16A34A)
        else -> Color(0xFF8A35F2)
    }

    Text(
        text = when {
            firebaseTripCompleted -> "Trip completed"
            firebaseTripCancelledByDriver -> "Driver cancelled trip"
            firebaseLiveTripStatus == "pending" ||
                    firebaseLiveTripStatus == "requested" ||
                    firebaseLiveTripStatus == "searching" ||
                    firebaseLiveTripStatus == "searching_driver" -> "Driver car is coming"
            firebaseLiveTripStatus == "accepted" -> "Driver accepted"
            firebaseLiveTripStatus == "driver_arriving" -> "Driver arrived at pickup"
            firebaseLiveTripStatus == "ride_started" -> "Trip in progress"
            firebaseDriverName != null -> "Driver accepted"
            uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER -> "Driver car is coming"
            uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND -> "Driver found"
            uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> "Driver arrived at pickup"
            uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED -> "Ride started"
            else -> "Ride status"
        },
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.headlineSmall
    )

    Spacer(modifier = Modifier.height(12.dp))

    firebaseRideMessage?.let {
        FirebaseMessageCard(message = it, success = true)
        Spacer(modifier = Modifier.height(10.dp))
    }

    firebaseRideError?.let {
        FirebaseMessageCard(message = it, success = false)
        Spacer(modifier = Modifier.height(10.dp))
    }

    firebaseDriverEmail?.let {
        if (!firebaseTripCancelledByDriver) {
            Text(
                text = it,
                color = Color(0xFF16A34A),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    LinearProgressIndicator(
        progress = {
            when {
                firebaseTripCompleted -> 1f
                firebaseTripCancelledByDriver -> 0f
                firebaseLiveTripStatus == "pending" ||
                        firebaseLiveTripStatus == "requested" ||
                        firebaseLiveTripStatus == "searching" ||
                        firebaseLiveTripStatus == "searching_driver" -> 0.25f
                firebaseLiveTripStatus == "accepted" -> 0.50f
                firebaseLiveTripStatus == "driver_arriving" -> 0.80f
                firebaseLiveTripStatus == "ride_started" -> 0.92f
                uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER -> 0.25f
                uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND -> 0.50f
                uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> 0.80f
                uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED -> 1f
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

    if (!firebaseTripCancelledByDriver && !firebaseTripCompleted) {
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
                    Text("🚗")
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = firebaseDriverName ?: uiState.driver?.name ?: "Driver car",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = uiState.driver?.let {
                            "${it.vehicleName} • ${it.vehicleNumber}"
                        } ?: "Visible on the map",
                        color = Color.Gray
                    )

                    Text(
                        text = when (firebaseLiveTripStatus) {
                            "pending", "requested", "searching", "searching_driver" -> "Nearby driver is coming"
                            "accepted" -> "Driver accepted your ride"
                            "driver_arriving" -> "Driver arrived at pickup"
                            "ride_started" -> "Trip is in progress"
                            else -> uiState.driver?.let { "⭐ ${it.rating} • ${it.arrivalTime}" }
                                ?: "Driver assigned"
                        },
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (isCancellingRideRequest) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50)),
            color = Color(0xFFEF4444),
            trackColor = Color(0xFFE5E7EB)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Cancelling ride request...",
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(10.dp))
    }

    OutlinedButton(
        enabled = !isCancellingRideRequest,
        onClick = onCancelRide,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (firebaseTripCompleted) Color(0xFF16A34A) else Color(0xFFEF4444)
        )
    ) {
        Text(
            text = when {
                isCancellingRideRequest -> "Cancelling..."
                firebaseTripCompleted -> "Done"
                firebaseTripCancelledByDriver -> "Back to Search"
                else -> "Cancel Ride"
            },
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FirebaseMessageCard(
    message: String,
    success: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (success) Color(0xFFE8FFF1) else Color(0xFFFFE5E5)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(13.dp),
            color = if (success) Color(0xFF16A34A) else Color(0xFFEF4444),
            fontWeight = FontWeight.Bold
        )
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

private fun resolveVisibleDriverLatLng(
    firebaseLiveTripStatus: String?,
    localRideRequestStatus: RideRequestStatus,
    pickupLatLng: LatLng?,
    dropoffLatLng: LatLng?,
    currentDriverLatLng: LatLng?,
    fallbackLocation: LatLng
): LatLng? {
    val cleanStatus = firebaseLiveTripStatus.orEmpty().lowercase()

    val shouldShowDriver =
        cleanStatus == "pending" ||
                cleanStatus == "requested" ||
                cleanStatus == "searching" ||
                cleanStatus == "searching_driver" ||
                cleanStatus == "accepted" ||
                cleanStatus == "driver_arriving" ||
                cleanStatus == "ride_started" ||
                localRideRequestStatus == RideRequestStatus.SEARCHING_DRIVER ||
                localRideRequestStatus == RideRequestStatus.DRIVER_FOUND ||
                localRideRequestStatus == RideRequestStatus.DRIVER_ARRIVING ||
                localRideRequestStatus == RideRequestStatus.RIDE_STARTED

    if (!shouldShowDriver) return null

    currentDriverLatLng?.let {
        return it
    }

    val pickup = pickupLatLng ?: fallbackLocation
    val dropoff = dropoffLatLng ?: pickup

    return when {
        cleanStatus == "pending" ||
                cleanStatus == "requested" ||
                cleanStatus == "searching" ||
                cleanStatus == "searching_driver" ||
                localRideRequestStatus == RideRequestStatus.SEARCHING_DRIVER -> {
            LatLng(
                (fallbackLocation.latitude * 0.55) + (pickup.latitude * 0.45),
                (fallbackLocation.longitude * 0.55) + (pickup.longitude * 0.45)
            )
        }

        cleanStatus == "accepted" ||
                localRideRequestStatus == RideRequestStatus.DRIVER_FOUND -> {
            LatLng(
                (fallbackLocation.latitude * 0.35) + (pickup.latitude * 0.65),
                (fallbackLocation.longitude * 0.35) + (pickup.longitude * 0.65)
            )
        }

        cleanStatus == "driver_arriving" ||
                localRideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> {
            LatLng(
                (fallbackLocation.latitude * 0.10) + (pickup.latitude * 0.90),
                (fallbackLocation.longitude * 0.10) + (pickup.longitude * 0.90)
            )
        }

        cleanStatus == "ride_started" ||
                localRideRequestStatus == RideRequestStatus.RIDE_STARTED -> {
            LatLng(
                (pickup.latitude + dropoff.latitude) / 2.0,
                (pickup.longitude + dropoff.longitude) / 2.0
            )
        }

        else -> {
            LatLng(
                (fallbackLocation.latitude + pickup.latitude) / 2.0,
                (fallbackLocation.longitude + pickup.longitude) / 2.0
            )
        }
    }
}

private fun buildDriverRoutePoints(
    driverLatLng: LatLng?,
    pickupLatLng: LatLng?,
    dropoffLatLng: LatLng?,
    status: String?,
    localRideRequestStatus: RideRequestStatus
): List<LatLng> {
    val driver = driverLatLng ?: return emptyList()
    val cleanStatus = status.orEmpty().lowercase()

    return when {
        cleanStatus == "ride_started" ||
                localRideRequestStatus == RideRequestStatus.RIDE_STARTED -> {
            val dropoff = dropoffLatLng ?: return emptyList()
            listOf(driver, dropoff)
        }

        else -> {
            val pickup = pickupLatLng ?: return emptyList()
            listOf(driver, pickup)
        }
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