package com.example.rideit.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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

@Immutable
private data class NotificationsThemeColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val unreadCard: Color,
    val innerCard: Color,
    val iconCard: Color,
    val primary: Color,
    val secondary: Color,
    val text: Color,
    val subText: Color,
    val border: Color,
    val success: Color,
    val warning: Color,
    val payment: Color,
    val onPrimary: Color,
    val heroEnd: Color
)

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    val colors = rememberNotificationsThemeColors()

    val promos = listOf(
        PromoOffer(
            id = "1",
            title = "50% OFF",
            subtitle = "On your next Mini ride",
            code = "RIDE50",
            color = colors.primary
        ),
        PromoOffer(
            id = "2",
            title = "Free Comfort Upgrade",
            subtitle = "Available this weekend",
            code = "COMFORT",
            color = colors.secondary
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
            NotificationsTopBar(
                colors = colors,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Promotions",
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(14.dp))

            promos.forEach { promo ->
                PromoCard(
                    promo = promo,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest Updates",
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.primary
                ) {
                    Text(
                        text = "2 New",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = colors.onPrimary,
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
                    NotificationCard(
                        notification = notification,
                        colors = colors
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificationsTopBar(
    colors: NotificationsThemeColors,
    onBackClick: () -> Unit
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
                text = "Notifications",
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Ride alerts, promos and updates",
                color = colors.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PromoCard(
    promo: PromoOffer,
    colors: NotificationsThemeColors
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
                            promo.color.copy(alpha = 0.82f),
                            colors.heroEnd
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = promo.title,
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = promo.subtitle,
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.20f)
                ) {
                    Text(
                        text = "Code: ${promo.code}",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
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
    notification: RideitNotification,
    colors: NotificationsThemeColors
) {
    val accentColor = when (notification.type) {
        "Ride" -> colors.primary
        "Offer" -> colors.warning
        "Trip" -> colors.success
        "Payment" -> colors.payment
        else -> colors.primary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (notification.unread) 1.5.dp else 1.dp,
                color = if (notification.unread) accentColor else colors.border,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (notification.unread) colors.unreadCard else colors.card,
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
                    .background(accentColor.copy(alpha = 0.18f)),
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
                        color = colors.text,
                        fontWeight = FontWeight.Black,
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
                    color = colors.subText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = notification.type,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = notification.time,
                        color = colors.subText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberNotificationsThemeColors(): NotificationsThemeColors {
    val scheme = MaterialTheme.colorScheme

    val isRoseTheme =
        scheme.primary == Color(0xFFFF5CA8) ||
                scheme.primary == Color(0xFFEC4899) ||
                scheme.primaryContainer == Color(0xFFFFD6E8)

    val isLightTheme = scheme.background.luminance() > 0.5f

    return remember(scheme.primary, scheme.background) {
        when {
            isRoseTheme -> NotificationsThemeColors(
                backgroundTop = Color(0xFFFFF7FB),
                backgroundMiddle = Color(0xFFFFEAF3),
                backgroundBottom = Color(0xFFFFFBFD),
                card = Color.White,
                unreadCard = Color(0xFFFFEAF3),
                innerCard = Color(0xFFFFEAF3),
                iconCard = Color(0xFFFFD6E8),
                primary = Color(0xFFFF5CA8),
                secondary = Color(0xFFEC4899),
                text = Color(0xFF24111A),
                subText = Color(0xFF7A445A),
                border = Color(0xFFF9A8D4),
                success = Color(0xFF16A34A),
                warning = Color(0xFFDB7C00),
                payment = Color(0xFFEC4899),
                onPrimary = Color.White,
                heroEnd = Color(0xFFBE185D)
            )

            isLightTheme -> NotificationsThemeColors(
                backgroundTop = Color(0xFFF8FAFC),
                backgroundMiddle = Color(0xFFEDE9FE),
                backgroundBottom = Color.White,
                card = Color.White,
                unreadCard = Color(0xFFF3EEFF),
                innerCard = Color(0xFFF1F5F9),
                iconCard = Color(0xFFEBDDFF),
                primary = scheme.primary,
                secondary = Color(0xFF2563EB),
                text = Color(0xFF111827),
                subText = Color(0xFF6B7280),
                border = Color(0xFFE5E7EB),
                success = Color(0xFF16A34A),
                warning = Color(0xFFDB7C00),
                payment = Color(0xFF2563EB),
                onPrimary = Color.White,
                heroEnd = Color(0xFF111827)
            )

            else -> NotificationsThemeColors(
                backgroundTop = Color(0xFF050505),
                backgroundMiddle = Color(0xFF15080B),
                backgroundBottom = Color(0xFF090909),
                card = Color(0xFF1B1B1D),
                unreadCard = Color(0xFF21182E),
                innerCard = Color(0xFF252529),
                iconCard = Color(0xFF2A2138),
                primary = Color(0xFF8A35F2),
                secondary = Color(0xFF2563EB),
                text = Color.White,
                subText = Color(0xFF9CA3AF),
                border = Color(0xFF2A2A31),
                success = Color(0xFF16A34A),
                warning = Color(0xFFE17A00),
                payment = Color(0xFF2563EB),
                onPrimary = Color.White,
                heroEnd = Color(0xFF111827)
            )
        }
    }
}