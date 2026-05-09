package com.example.rideit.driver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

private data class DriverWalletTrip(
    val requestId: String,
    val riderEmail: String,
    val pickup: String,
    val dropoff: String,
    val fareText: String,
    val fareAmount: Int,
    val status: String,
    val createdAt: Timestamp?,
    val completedAt: Timestamp?,
    val cancelledAt: Timestamp?,
    val updatedAt: Timestamp?,
    val sortTimeMillis: Long
)

@Composable
fun DriverWalletScreen(
    driverName: String = "Shameer Khan",
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var firebaseError by remember { mutableStateOf<String?>(null) }
    var walletTrips by remember { mutableStateOf<List<DriverWalletTrip>>(emptyList()) }

    val driverId = remember { FirebaseManager.currentUserId().orEmpty() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    DisposableEffect(driverId) {
        var listenerRegistration: ListenerRegistration? = null

        if (driverId.isBlank()) {
            isLoading = false
            firebaseError = "Driver account not found. Please login again."
            walletTrips = emptyList()
        } else {
            isLoading = true
            firebaseError = null

            listenerRegistration = firestore.collection("ride_requests")
                .whereEqualTo("driverId", driverId)
                .limit(75)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        isLoading = false
                        firebaseError = error.message ?: "Failed to load wallet earnings."
                        walletTrips = emptyList()
                        return@addSnapshotListener
                    }

                    val walletStatuses = setOf(
                        "completed",
                        "cancelled_by_driver",
                        "cancelled_by_rider"
                    )

                    val trips = snapshots
                        ?.documents
                        ?.mapNotNull { document ->
                            val statusRaw = document.getString("status").orEmpty().lowercase()

                            if (statusRaw !in walletStatuses) {
                                return@mapNotNull null
                            }

                            val fareText = document.getString("fareEstimate")
                                ?: document.getString("fare")
                                ?: "₨ 0"

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

                            val createdAt = document.getTimestamp("createdAt")
                            val completedAt = document.getTimestamp("completedAt")
                            val cancelledAt = document.getTimestamp("cancelledAt")
                            val updatedAt = document.getTimestamp("updatedAt")

                            val finalTimestamp = completedAt
                                ?: cancelledAt
                                ?: updatedAt
                                ?: createdAt

                            DriverWalletTrip(
                                requestId = document.id,
                                riderEmail = riderEmail,
                                pickup = pickup,
                                dropoff = dropoff,
                                fareText = normalizeFareText(fareText),
                                fareAmount = extractFareAmount(fareText),
                                status = statusRaw,
                                createdAt = createdAt,
                                completedAt = completedAt,
                                cancelledAt = cancelledAt,
                                updatedAt = updatedAt,
                                sortTimeMillis = finalTimestamp?.toDate()?.time ?: 0L
                            )
                        }
                        ?.sortedByDescending { it.sortTimeMillis }
                        .orEmpty()

                    walletTrips = trips
                    isLoading = false
                    firebaseError = null
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    val completedTrips = walletTrips.filter { it.status == "completed" }
    val cancelledTrips = walletTrips.filter { it.status != "completed" }

    val totalEarnings = completedTrips.sumOf { it.fareAmount }
    val pendingPayout = (totalEarnings * 0.25f).roundToInt()
    val rideitFee = (totalEarnings * 0.12f).roundToInt()
    val netEarnings = (totalEarnings - rideitFee).coerceAtLeast(0)

    val todayTrips = completedTrips.filter { isToday(it.completedAt ?: it.updatedAt ?: it.createdAt) }
    val todayEarnings = todayTrips.sumOf { it.fareAmount }

    val weekTrips = completedTrips.filter { isWithinLastDays(it.completedAt ?: it.updatedAt ?: it.createdAt, 7) }
    val weekEarnings = weekTrips.sumOf { it.fareAmount }

    val averageFare = if (completedTrips.isNotEmpty()) {
        totalEarnings / completedTrips.size
    } else {
        0
    }

    val weeklyTarget = 50000
    val weeklyProgress = if (weeklyTarget <= 0) {
        0f
    } else {
        (weekEarnings.toFloat() / weeklyTarget.toFloat()).coerceIn(0f, 1f)
    }

    val weeklyProgressPercent = (weeklyProgress * 100).roundToInt()

    val recentActivities = walletTrips.take(6)

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
            DriverWalletHeader(
                driverName = driverName,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                WalletLoadingCard()

                Spacer(modifier = Modifier.height(16.dp))
            }

            firebaseError?.let { error ->
                WalletMessageCard(
                    title = "Unable to load wallet",
                    message = error,
                    success = false
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            WalletBalanceCard(
                availableBalance = netEarnings,
                pendingPayout = pendingPayout,
                completedTrips = completedTrips.size,
                onWithdrawClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = if (netEarnings > 0) {
                                "Withdraw request prepared for ${formatRupees(netEarnings)}. Real payout will be connected later."
                            } else {
                                "No completed Firebase earnings available for withdrawal yet."
                            }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            WalletStatsGrid(
                todayEarnings = todayEarnings,
                todayTripCount = todayTrips.size,
                weekEarnings = weekEarnings,
                weekTripCount = weekTrips.size,
                completedTripCount = completedTrips.size,
                cancelledTripCount = cancelledTrips.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeeklyEarningsCard(
                weekEarnings = weekEarnings,
                weeklyTarget = weeklyTarget,
                weeklyProgress = weeklyProgress,
                weeklyProgressPercent = weeklyProgressPercent,
                weekTripCount = weekTrips.size,
                averageFare = averageFare
            )

            Spacer(modifier = Modifier.height(16.dp))

            EarningsBreakdownCard(
                rideFares = totalEarnings,
                rideitFee = rideitFee,
                netEarnings = netEarnings,
                completedTrips = completedTrips.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            RecentWalletActivityCard(
                activities = recentActivities,
                onActivityClick = { trip ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "${statusLabel(trip.status)} • ${trip.fareText} • ${trip.pickup} to ${trip.dropoff}"
                        )
                    }
                }
            )

            if (!isLoading && firebaseError == null && walletTrips.isEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                WalletMessageCard(
                    title = "No Firebase earnings yet",
                    message = "Complete a real ride as driver and your wallet earnings will appear here automatically.",
                    success = true
                )
            }

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
private fun DriverWalletHeader(
    driverName: String,
    onBackClick: () -> Unit
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
                    Brush.linearGradient(
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
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clickable { onBackClick() },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "‹",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Driver Wallet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.76f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF22C55E).copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "Firebase",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Track real Rideit earnings from completed Firebase trips, wallet balance, fees, and recent driver activity.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WalletBalanceCard(
    availableBalance: Int,
    pendingPayout: Int,
    completedTrips: Int,
    onWithdrawClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 14.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Available balance",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = formatRupees(availableBalance),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Pending payout: ${formatRupees(pendingPayout)} • Completed Firebase trips: $completedTrips",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onWithdrawClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8A35F2)
                )
            ) {
                Text(
                    text = "Withdraw Earnings",
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun WalletStatsGrid(
    todayEarnings: Int,
    todayTripCount: Int,
    weekEarnings: Int,
    weekTripCount: Int,
    completedTripCount: Int,
    cancelledTripCount: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WalletStatCard(
                title = "Today",
                value = formatRupees(todayEarnings),
                subtitle = "$todayTripCount trips",
                modifier = Modifier.weight(1f)
            )

            WalletStatCard(
                title = "This week",
                value = formatRupees(weekEarnings),
                subtitle = "$weekTripCount trips",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WalletStatCard(
                title = "Completed",
                value = completedTripCount.toString(),
                subtitle = "Paid rides",
                modifier = Modifier.weight(1f)
            )

            WalletStatCard(
                title = "Cancelled",
                value = cancelledTripCount.toString(),
                subtitle = "No earnings",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WalletStatCard(
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
                style = MaterialTheme.typography.titleLarge,
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
private fun WeeklyEarningsCard(
    weekEarnings: Int,
    weeklyTarget: Int,
    weeklyProgress: Float,
    weeklyProgressPercent: Int,
    weekTripCount: Int,
    averageFare: Int
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weekly target",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${formatRupees(weekEarnings)} of ${formatRupees(weeklyTarget)} completed",
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
                        text = "$weeklyProgressPercent%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8A35F2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { weeklyProgress },
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
                SmallWalletMetric("Trips", weekTripCount.toString())
                SmallWalletMetric("Avg/trip", formatRupees(averageFare))
                SmallWalletMetric("Target", formatRupees(weeklyTarget))
            }
        }
    }
}

@Composable
private fun SmallWalletMetric(
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
private fun EarningsBreakdownCard(
    rideFares: Int,
    rideitFee: Int,
    netEarnings: Int,
    completedTrips: Int
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
                text = "Earnings breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(14.dp))

            BreakdownRow("Ride fares", formatRupees(rideFares), Color(0xFF8A35F2))
            BreakdownRow("Completed trips", completedTrips.toString(), Color(0xFF16A34A))
            BreakdownRow("Tips", "₨ 0", Color(0xFF16A34A))
            BreakdownRow("Peak bonuses", "₨ 0", Color(0xFFE17A00))
            BreakdownRow("Rideit fee", "-${formatRupees(rideitFee)}", Color(0xFFEF4444))

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Net earnings",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )

                Text(
                    text = formatRupees(netEarnings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF16A34A)
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    title: String,
    amount: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

@Composable
private fun RecentWalletActivityCard(
    activities: List<DriverWalletTrip>,
    onActivityClick: (DriverWalletTrip) -> Unit
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
                text = "Recent Firebase activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = activities.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "No wallet activity yet. Completed Firebase trips will show here.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
            }

            activities.forEach { trip ->
                WalletActivityItem(
                    title = statusLabel(trip.status),
                    subtitle = "${trip.pickup} → ${trip.dropoff}",
                    amount = if (trip.status == "completed") {
                        "+${trip.fareText}"
                    } else {
                        "₨ 0"
                    },
                    positive = trip.status == "completed",
                    onClick = { onActivityClick(trip) }
                )
            }
        }
    }
}

@Composable
private fun WalletActivityItem(
    title: String,
    subtitle: String,
    amount: String,
    positive: Boolean,
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
                .background(
                    if (positive) {
                        Color(0xFF16A34A).copy(alpha = 0.12f)
                    } else {
                        Color(0xFFEF4444).copy(alpha = 0.12f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (positive) "+" else "-",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (positive) Color(0xFF16A34A) else Color(0xFFEF4444)
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
            text = amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = if (positive) Color(0xFF16A34A) else Color(0xFFEF4444)
        )
    }
}

@Composable
private fun WalletLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Loading Firebase wallet...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF8A35F2),
                trackColor = Color(0xFFE5E7EB)
            )
        }
    }
}

@Composable
private fun WalletMessageCard(
    title: String,
    message: String,
    success: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = if (success) Color.White else Color(0xFFFFE5E5),
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (success) {
                            Color(0xFF8A35F2).copy(alpha = 0.12f)
                        } else {
                            Color(0xFFEF4444).copy(alpha = 0.12f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (success) "₨" else "!",
                    color = if (success) Color(0xFF8A35F2) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (success) Color(0xFF6B7280) else Color(0xFFEF4444)
                )
            }
        }
    }
}

private fun normalizeFareText(fare: String): String {
    val cleanFare = fare.trim()

    if (cleanFare.isBlank()) return "₨ 0"

    return cleanFare
        .replace("Rs.", "₨")
        .replace("Rs", "₨")
        .replace("PKR", "₨")
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

private fun statusLabel(status: String): String {
    return when (status.lowercase()) {
        "completed" -> "Trip completed"
        "cancelled_by_driver" -> "Cancelled by you"
        "cancelled_by_rider" -> "Cancelled by rider"
        else -> "Wallet activity"
    }
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