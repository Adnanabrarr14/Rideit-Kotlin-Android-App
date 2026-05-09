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
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

private enum class DriverHomeDestination {
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

private data class DriverDashboardStats(
    val todayEarnings: Int = 0,
    val todayCompletedTrips: Int = 0,
    val totalCompletedTrips: Int = 0,
    val totalCancelledTrips: Int = 0,
    val averageRating: Double = 0.0,
    val acceptRatePercent: Int = 0,
    val weekEarnings: Int = 0,
    val dailyTarget: Int = 10000
)

@Composable
fun DriverHomeScreen() {
    var isOnline by remember { mutableStateOf(false) }
    var showRideRequest by remember { mutableStateOf(false) }
    var activeDestination by remember { mutableStateOf(DriverHomeDestination.Home) }

    var pendingRideRequest by remember { mutableStateOf<PendingRideRequest?>(null) }
    var activeRideRequestId by remember { mutableStateOf<String?>(null) }

    var isRideActionLoading by remember { mutableStateOf(false) }
    var isCheckingRideRequest by remember { mutableStateOf(false) }

    var firebaseStatusText by remember {
        mutableStateOf("Driver is offline. Turn on Live to receive rides.")
    }

    var dashboardStats by remember { mutableStateOf(DriverDashboardStats()) }
    var isDashboardStatsLoading by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val driverId = remember { FirebaseManager.currentUserId().orEmpty() }

    fun resetRideRequestUi() {
        pendingRideRequest = null
        showRideRequest = false
        isRideActionLoading = false
        isCheckingRideRequest = false
    }

    fun checkForPendingRideRequest() {
        if (!isOnline) {
            scope.launch {
                snackbarHostState.showSnackbar("Turn Live ON first.")
            }
            return
        }

        if (driverId.isBlank()) {
            firebaseStatusText = "Driver account not found. Please login again."
            resetRideRequestUi()
            return
        }

        if (isCheckingRideRequest) return

        isCheckingRideRequest = true
        firebaseStatusText = "Checking ride requests..."
        pendingRideRequest = null
        showRideRequest = false

        firestore.collection("ride_requests")
            .limit(150)
            .get()
            .addOnSuccessListener { snapshots ->
                try {
                    val allDocuments = snapshots.documents

                    val openRequest = allDocuments
                        .filter { document ->
                            val status = document.safeText("status").trim().lowercase()

                            status.isBlank() ||
                                    status == "pending" ||
                                    status == "requested" ||
                                    status == "searching" ||
                                    status == "searching_driver" ||
                                    status == "waiting" ||
                                    status == "waiting_for_driver" ||
                                    status == "driver_pending" ||
                                    status == "looking_for_driver" ||
                                    status == "new"
                        }
                        .maxByOrNull { document ->
                            document.safeSortTime()
                        }

                    val latestNotClosedRequest = allDocuments
                        .filter { document ->
                            val status = document.safeText("status").trim().lowercase()

                            status != "accepted" &&
                                    status != "driver_arriving" &&
                                    status != "ride_started" &&
                                    status != "completed" &&
                                    status != "cancelled" &&
                                    status != "cancelled_by_driver" &&
                                    status != "cancelled_by_rider" &&
                                    status != "declined"
                        }
                        .maxByOrNull { document ->
                            document.safeSortTime()
                        }

                    val latestAnyRiderRequest = allDocuments
                        .filter { document ->
                            document.safeText("riderEmail").isNotBlank() ||
                                    document.safeText("userEmail").isNotBlank() ||
                                    document.safeText("pickupAddress").isNotBlank() ||
                                    document.safeText("pickupText").isNotBlank() ||
                                    document.safeText("dropoffAddress").isNotBlank() ||
                                    document.safeText("dropoffText").isNotBlank()
                        }
                        .maxByOrNull { document ->
                            document.safeSortTime()
                        }

                    val selectedDocument = openRequest
                        ?: latestNotClosedRequest
                        ?: latestAnyRiderRequest

                    if (selectedDocument == null) {
                        pendingRideRequest = null
                        showRideRequest = false
                        firebaseStatusText =
                            "No ride request found. Book a ride from rider account, then tap Check."
                    } else {
                        val request = selectedDocument.toPendingRideRequestSafely()
                        pendingRideRequest = request
                        showRideRequest = true
                        firebaseStatusText =
                            "New ride request found. Status: ${request.status}."
                    }
                } catch (exception: Exception) {
                    pendingRideRequest = null
                    showRideRequest = false
                    firebaseStatusText =
                        exception.message ?: "Failed to read ride request safely."
                }

                isCheckingRideRequest = false
            }
            .addOnFailureListener { exception ->
                isCheckingRideRequest = false
                pendingRideRequest = null
                showRideRequest = false
                firebaseStatusText =
                    exception.message ?: "Failed to check ride requests."
            }
    }

    BackHandler(enabled = activeDestination != DriverHomeDestination.Home) {
        activeDestination = DriverHomeDestination.Home
        activeRideRequestId = null
        resetRideRequestUi()
        isOnline = false
        firebaseStatusText = "Driver is offline. Turn on Live to receive rides."
    }

    DisposableEffect(driverId) {
        var listenerRegistration: ListenerRegistration? = null

        if (driverId.isBlank()) {
            isDashboardStatsLoading = false
            dashboardStats = DriverDashboardStats()
            firebaseStatusText = "Driver account not found. Please login again."
        } else {
            isDashboardStatsLoading = true

            listenerRegistration = firestore.collection("ride_requests")
                .whereEqualTo("driverId", driverId)
                .limit(100)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        isDashboardStatsLoading = false
                        return@addSnapshotListener
                    }

                    try {
                        val documents = snapshots?.documents.orEmpty()

                        val completedDocs = documents.filter { document ->
                            document.safeText("status").lowercase() == "completed"
                        }

                        val cancelledDocs = documents.filter { document ->
                            val status = document.safeText("status").lowercase()
                            status == "cancelled_by_driver" || status == "cancelled_by_rider"
                        }

                        val declinedDocs = documents.filter { document ->
                            document.safeText("status").lowercase() == "declined"
                        }

                        val acceptedDecisionCount = completedDocs.size + cancelledDocs.size
                        val totalDecisionCount = acceptedDecisionCount + declinedDocs.size

                        val acceptRate = if (totalDecisionCount > 0) {
                            ((acceptedDecisionCount.toFloat() / totalDecisionCount.toFloat()) * 100f)
                                .roundToInt()
                                .coerceIn(0, 100)
                        } else {
                            0
                        }

                        val todayCompletedDocs = completedDocs.filter { document ->
                            isToday(
                                document.getTimestamp("completedAt")
                                    ?: document.getTimestamp("updatedAt")
                                    ?: document.getTimestamp("createdAt")
                            )
                        }

                        val weekCompletedDocs = completedDocs.filter { document ->
                            isWithinLastDays(
                                timestamp = document.getTimestamp("completedAt")
                                    ?: document.getTimestamp("updatedAt")
                                    ?: document.getTimestamp("createdAt"),
                                days = 7
                            )
                        }

                        val todayEarnings = todayCompletedDocs.sumOf { document ->
                            extractFareAmount(
                                document.safeText("fareEstimate")
                                    .ifBlank { document.safeText("fare") }
                            )
                        }

                        val weekEarnings = weekCompletedDocs.sumOf { document ->
                            extractFareAmount(
                                document.safeText("fareEstimate")
                                    .ifBlank { document.safeText("fare") }
                            )
                        }

                        val ratings = completedDocs.mapNotNull { document ->
                            document.safeNumber("riderRating")
                        }

                        dashboardStats = DriverDashboardStats(
                            todayEarnings = todayEarnings,
                            todayCompletedTrips = todayCompletedDocs.size,
                            totalCompletedTrips = completedDocs.size,
                            totalCancelledTrips = cancelledDocs.size,
                            averageRating = if (ratings.isNotEmpty()) ratings.average() else 0.0,
                            acceptRatePercent = acceptRate,
                            weekEarnings = weekEarnings,
                            dailyTarget = 10000
                        )
                    } catch (_: Exception) {
                        dashboardStats = DriverDashboardStats()
                    }

                    isDashboardStatsLoading = false
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    when (activeDestination) {
        DriverHomeDestination.ActiveTrip -> {
            DriverTripScreen(
                driverName = "Shameer Khan",
                rideRequestId = activeRideRequestId,
                onBackToDriverHome = {
                    activeRideRequestId = null
                    resetRideRequestUi()
                    isOnline = false
                    activeDestination = DriverHomeDestination.Home
                    firebaseStatusText = "Driver is offline. Turn on Live to receive rides."

                    scope.launch {
                        snackbarHostState.showSnackbar("Returned to Driver Home.")
                    }
                }
            )
            return
        }

        DriverHomeDestination.Wallet -> {
            DriverWalletScreen(
                driverName = "Shameer Khan",
                onBackClick = {
                    activeDestination = DriverHomeDestination.Home
                }
            )
            return
        }

        DriverHomeDestination.TripHistory -> {
            DriverTripHistoryScreen(
                driverName = "Shameer Khan",
                onBackClick = {
                    activeDestination = DriverHomeDestination.Home
                }
            )
            return
        }

        DriverHomeDestination.Documents -> {
            DriverDocumentsScreen(
                driverName = "Shameer Khan",
                onBackClick = {
                    activeDestination = DriverHomeDestination.Home
                }
            )
            return
        }

        DriverHomeDestination.Support -> {
            DriverSupportScreen(
                driverName = "Shameer Khan",
                onBackClick = {
                    activeDestination = DriverHomeDestination.Home
                }
            )
            return
        }

        DriverHomeDestination.Home -> {
            // Home UI continues below.
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
                onOnlineChanged = { newValue ->
                    isOnline = newValue
                    resetRideRequestUi()

                    firebaseStatusText = if (newValue) {
                        "You are Live. Tap Check to find ride requests."
                    } else {
                        "Driver is offline. Turn on Live to receive rides."
                    }

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = if (newValue) {
                                "You are live. Tap Check to find ride requests."
                            } else {
                                "You are offline."
                            }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverRideRequestStatusCard(
                isOnline = isOnline,
                statusText = firebaseStatusText,
                pendingRideRequest = pendingRideRequest,
                isCheckingRideRequest = isCheckingRideRequest,
                onRefreshClick = {
                    checkForPendingRideRequest()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverStatsGrid(
                stats = dashboardStats,
                isLoading = isDashboardStatsLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverTodayEarningsCard(
                stats = dashboardStats,
                isLoading = isDashboardStatsLoading,
                isOnline = isOnline
            )

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
                            if (isRideActionLoading) return@IncomingRideRequestCard

                            isRideActionLoading = true

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
                                    activeRideRequestId = request.requestId
                                    activeDestination = DriverHomeDestination.ActiveTrip
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
                            if (isRideActionLoading) return@IncomingRideRequestCard

                            isRideActionLoading = true

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
                                    firebaseStatusText = "Ride declined. Tap Check for another request."

                                    scope.launch {
                                        snackbarHostState.showSnackbar("Ride request declined.")
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
                        "Wallet" -> activeDestination = DriverHomeDestination.Wallet
                        "Trip history" -> activeDestination = DriverHomeDestination.TripHistory
                        "Vehicle documents" -> activeDestination = DriverHomeDestination.Documents
                        "Support" -> activeDestination = DriverHomeDestination.Support
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
                            text = if (isOnline) "You are live" else "You are offline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = if (isOnline) {
                                "Tap Check to find ride requests."
                            } else {
                                "Turn on Live to start receiving rides."
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
private fun DriverRideRequestStatusCard(
    isOnline: Boolean,
    statusText: String,
    pendingRideRequest: PendingRideRequest?,
    isCheckingRideRequest: Boolean,
    onRefreshClick: () -> Unit
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
        Column(
            modifier = Modifier.padding(17.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = if (pendingRideRequest != null) "✓" else if (isOnline) "•" else "×",
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ride requests",
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

                OutlinedButton(
                    enabled = !isCheckingRideRequest,
                    onClick = onRefreshClick,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isCheckingRideRequest) "..." else "Check",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = isCheckingRideRequest,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = Color(0xFF8A35F2),
                        trackColor = Color(0xFFE5E7EB)
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverStatsGrid(
    stats: DriverDashboardStats,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriverStatCard(
                title = "Today",
                value = if (isLoading) "..." else formatRupees(stats.todayEarnings),
                subtitle = "${stats.todayCompletedTrips} completed",
                modifier = Modifier.weight(1f)
            )

            DriverStatCard(
                title = "Trips",
                value = if (isLoading) "..." else stats.totalCompletedTrips.toString(),
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
                value = if (isLoading) {
                    "..."
                } else if (stats.averageRating > 0.0) {
                    String.format(Locale.US, "%.1f", stats.averageRating)
                } else {
                    "—"
                },
                subtitle = "Rider feedback",
                modifier = Modifier.weight(1f)
            )

            DriverStatCard(
                title = "Accept",
                value = if (isLoading) "..." else "${stats.acceptRatePercent}%",
                subtitle = "Firebase rate",
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
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8A35F2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DriverTodayEarningsCard(
    stats: DriverDashboardStats,
    isLoading: Boolean,
    isOnline: Boolean
) {
    val progress = if (stats.dailyTarget <= 0) {
        0f
    } else {
        (stats.todayEarnings.toFloat() / stats.dailyTarget.toFloat()).coerceIn(0f, 1f)
    }

    val percent = (progress * 100f).roundToInt()

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
                        text = if (isLoading) {
                            "Loading dashboard..."
                        } else {
                            "${formatRupees(stats.todayEarnings)} of ${formatRupees(stats.dailyTarget)} completed"
                        },
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
                        text = if (isLoading) "..." else "$percent%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8A35F2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { if (isLoading) 0f else progress },
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
                    title = "Status",
                    value = if (isOnline) "Live" else "Offline"
                )

                EarningsMiniMetric(
                    title = "Week",
                    value = formatRupees(stats.weekEarnings)
                )

                EarningsMiniMetric(
                    title = "Cancelled",
                    value = stats.totalCancelledTrips.toString()
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
            color = Color(0xFF111827),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
                        text = "Ride request",
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

            Spacer(modifier = Modifier.height(14.dp))

            DriverRequestMiniMap(
                pickup = request.pickup,
                dropoff = request.dropoff
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF8A35F2).copy(alpha = 0.10f)
            ) {
                Text(
                    text = "Status: ${request.status} • Ride: ${request.rideType}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8A35F2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    text = "Updating ride request...",
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
private fun DriverRequestMiniMap(
    pickup: String,
    dropoff: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFFF1F5F9),
        shadowElevation = 4.dp,
        tonalElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val routePath = Path().apply {
                    moveTo(w * 0.16f, h * 0.68f)
                    cubicTo(
                        w * 0.32f,
                        h * 0.20f,
                        w * 0.58f,
                        h * 0.84f,
                        w * 0.84f,
                        h * 0.30f
                    )
                }

                drawPath(
                    path = routePath,
                    color = Color(0xFF8A35F2),
                    style = Stroke(
                        width = 9f,
                        cap = StrokeCap.Round
                    )
                )

                drawCircle(
                    color = Color(0xFF16A34A),
                    radius = 13f,
                    center = Offset(w * 0.16f, h * 0.68f)
                )

                drawCircle(
                    color = Color(0xFF8A35F2),
                    radius = 13f,
                    center = Offset(w * 0.84f, h * 0.30f)
                )

                drawCircle(
                    color = Color(0xFF2563EB),
                    radius = 15f,
                    center = Offset(w * 0.44f, h * 0.48f)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "Route preview",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = pickup,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF16A34A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = dropoff,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8A35F2),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

private fun DocumentSnapshot.toPendingRideRequestSafely(): PendingRideRequest {
    val pickup = safeText("pickupAddress")
        .ifBlank { safeText("pickupText") }
        .ifBlank { safeText("pickup") }
        .ifBlank { safeText("from") }
        .ifBlank { "Pickup location" }

    val dropoff = safeText("dropoffAddress")
        .ifBlank { safeText("dropText") }
        .ifBlank { safeText("dropoffText") }
        .ifBlank { safeText("dropoff") }
        .ifBlank { safeText("destination") }
        .ifBlank { safeText("to") }
        .ifBlank { "Dropoff location" }

    val riderEmail = safeText("riderEmail")
        .ifBlank { safeText("userEmail") }
        .ifBlank { safeText("email") }
        .ifBlank { safeText("customerEmail") }
        .ifBlank { "Rider" }

    val rideType = safeText("rideType")
        .ifBlank { safeText("selectedRideType") }
        .ifBlank { safeText("vehicleType") }
        .ifBlank { "Rideit" }

    val fare = normalizeFareText(
        safeText("fareEstimate")
            .ifBlank { safeText("fare") }
            .ifBlank { safeText("estimatedFare") }
            .ifBlank { safeText("price") }
            .ifBlank { "Fare pending" }
    )

    val status = safeText("status")
        .ifBlank { "pending" }

    return PendingRideRequest(
        requestId = id,
        riderEmail = riderEmail,
        pickup = pickup,
        dropoff = dropoff,
        rideType = rideType,
        fare = fare,
        status = status
    )
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

private fun DocumentSnapshot.safeSortTime(): Long {
    return try {
        getTimestamp("createdAt")?.seconds
            ?: getTimestamp("updatedAt")?.seconds
            ?: getTimestamp("acceptedAt")?.seconds
            ?: getTimestamp("completedAt")?.seconds
            ?: 0L
    } catch (_: Exception) {
        0L
    }
}

private fun normalizeFareText(fare: String): String {
    val cleanFare = fare.trim()

    if (cleanFare.isBlank()) return "₨ 0"

    val digitsOnly = cleanFare.filter { it.isDigit() }

    return when {
        cleanFare.contains("₨") -> cleanFare
        cleanFare.contains("Rs", ignoreCase = true) -> cleanFare
            .replace("Rs.", "₨")
            .replace("Rs", "₨")
        cleanFare.contains("PKR", ignoreCase = true) -> cleanFare.replace("PKR", "₨")
        digitsOnly.isNotBlank() -> "₨ $digitsOnly"
        else -> cleanFare
    }
}

private fun extractFareAmount(fare: String): Int {
    return fare
        .filter { it.isDigit() }
        .toIntOrNull()
        ?: 0
}

private fun formatRupees(amount: Int): String {
    if (amount <= 0) return "₨ 0"

    return "₨ ${String.format(Locale.US, "%,d", amount)}"
}

private fun isToday(timestamp: Timestamp?): Boolean {
    if (timestamp == null) return false

    return try {
        val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = formatter.format(System.currentTimeMillis())
        val itemDate = formatter.format(timestamp.toDate())
        today == itemDate
    } catch (_: Exception) {
        false
    }
}

private fun isWithinLastDays(
    timestamp: Timestamp?,
    days: Int
): Boolean {
    if (timestamp == null) return false

    return try {
        val now = System.currentTimeMillis()
        val itemTime = timestamp.toDate().time
        val diffMillis = now - itemTime
        diffMillis in 0..(days * 24L * 60L * 60L * 1000L)
    } catch (_: Exception) {
        false
    }
}