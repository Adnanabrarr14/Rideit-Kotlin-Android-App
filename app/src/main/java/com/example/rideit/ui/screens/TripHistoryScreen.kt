package com.example.rideit.ui.screens

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
    val feedbackTags: List<String>,
    val sortTimeMillis: Long
)

@Composable
fun TripHistoryScreen(
    onBackClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var firebaseError by remember { mutableStateOf<String?>(null) }
    var trips by remember { mutableStateOf<List<TripHistoryItem>>(emptyList()) }

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
                                ?: document.getString("driverEarningText")
                                ?: "Rs 0"

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
                                ?: document.getDouble("riderRating")?.toInt()?.toString()
                                ?: "—"

                            val feedbackTags = document.get("riderFeedbackTags")
                                .safeStringList()

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
                                feedbackTags = feedbackTags,
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
                        Color(0xFF050505),
                        Color(0xFF15080B),
                        Color(0xFF090909)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(18.dp))

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
                        text = "Trip History",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Your rides, ratings and feedback",
                        color = Color(0xFF9CA3AF),
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
                cancelledTrips = cancelledTrips
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                TripHistoryLoadingCard()
                Spacer(modifier = Modifier.height(14.dp))
            }

            firebaseError?.let { error ->
                TripHistoryMessageCard(
                    title = "Unable to load trips",
                    message = error,
                    success = false
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            if (!isLoading && firebaseError == null && trips.isEmpty()) {
                TripHistoryMessageCard(
                    title = "No Firebase trips yet",
                    message = "Book and complete a real Rideit trip, then it will appear here automatically.",
                    success = true
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
                        TripCard(trip = trip)
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
    cancelledTrips: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF1B1B1D),
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
                    value = tripCount.toString()
                )

                StatDivider()

                StatItem(
                    title = "Spent",
                    value = formatRupees(totalSpent)
                )

                StatDivider()

                StatItem(
                    title = "Rating",
                    value = if (averageRating > 0.0) {
                        String.format(Locale.US, "%.1f", averageRating)
                    } else {
                        "—"
                    }
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
                    color = Color(0xFF22C55E)
                )

                MiniStatPill(
                    label = "Cancelled",
                    value = cancelledTrips.toString(),
                    color = Color(0xFFEF4444)
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
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = title,
            color = Color(0xFF9CA3AF),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(38.dp)
            .width(1.dp)
            .background(Color(0xFF303036))
    )
}

@Composable
private fun TripCard(
    trip: TripHistoryItem
) {
    val accentColor = statusColor(trip.status, trip.rideType)
    val hasRating = trip.rating != "—"
    val hasFeedback = trip.feedback.isNotBlank()
    val hasTags = trip.feedbackTags.isNotEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFF1B1B1D),
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
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = trip.fare,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TripLocationRow(
                dotColor = Color(0xFF22C55E),
                title = trip.pickup,
                subtitle = "Pickup"
            )

            Spacer(modifier = Modifier.height(10.dp))

            TripLocationRow(
                dotColor = accentColor,
                title = trip.dropoff,
                subtitle = "Dropoff"
            )

            Spacer(modifier = Modifier.height(14.dp))

            RiderFeedbackSummaryCard(
                hasRating = hasRating,
                rating = trip.rating,
                hasFeedback = hasFeedback,
                feedback = trip.feedback,
                hasTags = hasTags,
                tags = trip.feedbackTags
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF252529)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trip.date,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "${trip.time} • Driver: ${trip.driverName}",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = trip.status,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun RiderFeedbackSummaryCard(
    hasRating: Boolean,
    rating: String,
    hasFeedback: Boolean,
    feedback: String,
    hasTags: Boolean,
    tags: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF252529)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your rating",
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (hasRating) "⭐ $rating / 5" else "Not rated yet",
                    color = if (hasRating) Color(0xFFFACC15) else Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your feedback",
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (hasFeedback) feedback else "No written feedback submitted.",
                color = if (hasFeedback) Color.White else Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (hasTags) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tags.joinToString(" • "),
                    color = Color(0xFF8A35F2),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TripLocationRow(
    dotColor: Color,
    title: String,
    subtitle: String
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
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TripHistoryLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1B1B1D),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Loading Firebase trips...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF8A35F2),
                trackColor = Color(0xFF303036)
            )
        }
    }
}

@Composable
private fun TripHistoryMessageCard(
    title: String,
    message: String,
    success: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (success) Color(0xFF1B1B1D) else Color(0xFF2A1212),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                color = if (success) Color.White else Color(0xFFFF6B6B),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun normalizeFareText(fare: String): String {
    val cleanFare = fare.trim()

    if (cleanFare.isBlank()) {
        return "Rs 0"
    }

    if (
        cleanFare.contains("Rs", ignoreCase = true) ||
        cleanFare.contains("₨") ||
        cleanFare.contains("$")
    ) {
        return cleanFare.replace("₨", "Rs")
    }

    return "Rs $cleanFare"
}

private fun extractFareAmount(fare: String): Int {
    return fare
        .filter { it.isDigit() }
        .toIntOrNull()
        ?: 0
}

private fun formatRupees(amount: Int): String {
    if (amount <= 0) return "Rs 0"

    return "Rs ${String.format(Locale.US, "%,d", amount)}"
}

private fun formatTripDate(timestamp: Timestamp?): String {
    if (timestamp == null) {
        return "Date pending"
    }

    return try {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(timestamp.toDate())
    } catch (_: Exception) {
        "Date pending"
    }
}

private fun formatTripTime(timestamp: Timestamp?): String {
    if (timestamp == null) {
        return "Time pending"
    }

    return try {
        SimpleDateFormat("h:mm a", Locale.getDefault())
            .format(timestamp.toDate())
    } catch (_: Exception) {
        "Time pending"
    }
}

private fun statusColor(
    status: String,
    rideType: String
): Color {
    return when {
        status.contains("cancel", ignoreCase = true) -> Color(0xFFEF4444)
        status.contains("declined", ignoreCase = true) -> Color(0xFFF97316)
        rideType.contains("comfort", ignoreCase = true) -> Color(0xFF2563EB)
        rideType.contains("business", ignoreCase = true) -> Color(0xFF8A35F2)
        else -> Color(0xFF22C55E)
    }
}

private fun Any?.safeStringList(): List<String> {
    return when (this) {
        is List<*> -> this.mapNotNull { item ->
            item?.toString()?.trim()?.takeIf { it.isNotBlank() }
        }

        is String -> this
            .split(",", "•")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        else -> emptyList()
    }
}