package com.example.rideit.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rideit.FirebaseManager
import com.example.rideit.map.model.LocationSuggestion
import com.example.rideit.map.model.MapUiState
import com.example.rideit.map.model.RideOption
import com.example.rideit.map.model.RideRequestStatus
import com.example.rideit.map.viewmodel.MapViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.DocumentSnapshot
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

    var pendingDetectAfterGpsDialog by remember { mutableStateOf(false) }
    var showLocationCard by remember { mutableStateOf(true) }
    var isBottomPanelExpanded by remember { mutableStateOf(true) }

    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val settingsClient = remember { LocationServices.getSettingsClient(context) }

    val defaultLocation = LatLng(33.6844, 73.0479)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 14f)
    }

    fun moveToDetectedLocation(location: LatLng, message: String) {
        mapViewModel.onCurrentLocationDetected(location)
        showLocationCard = false

        scope.launch {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 16.5f),
                durationMs = 900
            )
            snackbarHostState.showSnackbar(message)
        }
    }

    @SuppressLint("MissingPermission")
    fun detectLocationWithFusedProvider(showSuccessSnackbar: Boolean = true) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) return

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                moveToDetectedLocation(
                    location = LatLng(location.latitude, location.longitude),
                    message = if (showSuccessSnackbar) {
                        "Current location detected."
                    } else {
                        "Map moved to your current location."
                    }
                )
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            moveToDetectedLocation(
                                location = LatLng(lastLocation.latitude, lastLocation.longitude),
                                message = if (showSuccessSnackbar) {
                                    "Current location detected."
                                } else {
                                    "Map moved to your current location."
                                }
                            )
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Unable to detect current GPS location. Please try again."
                                )
                            }
                        }
                    }
                    .addOnFailureListener {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Unable to detect current GPS location. Please try again."
                            )
                        }
                    }
            }
        }.addOnFailureListener {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Unable to detect current GPS location. Please try again."
                )
            }
        }
    }

    val gpsSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || pendingDetectAfterGpsDialog) {
            pendingDetectAfterGpsDialog = false
            detectLocationWithFusedProvider(showSuccessSnackbar = true)
        } else {
            pendingDetectAfterGpsDialog = false
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Device location is still off. Turn it on to detect your current location."
                )
            }
        }
    }

    fun checkGpsSettingsThenDetectLocation(showSuccessSnackbar: Boolean = true) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1_000L
        )
            .setMinUpdateIntervalMillis(500L)
            .setWaitForAccurateLocation(false)
            .build()

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                detectLocationWithFusedProvider(showSuccessSnackbar = showSuccessSnackbar)
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    pendingDetectAfterGpsDialog = true

                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(
                            exception.resolution
                        ).build()

                        gpsSettingsLauncher.launch(intentSenderRequest)
                    } catch (_: IntentSender.SendIntentException) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Unable to open location settings. Please turn on GPS manually."
                            )
                        }
                    }
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Please turn on device location services."
                        )
                    }
                }
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            checkGpsSettingsThenDetectLocation(showSuccessSnackbar = true)
        } else {
            showLocationCard = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Location permission is required to find nearby rides."
                )
            }
        }
    }

    fun requestLocationPermissionAndGps() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            checkGpsSettingsThenDetectLocation(showSuccessSnackbar = true)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            delay(650)
            checkGpsSettingsThenDetectLocation(showSuccessSnackbar = false)
        } else {
            delay(450)
            showLocationCard = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
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
                        showPanel = false
                        mapViewModel.onConfirmRideClicked()
                    }

                    "driver_arriving" -> {
                        activeRideRequestId = requestId
                        completedRideRequestId = null
                        firebaseTripCompleted = false
                        firebaseTripCancelledByDriver = false
                        firebaseRideMessage = "${driverName ?: "Your driver"} arrived at pickup."
                        firebaseRideError = null
                        showPanel = false
                        mapViewModel.onConfirmRideClicked()
                    }

                    "ride_started" -> {
                        activeRideRequestId = requestId
                        completedRideRequestId = null
                        firebaseTripCompleted = false
                        firebaseTripCancelledByDriver = false
                        firebaseRideMessage = "Trip in progress. Enjoy your Rideit ride."
                        firebaseRideError = null
                        showPanel = false
                        mapViewModel.onConfirmRideClicked()
                    }

                    "completed", "trip_completed", "completed_by_driver", "ride_completed" -> {
                        activeRideRequestId = null
                        completedRideRequestId = requestId
                        firebaseTripCompleted = true
                        firebaseTripCancelledByDriver = false
                        firebaseLiveTripStatus = "completed"
                        firebaseRideMessage = "Trip completed. Please rate your driver before viewing receipt."
                        firebaseRideError = null
                        submittedRating = null
                        showPanel = false
                        showRideCompletionSheet = true
                        showReceiptPreviewSheet = false
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
                            showPanel = false
                        }

                        "driver_arriving" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = driverName ?: firebaseDriverName ?: "Your driver"
                            firebaseDriverEmail = driverEmail ?: firebaseDriverEmail
                            firebaseRideMessage = "${driverName ?: firebaseDriverName ?: "Your driver"} arrived at pickup."
                            firebaseRideError = null
                            showPanel = false
                        }

                        "ride_started" -> {
                            firebaseTripCompleted = false
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = driverName ?: firebaseDriverName ?: "Your driver"
                            firebaseDriverEmail = driverEmail ?: firebaseDriverEmail
                            firebaseRideMessage = "Trip in progress. Enjoy your Rideit ride."
                            firebaseRideError = null
                            showPanel = false
                        }

                        "completed", "trip_completed", "completed_by_driver", "ride_completed" -> {
                            completedRideRequestId = requestId
                            firebaseTripCompleted = true
                            firebaseTripCancelledByDriver = false
                            firebaseDriverName = driverName ?: firebaseDriverName ?: "Your driver"
                            firebaseDriverEmail = driverEmail ?: firebaseDriverEmail
                            firebaseRideMessage = "Trip completed successfully. Please rate your driver before viewing receipt."
                            firebaseRideError = null
                            activeRideRequestId = null
                            submittedRating = null
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
                            firebaseRideMessage = "Ride request saved. Waiting for a driver to accept."
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

    LaunchedEffect(
        firebaseTripCompleted,
        completedRideRequestId,
        submittedRating,
        showRideCompletionSheet,
        showReceiptPreviewSheet
    ) {
        if (
            firebaseTripCompleted &&
            !completedRideRequestId.isNullOrBlank() &&
            submittedRating == null &&
            !showRideCompletionSheet
        ) {
            showPanel = false
            showRideCompletionSheet = true
            showReceiptPreviewSheet = false
            firebaseRideMessage = "Trip completed successfully. Please rate your driver before viewing receipt."
        }
    }

    DisposableEffect(Unit) {
        var riderIdCompletionListener: ListenerRegistration? = null
        var riderEmailCompletionListener: ListenerRegistration? = null
        var userEmailCompletionListener: ListenerRegistration? = null

        val riderId = FirebaseManager.currentUserId().orEmpty()
        val riderEmail = FirebaseManager.currentUserEmail().orEmpty()

        fun handleCompletedRideDocuments(documents: List<DocumentSnapshot>) {
            val latestCompletedDocument = documents
                .filter { document ->
                    val status = document.getString("status").orEmpty().trim().lowercase()
                    val feedbackSubmitted = document.getBoolean("feedbackSubmitted") ?: false

                    (status == "completed" ||
                            status == "trip_completed" ||
                            status == "completed_by_driver" ||
                            status == "ride_completed") && !feedbackSubmitted
                }
                .maxByOrNull { document ->
                    document.getTimestamp("completedAt")?.seconds
                        ?: document.getTimestamp("updatedAt")?.seconds
                        ?: document.getTimestamp("createdAt")?.seconds
                        ?: 0L
                }

            if (latestCompletedDocument != null) {
                val completedId = latestCompletedDocument.id

                completedRideRequestId = completedId
                activeRideRequestId = null
                firebaseTripCompleted = true
                firebaseTripCancelledByDriver = false
                firebaseLiveTripStatus = "completed"

                firebaseDriverName = latestCompletedDocument.getString("driverName")
                    ?: firebaseDriverName
                            ?: "Your driver"

                firebaseDriverEmail = latestCompletedDocument.getString("driverEmail")
                    ?: firebaseDriverEmail

                firebaseRideMessage = "Trip completed successfully. Please rate your driver before viewing receipt."
                firebaseRideError = null

                submittedRating = null
                showPanel = false
                showRideCompletionSheet = true
                showReceiptPreviewSheet = false
            }
        }

        if (riderId.isNotBlank()) {
            riderIdCompletionListener = firestore.collection("ride_requests")
                .whereEqualTo("riderId", riderId)
                .limit(25)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        firebaseRideError = error.message ?: "Unable to listen for completed trip."
                        return@addSnapshotListener
                    }

                    handleCompletedRideDocuments(snapshots?.documents.orEmpty())
                }
        }

        if (riderEmail.isNotBlank()) {
            riderEmailCompletionListener = firestore.collection("ride_requests")
                .whereEqualTo("riderEmail", riderEmail)
                .limit(25)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    handleCompletedRideDocuments(snapshots?.documents.orEmpty())
                }

            userEmailCompletionListener = firestore.collection("ride_requests")
                .whereEqualTo("userEmail", riderEmail)
                .limit(25)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    handleCompletedRideDocuments(snapshots?.documents.orEmpty())
                }
        }

        onDispose {
            riderIdCompletionListener?.remove()
            riderEmailCompletionListener?.remove()
            userEmailCompletionListener?.remove()
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

    LaunchedEffect(
        uiState.showRideOptions,
        uiState.rideRequestStatus,
        firebaseLiveTripStatus
    ) {
        val shouldAutoExpandPanel =
            uiState.showRideOptions ||
                    uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER ||
                    firebaseLiveTripStatus == "pending" ||
                    firebaseLiveTripStatus == "requested" ||
                    firebaseLiveTripStatus == "searching" ||
                    firebaseLiveTripStatus == "searching_driver"

        if (shouldAutoExpandPanel) {
            isBottomPanelExpanded = true
        }
    }

    val overlayVisible = showRideCompletionSheet || showReceiptPreviewSheet
    val hasActiveRoute = uiState.routePoints.size >= 2

    val isCompactTrackingMode =
        firebaseLiveTripStatus == "accepted" ||
                firebaseLiveTripStatus == "driver_arriving" ||
                firebaseLiveTripStatus == "ride_started" ||
                uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND ||
                uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING ||
                uiState.rideRequestStatus == RideRequestStatus.RIDE_STARTED

    val hasActiveRideFlow = currentTripStatus != RideitTripStatus.Idle

    val shouldShowCompactRouteChip =
        !overlayVisible &&
                !firebaseTripCompleted &&
                !firebaseTripCancelledByDriver &&
                currentTripStatus == RideitTripStatus.Idle &&
                (
                        uiState.showRideOptions ||
                                uiState.selectedRideOption != null
                        )

    val shouldShowRiderTopHeader =
        !overlayVisible &&
                currentTripStatus == RideitTripStatus.Idle &&
                !uiState.showRideOptions &&
                uiState.selectedRideOption == null

    val driverDisplayName = firebaseDriverName ?: uiState.driver?.name ?: "Driver"
    val driverPhone = "+92 300 1234567"
    val driverVehicleModel = uiState.driver?.vehicleName ?: selectedRide?.title?.let { "$it Rideit Car" } ?: "Toyota Corolla"
    val driverVehicleNumber = uiState.driver?.vehicleNumber ?: "RIA-2026"
    val driverRating = uiState.driver?.rating?.toString() ?: "5.0"
    val driverArrivalTime = uiState.driver?.arrivalTime ?: "Coming"

    val driverMiniStatusText = when {
        firebaseTripCompleted -> "Trip completed successfully"
        firebaseTripCancelledByDriver -> "Driver cancelled the trip"
        firebaseLiveTripStatus == "pending" ||
                firebaseLiveTripStatus == "requested" ||
                firebaseLiveTripStatus == "searching" ||
                firebaseLiveTripStatus == "searching_driver" -> "Waiting for driver to accept"
        firebaseLiveTripStatus == "accepted" -> "$driverDisplayName accepted your ride"
        firebaseLiveTripStatus == "driver_arriving" -> "Driver is coming to pickup"
        firebaseLiveTripStatus == "ride_started" -> "Trip is currently in progress"
        uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER -> "Finding your driver"
        uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND -> "$driverDisplayName accepted your ride"
        uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> "Driver is coming to pickup"
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
        isCompactTrackingMode &&
                !overlayVisible &&
                !firebaseTripCancelledByDriver &&
                !firebaseTripCompleted &&
                visibleDriverLatLng != null

    val premiumMapStatusText = when {
        firebaseLiveTripStatus == "pending" ||
                firebaseLiveTripStatus == "requested" ||
                firebaseLiveTripStatus == "searching" ||
                firebaseLiveTripStatus == "searching_driver" -> "Finding driver"
        firebaseLiveTripStatus == "accepted" -> "Driver accepted"
        firebaseLiveTripStatus == "driver_arriving" -> "Driver is coming"
        firebaseLiveTripStatus == "ride_started" -> "Trip in progress"
        else -> when (currentTripStatus) {
            RideitTripStatus.SearchingDriver -> "Finding driver"
            RideitTripStatus.DriverFound -> "Driver assigned"
            RideitTripStatus.DriverArriving -> "Driver is coming"
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

    fun cancelCurrentRide() {
        if (firebaseTripCompleted || firebaseTripCancelledByDriver) {
            resetCompletedTripUi()
            return
        }

        val requestId = activeRideRequestId

        if (requestId.isNullOrBlank()) {
            resetCompletedTripUi()
            return
        }

        if (isCancellingRideRequest) return

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
                showPanel = true

                scope.launch {
                    snackbarHostState.showSnackbar("Ride cancelled successfully.")
                }
            },
            onError = { error ->
                isCancellingRideRequest = false
                firebaseRideError = error
            }
        )
    }

    LaunchedEffect(cameraPositionState.isMoving, overlayVisible, isCompactTrackingMode) {
        if (overlayVisible || isCompactTrackingMode) {
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
                    Marker(
                        state = MarkerState(it),
                        title = "Your location",
                        snippet = "Current GPS pickup location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
                    )
                }

                uiState.dropoffLatLng?.let {
                    Marker(state = MarkerState(it), title = "Dropoff")
                }

                visibleDriverLatLng?.let {
                    Marker(
                        state = MarkerState(it),
                        title = "🚗 Driver car",
                        snippet = when (firebaseLiveTripStatus) {
                            "pending", "requested", "searching", "searching_driver" -> "Waiting for driver"
                            "accepted" -> "Driver accepted your ride"
                            "driver_arriving" -> "Driver is coming to pickup"
                            "ride_started" -> "Trip in progress"
                            else -> "Driver is on the way"
                        },
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }

                if (uiState.routePoints.size >= 2) {
                    Polyline(
                        points = uiState.routePoints,
                        width = 10f,
                        color = Color.White,
                        geodesic = false
                    )

                    Polyline(
                        points = uiState.routePoints,
                        width = 6f,
                        color = Color(0xFF7B1DE8),
                        geodesic = false
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

        RiderMapTopChrome(
            visible = shouldShowRiderTopHeader,
            showLocationCard = showLocationCard,
            locationLabel = if (uiState.pickupLatLng != null) "Current area" else "GPS off",
            onPermissionClick = {
                requestLocationPermissionAndGps()
            },
            onDismissLocationCard = {
                showLocationCard = false
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
        )

        MapFloatingControls(
            visible = false,
            onMyLocation = {
                requestLocationPermissionAndGps()
            },
            onZoomIn = {
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 350)
                }
            },
            onZoomOut = {
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 350)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp, top = 44.dp)
        )

        PremiumTripStatusBanner(
            status = currentTripStatus,
            driverName = firebaseDriverName ?: uiState.driver?.name,
            etaText = uiState.driver?.arrivalTime,
            vehicleText = uiState.driver?.vehicleName,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = if (shouldShowRiderTopHeader && showLocationCard) 144.dp else 82.dp)
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
                    top = if (currentTripStatus == RideitTripStatus.Idle) {
                        if (showLocationCard) 170.dp else 104.dp
                    } else {
                        116.dp
                    },
                    start = 16.dp,
                    end = 120.dp
                )
        )

        RiderActiveTripCompactCard(
            visible = shouldShowDriverMiniCard,
            driverName = driverDisplayName,
            phoneNumber = driverPhone,
            vehicleModel = driverVehicleModel,
            vehicleNumber = driverVehicleNumber,
            rating = driverRating,
            arrivalTime = driverArrivalTime,
            statusText = driverMiniStatusText,
            progress = driverMiniProgress,
            isCancelling = isCancellingRideRequest,
            onCancelRide = {
                cancelCurrentRide()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        )

        AnimatedVisibility(
            visible = showPanel && !overlayVisible && !isCompactTrackingMode,
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
                isPanelExpanded = isBottomPanelExpanded,
                onTogglePanelExpanded = {
                    isBottomPanelExpanded = !isBottomPanelExpanded
                },
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
                            firebaseRideMessage = "Ride request saved. Waiting for a driver to accept."
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
                    cancelCurrentRide()
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
                showReceiptPreviewSheet = true
            },
            onSubmitRating = { rating, tags, feedback ->
                if (isSavingFeedback) return@PremiumRideCompletionSheet

                val requestIdForFeedback = completedRideRequestId ?: activeRideRequestId

                submittedRating = rating
                showRideCompletionSheet = false
                showReceiptPreviewSheet = true

                if (requestIdForFeedback.isNullOrBlank()) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Receipt opened. Completed ride feedback could not be saved because ride ID was missing."
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
                        submittedRating = rating
                        showRideCompletionSheet = false
                        showReceiptPreviewSheet = true

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Receipt opened. Feedback save issue: $error"
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
private fun RiderMapTopChrome(
    visible: Boolean,
    showLocationCard: Boolean,
    locationLabel: String,
    onPermissionClick: () -> Unit,
    onDismissLocationCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, end = 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.98f),
                    shadowElevation = 10.dp
                ) {
                    Text(
                        text = "RideIt",
                        color = Color(0xFF8A35F2),
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.98f),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (locationLabel == "GPS off") {
                                        Color(0xFFEF4444)
                                    } else {
                                        Color(0xFF22C55E)
                                    }
                                )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = locationLabel,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (showLocationCard) {
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.97f),
                    shadowElevation = 14.dp,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF1ECFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📍",
                                color = Color(0xFF8A35F2),
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable location",
                                color = Color(0xFF111827),
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "Use GPS for nearby rides",
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            modifier = Modifier.clickable { onPermissionClick() },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF8A35F2)
                        ) {
                            Text(
                                text = "Share",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { onDismissLocationCard() },
                            shape = CircleShape,
                            color = Color(0xFFF3F4F6)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "×",
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapFloatingControls(
    visible: Boolean,
    onMyLocation: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FloatingMapButton(text = "⌖", onClick = onMyLocation)

            Spacer(modifier = Modifier.height(10.dp))

            FloatingMapButton(text = "+", onClick = onZoomIn)

            Spacer(modifier = Modifier.height(10.dp))

            FloatingMapButton(text = "−", onClick = onZoomOut)
        }
    }
}

@Composable
private fun FloatingMapButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 12.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun RiderActiveTripCompactCard(
    visible: Boolean,
    driverName: String,
    phoneNumber: String,
    vehicleModel: String,
    vehicleNumber: String,
    rating: String,
    arrivalTime: String,
    statusText: String,
    progress: Float,
    isCancelling: Boolean,
    onCancelRide: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.98f),
            shadowElevation = 18.dp,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚗", style = MaterialTheme.typography.titleLarge)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = driverName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = "⭐ $rating",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFF2563EB),
                    trackColor = Color(0xFFE5E7EB)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF8FAFC)
                ) {
                    Column(modifier = Modifier.padding(13.dp)) {
                        InfoLine(label = "Phone", value = phoneNumber)
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoLine(label = "Car", value = vehicleModel)
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoLine(label = "Vehicle No.", value = vehicleNumber)
                        Spacer(modifier = Modifier.height(6.dp))
                        InfoLine(label = "ETA", value = arrivalTime)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    enabled = !isCancelling,
                    onClick = onCancelRide,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Text(
                        text = if (isCancelling) "Cancelling..." else "Cancel Ride",
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280),
            modifier = Modifier.width(86.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111827),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
    isPanelExpanded: Boolean,
    onTogglePanelExpanded: () -> Unit,
    onConfirmRideWithFirebase: () -> Unit,
    onCancelRideWithFirebase: () -> Unit,
    onCompleteRideClicked: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(isPanelExpanded) {
                var totalDrag = 0f

                detectVerticalDragGestures(
                    onDragStart = {
                        totalDrag = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (totalDrag < -42f && !isPanelExpanded) {
                            onTogglePanelExpanded()
                        }

                        if (totalDrag > 42f && isPanelExpanded) {
                            onTogglePanelExpanded()
                        }
                    }
                )
            },
        shape = RoundedCornerShape(
            topStart = 34.dp,
            topEnd = 34.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        color = Color.White,
        shadowElevation = 18.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = if (isPanelExpanded) 540.dp else 124.dp)
                .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFC9CBD3))
                    .align(Alignment.CenterHorizontally)
                    .clickable { onTogglePanelExpanded() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!isPanelExpanded) {
                CollapsedRideSearchPanel(
                    pickupText = uiState.pickupText.ifBlank { "Current location" },
                    dropoffText = uiState.dropoffText.ifBlank { "Where to?" },
                    onExpandClick = onTogglePanelExpanded
                )
                return@Column
            }

            when {
                uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER ||
                        firebaseLiveTripStatus == "pending" ||
                        firebaseLiveTripStatus == "requested" ||
                        firebaseLiveTripStatus == "searching" ||
                        firebaseLiveTripStatus == "searching_driver" -> {
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
                        onQuickPlaceSelected = mapViewModel::onQuickPlaceSelected,
                        onSearchClick = mapViewModel::onSearchClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsedRideSearchPanel(
    pickupText: String,
    dropoffText: String,
    onExpandClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onExpandClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8A35F2).copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⌃",
                    color = Color(0xFF8A35F2),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dropoffText,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = pickupText,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "⌃",
                color = Color(0xFF8A35F2),
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun SearchContent(
    uiState: MapUiState,
    onPickupChanged: (String) -> Unit,
    onDropoffChanged: (String) -> Unit,
    onSuggestionSelected: (LocationSuggestion) -> Unit,
    onQuickPlaceSelected: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Text(
        text = "Where are you going?",
        fontWeight = FontWeight.Black,
        color = Color(0xFF111827),
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(12.dp))

    ModernLocationField(
        value = uiState.pickupText.ifBlank { "Current location" },
        leadingColor = Color(0xFF22C55E),
        placeholder = "Current location",
        trailing = "×",
        onClick = { onPickupChanged(uiState.pickupText) }
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "⋮",
        color = Color(0xFFD1D5DB),
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(start = 22.dp)
    )

    Spacer(modifier = Modifier.height(6.dp))

    ModernEditableLocationField(
        value = uiState.dropoffText,
        onValueChange = onDropoffChanged
    )

    if (uiState.locationSuggestions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
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
                        Text(
                            text = suggestion.title,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )

                        Text(
                            text = suggestion.fullAddress,
                            color = Color(0xFF777777),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickPlaceChip(
                icon = "🏠",
                text = "Home",
                modifier = Modifier.weight(1f),
                onClick = { onQuickPlaceSelected("home") }
            )

            QuickPlaceChip(
                icon = "💼",
                text = "Work",
                modifier = Modifier.weight(1f),
                onClick = { onQuickPlaceSelected("work") }
            )

            QuickPlaceChip(
                icon = "🛍️",
                text = "Mall",
                modifier = Modifier.weight(1f),
                onClick = { onQuickPlaceSelected("mall") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickPlaceChip(
                icon = "✈️",
                text = "Airport",
                modifier = Modifier.weight(1f),
                onClick = { onQuickPlaceSelected("airport") }
            )

            QuickPlaceChip(
                icon = "🍽️",
                text = "Restaurant",
                modifier = Modifier.weight(1f),
                onClick = { onQuickPlaceSelected("restaurant") }
            )
        }
    }

    uiState.errorMessage?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = it,
            color = if (
                it.contains("Select", ignoreCase = true) ||
                it.contains("Searching", ignoreCase = true) ||
                it.contains("Detecting", ignoreCase = true)
            ) {
                Color(0xFF6B7280)
            } else {
                Color.Red
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable { onSearchClick() },
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        shadowElevation = 14.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF9E3BFF),
                            Color(0xFF7B1DE8)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔍   Search Ride",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ModernLocationField(
    value: String,
    leadingColor: Color,
    placeholder: String,
    trailing: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(leadingColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = value.ifBlank { placeholder },
                color = Color(0xFF111827),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = trailing,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ModernEditableLocationField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        placeholder = {
            Text(
                text = "Where to?",
                color = Color(0xFF9CA3AF)
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8A35F2))
            )
        },
        trailingIcon = {
            Text(
                text = "🔍",
                color = Color(0xFF8A35F2)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(0xFF111827),
            unfocusedTextColor = Color(0xFF111827),
            focusedBorderColor = Color(0xFFE5E7EB),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = Color(0xFF8A35F2)
        )
    )
}

@Composable
private fun QuickPlaceChip(
    icon: String,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(36.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(50),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color(0xFFE5E7EB),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(icon, style = MaterialTheme.typography.labelMedium)

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = text,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
                    firebaseLiveTripStatus == "searching_driver" -> "Finding your driver"
            firebaseLiveTripStatus == "accepted" -> "Driver accepted"
            firebaseLiveTripStatus == "driver_arriving" -> "Driver is coming to pickup"
            firebaseLiveTripStatus == "ride_started" -> "Trip in progress"
            firebaseDriverName != null -> "Driver accepted"
            uiState.rideRequestStatus == RideRequestStatus.SEARCHING_DRIVER -> "Finding your driver"
            uiState.rideRequestStatus == RideRequestStatus.DRIVER_FOUND -> "Driver found"
            uiState.rideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> "Driver is coming to pickup"
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

    Spacer(modifier = Modifier.height(14.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF8F7FF)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Trip details",
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pickup: ${uiState.pickupText.ifBlank { "Current location" }}",
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Dropoff: ${uiState.dropoffText.ifBlank { "Selected destination" }}",
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (isCancellingRideRequest) {
        Spacer(modifier = Modifier.height(12.dp))

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
            .height(98.dp)
            .clickable { onClick() }
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) mainColor else Color(0xFFE7E7E7),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) lightColor else Color.White,
        shadowElevation = if (selected) 8.dp else 0.dp,
        tonalElevation = if (selected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .width(102.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(22.dp),
                color = if (selected) {
                    Color.White.copy(alpha = 0.82f)
                } else {
                    Color(0xFFF8FAFC)
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ProfessionalVehicleIcon(
                        rideTitle = option.title,
                        accentColor = if (selected) mainColor else Color(0xFF6B7280),
                        selected = selected,
                        modifier = Modifier
                            .width(88.dp)
                            .height(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    fontWeight = FontWeight.Black,
                    color = if (selected) mainColor else Color(0xFF111827),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = option.subtitle,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "⏱ ${option.estimatedTime} • 👤 ${seatsForRide(option.title)} seats",
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = option.estimatedFare,
                    fontWeight = FontWeight.Black,
                    color = if (selected) mainColor else Color(0xFF111827),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )

                if (selected) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(mainColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfessionalVehicleIcon(
    rideTitle: String,
    accentColor: Color,
    selected: Boolean,
    modifier: Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val bodyColor = if (selected) accentColor else Color(0xFF9CA3AF)
        val roofColor = if (selected) accentColor.copy(alpha = 0.92f) else Color(0xFF6B7280)
        val glassColor = Color(0xFFEFF6FF)
        val wheelColor = Color(0xFF111827)
        val wheelInnerColor = if (selected) accentColor.copy(alpha = 0.88f) else Color(0xFFD1D5DB)
        val highlightColor = Color.White.copy(alpha = if (selected) 0.72f else 0.55f)

        drawRoundRect(
            color = Color.Black.copy(alpha = 0.08f),
            topLeft = Offset(w * 0.10f, h * 0.73f),
            size = Size(w * 0.78f, h * 0.10f),
            cornerRadius = CornerRadius(h * 0.06f, h * 0.06f)
        )

        when (rideTitle.lowercase()) {
            "business" -> {
                drawRoundRect(
                    color = bodyColor,
                    topLeft = Offset(w * 0.08f, h * 0.42f),
                    size = Size(w * 0.84f, h * 0.25f),
                    cornerRadius = CornerRadius(h * 0.07f, h * 0.07f)
                )

                drawRoundRect(
                    color = roofColor,
                    topLeft = Offset(w * 0.24f, h * 0.25f),
                    size = Size(w * 0.43f, h * 0.24f),
                    cornerRadius = CornerRadius(h * 0.08f, h * 0.08f)
                )

                drawRoundRect(
                    color = glassColor,
                    topLeft = Offset(w * 0.31f, h * 0.30f),
                    size = Size(w * 0.13f, h * 0.12f),
                    cornerRadius = CornerRadius(h * 0.03f, h * 0.03f)
                )

                drawRoundRect(
                    color = glassColor,
                    topLeft = Offset(w * 0.47f, h * 0.30f),
                    size = Size(w * 0.15f, h * 0.12f),
                    cornerRadius = CornerRadius(h * 0.03f, h * 0.03f)
                )

                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(w * 0.15f, h * 0.46f),
                    size = Size(w * 0.52f, h * 0.035f),
                    cornerRadius = CornerRadius(h * 0.02f, h * 0.02f)
                )
            }

            "comfort" -> {
                drawRoundRect(
                    color = bodyColor,
                    topLeft = Offset(w * 0.09f, h * 0.44f),
                    size = Size(w * 0.82f, h * 0.24f),
                    cornerRadius = CornerRadius(h * 0.10f, h * 0.10f)
                )

                drawRoundRect(
                    color = roofColor,
                    topLeft = Offset(w * 0.27f, h * 0.25f),
                    size = Size(w * 0.40f, h * 0.26f),
                    cornerRadius = CornerRadius(h * 0.10f, h * 0.10f)
                )

                drawRoundRect(
                    color = glassColor,
                    topLeft = Offset(w * 0.34f, h * 0.31f),
                    size = Size(w * 0.11f, h * 0.12f),
                    cornerRadius = CornerRadius(h * 0.03f, h * 0.03f)
                )

                drawRoundRect(
                    color = glassColor,
                    topLeft = Offset(w * 0.48f, h * 0.31f),
                    size = Size(w * 0.13f, h * 0.12f),
                    cornerRadius = CornerRadius(h * 0.03f, h * 0.03f)
                )

                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(w * 0.17f, h * 0.49f),
                    size = Size(w * 0.44f, h * 0.035f),
                    cornerRadius = CornerRadius(h * 0.02f, h * 0.02f)
                )
            }

            else -> {
                drawRoundRect(
                    color = bodyColor,
                    topLeft = Offset(w * 0.12f, h * 0.46f),
                    size = Size(w * 0.76f, h * 0.23f),
                    cornerRadius = CornerRadius(h * 0.11f, h * 0.11f)
                )

                drawRoundRect(
                    color = roofColor,
                    topLeft = Offset(w * 0.31f, h * 0.28f),
                    size = Size(w * 0.32f, h * 0.24f),
                    cornerRadius = CornerRadius(h * 0.10f, h * 0.10f)
                )

                drawRoundRect(
                    color = glassColor,
                    topLeft = Offset(w * 0.37f, h * 0.33f),
                    size = Size(w * 0.20f, h * 0.12f),
                    cornerRadius = CornerRadius(h * 0.03f, h * 0.03f)
                )

                drawRoundRect(
                    color = highlightColor,
                    topLeft = Offset(w * 0.20f, h * 0.51f),
                    size = Size(w * 0.35f, h * 0.035f),
                    cornerRadius = CornerRadius(h * 0.02f, h * 0.02f)
                )
            }
        }

        drawCircle(
            color = wheelColor,
            radius = h * 0.13f,
            center = Offset(w * 0.28f, h * 0.69f)
        )

        drawCircle(
            color = wheelColor,
            radius = h * 0.13f,
            center = Offset(w * 0.72f, h * 0.69f)
        )

        drawCircle(
            color = wheelInnerColor,
            radius = h * 0.065f,
            center = Offset(w * 0.28f, h * 0.69f)
        )

        drawCircle(
            color = wheelInnerColor,
            radius = h * 0.065f,
            center = Offset(w * 0.72f, h * 0.69f)
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.55f),
            topLeft = Offset(w * 0.80f, h * 0.49f),
            size = Size(w * 0.06f, h * 0.035f),
            cornerRadius = CornerRadius(h * 0.02f, h * 0.02f)
        )

        drawRoundRect(
            color = Color(0xFFFFF7CC),
            topLeft = Offset(w * 0.12f, h * 0.51f),
            size = Size(w * 0.045f, h * 0.032f),
            cornerRadius = CornerRadius(h * 0.02f, h * 0.02f)
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
        cleanStatus == "accepted" ||
                cleanStatus == "driver_arriving" ||
                cleanStatus == "ride_started" ||
                localRideRequestStatus == RideRequestStatus.DRIVER_FOUND ||
                localRideRequestStatus == RideRequestStatus.DRIVER_ARRIVING ||
                localRideRequestStatus == RideRequestStatus.RIDE_STARTED

    if (!shouldShowDriver) return null

    currentDriverLatLng?.let { return it }

    val pickup = pickupLatLng ?: fallbackLocation
    val dropoff = dropoffLatLng ?: pickup

    return when {
        cleanStatus == "accepted" || localRideRequestStatus == RideRequestStatus.DRIVER_FOUND -> {
            LatLng(
                (fallbackLocation.latitude * 0.35) + (pickup.latitude * 0.65),
                (fallbackLocation.longitude * 0.35) + (pickup.longitude * 0.65)
            )
        }

        cleanStatus == "driver_arriving" || localRideRequestStatus == RideRequestStatus.DRIVER_ARRIVING -> {
            LatLng(
                (fallbackLocation.latitude * 0.10) + (pickup.latitude * 0.90),
                (fallbackLocation.longitude * 0.10) + (pickup.longitude * 0.90)
            )
        }

        cleanStatus == "ride_started" || localRideRequestStatus == RideRequestStatus.RIDE_STARTED -> {
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