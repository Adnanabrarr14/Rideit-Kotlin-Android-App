package com.example.rideit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
data class RideitNotification(
    val id: String,
    val icon: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String,
    val unread: Boolean
)

@Immutable
data class PromoOffer(
    val id: String,
    val title: String,
    val subtitle: String,
    val code: String,
    val color: Color
)

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    val promos = listOf(
        PromoOffer(
            id = "1",
            title = "50% OFF",
            subtitle = "On your next Mini ride",
            code = "RIDE50",
            color = Color(0xFF8A35F2)
        ),
        PromoOffer(
            id = "2",
            title = "Free Comfort Upgrade",
            subtitle = "Available this weekend",
            code = "COMFORT",
            color = Color(0xFF2563EB)
        )
    )

    val notifications = listOf(
        RideitNotification(
            id = "1",
            icon = "🚗",
            title = "Driver arriving soon",
            message = "Ali Khan is 2 minutes away from your pickup point.",
            time = "Now",
            type = "Ride",
            unread = true
        ),
        RideitNotification(
            id = "2",
            icon = "🎁",
            title = "Promo unlocked",
            message = "Use RIDE50 and get 50% off on your next ride.",
            time = "15 min ago",
            type = "Offer",
            unread = true
        ),
        RideitNotification(
            id = "3",
            icon = "✅",
            title = "Ride completed",
            message = "Your trip to G-10 Markaz was completed successfully.",
            time = "Today",
            type = "Trip",
            unread = false
        ),
        RideitNotification(
            id = "4",
            icon = "💳",
            title = "Payment method ready",
            message = "Cash is selected as your default payment method.",
            time = "Yesterday",
            type = "Payment",
            unread = false
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
                        text = "Notifications",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Ride alerts, promos and updates",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Promotions",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(14.dp))

            promos.forEach { promo ->
                PromoCard(promo = promo)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest Updates",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF8A35F2)
                ) {
                    Text(
                        text = "2 New",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(notification = notification)
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun PromoCard(
    promo: PromoOffer
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        shadowElevation = 14.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            promo.color,
                            promo.color.copy(alpha = 0.75f),
                            Color(0xFF111827)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = promo.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = promo.subtitle,
                    color = Color(0xFFE5E7EB),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "Code: ${promo.code}",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎁",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: RideitNotification
) {
    val accentColor = when (notification.type) {
        "Ride" -> Color(0xFF8A35F2)
        "Offer" -> Color(0xFFE17A00)
        "Trip" -> Color(0xFF16A34A)
        "Payment" -> Color(0xFF2563EB)
        else -> Color(0xFF8A35F2)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (notification.unread) 1.5.dp else 1.dp,
                color = if (notification.unread) accentColor else Color(0xFF2A2A31),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (notification.unread) Color(0xFF21182E) else Color(0xFF1B1B1D),
        shadowElevation = if (notification.unread) 10.dp else 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accentColor.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (notification.unread) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = notification.message,
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = notification.type,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = notification.time,
                        color = Color(0xFF6B7280),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

