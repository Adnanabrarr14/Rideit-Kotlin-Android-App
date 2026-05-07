package com.example.rideit.driver.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

private enum class DriverActiveScreen {
    Home,
    ActiveTrip,
    Wallet,
    TripHistory,
    Documents,
    Support
}

private data class PendingRideRequest(
    val requestId: String,
    val riderEmail: String,
    val pickup: String,
    val dropoff: String,
    val rideType: String,
    val fare: String,
    val status: String
)

@Composable
fun DriverHomeScreen() {
    var isOnline by remember { mutableStateOf(false) }
    var showRideRequest by remember { mutableStateOf(false) }
    var activeScreen by remember { mutableStateOf(DriverActiveScreen.Home) }

    var pendingRideRequest by remember { mutableStateOf<PendingRideRequest?>(null) }
    var isRideActionLoading by remember { mutableStateOf(false) }
    var firebaseStatusText by remember { mutableStateOf("Driver is offline.") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }

    BackHandler(enabled = activeScreen != DriverActiveScreen.Home) {
        activeScreen = DriverActiveScreen.Home
        showRideRequest = false
    }

    DisposableEffect(isOnline) {
        var listenerRegistration: ListenerRegistration? = null

        if (isOnline) {
            firebaseStatusText = "Listening for pending Firebase ride requests..."

            listenerRegistration = firestore.collection("ride_requests")
                .whereIn("status", listOf("pending", "requested"))
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        firebaseStatusText = error.message ?: "Failed to load ride requests."
                        pendingRideRequest = null
                        showRideRequest = false
                        return@addSnapshotListener
                    }

                    val document = snapshots?.documents?.firstOrNull()

                    if (document == null) {
                        pendingRideRequest = null
                        showRideRequest = false
                        firebaseStatusText = "Online. Waiting for new rider requests..."
                        return@addSnapshotListener
                    }

                    val pickup = document.getString("pickupAddress")
                        ?: document.getString("pickupText")
                        ?: "Pickup location"

                    val dropoff = document.getString("dropoffAddress")
                        ?: document.getString("dropText")
                        ?: document.getString("dropoffText")
                        ?: "Dropoff location"

                    val riderEmail = document.getString("riderEmail")
                        ?: document.getString("userEmail")
                        ?: "Rider"

                    val rideType = document.getString("rideType")
                        ?: "Rideit"

                    val fare = document.getString("fareEstimate")
                        ?: document.getString("fare")
                        ?: "Fare pending"

                    val status = document.getString("status")
                        ?: "pending"

                    pendingRideRequest = PendingRideRequest(
                        requestId = document.id,
                        riderEmail = riderEmail,
                        pickup = pickup,
                        dropoff = dropoff,
                        rideType = rideType,
                        fare = fare,
                        status = status
                    )

                    showRideRequest = true
                    firebaseStatusText = "New Firebase ride request received."
                }
        } else {
            pendingRideRequest = null
            showRideRequest = false
            firebaseStatusText = "Driver is offline."
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    LaunchedEffect(isOnline, pendingRideRequest) {
        if (!isOnline) {
            return@LaunchedEffect
        }

        if (pendingRideRequest != null) {
            snackbarHostState.showSnackbar("New ride request available.")
        }
    }

    when (activeScreen) {
        DriverActiveScreen.ActiveTrip -> {
            DriverTripScreen(
                driverName = "Shameer Khan",
                onBackToDriverHome = {
                    activeScreen = DriverActiveScreen.Home
                    isOnline = true
                    showRideRequest = false

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Returned to Driver Home."
                        )
                    }
                }
            )
            return
        }

        DriverActiveScreen.Wallet -> {
            DriverWalletScreen(
                driverName = "Shameer Khan",
                onBackClick = {
                    activeScreen = DriverActiveScreen.Home
                }
            )
            return
        }

        DriverActiveScreen.TripHistory -> {
            DriverTripHistoryScreen(
                driverName = "Shameer Khan",
                onBackClick = {
                    activeScreen = DriverActiveScreen.Home
                }
            )
            return
        }

        DriverActiveScreen.Documents -> {
            DriverDocumentsScreen(
                driverName = "Shameer Khan",
                onBackClick = {
                    activeScreen = DriverActiveScreen.Home
                }
            )
            return
        }

        DriverActiveScreen.Support -> {
            DriverSupportScreen(
                driverName = "Shameer Khan",
                onBackClick = {
                    activeScreen = DriverActiveScreen.Home
                }
            )
            return
        }

        DriverActiveScreen.Home -> {
            // Continue below.
        }
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
            DriverHeaderCard(
                driverName = "Shameer Khan",
                isOnline = isOnline,
                onOnlineChanged = {
                    isOnline = it

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = if (it) {
                                "You are online. Listening for Firebase ride requests."
                            } else {
                                "You are offline."
                            }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverFirebaseStatusCard(
                isOnline = isOnline,
                statusText = firebaseStatusText,
                pendingRideRequest = pendingRideRequest
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverStatsGrid()

            Spacer(modifier = Modifier.height(16.dp))

            DriverTodayEarningsCard()

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showRideRequest && pendingRideRequest != null,
                enter = fadeIn(animationSpec = tween(220)) + scaleIn(
                    animationSpec = tween(240),
                    initialScale = 0.94f
                ),
                exit = fadeOut(animationSpec = tween(180)) + scaleOut(
                    animationSpec = tween(180),
                    targetScale = 0.94f
                )
            ) {
                pendingRideRequest?.let { request ->
                    IncomingRideRequestCard(
                        request = request,
                        isLoading = isRideActionLoading,
                        onAcceptClick = {
                            if (isRideActionLoading) {
                                return@IncomingRideRequestCard
                            }

                            isRideActionLoading = true

                            val driverId = FirebaseManager.currentUserId().orEmpty()
                            val driverEmail = FirebaseManager.currentUserEmail().orEmpty()

                            firestore.collection("ride_requests")
                                .document(request.requestId)
                                .update(
                                    mapOf(
                                        "status" to "accepted",
                                        "driverId" to driverId,
                                        "driverEmail" to driverEmail,
                                        "driverName" to "Shameer Khan",
                                        "acceptedAt" to Timestamp.now(),
                                        "updatedAt" to Timestamp.now()
                                    )
                                )
                                .addOnSuccessListener {
                                    isRideActionLoading = false
                                    showRideRequest = false
                                    pendingRideRequest = null
                                    activeScreen = DriverActiveScreen.ActiveTrip
                                }
                                .addOnFailureListener { exception ->
                                    isRideActionLoading = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            exception.message ?: "Failed to accept ride."
                                        )
                                    }
                                }
                        },
                        onDeclineClick = {
                            if (isRideActionLoading) {
                                return@IncomingRideRequestCard
                            }

                            isRideActionLoading = true

                            val driverId = FirebaseManager.currentUserId().orEmpty()
                            val driverEmail = FirebaseManager.currentUserEmail().orEmpty()

                            firestore.collection("ride_requests")
                                .document(request.requestId)
                                .update(
                                    mapOf(
                                        "status" to "declined",
                                        "declinedByDriverId" to driverId,
                                        "declinedByDriverEmail" to driverEmail,
                                        "declinedAt" to Timestamp.now(),
                                        "updatedAt" to Timestamp.now()
                                    )
                                )
                                .addOnSuccessListener {
                                    isRideActionLoading = false
                                    showRideRequest = false
                                    pendingRideRequest = null

                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Ride request declined."
                                        )
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    isRideActionLoading = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            exception.message ?: "Failed to decline ride."
                                        )
                                    }
                                }
                        }
                    )
                }
            }

            if (showRideRequest && pendingRideRequest != null) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            DriverQuickActionsCard(
                onActionClick = { action ->
                    when (action) {
                        "Wallet" -> {
                            activeScreen = DriverActiveScreen.Wallet
                        }

                        "Trip history" -> {
                            activeScreen = DriverActiveScreen.TripHistory
                        }

                        "Vehicle documents" -> {
                            activeScreen = DriverActiveScreen.Documents
                        }

                        "Support" -> {
                            activeScreen = DriverActiveScreen.Support
                        }

                        else -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "$action will be connected in upcoming driver phases."
                                )
                            }
                        }
                    }
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
private fun DriverHeaderCard(
    driverName: String,
    isOnline: Boolean,
    onOnlineChanged: (Boolean) -> Unit
) {
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
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF111827),
                            Color(0xFF1F2937),
                            Color(0xFF8A35F2)
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
                        text = "S",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Good day",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.78f)
                    )

                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OnlinePulseDot(active = isOnline)
            }

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOnline) "You are online" else "You are offline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = if (isOnline) {
                                "Listening for real Firebase ride requests."
                            } else {
                                "Go online to start receiving rides."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.76f)
                        )
                    }

                    Switch(
                        checked = isOnline,
                        onCheckedChange = onOnlineChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF16A34A),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.28f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun OnlinePulseDot(
    active: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "driver_online_dot")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 850,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driver_online_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .alpha(if (active) pulseAlpha else 1f)
                .background(
                    color = if (active) Color(0xFF22C55E) else Color(0xFF9CA3AF),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = if (active) "Live" else "Off",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun DriverFirebaseStatusCard(
    isOnline: Boolean,
    statusText: String,
    pendingRideRequest: PendingRideRequest?
) {
    val statusColor = when {
        !isOnline -> Color(0xFF6B7280)
        pendingRideRequest != null -> Color(0xFF16A34A)
        else -> Color(0xFF8A35F2)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (pendingRideRequest != null) "✓" else "•",
                    color = statusColor,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Firebase ride feed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun DriverStatsGrid() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriverStatCard(
                title = "Today",
                value = "₨ 6,850",
                subtitle = "Earnings",
                modifier = Modifier.weight(1f)
            )

            DriverStatCard(
                title = "Trips",
                value = "14",
                subtitle = "Completed",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriverStatCard(
                title = "Rating",
                value = "4.9",
                subtitle = "Excellent",
                modifier = Modifier.weight(1f)
            )

            DriverStatCard(
                title = "Accept",
                value = "92%",
                subtitle = "Rate",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DriverStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8A35F2)
            )
        }
    }
}

