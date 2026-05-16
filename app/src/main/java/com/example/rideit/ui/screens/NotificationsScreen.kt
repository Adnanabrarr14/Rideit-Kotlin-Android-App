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
import com.example.rideit.RideitNotificationCenter
import com.example.rideit.RideitUserNotification

@Immutable
private data class NotificationsThemeColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val unreadCard: Color,
    val iconCard: Color,
    val primary: Color,
    val text: Color,
    val subText: Color,
    val border: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val onPrimary: Color
)

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    val colors = rememberNotificationsThemeColors()
    var notifications by remember { mutableStateOf<List<RideitUserNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isMarkingRead by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { it.unread }

    DisposableEffect(Unit) {
        val registration = RideitNotificationCenter.listenToCurrentUserNotifications(
            onChange = { loadedNotifications ->
                notifications = loadedNotifications
                isLoading = false
                errorMessage = null
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
            }
        )

        if (registration == null) {
            isLoading = false
            errorMessage = "Please login again to load notifications."
        }

        onDispose {
            registration?.remove()
        }
    }

    fun markAllRead() {
        if (isMarkingRead || unreadCount == 0) return

        isMarkingRead = true
        RideitNotificationCenter.markCurrentUserNotificationsRead(
            onComplete = {
                isMarkingRead = false
            },
            onError = { error ->
                isMarkingRead = false
                errorMessage = error
            }
        )
    }

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
                unreadCount = unreadCount,
                isMarkingRead = isMarkingRead,
                onBackClick = onBackClick,
                onMarkReadClick = ::markAllRead
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ride Updates",
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                if (unreadCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colors.primary
                    ) {
                        Text(
                            text = "$unreadCount New",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when {
                isLoading -> {
                    NotificationStateCard(
                        title = "Loading notifications",
                        message = "Fetching your latest ride updates.",
                        colors = colors
                    )
                }

                errorMessage != null -> {
                    NotificationStateCard(
                        title = "Unable to load notifications",
                        message = errorMessage.orEmpty(),
                        colors = colors
                    )
                }

                notifications.isEmpty() -> {
                    NotificationStateCard(
                        title = "No ride notifications yet",
                        message = "Ride updates will appear here when you book, accept, start, complete, or cancel trips.",
                        colors = colors
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = notifications,
                            key = { notification -> notification.id }
                        ) { notification ->
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
    }
}

@Composable
private fun NotificationsTopBar(
    colors: NotificationsThemeColors,
    unreadCount: Int,
    isMarkingRead: Boolean,
    onBackClick: () -> Unit,
    onMarkReadClick: () -> Unit
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
                text = "< Back",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Notifications",
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Ride alerts and trip updates",
                color = colors.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        if (unreadCount > 0) {
            OutlinedButton(
                onClick = onMarkReadClick,
                enabled = !isMarkingRead,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primary
                )
            ) {
                Text(
                    text = if (isMarkingRead) "Saving" else "Mark read",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun NotificationStateCard(
    title: String,
    message: String,
    colors: NotificationsThemeColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                color = colors.subText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NotificationCard(
    notification: RideitUserNotification,
    colors: NotificationsThemeColors
) {
    val accentColor = when (notification.type) {
        RideitNotificationCenter.TYPE_DRIVER -> colors.primary
        RideitNotificationCenter.TYPE_TRIP -> colors.success
        RideitNotificationCenter.TYPE_CANCELLATION -> colors.danger
        else -> colors.warning
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
                    color = accentColor,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
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
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
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
                        text = notification.timeText,
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
                iconCard = Color(0xFFFFD6E8),
                primary = Color(0xFFFF5CA8),
                text = Color(0xFF24111A),
                subText = Color(0xFF7A445A),
                border = Color(0xFFF9A8D4),
                success = Color(0xFF16A34A),
                warning = Color(0xFFDB7C00),
                danger = Color(0xFFDC2626),
                onPrimary = Color.White
            )

            isLightTheme -> NotificationsThemeColors(
                backgroundTop = Color(0xFFF8FAFC),
                backgroundMiddle = Color(0xFFEDE9FE),
                backgroundBottom = Color.White,
                card = Color.White,
                unreadCard = Color(0xFFF3EEFF),
                iconCard = Color(0xFFEBDDFF),
                primary = scheme.primary,
                text = Color(0xFF111827),
                subText = Color(0xFF6B7280),
                border = Color(0xFFE5E7EB),
                success = Color(0xFF16A34A),
                warning = Color(0xFF8A35F2),
                danger = Color(0xFFDC2626),
                onPrimary = Color.White
            )

            else -> NotificationsThemeColors(
                backgroundTop = Color(0xFF050505),
                backgroundMiddle = Color(0xFF15080B),
                backgroundBottom = Color(0xFF090909),
                card = Color(0xFF1B1B1D),
                unreadCard = Color(0xFF21182E),
                iconCard = Color(0xFF2A2138),
                primary = Color(0xFF8A35F2),
                text = Color.White,
                subText = Color(0xFF9CA3AF),
                border = Color(0xFF2A2A31),
                success = Color(0xFF16A34A),
                warning = Color(0xFF8A35F2),
                danger = Color(0xFFEF4444),
                onPrimary = Color.White
            )
        }
    }
}
