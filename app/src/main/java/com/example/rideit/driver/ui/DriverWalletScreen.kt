package com.example.rideit.driver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import java.util.Locale
import kotlin.math.roundToInt

@Immutable
private data class DriverWalletStats(
    val availableBalance: Int = 0,
    val todayEarnings: Int = 0,
    val weekEarnings: Int = 0,
    val totalEarnings: Int = 0,
    val completedTrips: Int = 0,
    val todayTrips: Int = 0,
    val pendingPayout: Int = 0,
    val averagePerTrip: Int = 0
)

@Composable
fun DriverWalletScreen(
    driverName: String = FirebaseManager.currentDriverDisplayName(),
    onBackClick: () -> Unit
) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val driverId = remember { FirebaseManager.currentUserId().orEmpty() }

    var stats by remember { mutableStateOf(DriverWalletStats()) }
    var isLoading by remember { mutableStateOf(true) }

    var statusMessage by rememberSaveable {
        mutableStateOf("Loading driver wallet...")
    }

    var recentOne by rememberSaveable {
        mutableStateOf("No completed trips yet")
    }

    var recentTwo by rememberSaveable {
        mutableStateOf("Driver wallet ready")
    }

    var recentThree by rememberSaveable {
        mutableStateOf("Safe demo payout mode")
    }

    DisposableEffect(driverId) {
        var listenerRegistration: ListenerRegistration? = null

        if (driverId.isBlank()) {
            isLoading = false
            statusMessage = "Driver account not found. Please login again."
        } else {
            isLoading = true
            statusMessage = "Listening for completed trips..."

            listenerRegistration = firestore.collection("ride_requests")
                .whereEqualTo("driverId", driverId)
                .limit(100)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        isLoading = false
                        statusMessage = error.message ?: "Failed to load driver wallet."
                        return@addSnapshotListener
                    }

                    val documents = snapshots?.documents.orEmpty()

                    val completedDocs = documents
                        .filter { document ->
                            document.safeText("status").trim().lowercase() == "completed"
                        }
                        .sortedByDescending { document ->
                            document.safeSortTime()
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

                    val totalEarnings = completedDocs.sumOf { document ->
                        document.safeDriverEarningAmount()
                    }

                    val todayEarnings = todayCompletedDocs.sumOf { document ->
                        document.safeDriverEarningAmount()
                    }

                    val weekEarnings = weekCompletedDocs.sumOf { document ->
                        document.safeDriverEarningAmount()
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
                        averagePerTrip = averagePerTrip
                    )

                    val recentRows = completedDocs
                        .take(3)
                        .map { document ->
                            val fareText = formatRupees(document.safeDriverEarningAmount())

                            val pickup = document.safeText("pickupAddress")
                                .ifBlank { document.safeText("pickupText") }
                                .ifBlank { "Pickup" }

                            val completedDate = document.safeCompletedDateLabel()

                            "Completed trip • $fareText • ${pickup.take(22)} • $completedDate"
                        }

                    recentOne = recentRows.getOrNull(0) ?: "No completed trips yet"
                    recentTwo = recentRows.getOrNull(1) ?: "Earnings appear after completed rides"
                    recentThree = recentRows.getOrNull(2) ?: "Safe demo payout mode"

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 34.dp, bottom = 22.dp)
        ) {
            DriverWalletTopBar(
                driverName = driverName,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            DriverWalletStatusPill(
                text = statusMessage
            )

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
                recentOne = recentOne,
                recentTwo = recentTwo,
                recentThree = recentThree
            )

            Spacer(modifier = Modifier.height(14.dp))

            DriverWalletSafetyCard()
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

        Column {
            Text(
                text = "Driver Wallet",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
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
                            Color(0xFF8A35F2)
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
                        style = MaterialTheme.typography.titleLarge
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
                    text = if (isLoading) "₨ ..." else formatRupees(stats.availableBalance),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${stats.completedTrips} completed trips • synced from Firebase",
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
            modifier = Modifier.fillMaxWidth()
        ) {
            DriverWalletMetricCard(
                title = "Today",
                value = if (isLoading) "₨ ..." else formatRupees(stats.todayEarnings),
                subtitle = "${stats.todayTrips} trips",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            DriverWalletMetricCard(
                title = "This Week",
                value = if (isLoading) "₨ ..." else formatRupees(stats.weekEarnings),
                subtitle = "Last 7 days",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DriverWalletMetricCard(
                title = "Total",
                value = if (isLoading) "₨ ..." else formatRupees(stats.totalEarnings),
                subtitle = "All completed rides",
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            DriverWalletMetricCard(
                title = "Average",
                value = if (isLoading) "₨ ..." else formatRupees(stats.averagePerTrip),
                subtitle = "Per completed trip",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        DriverWalletMetricCard(
            title = "Completed Trips",
            value = if (isLoading) "..." else stats.completedTrips.toString(),
            subtitle = "Only completed rides count toward earnings",
            modifier = Modifier.fillMaxWidth()
        )
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
                style = MaterialTheme.typography.labelMedium
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
                style = MaterialTheme.typography.bodySmall
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
                text = "Withdrawals are demo-only for now. Real bank, JazzCash, EasyPaisa or Stripe payout can be connected later near launch readiness.",
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
                color = Color(0xFF8A35F2),
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
                            .background(Color(0xFF8A35F2).copy(alpha = 0.18f)),
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
                            text = "Request Demo Withdrawal",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
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
                        color = Color(0xFF8A35F2),
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
    recentOne: String,
    recentTwo: String,
    recentThree: String
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

            DriverEarningRow(
                icon = "✓",
                text = recentOne
            )

            Spacer(modifier = Modifier.height(12.dp))

            DriverEarningRow(
                icon = "₨",
                text = recentTwo
            )

            Spacer(modifier = Modifier.height(12.dp))

            DriverEarningRow(
                icon = "🔒",
                text = recentThree
            )
        }
    }
}

@Composable
private fun DriverEarningRow(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
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

        Text(
            text = text,
            color = Color(0xFFE5E7EB),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
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
                    text = "Earnings are calculated only from completed Firebase ride requests. Fare ranges are parsed safely, and no real payout, bank transfer, wallet withdrawal or payment gateway is connected yet.",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
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
        getTimestamp("completedAt")?.seconds
            ?: getTimestamp("updatedAt")?.seconds
            ?: getTimestamp("createdAt")?.seconds
            ?: 0L
    } catch (_: Exception) {
        0L
    }
}

private fun DocumentSnapshot.safeCompletedDateLabel(): String {
    return try {
        val timestamp = getTimestamp("completedAt")
            ?: getTimestamp("updatedAt")
            ?: getTimestamp("createdAt")
            ?: return "recent"

        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
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
        ((amountMatches[0] + amountMatches[1]).toFloat() / 2f).roundToInt()
    } else {
        amountMatches.first()
    }
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