@Composable
private fun DriverTodayEarningsCard() {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily target",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₨ 6,850 of ₨ 10,000 completed",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF8A35F2).copy(alpha = 0.10f)
                ) {
                    Text(
                        text = "68%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8A35F2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { 0.68f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF8A35F2),
                trackColor = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EarningsMiniMetric(
                    title = "Online",
                    value = "5h 20m"
                )

                EarningsMiniMetric(
                    title = "Distance",
                    value = "84 km"
                )

                EarningsMiniMetric(
                    title = "Bonus",
                    value = "₨ 750"
                )
            }
        }
    }
}

@Composable
private fun EarningsMiniMetric(
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

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111827)
        )
    }
}

@Composable
private fun IncomingRideRequestCard(
    request: PendingRideRequest,
    isLoading: Boolean,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 16.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8A35F2).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8A35F2)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Firebase ride request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = request.riderEmail,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF16A34A).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = request.fare,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF16A34A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF8A35F2).copy(alpha = 0.10f)
            ) {
                Text(
                    text = "Ride type: ${request.rideType}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8A35F2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RideRoutePreview(
                pickup = request.pickup,
                dropoff = request.dropoff
            )

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
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
                    text = "Updating Firebase ride request...",
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                OutlinedButton(
                    enabled = !isLoading,
                    onClick = onDeclineClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Text(
                        text = "Decline",
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    enabled = !isLoading,
                    onClick = onAcceptClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A),
                        disabledContainerColor = Color(0xFF16A34A).copy(alpha = 0.55f)
                    )
                ) {
                    Text(
                        text = "Accept",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RideRoutePreview(
    pickup: String,
    dropoff: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            RouteLineItem(
                dotColor = Color(0xFF16A34A),
                label = "Pickup",
                value = pickup
            )

            Spacer(modifier = Modifier.height(14.dp))

            RouteLineItem(
                dotColor = Color(0xFF8A35F2),
                label = "Dropoff",
                value = dropoff
            )
        }
    }
}

@Composable
private fun RouteLineItem(
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
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

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
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DriverQuickActionsCard(
    onActionClick: (String) -> Unit
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
                text = "Driver tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(14.dp))

            DriverToolRow(
                title = "Wallet",
                subtitle = "Earnings and payouts",
                emoji = "₨",
                onClick = { onActionClick("Wallet") }
            )

            DriverToolRow(
                title = "Trip history",
                subtitle = "Completed driver rides",
                emoji = "↗",
                onClick = { onActionClick("Trip history") }
            )

            DriverToolRow(
                title = "Vehicle documents",
                subtitle = "CNIC, license and car docs",
                emoji = "✓",
                onClick = { onActionClick("Vehicle documents") }
            )

            DriverToolRow(
                title = "Support",
                subtitle = "Get help from Rideit",
                emoji = "?",
                onClick = { onActionClick("Support") }
            )
        }
    }
}

@Composable
private fun DriverToolRow(
    title: String,
    subtitle: String,
    emoji: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 11.dp),
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
                text = emoji,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF8A35F2)
            )
        }

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "›",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9CA3AF)
        )
    }
}