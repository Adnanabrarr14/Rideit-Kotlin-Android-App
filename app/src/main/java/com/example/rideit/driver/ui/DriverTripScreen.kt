package com.example.rideit.driver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun DriverTripScreen(
    driverName: String = "Shameer Khan",
    rideRequestId: String? = null,
    onBackToDriverHome: () -> Unit
) {
    val defaultPickupLatLng = LatLng(33.6844, 73.0479)
    val defaultDropoffLatLng = LatLng(33.7008, 73.0650)
    val defaultDriverLatLng = LatLng(33.6920, 73.0560)

    var tripStep by remember { mutableIntStateOf(0) }
    var isFirebaseActionLoading by remember { mutableStateOf(false) }
    var firebaseTripMessage by remember { mutableStateOf<String?>(null) }
    var firebaseTripError by remember { mutableStateOf<String?>(null) }

    var riderEmail by remember { mutableStateOf("Rider") }
    var pickupText by remember { mutableStateOf("Pickup location") }
    var dropoffText by remember { mutableStateOf("Dropoff location") }
    var rideType by remember { mutableStateOf("Rideit") }
    var fareText by remember { mutableStateOf("Fare pending") }

    var pickupLatLng by remember { mutableStateOf(defaultPickupLatLng) }
    var dropoffLatLng by remember { mutableStateOf(defaultDropoffLatLng) }
    var driverLatLng by remember { mutableStateOf(defaultDriverLatLng) }

    var tripCancelledSafely by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }

    LaunchedEffect(rideRequestId) {
        val requestId = rideRequestId

        if (requestId.isNullOrBlank()) {
            firebaseTripError = "Firebase ride request ID not found."
            return@LaunchedEffect
        }

        firestore.collection("ride_requests")
            .document(requestId)
            .get()
            .addOnSuccessListener { document ->
                riderEmail = document.safeText("riderEmail")
                    .ifBlank { document.safeText("userEmail") }
                    .ifBlank { document.safeText("email") }
                    .ifBlank { "Rider" }

                pickupText = document.safeText("pickupAddress")
                    .ifBlank { document.safeText("pickupText") }
                    .ifBlank { document.safeText("pickup") }
                    .ifBlank { "Pickup location" }

                dropoffText = document.safeText("dropoffAddress")
                    .ifBlank { document.safeText("dropText") }
                    .ifBlank { document.safeText("dropoffText") }
                    .ifBlank { document.safeText("dropoff") }
                    .ifBlank { document.safeText("destination") }
                    .ifBlank { "Dropoff location" }

                rideType = document.safeText("rideType")
                    .ifBlank { document.safeText("selectedRideType") }
                    .ifBlank { "Rideit" }

                fareText = document.safeText("fareEstimate")
                    .ifBlank { document.safeText("fare") }
                    .ifBlank { document.safeText("estimatedFare") }
                    .ifBlank { "Fare pending" }

                pickupLatLng = document.safeLatLng(
                    geoPointFields = listOf("pickupLatLng", "pickupLocation", "pickupGeoPoint"),
                    latFields = listOf("pickupLat", "pickupLatitude", "pickup_lat", "pickup_latitude"),
                    lngFields = listOf("pickupLng", "pickupLongitude", "pickup_lng", "pickup_longitude"),
                    fallback = defaultPickupLatLng
                )

                dropoffLatLng = document.safeLatLng(
                    geoPointFields = listOf("dropoffLatLng", "dropoffLocation", "dropoffGeoPoint", "destinationLatLng"),
                    latFields = listOf("dropoffLat", "dropoffLatitude", "dropoff_lat", "dropoff_latitude", "destinationLat"),
                    lngFields = listOf("dropoffLng", "dropoffLongitude", "dropoff_lng", "dropoff_longitude", "destinationLng"),
                    fallback = defaultDropoffLatLng
                )

                driverLatLng = document.safeLatLng(
                    geoPointFields = listOf("driverLatLng", "driverLocation", "driverGeoPoint"),
                    latFields = listOf("driverLat", "driverLatitude", "driver_lat", "driver_latitude"),
                    lngFields = listOf("driverLng", "driverLongitude", "driver_lng", "driver_longitude"),
                    fallback = midpointLatLng(pickupLatLng, dropoffLatLng)
                )

                when (document.safeText("status").lowercase()) {
                    "driver_arriving" -> tripStep = 1
                    "ride_started" -> tripStep = 2
                    "completed" -> tripStep = 3
                    "cancelled_by_driver" -> {
                        tripStep = 4
                        tripCancelledSafely = true
                    }
                    else -> tripStep = 0
                }

                firebaseTripMessage = "Trip loaded."
                firebaseTripError = null
            }
            .addOnFailureListener { exception ->
                firebaseTripError = exception.message ?: "Failed to load trip."
            }
    }

    fun updateFirebaseTripStatus(
        status: String,
        successMessage: String,
        stepAfterSuccess: Int,
        snackbarMessage: String
    ) {
        val requestId = rideRequestId

        if (requestId.isNullOrBlank()) {
            firebaseTripMessage = null
            firebaseTripError = "Firebase request ID not found."
            return
        }

        if (isFirebaseActionLoading) return

        isFirebaseActionLoading = true
        firebaseTripError = null
        firebaseTripMessage = "Updating trip status..."

        val updateData = when (status) {
            "driver_arriving" -> mapOf(
                "status" to "driver_arriving",
                "driverArrivedAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            "ride_started" -> mapOf(
                "status" to "ride_started",
                "rideStartedAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            else -> mapOf(
                "status" to status,
                "updatedAt" to Timestamp.now()
            )
        }

        firestore.collection("ride_requests")
            .document(requestId)
            .update(updateData)
            .addOnSuccessListener {
                isFirebaseActionLoading = false
                tripStep = stepAfterSuccess
                firebaseTripMessage = successMessage
                firebaseTripError = null

                scope.launch {
                    snackbarHostState.showSnackbar(snackbarMessage)
                }
            }
            .addOnFailureListener { exception ->
                isFirebaseActionLoading = false
                firebaseTripMessage = null
                firebaseTripError = exception.message ?: "Failed to update trip status."
            }
    }

    val stepTitle = when {
        tripCancelledSafely -> "Trip cancelled"
        tripStep == 0 -> "Go to pickup"
        tripStep == 1 -> "Arrived at pickup"
        tripStep == 2 -> "Trip in progress"
        tripStep == 3 -> "Trip completed"
        else -> "Trip cancelled"
    }

    val stepSubtitle = when {
        tripCancelledSafely -> "This trip was cancelled safely. You can return to Driver Home."
        tripStep == 0 -> "Navigate safely to the rider pickup point."
        tripStep == 1 -> "Confirm rider pickup and start the trip."
        tripStep == 2 -> "Follow the route to the dropoff location."
        tripStep == 3 -> "Trip completed successfully."
        else -> "This trip was cancelled safely."
    }

    val progress = when {
        tripCancelledSafely -> 1f
        tripStep == 0 -> 0.33f
        tripStep == 1 -> 0.58f
        tripStep == 2 -> 0.86f
        else -> 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            DriverTripHeader(
                driverName = driverName,
                stepTitle = stepTitle,
                stepSubtitle = stepSubtitle,
                progress = progress,
                cancelled = tripCancelledSafely
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverGoogleRouteMapCard(
                tripStep = tripStep,
                pickupText = pickupText,
                dropoffText = dropoffText,
                pickupLatLng = pickupLatLng,
                dropoffLatLng = dropoffLatLng,
                driverLatLng = driverLatLng,
                cancelled = tripCancelledSafely
            )

            Spacer(modifier = Modifier.height(16.dp))

            firebaseTripMessage?.let {
                DriverTripMessageCard(
                    message = it,
                    success = true
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            firebaseTripError?.let {
                DriverTripMessageCard(
                    message = it,
                    success = false
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            DriverTripRiderCard(
                riderEmail = riderEmail
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripRouteCard(
                pickupText = pickupText,
                dropoffText = dropoffText
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripMetricsCard(
                rideType = rideType,
                fareText = fareText
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripActionCard(
                tripStep = tripStep,
                isFirebaseActionLoading = isFirebaseActionLoading,
                tripCancelledSafely = tripCancelledSafely,
                onPrimaryClick = {
                    when {
                        tripCancelledSafely -> {
                            onBackToDriverHome()
                        }

                        tripStep == 0 -> {
                            updateFirebaseTripStatus(
                                status = "driver_arriving",
                                successMessage = "Driver arrived at pickup. Firebase status updated.",
                                stepAfterSuccess = 1,
                                snackbarMessage = "Arrived at pickup."
                            )
                        }

                        tripStep == 1 -> {
                            updateFirebaseTripStatus(
                                status = "ride_started",
                                successMessage = "Trip started. Firebase status updated.",
                                stepAfterSuccess = 2,
                                snackbarMessage = "Trip started."
                            )
                        }

                        tripStep == 2 -> {
                            val requestId = rideRequestId

                            if (requestId.isNullOrBlank()) {
                                firebaseTripMessage = null
                                firebaseTripError = "Firebase request ID not found."
                                return@DriverTripActionCard
                            }

                            if (isFirebaseActionLoading) return@DriverTripActionCard

                            isFirebaseActionLoading = true
                            firebaseTripError = null
                            firebaseTripMessage = "Completing trip in Firebase..."

                            FirebaseManager.completeDriverTrip(
                                requestId = requestId,
                                onSuccess = {
                                    isFirebaseActionLoading = false
                                    tripStep = 3
                                    firebaseTripMessage = "Trip completed successfully in Firebase."
                                    firebaseTripError = null

                                    scope.launch {
                                        snackbarHostState.showSnackbar("Trip completed in Firebase.")
                                    }
                                },
                                onError = { error ->
                                    isFirebaseActionLoading = false
                                    firebaseTripError = error
                                    firebaseTripMessage = null
                                }
                            )
                        }

                        else -> {
                            onBackToDriverHome()
                        }
                    }
                },
                onSupportClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Driver support will be connected later.")
                    }
                },
                onCancelClick = {
                    val requestId = rideRequestId

                    if (requestId.isNullOrBlank()) {
                        firebaseTripMessage = null
                        firebaseTripError = "Firebase request ID not found."
                        return@DriverTripActionCard
                    }

                    if (isFirebaseActionLoading) return@DriverTripActionCard

                    isFirebaseActionLoading = true
                    firebaseTripError = null
                    firebaseTripMessage = "Cancelling trip in Firebase..."

                    FirebaseManager.cancelDriverTrip(
                        requestId = requestId,
                        onSuccess = {
                            isFirebaseActionLoading = false
                            tripStep = 4
                            tripCancelledSafely = true
                            firebaseTripMessage = "Trip cancelled safely. Tap Back to Driver Home."
                            firebaseTripError = null
                        },
                        onError = { error ->
                            isFirebaseActionLoading = false
                            firebaseTripError = error
                            firebaseTripMessage = null
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
        )
    }
}

@Composable
private fun DriverTripHeader(
    driverName: String,
    stepTitle: String,
    stepSubtitle: String,
    progress: Float,
    cancelled: Boolean
) {
    val mainColor = if (cancelled) Color(0xFFEF4444) else Color(0xFF8A35F2)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF111827),
        shadowElevation = 14.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF111827),
                            Color(0xFF1F2937),
                            mainColor
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = driverName.firstOrNull()?.uppercase() ?: "D",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = if (cancelled) "Cancelled driver trip" else "Active driver trip",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.76f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = if (cancelled) "Cancelled" else "Live",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stepTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stepSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.76f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (cancelled) Color(0xFFEF4444) else Color(0xFF22C55E),
                        trackColor = Color.White.copy(alpha = 0.18f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverGoogleRouteMapCard(
    tripStep: Int,
    pickupText: String,
    dropoffText: String,
    pickupLatLng: LatLng,
    dropoffLatLng: LatLng,
    driverLatLng: LatLng,
    cancelled: Boolean
) {
    val statusText = when {
        cancelled -> "Trip cancelled"
        tripStep == 0 -> "Go to pickup"
        tripStep == 1 -> "At pickup"
        tripStep == 2 -> "12 min to dropoff"
        else -> "Completed"
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(driverLatLng, 12f)
    }

    LaunchedEffect(pickupLatLng, dropoffLatLng, driverLatLng) {
        try {
            val bounds = LatLngBounds.Builder()
                .include(pickupLatLng)
                .include(dropoffLatLng)
                .include(driverLatLng)
                .build()

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 120),
                durationMs = 800
            )
        } catch (_: Exception) {
            try {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(driverLatLng, 13f),
                    durationMs = 500
                )
            } catch (_: Exception) {
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    compassEnabled = false
                )
            ) {
                Polyline(
                    points = when {
                        tripStep >= 2 -> listOf(driverLatLng, dropoffLatLng)
                        else -> listOf(driverLatLng, pickupLatLng, dropoffLatLng)
                    },
                    color = if (cancelled) Color(0xFFEF4444) else Color(0xFF8A35F2),
                    width = 10f
                )

                Marker(
                    state = MarkerState(position = pickupLatLng),
                    title = "Pickup",
                    snippet = pickupText,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )

                Marker(
                    state = MarkerState(position = dropoffLatLng),
                    title = "Dropoff",
                    snippet = dropoffText,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                )

                Marker(
                    state = MarkerState(position = driverLatLng),
                    title = "Driver",
                    snippet = "Current driver position",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = if (cancelled) Color(0xFFEF4444) else Color(0xFF111827)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Text(
                        text = pickupText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF16A34A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = dropoffText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (cancelled) Color(0xFFEF4444) else Color(0xFF8A35F2),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverTripMessageCard(
    message: String,
    success: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (success) Color(0xFFE8FFF1) else Color(0xFFFFE5E5),
        shadowElevation = 6.dp
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(15.dp),
            color = if (success) Color(0xFF16A34A) else Color(0xFFEF4444),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DriverTripRiderCard(
    riderEmail: String
) {
    val riderInitial = riderEmail.firstOrNull()?.uppercase() ?: "R"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8A35F2).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = riderInitial,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8A35F2)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rider",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = riderEmail,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DriverTripRouteCard(
    pickupText: String,
    dropoffText: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Trip route",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverRoutePoint(
                dotColor = Color(0xFF16A34A),
                label = "Pickup",
                value = pickupText
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverRoutePoint(
                dotColor = Color(0xFF8A35F2),
                label = "Dropoff",
                value = dropoffText
            )
        }
    }
}

@Composable
private fun DriverRoutePoint(
    dotColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(13.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DriverTripMetricsCard(
    rideType: String,
    fareText: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DriverTripMetric(title = "Fare", value = normalizeFareText(fareText))
            DriverTripMetric(title = "Ride", value = rideType)
            DriverTripMetric(title = "ETA", value = "15 min")
        }
    }
}

@Composable
private fun DriverTripMetric(
    title: String,
    value: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111827),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DriverTripActionCard(
    tripStep: Int,
    isFirebaseActionLoading: Boolean,
    tripCancelledSafely: Boolean,
    onPrimaryClick: () -> Unit,
    onSupportClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            if (isFirebaseActionLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Color(0xFF8A35F2),
                    trackColor = Color(0xFFE5E7EB)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Updating Firebase trip status...",
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            Button(
                enabled = !isFirebaseActionLoading,
                onClick = onPrimaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tripCancelledSafely) {
                        Color(0xFF8A35F2)
                    } else {
                        when (tripStep) {
                            0 -> Color(0xFF8A35F2)
                            1 -> Color(0xFF16A34A)
                            2 -> Color(0xFF111827)
                            else -> Color(0xFF8A35F2)
                        }
                    }
                )
            ) {
                Text(
                    text = if (tripCancelledSafely) {
                        "Back to Driver Home"
                    } else {
                        when (tripStep) {
                            0 -> "Arrived at Pickup"
                            1 -> "Start Trip"
                            2 -> "Complete Trip"
                            else -> "Back to Driver Home"
                        }
                    },
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(11.dp))

            OutlinedButton(
                enabled = !isFirebaseActionLoading,
                onClick = onSupportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Contact Support",
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = !tripCancelledSafely && tripStep < 3,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(11.dp))

                    OutlinedButton(
                        enabled = !isFirebaseActionLoading,
                        onClick = onCancelClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text(
                            text = "Cancel Trip",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun DocumentSnapshot.safeText(field: String): String {
    return try {
        val value = get(field)
        when (value) {
            null -> ""
            is String -> value
            is Number -> value.toString()
            is Boolean -> value.toString()
            else -> value.toString()
        }
    } catch (_: Exception) {
        ""
    }
}

private fun DocumentSnapshot.safeNumber(field: String): Double? {
    return try {
        val value = get(field)
        when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

private fun DocumentSnapshot.safeLatLng(
    geoPointFields: List<String>,
    latFields: List<String>,
    lngFields: List<String>,
    fallback: LatLng
): LatLng {
    return try {
        geoPointFields.forEach { field ->
            val geoPoint = getGeoPoint(field)
            if (geoPoint != null) {
                return LatLng(geoPoint.latitude, geoPoint.longitude)
            }
        }

        var lat: Double? = null
        var lng: Double? = null

        latFields.forEach { field ->
            if (lat == null) {
                lat = safeNumber(field)
            }
        }

        lngFields.forEach { field ->
            if (lng == null) {
                lng = safeNumber(field)
            }
        }

        if (lat != null && lng != null) {
            LatLng(lat ?: fallback.latitude, lng ?: fallback.longitude)
        } else {
            fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

private fun midpointLatLng(
    first: LatLng,
    second: LatLng
): LatLng {
    return LatLng(
        (first.latitude + second.latitude) / 2.0,
        (first.longitude + second.longitude) / 2.0
    )
}

private fun normalizeFareText(fare: String): String {
    val cleanFare = fare.trim()

    if (cleanFare.isBlank()) return "₨ 0"

    return cleanFare
        .replace("Rs.", "₨")
        .replace("Rs", "₨")
        .replace("PKR", "₨")
}