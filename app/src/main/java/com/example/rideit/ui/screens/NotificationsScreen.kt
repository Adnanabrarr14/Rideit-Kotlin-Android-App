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
import com.example.rideit.FirebaseManager
import com.example.rideit.RideitNotificationCenter
import com.example.rideit.RideitUserNotification
import com.google.firebase.Timestamp
import java.util.Date
import java.util.concurrent.TimeUnit

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
    accountRole: String = FirebaseManager.ROLE_RIDER,
    onBackClick: () -> Unit
) {
    val colors = rememberNotificationsThemeColors()
    val isDriver = accountRole == FirebaseManager.ROLE_DRIVER
    val demoNotifications = remember(accountRole) {
        demoNotificationsForRole(accountRole)
    }
    var notifications by remember { mutableStateOf<List<RideitUserNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isMarkingRead by remember { mutableStateOf(false) }
    var usingDemoNotifications by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { it.unread }
    val subtitle = if (isDriver) {
        "Ride requests, earnings and account updates"
    } else {
        "Ride alerts, promos and updates"
    }
    val sectionTitle = if (isDriver) "Driver Updates" else "Ride Updates"

    fun showDemoNotifications() {
        notifications = demoNotifications
        usingDemoNotifications = true
        isLoading = false
        errorMessage = null
    }

    DisposableEffect(accountRole) {
        notifications = emptyList()
        isLoading = true
        errorMessage = null
        usingDemoNotifications = false

        val registration = RideitNotificationCenter.listenToCurrentUserNotifications(
            onChange = { loadedNotifications ->
                if (loadedNotifications.isEmpty()) {
                    showDemoNotifications()
                } else {
                    notifications = loadedNotifications
                    usingDemoNotifications = false
                    isLoading = false
                    errorMessage = null
                }
            },
            onError = {
                showDemoNotifications()
            }
        )

        if (registration == null) {
            showDemoNotifications()
        }

        onDispose {
            registration?.remove()
        }
    }

    fun markAllRead() {
        if (isMarkingRead || unreadCount == 0) return

        isMarkingRead = true

        if (usingDemoNotifications) {
            notifications = notifications.map { notification ->
                notification.copy(isRead = true)
            }
            isMarkingRead = false
            errorMessage = null
            return
        }

        RideitNotificationCenter.markCurrentUserNotificationsRead(
            onComplete = {
                isMarkingRead = false
            },
            onError = {
                notifications = notifications.map { notification ->
                    notification.copy(isRead = true)
                }
                isMarkingRead = false
                errorMessage = null
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
                subtitle = subtitle,
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
                    text = sectionTitle,
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
                        title = "Unable to load notifications.",
                        message = "Please try again.",
                        colors = colors
                    )
                }

                notifications.isEmpty() -> {
                    NotificationStateCard(
                        title = "No notifications yet.",
                        message = "You're all caught up.",
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
    subtitle: String,
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
                text = subtitle,
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

private fun demoNotificationsForRole(
    accountRole: String
): List<RideitUserNotification> {
    return if (accountRole == FirebaseManager.ROLE_DRIVER) {
        listOf(
            RideitUserNotification(
                id = "driver_demo_new_request",
                icon = "N",
                title = "New ride request",
                message = "A nearby rider is ready for pickup. Review the route and fare before accepting.",
                type = RideitNotificationCenter.TYPE_DRIVER,
                eventKey = "demo_driver_new_request",
                createdAt = demoNotificationTime(minutesAgo = 4),
                isRead = false
            ),
            RideitUserNotification(
                id = "driver_demo_trip_completed",
                icon = "T",
                title = "Trip completed",
                message = "Your latest trip was completed and added to your driver history.",
                type = RideitNotificationCenter.TYPE_TRIP,
                eventKey = "demo_driver_trip_completed",
                createdAt = demoNotificationTime(minutesAgo = 18),
                isRead = false
            ),
            RideitUserNotification(
                id = "driver_demo_earnings_updated",
                icon = "Rs",
                title = "Earnings updated",
                message = "Today's completed trips are reflected in your wallet summary.",
                type = "Earnings",
                eventKey = "demo_driver_earnings_updated",
                createdAt = demoNotificationTime(minutesAgo = 46),
                isRead = true
            ),
            RideitUserNotification(
                id = "driver_demo_document_verified",
                icon = "V",
                title = "Document verified",
                message = "Your vehicle document has been reviewed and marked verified.",
                type = "Documents",
                eventKey = "demo_driver_document_verified",
                createdAt = demoNotificationTime(hoursAgo = 3),
                isRead = true
            ),
            RideitUserNotification(
                id = "driver_demo_payout_ready",
                icon = "P",
                title = "Payout demo ready",
                message = "Demo withdrawal tools are available from your driver wallet.",
                type = "Payout",
                eventKey = "demo_driver_payout_ready",
                createdAt = demoNotificationTime(hoursAgo = 6),
                isRead = true
            ),
            RideitUserNotification(
                id = "driver_demo_safety_reminder",
                icon = "S",
                title = "Safety reminder",
                message = "Confirm pickup details in the app before starting every trip.",
                type = "Safety",
                eventKey = "demo_driver_safety_reminder",
                createdAt = demoNotificationTime(hoursAgo = 10),
                isRead = true
            )
        )
    } else {
        listOf(
            RideitUserNotification(
                id = "rider_demo_driver_arriving",
                icon = "D",
                title = "Driver arriving soon",
                message = "Your driver is close to the pickup point. Please be ready outside.",
                type = RideitNotificationCenter.TYPE_DRIVER,
                eventKey = "demo_rider_driver_arriving",
                createdAt = demoNotificationTime(minutesAgo = 3),
                isRead = false
            ),
            RideitUserNotification(
                id = "rider_demo_ride_completed",
                icon = "T",
                title = "Ride completed",
                message = "Your Rideit trip summary and receipt are ready to review.",
                type = RideitNotificationCenter.TYPE_TRIP,
                eventKey = "demo_rider_ride_completed",
                createdAt = demoNotificationTime(minutesAgo = 21),
                isRead = false
            ),
            RideitUserNotification(
                id = "rider_demo_payment_ready",
                icon = "P",
                title = "Payment method ready",
                message = "Cash and demo card options are ready for your next booking.",
                type = "Payment",
                eventKey = "demo_rider_payment_ready",
                createdAt = demoNotificationTime(minutesAgo = 54),
                isRead = true
            ),
            RideitUserNotification(
                id = "rider_demo_promo_unlocked",
                icon = "%",
                title = "Promo unlocked",
                message = "A portfolio promo has been added for your next Rideit trip.",
                type = "Promo",
                eventKey = "demo_rider_promo_unlocked",
                createdAt = demoNotificationTime(hoursAgo = 4),
                isRead = true
            ),
            RideitUserNotification(
                id = "rider_demo_wallet_updated",
                icon = "W",
                title = "Wallet balance updated",
                message = "Your demo wallet balance is refreshed and ready for upcoming rides.",
                type = "Wallet",
                eventKey = "demo_rider_wallet_updated",
                createdAt = demoNotificationTime(hoursAgo = 8),
                isRead = true
            )
        )
    }
}

private fun demoNotificationTime(
    minutesAgo: Long = 0,
    hoursAgo: Long = 0
): Timestamp {
    val offsetMillis = TimeUnit.MINUTES.toMillis(minutesAgo) + TimeUnit.HOURS.toMillis(hoursAgo)
    return Timestamp(Date(System.currentTimeMillis() - offsetMillis))
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
