package com.example.rideit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    val time: String
)

@Composable
fun TripHistoryScreen(
    onBackClick: () -> Unit
) {
    val trips = listOf(
        TripHistoryItem(
            id = "1",
            date = "Today",
            pickup = "Current Location",
            dropoff = "G-10 Markaz",
            rideType = "Mini",
            fare = "Rs. 180",
            status = "Completed",
            driverName = "Ali Khan",
            time = "2:45 PM"
        ),
        TripHistoryItem(
            id = "2",
            date = "Yesterday",
            pickup = "F-10 Markaz",
            dropoff = "Centaurus Mall",
            rideType = "Comfort",
            fare = "Rs. 320",
            status = "Completed",
            driverName = "Hassan Raza",
            time = "7:20 PM"
        ),
        TripHistoryItem(
            id = "3",
            date = "Monday",
            pickup = "Blue Area",
            dropoff = "Saddar Rawalpindi",
            rideType = "Business",
            fare = "Rs. 580",
            status = "Completed",
            driverName = "Usman Malik",
            time = "11:15 AM"
        ),
        TripHistoryItem(
            id = "4",
            date = "Last Week",
            pickup = "I-8 Markaz",
            dropoff = "Faizabad",
            rideType = "Mini",
            fare = "Rs. 210",
            status = "Cancelled",
            driverName = "Not assigned",
            time = "9:30 PM"
        )
    )

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
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(34.dp))

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
                    Text("‹ Back")
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Trip History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Your recent Rideit activity",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TripStatsCard()

            Spacer(modifier = Modifier.height(18.dp))

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

@Composable
private fun TripStatsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF1B1B1D),
        shadowElevation = 18.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                title = "Trips",
                value = "12"
            )

            StatDivider()

            StatItem(
                title = "Spent",
                value = "Rs. 3.2k"
            )

            StatDivider()

            StatItem(
                title = "Rating",
                value = "4.9"
            )
        }
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
            style = MaterialTheme.typography.titleLarge
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
    val isCancelled = trip.status == "Cancelled"
    val accentColor = if (isCancelled) Color(0xFFEF4444) else rideColor(trip.rideType)

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
                        style = MaterialTheme.typography.bodyMedium
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

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF252529)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = trip.date,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "${trip.time} • Driver: ${trip.driverName}",
                            color = Color(0xFF9CA3AF),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

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
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = subtitle,
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun rideColor(type: String): Color {
    return when (type.lowercase()) {
        "mini" -> Color(0xFF8A35F2)
        "comfort" -> Color(0xFF2563EB)
        "business" -> Color(0xFFE17A00)
        else -> Color(0xFF8A35F2)
    }
}

