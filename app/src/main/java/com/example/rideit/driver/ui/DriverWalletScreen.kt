package com.example.rideit.driver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Immutable
private data class DriverWalletTrip(
    val id: String,
    val riderName: String,
    val pickup: String,
    val dropoff: String,
    val earningAmount: Int,
    val earningText: String,
    val dateText: String,
    val paymentMethod: String,
    val ratingText: String,
    val feedbackText: String,
    val sortTime: Long
)

@Immutable
private data class DriverWalletStats(
    val availableBalance: Int = 0,
    val todayEarnings: Int = 0,
    val weekEarnings: Int = 0,
    val totalEarnings: Int = 0,
    val completedTrips: Int = 0,
    val todayTrips: Int = 0,
    val pendingPayout: Int = 0,
    val averagePerTrip: Int = 0,
    val ratedTrips: Int = 0,
    val averageRating: Double = 0.0
)

@Composable
fun DriverWalletScreen(
    driverName: String = FirebaseManager.currentDriverDisplayName(),
    onBackClick: () -> Unit
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val driverId = remember { FirebaseManager.currentUserId().orEmpty() }

    var stats by remember { mutableStateOf(DriverWalletStats()) }
    var recentTrips by remember { mutableStateOf<List<DriverWalletTrip>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var statusMessage by rememberSaveable {
        mutableStateOf("Loading driver wallet...")
    }

    DisposableEffect(driverId) {
        var listenerRegistration: ListenerRegistration? = null

        if (driverId.isBlank()) {
            isLoading = false
            stats = DriverWalletStats()
            recentTrips = emptyList()
            statusMessage = "Driver account not found. Please login again."
        } else {
            isLoading = true
            statusMessage = "Loading completed trips..."

            listenerRegistration = firestore.collection("ride_requests")
                .whereEqualTo("driverId", driverId)
                .limit(120)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        isLoading = false
                        stats = DriverWalletStats()
                        recentTrips = emptyList()
                        statusMessage = error.message ?: "Failed to load driver wallet."
                        return@addSnapshotListener
                    }

                    val completedDocs = snapshots
                        ?.documents
                        .orEmpty()
                        .filter { document ->
                            document.safeText("status").trim().lowercase() == "completed"
                        }
                        .sortedByDescending { document ->
                            document.safeSortTime()
                        }

                    val todayCompletedDocs = completedDocs.filter { document ->
                        isToday(document.safeFinalTimestamp())
                    }

                    val weekCompletedDocs = completedDocs.filter { document ->
                        isWithinLastDays(
                            timestamp = document.safeFinalTimestamp(),
                            days = 7
                        )
                    }

                    val totalEarnings = completedDocs.sumOf { document ->
                        document.safeDriverEarningAmount()
                    }

                    val todayEarnings = todayCompletedDocs.sumOf { document ->
                        document.safeDriverEarningAmount()
                    }

                    val weekEarnings = weekCompletedDocs.sumOf { document ->
                        document.safeDriverEarningAmount()
                    }

                    val ratings = completedDocs.mapNotNull { document ->
                        document.safeNumber("riderRating")?.takeIf { it in 1..5 }
                    }

                    val averagePerTrip = if (completedDocs.isNotEmpty()) {
                        (totalEarnings.toFloat() / completedDocs.size.toFloat()).roundToInt()
                    } else {
                        0
                    }

                    stats = DriverWalletStats(
                        availableBalance = totalEarnings,
                        todayEarnings = todayEarnings,
                        weekEarnings = weekEarnings,
                        totalEarnings = totalEarnings,
                        completedTrips = completedDocs.size,
                        todayTrips = todayCompletedDocs.size,
                        pendingPayout = totalEarnings,
                        averagePerTrip = averagePerTrip,
                        ratedTrips = ratings.size,
                        averageRating = if (ratings.isNotEmpty()) ratings.average() else 0.0
                    )

                    recentTrips = completedDocs
                        .take(5)
                        .map { document ->
                            document.toDriverWalletTrip()
                        }

                    statusMessage = if (completedDocs.isEmpty()) {
                        "No completed trips yet"
                    } else {
                        "Wallet synced from ${completedDocs.size} completed trips"
                    }

                    isLoading = false
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050505),
                        Color(0xFF111827),
                        Color(0xFF090909)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            DriverWalletTopBar(
                driverName = driverName,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            DriverWalletStatusPill(text = statusMessage)

            Spacer(modifier = Modifier.height(14.dp))

            DriverWalletHeroCard(
                stats = stats,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            DriverWalletStatsGrid(
                stats = stats,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            DriverPayoutCard(
                stats = stats,
                isLoading = isLoading,
                onWithdrawClick = {
                    statusMessage = if (stats.pendingPayout > 0) {
                        "Demo withdrawal requested for ${formatRupees(stats.pendingPayout)}. Real payout is not connected yet."
                    } else {
                        "No available earnings yet. Complete trips first."
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            DriverRecentEarningsCard(
                trips = recentTrips,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(14.dp))

            DriverWalletSafetyCard()

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun DriverWalletTopBar(
    driverName: String,
    onBackClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onBackClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text(
                text = "‹ Back",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Driver Wallet",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = driverName,
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DriverWalletStatusPill(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            color = Color(0xFFE5E7EB),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DriverWalletHeroCard(
    stats: DriverWalletStats,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.Transparent,
        shadowElevation = 20.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF111827),
                            Color(0xFF2563EB),
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Earnings",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "Demo payout",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isLoading) "Rs ..." else formatRupees(stats.availableBalance),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${stats.completedTrips} completed trips • Synced",
                    color = Color(0xFFE5E7EB),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₨",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Composable
private fun DriverWalletStatsGrid(
    stats: DriverWalletStats,
    isLoading: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriverWalletMetricCard(
                title = "Today",
                value = if (isLoading) "Rs ..." else formatRupees(stats.todayEarnings),
                subtitle = "${stats.todayTrips} trips",
                modifier = Modifier.weight(1f)
            )

            DriverWalletMetricCard(
                title = "This Week",
                value = if (isLoading) "Rs ..." else formatRupees(stats.weekEarnings),
                subtitle = "Last 7 days",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriverWalletMetricCard(
                title = "Total",
                value = if (isLoading) "Rs ..." else formatRupees(stats.totalEarnings),
                subtitle = "Completed rides",
                modifier = Modifier.weight(1f)
            )

            DriverWalletMetricCard(
                title = "Average",
                value = if (isLoading) "Rs ..." else formatRupees(stats.averagePerTrip),
                subtitle = "Per trip",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriverWalletMetricCard(
                title = "Completed",
                value = if (isLoading) "..." else stats.completedTrips.toString(),
                subtitle = "Earned rides only",
                modifier = Modifier.weight(1f)
            )

            DriverWalletMetricCard(
                title = "Rating",
                value = if (isLoading) {
                    "..."
                } else if (stats.averageRating > 0.0) {
                    String.format(Locale.US, "%.1f", stats.averageRating)
                } else {
                    "—"
                },
                subtitle = "${stats.ratedTrips} rated trips",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DriverWalletMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF17171A),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = subtitle,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DriverPayoutCard(
    stats: DriverWalletStats,
    isLoading: Boolean,
    onWithdrawClick: () -> Unit
) {
    val payoutProgress = if (stats.pendingPayout <= 0) 0f else 1f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF121216)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Payout",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Withdrawals are demo-only for now. Real bank, JazzCash, EasyPaisa, Stripe, or in-app payout can be connected later.",
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = payoutProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xFF2A2A31),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable(enabled = !isLoading) {
                        onWithdrawClick()
                    },
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF1B1B1D)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↗",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Demo Withdrawal",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = if (isLoading) {
                                "Loading balance..."
                            } else {
                                "Available: ${formatRupees(stats.pendingPayout)}"
                            },
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = "Demo",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverRecentEarningsCard(
    trips: List<DriverWalletTrip>,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF17171A)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Recent Earnings",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            when {
                isLoading -> {
                    DriverEarningEmptyRow(
                        icon = "…",
                        title = "Loading recent earnings",
                        subtitle = "Checking completed trips"
                    )
                }

                trips.isEmpty() -> {
                    DriverEarningEmptyRow(
                        icon = "₨",
                        title = "No completed trips yet",
                        subtitle = "Earnings will appear here after driver completes a trip."
                    )
                }

                else -> {
                    trips.forEachIndexed { index, trip ->
                        DriverEarningTripRow(trip = trip)

                        if (index != trips.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverEarningTripRow(
    trip: DriverWalletTrip
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFF22C55E).copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = Color(0xFF22C55E),
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.earningText,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = trip.paymentMethod,
                        color = Color(0xFFB98CFF),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${trip.riderName} • ${trip.dateText}",
                color = Color(0xFFE5E7EB),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "${trip.pickup} → ${trip.dropoff}",
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (trip.ratingText != "—" || trip.feedbackText.isNotBlank()) {
                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = if (trip.feedbackText.isNotBlank()) {
                        "⭐ ${trip.ratingText} • ${trip.feedbackText}"
                    } else {
                        "⭐ ${trip.ratingText}"
                    },
                    color = Color(0xFFFACC15),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DriverEarningEmptyRow(
    icon: String,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DriverWalletSafetyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1B1B1D)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Safe driver wallet",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Earnings are calculated only from completed ride requests. Cancelled and declined trips do not increase wallet balance. Real payout is not connected yet.",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun DocumentSnapshot.toDriverWalletTrip(): DriverWalletTrip {
    val riderEmail = safeText("riderEmail")
        .ifBlank { safeText("userEmail") }
        .ifBlank { "Rider" }

    val riderName = safeText("riderName")
        .ifBlank { riderEmail.toReadableName() }

    val pickup = safeText("pickupAddress")
        .ifBlank { safeText("pickupText") }
        .ifBlank { "Pickup" }

    val dropoff = safeText("dropoffAddress")
        .ifBlank { safeText("dropText") }
        .ifBlank { safeText("dropoffText") }
        .ifBlank { "Dropoff" }

    val earningAmount = safeDriverEarningAmount()

    val ratingText = safeNumber("riderRating")
        ?.takeIf { it in 1..5 }
        ?.toString()
        ?: "—"

    return DriverWalletTrip(
        id = id,
        riderName = riderName,
        pickup = pickup,
        dropoff = dropoff,
        earningAmount = earningAmount,
        earningText = formatRupees(earningAmount),
        dateText = safeCompletedDateLabel(),
        paymentMethod = safeText("paymentMethodTitle")
            .ifBlank { safeText("paymentMethodId") }
            .ifBlank { "Cash" }
            .toPaymentLabel(),
        ratingText = ratingText,
        feedbackText = safeText("riderFeedback"),
        sortTime = safeSortTime()
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
        }.trim()
    } catch (_: Exception) {
        ""
    }
}

private fun DocumentSnapshot.safeNumber(field: String): Int? {
    return try {
        when (val value = get(field)) {
            is Int -> value
            is Long -> value.toInt()
            is Double -> value.roundToInt()
            is Float -> value.roundToInt()
            is Number -> value.toInt()
            is String -> parseFareTextToAmount(value)
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

private fun DocumentSnapshot.safeDriverEarningAmount(): Int {
    val numericFields = listOf(
        "driverEarningAmount",
        "driverEarnings",
        "driverEarning",
        "earningAmount",
        "fareAmount",
        "fareValue",
        "priceAmount",
        "totalFare"
    )

    numericFields.forEach { field ->
        val value = safeNumber(field)
        if (value != null && value > 0) {
            return value
        }
    }

    val textFields = listOf(
        "driverEarningText",
        "fareEstimate",
        "fare",
        "estimatedFare",
        "price",
        "paymentAmount"
    )

    textFields.forEach { field ->
        val amount = parseFareTextToAmount(safeText(field))
        if (amount > 0) {
            return amount
        }
    }

    return 0
}

private fun DocumentSnapshot.safeSortTime(): Long {
    return try {
        safeFinalTimestamp()?.seconds ?: 0L
    } catch (_: Exception) {
        0L
    }
}

private fun DocumentSnapshot.safeFinalTimestamp(): Timestamp? {
    return try {
        getTimestamp("completedAt")
            ?: getTimestamp("driverEarningRecordedAt")
            ?: getTimestamp("updatedAt")
            ?: getTimestamp("createdAt")
    } catch (_: Exception) {
        null
    }
}

private fun DocumentSnapshot.safeCompletedDateLabel(): String {
    return try {
        val timestamp = safeFinalTimestamp() ?: return "recent"

        val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        formatter.format(timestamp.toDate())
    } catch (_: Exception) {
        "recent"
    }
}

private fun parseFareTextToAmount(fare: String): Int {
    val cleanFare = fare.trim()

    if (cleanFare.isBlank()) {
        return 0
    }

    val amountMatches = Regex("""\d[\d,]*""")
        .findAll(cleanFare)
        .mapNotNull { match ->
            match.value.replace(",", "").toIntOrNull()
        }
        .filter { amount ->
            amount > 0
        }
        .toList()

    if (amountMatches.isEmpty()) {
        return 0
    }

    val looksLikeRange =
        cleanFare.contains("-") ||
                cleanFare.contains(" to ", ignoreCase = true) ||
                cleanFare.contains("–") ||
                cleanFare.contains("—")

    return if (looksLikeRange && amountMatches.size >= 2) {
        ((amountMatches[0] + amountMatches[1]) / 2f).roundToInt()
    } else {
        amountMatches.first()
    }
}

private fun isToday(timestamp: Timestamp?): Boolean {
    if (timestamp == null) {
        return false
    }

    return try {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            time = timestamp.toDate()
        }

        now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    } catch (_: Exception) {
        false
    }
}

private fun isWithinLastDays(
    timestamp: Timestamp?,
    days: Int
): Boolean {
    if (timestamp == null) {
        return false
    }

    return try {
        val nowMillis = System.currentTimeMillis()
        val targetMillis = timestamp.toDate().time
        val diffMillis = nowMillis - targetMillis
        diffMillis in 0..(days * 24L * 60L * 60L * 1000L)
    } catch (_: Exception) {
        false
    }
}

private fun formatRupees(amount: Int): String {
    if (amount <= 0) {
        return "Rs 0"
    }

    return "Rs ${String.format(Locale.US, "%,d", amount)}"
}

private fun String.toReadableName(): String {
    val source = substringBefore("@")
        .replace(".", " ")
        .replace("_", " ")
        .replace("-", " ")
        .trim()

    if (source.isBlank()) {
        return "Rideit Rider"
    }

    return source
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) {
                    char.titlecase()
                } else {
                    char.toString()
                }
            }
        }
        .ifBlank { "Rideit Rider" }
}

private fun String.toPaymentLabel(): String {
    return when (trim().lowercase()) {
        "cash" -> "Cash"
        "card" -> "Card"
        "wallet" -> "Wallet"
        "debit / credit card" -> "Card"
        "rideit wallet" -> "Wallet"
        else -> trim().ifBlank { "Cash" }
    }
}
