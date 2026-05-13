package com.example.rideit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

@Immutable
data class TripHistoryItem(
    val id: String,
    val date: String,
    val pickup: String,
    val dropoff: String,
    val rideType: String,
    val fare: String,
    val status: String,
    val driverName: String,
    val time: String,
    val rating: String,
    val feedback: String,
    val sortTimeMillis: Long
)

@Immutable
private data class TripHistoryThemeColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val innerCard: Color,
    val iconCard: Color,
    val primary: Color,
    val secondary: Color,
    val text: Color,
    val subText: Color,
    val border: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val onPrimary: Color
)

@Composable
fun TripHistoryScreen(
    onBackClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var firebaseError by remember { mutableStateOf<String?>(null) }
    var trips by remember { mutableStateOf<List<TripHistoryItem>>(emptyList()) }

    val colors = rememberTripHistoryThemeColors()
    val riderId = remember { FirebaseManager.currentUserId().orEmpty() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    DisposableEffect(riderId) {
        var listenerRegistration: ListenerRegistration? = null

        if (riderId.isBlank()) {
            isLoading = false
            firebaseError = "Rider account not found. Please login again."
            trips = emptyList()
        } else {
            isLoading = true
            firebaseError = null

            listenerRegistration = firestore.collection("ride_requests")
                .whereEqualTo("riderId", riderId)
                .limit(75)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        isLoading = false
                        firebaseError = error.message ?: "Failed to load rider trip history."
                        trips = emptyList()
                        return@addSnapshotListener
                    }

                    val historyStatuses = setOf(
                        "completed",
                        "cancelled_by_rider",
                        "cancelled_by_driver",
                        "declined"
                    )

                    val firebaseTrips = snapshots
                        ?.documents
                        ?.mapNotNull { document ->
                            val statusRaw = document.getString("status").orEmpty().lowercase()

                            if (statusRaw !in historyStatuses) {
                                return@mapNotNull null
                            }

                            val pickup = document.getString("pickupAddress")
                                ?: document.getString("pickupText")
                                ?: "Pickup location"

                            val dropoff = document.getString("dropoffAddress")
                                ?: document.getString("dropText")
                                ?: document.getString("dropoffText")
                                ?: "Dropoff location"

                            val rideType = document.getString("rideType") ?: "Rideit"

                            val fare = document.getString("fareEstimate")
                                ?: document.getString("fare")
                                ?: "₨ 0"

                            val driverName = document.getString("driverName")
                                ?: document.getString("driverEmail")
                                ?: "Not assigned"

                            val createdAt = document.getTimestamp("createdAt")
                            val completedAt = document.getTimestamp("completedAt")
                            val cancelledAt = document.getTimestamp("cancelledAt")
                            val declinedAt = document.getTimestamp("declinedAt")
                            val updatedAt = document.getTimestamp("updatedAt")

                            val finalTimestamp = completedAt
                                ?: cancelledAt
                                ?: declinedAt
                                ?: updatedAt
                                ?: createdAt

                            val statusText = when (statusRaw) {
                                "completed" -> "Completed"
                                "cancelled_by_rider" -> "Cancelled by you"
                                "cancelled_by_driver" -> "Cancelled by driver"
                                "declined" -> "Declined"
                                else -> statusRaw
                            }

                            val ratingText = document.getLong("riderRating")?.toString()
                                ?: document.getDouble("riderRating")?.toString()
                                ?: "—"

                            TripHistoryItem(
                                id = document.id,
                                date = formatTripDate(finalTimestamp),
                                pickup = pickup,
                                dropoff = dropoff,
                                rideType = rideType,
                                fare = normalizeFareText(fare),
                                status = statusText,
                                driverName = driverName,
                                time = formatTripTime(finalTimestamp),
                                rating = ratingText,
                                feedback = document.getString("riderFeedback").orEmpty(),
                                sortTimeMillis = finalTimestamp?.toDate()?.time ?: 0L
                            )
                        }
                        ?.sortedByDescending { it.sortTimeMillis }
                        .orEmpty()

                    trips = firebaseTrips
                    isLoading = false
                    firebaseError = null
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    val completedTrips = trips.count { it.status == "Completed" }
    val cancelledTrips = trips.count { it.status != "Completed" }
    val totalSpent = trips
        .filter { it.status == "Completed" }
        .sumOf { extractFareAmount(it.fare) }

    val averageRating = trips
        .mapNotNull { it.rating.toDoubleOrNull() }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?: 0.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.backgroundTop,
                        colors.backgroundMiddle,
                        colors.backgroundBottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 22.dp, bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.text
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
                        text = "Trip History",
                        color = colors.text,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Your real Firebase Rideit activity",
                        color = colors.subText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TripStatsCard(
                tripCount = trips.size,
                totalSpent = totalSpent,
                averageRating = averageRating,
                completedTrips = completedTrips,
                cancelledTrips = cancelledTrips,
                colors = colors
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                TripHistoryLoadingCard(colors = colors)
                Spacer(modifier = Modifier.height(14.dp))
            }

            firebaseError?.let { error ->
                TripHistoryMessageCard(
                    title = "Unable to load trips",
                    message = error,
                    success = false,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            if (!isLoading && firebaseError == null && trips.isEmpty()) {
                TripHistoryMessageCard(
                    title = "No Firebase trips yet",
                    message = "Book and complete a real Rideit trip, then it will appear here automatically.",
                    success = true,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            AnimatedVisibility(
                visible = trips.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(trips) { trip ->
                        TripCard(
                            trip = trip,
                            colors = colors
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TripStatsCard(
    tripCount: Int,
    totalSpent: Int,
    averageRating: Double,
    completedTrips: Int,
    cancelledTrips: Int,
    colors: TripHistoryThemeColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.card,
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    title = "Trips",
                    value = tripCount.toString(),
                    colors = colors
                )

                StatDivider(colors = colors)

                StatItem(
                    title = "Spent",
                    value = formatRupees(totalSpent),
                    colors = colors
                )

                StatDivider(colors = colors)

                StatItem(
                    title = "Rating",
                    value = if (averageRating > 0.0) {
                        String.format(Locale.US, "%.1f", averageRating)
                    } else {
                        "—"
                    },
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniStatPill(
                    label = "Completed",
                    value = completedTrips.toString(),
                    color = colors.success
                )

                MiniStatPill(
                    label = "Cancelled",
                    value = cancelledTrips.toString(),
                    color = colors.danger
                )
            }
        }
    }
}

@Composable
private fun MiniStatPill(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = "$label: $value",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    colors: TripHistoryThemeColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = colors.text,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = title,
            color = colors.subText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatDivider(
    colors: TripHistoryThemeColors
) {
    Box(
        modifier = Modifier
            .height(38.dp)
            .width(1.dp)
            .background(colors.border)
    )
}

@Composable
private fun TripCard(
    trip: TripHistoryItem,
    colors: TripHistoryThemeColors
) {
    val accentColor = statusColor(
        status = trip.status,
        rideType = trip.rideType,
        colors = colors
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        color = colors.card,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.18f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = trip.rideType,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = trip.fare,
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TripLocationRow(
                dotColor = colors.success,
                title = trip.pickup,
                subtitle = "Pickup",
                colors = colors
            )

            Spacer(modifier = Modifier.height(10.dp))

            TripLocationRow(
                dotColor = accentColor,
                title = trip.dropoff,
                subtitle = "Dropoff",
                colors = colors
            )

            if (trip.feedback.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = colors.innerCard
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Your feedback",
                            color = colors.subText,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = trip.feedback,
                            color = colors.text,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.innerCard
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trip.date,
                            color = colors.text,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "${trip.time} • Driver: ${trip.driverName}",
                            color = colors.subText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Rating: ${if (trip.rating == "—") "Not rated" else "⭐ ${trip.rating}"}",
                            color = colors.subText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = trip.status,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TripLocationRow(
    dotColor: Color,
    title: String,
    subtitle: String,
    colors: TripHistoryThemeColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(dotColor)
                .padding(6.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = colors.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TripHistoryLoadingCard(
    colors: TripHistoryThemeColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Loading Firebase trips...",
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = colors.primary,
                trackColor = colors.innerCard
            )
        }
    }
}

@Composable
private fun TripHistoryMessageCard(
    title: String,
    message: String,
    success: Boolean,
    colors: TripHistoryThemeColors
) {
    val color = if (success) colors.primary else colors.danger

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f))
                    .padding(13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (success) "↗" else "!",
                    color = color,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = message,
                    color = colors.subText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun rememberTripHistoryThemeColors(): TripHistoryThemeColors {
    val scheme = MaterialTheme.colorScheme

    val isRoseTheme =
        scheme.primary == Color(0xFFFF5CA8) ||
                scheme.primary == Color(0xFFEC4899) ||
                scheme.primaryContainer == Color(0xFFFFD6E8)

    val isLightTheme = scheme.background.luminance() > 0.5f

    return remember(scheme.primary, scheme.background) {
        when {
            isRoseTheme -> TripHistoryThemeColors(
                backgroundTop = Color(0xFFFFF7FB),
                backgroundMiddle = Color(0xFFFFEAF3),
                backgroundBottom = Color(0xFFFFFBFD),
                card = Color.White,
                innerCard = Color(0xFFFFEAF3),
                iconCard = Color(0xFFFFD6E8),
                primary = Color(0xFFFF5CA8),
                secondary = Color(0xFFEC4899),
                text = Color(0xFF24111A),
                subText = Color(0xFF7A445A),
                border = Color(0xFFF9A8D4),
                success = Color(0xFF16A34A),
                danger = Color(0xFFE11D48),
                warning = Color(0xFFDB7C00),
                onPrimary = Color.White
            )

            isLightTheme -> TripHistoryThemeColors(
                backgroundTop = Color(0xFFF8FAFC),
                backgroundMiddle = Color(0xFFEDE9FE),
                backgroundBottom = Color.White,
                card = Color.White,
                innerCard = Color(0xFFF1F5F9),
                iconCard = Color(0xFFEBDDFF),
                primary = scheme.primary,
                secondary = Color(0xFF2563EB),
                text = Color(0xFF111827),
                subText = Color(0xFF6B7280),
                border = Color(0xFFE5E7EB),
                success = Color(0xFF16A34A),
                danger = Color(0xFFEF4444),
                warning = Color(0xFFDB7C00),
                onPrimary = Color.White
            )

            else -> TripHistoryThemeColors(
                backgroundTop = Color(0xFF050505),
                backgroundMiddle = Color(0xFF15080B),
                backgroundBottom = Color(0xFF090909),
                card = Color(0xFF1B1B1D),
                innerCard = Color(0xFF252529),
                iconCard = Color(0xFF2A2138),
                primary = Color(0xFF8A35F2),
                secondary = Color(0xFF2563EB),
                text = Color.White,
                subText = Color(0xFF9CA3AF),
                border = Color(0xFF303036),
                success = Color(0xFF22C55E),
                danger = Color(0xFFEF4444),
                warning = Color(0xFFE17A00),
                onPrimary = Color.White
            )
        }
    }
}

private fun rideColor(
    type: String,
    colors: TripHistoryThemeColors
): Color {
    return when (type.lowercase()) {
        "mini" -> colors.primary
        "comfort" -> colors.secondary
        "business" -> colors.warning
        else -> colors.primary
    }
}

private fun statusColor(
    status: String,
    rideType: String,
    colors: TripHistoryThemeColors
): Color {
    return when (status.lowercase()) {
        "completed" -> rideColor(rideType, colors)
        "cancelled by you" -> colors.danger
        "cancelled by driver" -> colors.warning
        "declined" -> colors.danger
        else -> rideColor(rideType, colors)
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

private fun formatTripDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "Date pending"

    return try {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(timestamp.toDate())
    } catch (_: Exception) {
        "Date pending"
    }
}

private fun formatTripTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "Time pending"

    return try {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        formatter.format(timestamp.toDate())
    } catch (_: Exception) {
        "Time pending"
    }
}