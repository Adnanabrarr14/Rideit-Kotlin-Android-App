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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

private data class DriverTripHistoryItem(
    val requestId: String,
    val riderName: String,
    val riderEmail: String,
    val pickup: String,
    val dropoff: String,
    val fare: String,
    val time: String,
    val date: String,
    val distance: String,
    val rating: String,
    val feedback: String,
    val feedbackTags: List<String>,
    val status: String,
    val statusColor: Color,
    val sortTimeMillis: Long
)

@Composable
fun DriverTripHistoryScreen(
    driverName: String = FirebaseManager.currentDriverDisplayName(),
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var firebaseError by remember { mutableStateOf<String?>(null) }
    var trips by remember { mutableStateOf<List<DriverTripHistoryItem>>(emptyList()) }

    val driverId = remember { FirebaseManager.currentUserId().orEmpty() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    DisposableEffect(driverId) {
        var listenerRegistration: ListenerRegistration? = null

        if (driverId.isBlank()) {
            isLoading = false
            firebaseError = "Driver account not found. Please login again."
            trips = emptyList()
        } else {
            isLoading = true
            firebaseError = null

            listenerRegistration = firestore.collection("ride_requests")
                .whereEqualTo("driverId", driverId)
                .limit(75)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        isLoading = false
                        firebaseError = error.message ?: "Failed to load driver trip history."
                        trips = emptyList()
                        return@addSnapshotListener
                    }

                    val completedStatuses = setOf(
                        "completed",
                        "cancelled_by_driver",
                        "cancelled_by_rider"
                    )

                    val historyTrips = snapshots
                        ?.documents
                        ?.mapNotNull { document ->
                            val statusRaw = document.getString("status").orEmpty().lowercase()

                            if (statusRaw !in completedStatuses) {
                                return@mapNotNull null
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

                            val riderName = document.getString("riderName")
                                ?: riderEmail
                                    .substringBefore("@")
                                    .replace(".", " ")
                                    .replace("_", " ")
                                    .split(" ")
                                    .filter { it.isNotBlank() }
                                    .joinToString(" ") { word ->
                                        word.replaceFirstChar { char ->
                                            if (char.isLowerCase()) char.titlecase() else char.toString()
                                        }
                                    }
                                    .ifBlank { "Rideit Rider" }

                            val fare = document.getString("driverEarningText")
                                ?: document.getString("fareEstimate")
                                ?: document.getString("fare")
                                ?: "Rs 0"

                            val completedAt = document.getTimestamp("completedAt")
                            val cancelledAt = document.getTimestamp("cancelledAt")
                            val updatedAt = document.getTimestamp("updatedAt")
                            val createdAt = document.getTimestamp("createdAt")

                            val finalTimestamp = completedAt
                                ?: cancelledAt
                                ?: updatedAt
                                ?: createdAt

                            val statusText = when (statusRaw) {
                                "completed" -> "Completed"
                                "cancelled_by_driver" -> "Cancelled by you"
                                "cancelled_by_rider" -> "Cancelled by rider"
                                else -> statusRaw
                            }

                            val statusColor = when (statusRaw) {
                                "completed" -> Color(0xFF16A34A)
                                "cancelled_by_driver" -> Color(0xFFEF4444)
                                "cancelled_by_rider" -> Color(0xFFE17A00)
                                else -> Color(0xFF6B7280)
                            }

                            val ratingValue = document.getLong("riderRating")?.toString()
                                ?: document.getDouble("riderRating")?.toInt()?.toString()
                                ?: "—"

                            val feedback = document.getString("riderFeedback").orEmpty()
                            val feedbackTags = document.get("riderFeedbackTags").safeStringList()

                            DriverTripHistoryItem(
                                requestId = document.id,
                                riderName = cleanDriverHistoryText(riderName),
                                riderEmail = cleanDriverHistoryText(riderEmail),
                                pickup = cleanDriverHistoryText(pickup),
                                dropoff = cleanDriverHistoryText(dropoff),
                                fare = normalizeFareText(fare),
                                time = estimateTripTimeText(createdAt, finalTimestamp),
                                date = formatTripDate(finalTimestamp),
                                distance = document.getString("distance")
                                    ?: document.getString("distanceText")
                                    ?: "—",
                                rating = ratingValue,
                                feedback = cleanDriverHistoryText(feedback),
                                feedbackTags = feedbackTags,
                                status = statusText,
                                statusColor = statusColor,
                                sortTimeMillis = finalTimestamp?.toDate()?.time ?: 0L
                            )
                        }
                        ?.sortedByDescending { it.sortTimeMillis }
                        .orEmpty()

                    trips = historyTrips
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
    val totalEarnings = trips
        .filter { it.status == "Completed" }
        .sumOf { extractFareAmount(it.fare) }

    val ratedTrips = trips
        .filter { it.status == "Completed" }
        .mapNotNull { it.rating.toDoubleOrNull() }

    val averageRating = ratedTrips
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?: 0.0

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
            DriverTripHistoryHeader(
                driverName = driverName,
                totalTrips = trips.size,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripHistorySummaryCard(
                completedTrips = completedTrips,
                cancelledTrips = cancelledTrips,
                totalEarnings = totalEarnings,
                averageRating = averageRating,
                ratedTrips = ratedTrips.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripHistoryPerformanceCard(
                completedTrips = completedTrips,
                totalTrips = trips.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Driver trips",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                DriverTripHistoryLoadingCard()
                Spacer(modifier = Modifier.height(12.dp))
            }

            firebaseError?.let { error ->
                DriverTripHistoryMessageCard(
                    title = "Unable to load trips",
                    message = error,
                    success = false
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!isLoading && firebaseError == null && trips.isEmpty()) {
                DriverTripHistoryMessageCard(
                    title = "No trips yet",
                    message = "Complete or cancel a real ride request, then it will appear here automatically.",
                    success = true
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            AnimatedVisibility(
                visible = trips.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    trips.forEach { trip ->
                        DriverTripHistoryCard(
                            trip = trip,
                            onClick = {
                                scope.launch {
                                    val ratingText = if (trip.rating != "—") {
                                        " Rating: ${trip.rating}/5."
                                    } else {
                                        " No rating yet."
                                    }

                                    val feedbackText = if (trip.feedback.isNotBlank()) {
                                        " Feedback: ${trip.feedback}"
                                    } else {
                                        ""
                                    }

                                    snackbarHostState.showSnackbar(
                                        message = "${trip.status} trip with ${trip.riderName}.$ratingText$feedbackText"
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
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
private fun DriverTripHistoryHeader(
    driverName: String,
    totalTrips: Int,
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
                        text = "Driver Trip History",
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
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "$totalTrips trips",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "See completed trips, earnings, rider ratings and feedback.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DriverTripHistorySummaryCard(
    completedTrips: Int,
    cancelledTrips: Int,
    totalEarnings: Int,
    averageRating: Double,
    ratedTrips: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryMetricBox(
                    title = "Completed",
                    value = completedTrips.toString(),
                    subtitle = "Trips",
                    modifier = Modifier.weight(1f)
                )

                HistoryMetricBox(
                    title = "Earned",
                    value = formatRupees(totalEarnings),
                    subtitle = "Total",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryMetricBox(
                    title = "Cancelled",
                    value = cancelledTrips.toString(),
                    subtitle = "Trips",
                    modifier = Modifier.weight(1f)
                )

                HistoryMetricBox(
                    title = "Rider Rating",
                    value = if (averageRating > 0.0) {
                        String.format(Locale.US, "%.1f", averageRating)
                    } else {
                        "—"
                    },
                    subtitle = "$ratedTrips rated trips",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HistoryMetricBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

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
private fun DriverTripHistoryPerformanceCard(
    completedTrips: Int,
    totalTrips: Int
) {
    val progress = if (totalTrips == 0) {
        0f
    } else {
        (completedTrips.toFloat() / totalTrips.toFloat()).coerceIn(0f, 1f)
    }

    val percent = (progress * 100).roundToInt()

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
                        text = "Trip completion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$completedTrips of $totalTrips history trips completed",
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
                        text = "$percent%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8A35F2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { progress },
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
private fun DriverTripHistoryLoadingCard() {
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
                text = "Loading trips...",
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
private fun DriverTripHistoryMessageCard(
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
                    text = if (success) "↗" else "!",
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

@Composable
private fun DriverTripHistoryCard(
    trip: DriverTripHistoryItem,
    onClick: () -> Unit
) {
    val hasRating = trip.rating != "—"
    val hasFeedback = trip.feedback.isNotBlank()
    val hasTags = trip.feedbackTags.isNotEmpty()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(trip.statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (trip.status == "Completed") "✓" else "!",
                        color = trip.statusColor,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip.riderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = trip.date,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = trip.fare,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827)
                    )

                    Text(
                        text = trip.status,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = trip.statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            DriverRouteMiniBlock(
                pickup = trip.pickup,
                dropoff = trip.dropoff
            )

            Spacer(modifier = Modifier.height(14.dp))

            DriverRiderFeedbackCard(
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
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF8FAFC)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    InfoRow(
                        label = "Rider email",
                        value = trip.riderEmail
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow(
                        label = "Duration",
                        value = trip.time
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoRow(
                        label = "Distance",
                        value = trip.distance
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverRiderFeedbackCard(
    hasRating: Boolean,
    rating: String,
    hasFeedback: Boolean,
    feedback: String,
    hasTags: Boolean,
    tags: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF111827)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rider rating",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleSmall
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
                text = "Rider feedback",
                color = Color(0xFFCBD5E1),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = if (hasFeedback) feedback else "No written feedback from rider yet.",
                color = if (hasFeedback) Color.White else Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (hasTags) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = tags.joinToString(" • "),
                    color = Color(0xFFB98CFF),
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
private fun DriverRouteMiniBlock(
    pickup: String,
    dropoff: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            RouteLine(
                dotColor = Color(0xFF16A34A),
                title = "Pickup",
                value = pickup
            )

            Spacer(modifier = Modifier.height(10.dp))

            RouteLine(
                dotColor = Color(0xFF8A35F2),
                title = "Dropoff",
                value = dropoff
            )
        }
    }
}

@Composable
private fun RouteLine(
    dotColor: Color,
    title: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = value,
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = Color(0xFF111827),
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

private fun cleanDriverHistoryText(value: String): String {
    return value
        .replace("Adna n Khan", "Adnan Khan")
        .replace("Toyota C orolla", "Toyota Corolla")
        .replace("No written f edback submitted.", "No written feedback submitted.")
        .replace(Regex("\\s+([@.])\\s*"), "$1")
        .replace(Regex("\\s+"), " ")
        .trim()
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

private fun estimateTripTimeText(
    startedAt: Timestamp?,
    endedAt: Timestamp?
): String {
    if (startedAt == null || endedAt == null) {
        return "Time pending"
    }

    val diffMillis = endedAt.toDate().time - startedAt.toDate().time
    val minutes = (diffMillis / 60000L).coerceAtLeast(1L)

    return "$minutes min"
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
