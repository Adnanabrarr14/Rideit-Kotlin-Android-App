package com.example.rideit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitDrawerShortcutType {
    PROFILE,
    TRIPS,
    HISTORY,
    SAVED_PLACES,
    PAYMENTS,
    SUPPORT,
    SAFETY,
    SETTINGS,
    DRIVER_DASHBOARD,
    EARNINGS,
    VEHICLE,
    LOGOUT,
    CUSTOM
}

enum class RideitDrawerShortcutStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

@Immutable
data class RideitDrawerShortcutUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val type: RideitDrawerShortcutType = RideitDrawerShortcutType.CUSTOM,
    val style: RideitDrawerShortcutStyle = RideitDrawerShortcutStyle.NEUTRAL,
    val badgeText: String? = null,
    val enabled: Boolean = true,
    val highlighted: Boolean = false
)

@Composable
fun RideitDrawerProfileHeaderCard(
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    name: String? = null,
    email: String? = null,
    ratingText: String? = null,
    statusText: String? = null,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 14.dp else 20.dp,
                shape = RoundedCornerShape(if (compact) 26.dp else 32.dp),
                spotColor = Color.Black.copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(if (compact) 26.dp else 32.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 26.dp else 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isDriverMode) {
                            listOf(Color(0xFF0F172A), Color(0xFF2563EB))
                        } else {
                            listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                        }
                    )
                )
                .padding(if (compact) 16.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitDrawerAvatar(
                name = name,
                compact = compact
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Text(
                    text = name?.takeIf { it.isNotBlank() }
                        ?: if (isDriverMode) "Rideit Driver" else "Rideit Rider",
                    color = Color.White,
                    fontSize = if (compact) 17.sp else 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = email?.takeIf { it.isNotBlank() }
                        ?: if (isDriverMode) "Driver account" else "Rider account",
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RideitDrawerHeaderMiniBadge(
                        text = if (isDriverMode) "Driver" else "Rider"
                    )

                    RideitDrawerHeaderMiniBadge(
                        text = ratingText?.takeIf { it.isNotBlank() } ?: "5.0 ★"
                    )

                    if (!statusText.isNullOrBlank()) {
                        RideitDrawerHeaderMiniBadge(text = statusText)
                    }
                }
            }
        }
    }
}

@Composable
fun RideitDrawerShortcutCard(
    shortcut: RideitDrawerShortcutUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    val alpha = if (shortcut.enabled) 1f else 0.55f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (shortcut.highlighted) 14.dp else 8.dp,
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
                spotColor = shortcut.style.dotColor().copy(alpha = 0.16f)
            )
            .border(
                width = if (shortcut.highlighted) 2.dp else 1.dp,
                color = if (shortcut.highlighted) {
                    shortcut.style.borderColor()
                } else {
                    Color.White.copy(alpha = 0.62f)
                },
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable(enabled = shortcut.enabled) {
                onClick()
            }
            .graphicsLayer {
                this.alpha = alpha
            },
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (shortcut.highlighted) {
                            listOf(shortcut.style.softBackgroundColor(), Color.White)
                        } else {
                            listOf(Color.White, Color(0xFFF8FAFC))
                        }
                    )
                )
                .padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitDrawerShortcutIcon(
                type = shortcut.type,
                style = shortcut.style,
                compact = compact,
                pulse = shortcut.highlighted || shortcut.type == RideitDrawerShortcutType.LOGOUT
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = shortcut.title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!shortcut.subtitle.isNullOrBlank()) {
                    Text(
                        text = shortcut.subtitle,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!shortcut.badgeText.isNullOrBlank()) {
                RideitDrawerShortcutBadge(
                    text = shortcut.badgeText,
                    style = shortcut.style,
                    compact = compact
                )
            }
        }
    }
}

@Composable
fun RideitDrawerShortcutList(
    shortcuts: List<RideitDrawerShortcutUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onShortcutClick: (RideitDrawerShortcutUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
    ) {
        shortcuts.forEach { shortcut ->
            RideitDrawerShortcutCard(
                shortcut = shortcut,
                compact = compact,
                onClick = {
                    onShortcutClick(shortcut)
                }
            )
        }
    }
}

@Composable
fun RideitDrawerShortcutSection(
    title: String,
    shortcuts: List<RideitDrawerShortcutUiModel>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    compact: Boolean = false,
    onShortcutClick: (RideitDrawerShortcutUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 15.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            RideitDrawerShortcutBadge(
                text = "${shortcuts.size}",
                style = RideitDrawerShortcutStyle.PRIMARY,
                compact = true
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        RideitDrawerShortcutList(
            shortcuts = shortcuts,
            compact = compact,
            onShortcutClick = onShortcutClick
        )
    }
}

@Composable
fun RideitDrawerMenuContent(
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    name: String? = null,
    email: String? = null,
    ratingText: String? = null,
    statusText: String? = null,
    compact: Boolean = true,
    onProfileClick: () -> Unit = {},
    onShortcutClick: (RideitDrawerShortcutUiModel) -> Unit = {}
) {
    val mainShortcuts = if (isDriverMode) {
        rideitDriverDrawerMainShortcuts()
    } else {
        rideitRiderDrawerMainShortcuts()
    }

    val accountShortcuts = if (isDriverMode) {
        rideitDriverDrawerAccountShortcuts()
    } else {
        rideitRiderDrawerAccountShortcuts()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 16.dp)
    ) {
        RideitDrawerProfileHeaderCard(
            isDriverMode = isDriverMode,
            name = name,
            email = email,
            ratingText = ratingText,
            statusText = statusText,
            compact = compact,
            onClick = onProfileClick
        )

        RideitDrawerShortcutSection(
            title = if (isDriverMode) "Driver menu" else "Rider menu",
            subtitle = if (isDriverMode) "Trips, earnings, and vehicle tools" else "Trips, places, payments, and support",
            shortcuts = mainShortcuts,
            compact = compact,
            onShortcutClick = onShortcutClick
        )

        RideitDrawerShortcutSection(
            title = "Account",
            subtitle = "Settings, safety, and session",
            shortcuts = accountShortcuts,
            compact = compact,
            onShortcutClick = onShortcutClick
        )
    }
}

@Composable
fun RideitAnimatedDrawerNoticeCard(
    visible: Boolean,
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    style: RideitDrawerShortcutStyle = RideitDrawerShortcutStyle.PRIMARY,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = style.borderColor(),
                    shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
                )
                .clickable { onClick() },
            shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
            colors = CardDefaults.cardColors(containerColor = style.softBackgroundColor()),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(if (compact) 13.dp else 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitDrawerShortcutIcon(
                    type = RideitDrawerShortcutType.CUSTOM,
                    style = style,
                    compact = compact,
                    pulse = true
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = title,
                        color = Color(0xFF0F172A),
                        fontSize = if (compact) 13.sp else 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = message,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun RideitDrawerAvatar(
    name: String?,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .size(if (compact) 54.dp else 62.dp)
            .background(
                color = Color.White.copy(alpha = 0.20f),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.28f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.toRideitDrawerInitials(),
            color = Color.White,
            fontSize = if (compact) 18.sp else 21.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RideitDrawerHeaderMiniBadge(
    text: String
) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitDrawerShortcutIcon(
    type: RideitDrawerShortcutType,
    style: RideitDrawerShortcutStyle,
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_drawer_shortcut_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_drawer_shortcut_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 38.dp else 44.dp)
            .background(
                color = style.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 16.dp else 19.dp)
                .graphicsLayer {
                    scaleX = if (pulse) scale else 1f
                    scaleY = if (pulse) scale else 1f
                }
                .background(
                    color = style.dotColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.iconText(),
                color = Color.White,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun RideitDrawerShortcutBadge(
    text: String,
    style: RideitDrawerShortcutStyle,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = style.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = style.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = if (compact) 8.dp else 9.dp,
                vertical = if (compact) 5.dp else 6.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = style.textColor(),
            fontSize = if (compact) 9.sp else 10.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun rideitRiderDrawerMainShortcuts(): List<RideitDrawerShortcutUiModel> {
    return listOf(
        RideitDrawerShortcutUiModel(
            id = "trips",
            title = "My trips",
            subtitle = "Active and recent rides",
            type = RideitDrawerShortcutType.TRIPS,
            style = RideitDrawerShortcutStyle.PRIMARY,
            badgeText = "Ride"
        ),
        RideitDrawerShortcutUiModel(
            id = "history",
            title = "Trip history",
            subtitle = "Receipts and completed rides",
            type = RideitDrawerShortcutType.HISTORY,
            style = RideitDrawerShortcutStyle.SUCCESS
        ),
        RideitDrawerShortcutUiModel(
            id = "saved_places",
            title = "Saved places",
            subtitle = "Home, work, and favorites",
            type = RideitDrawerShortcutType.SAVED_PLACES,
            style = RideitDrawerShortcutStyle.PREMIUM
        ),
        RideitDrawerShortcutUiModel(
            id = "payments",
            title = "Payments",
            subtitle = "Fare, cash, and receipts",
            type = RideitDrawerShortcutType.PAYMENTS,
            style = RideitDrawerShortcutStyle.WARNING
        )
    )
}

fun rideitRiderDrawerAccountShortcuts(): List<RideitDrawerShortcutUiModel> {
    return listOf(
        RideitDrawerShortcutUiModel(
            id = "support",
            title = "Support",
            subtitle = "Help center and trip support",
            type = RideitDrawerShortcutType.SUPPORT,
            style = RideitDrawerShortcutStyle.PRIMARY
        ),
        RideitDrawerShortcutUiModel(
            id = "safety",
            title = "Safety",
            subtitle = "Emergency and trusted tools",
            type = RideitDrawerShortcutType.SAFETY,
            style = RideitDrawerShortcutStyle.SUCCESS
        ),
        RideitDrawerShortcutUiModel(
            id = "settings",
            title = "Settings",
            subtitle = "Account preferences",
            type = RideitDrawerShortcutType.SETTINGS,
            style = RideitDrawerShortcutStyle.NEUTRAL
        ),
        RideitDrawerShortcutUiModel(
            id = "logout",
            title = "Logout",
            subtitle = "Sign out from Rideit",
            type = RideitDrawerShortcutType.LOGOUT,
            style = RideitDrawerShortcutStyle.DANGER
        )
    )
}

fun rideitDriverDrawerMainShortcuts(): List<RideitDrawerShortcutUiModel> {
    return listOf(
        RideitDrawerShortcutUiModel(
            id = "driver_dashboard",
            title = "Driver dashboard",
            subtitle = "Requests and active ride",
            type = RideitDrawerShortcutType.DRIVER_DASHBOARD,
            style = RideitDrawerShortcutStyle.PRIMARY,
            badgeText = "Live"
        ),
        RideitDrawerShortcutUiModel(
            id = "earnings",
            title = "Earnings",
            subtitle = "Trips, income, and payouts",
            type = RideitDrawerShortcutType.EARNINGS,
            style = RideitDrawerShortcutStyle.SUCCESS
        ),
        RideitDrawerShortcutUiModel(
            id = "vehicle",
            title = "Vehicle",
            subtitle = "Car details and availability",
            type = RideitDrawerShortcutType.VEHICLE,
            style = RideitDrawerShortcutStyle.PREMIUM
        ),
        RideitDrawerShortcutUiModel(
            id = "history",
            title = "Driver history",
            subtitle = "Completed and cancelled trips",
            type = RideitDrawerShortcutType.HISTORY,
            style = RideitDrawerShortcutStyle.WARNING
        )
    )
}

fun rideitDriverDrawerAccountShortcuts(): List<RideitDrawerShortcutUiModel> {
    return listOf(
        RideitDrawerShortcutUiModel(
            id = "support",
            title = "Driver support",
            subtitle = "Ride, payout, and account help",
            type = RideitDrawerShortcutType.SUPPORT,
            style = RideitDrawerShortcutStyle.PRIMARY
        ),
        RideitDrawerShortcutUiModel(
            id = "safety",
            title = "Driver safety",
            subtitle = "Safety tools for active rides",
            type = RideitDrawerShortcutType.SAFETY,
            style = RideitDrawerShortcutStyle.SUCCESS
        ),
        RideitDrawerShortcutUiModel(
            id = "settings",
            title = "Settings",
            subtitle = "Driver account preferences",
            type = RideitDrawerShortcutType.SETTINGS,
            style = RideitDrawerShortcutStyle.NEUTRAL
        ),
        RideitDrawerShortcutUiModel(
            id = "logout",
            title = "Logout",
            subtitle = "Sign out from Rideit",
            type = RideitDrawerShortcutType.LOGOUT,
            style = RideitDrawerShortcutStyle.DANGER
        )
    )
}

private fun String?.toRideitDrawerInitials(): String {
    val clean = this?.trim().orEmpty()

    if (clean.isBlank()) return "R"

    val parts = clean.split(" ").filter { it.isNotBlank() }

    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        clean.length >= 2 -> clean.take(2).uppercase()
        else -> clean.take(1).uppercase()
    }
}

private fun RideitDrawerShortcutType.iconText(): String {
    return when (this) {
        RideitDrawerShortcutType.PROFILE -> "P"
        RideitDrawerShortcutType.TRIPS -> "R"
        RideitDrawerShortcutType.HISTORY -> "H"
        RideitDrawerShortcutType.SAVED_PLACES -> "★"
        RideitDrawerShortcutType.PAYMENTS -> "Rs"
        RideitDrawerShortcutType.SUPPORT -> "?"
        RideitDrawerShortcutType.SAFETY -> "✓"
        RideitDrawerShortcutType.SETTINGS -> "S"
        RideitDrawerShortcutType.DRIVER_DASHBOARD -> "D"
        RideitDrawerShortcutType.EARNINGS -> "Rs"
        RideitDrawerShortcutType.VEHICLE -> "V"
        RideitDrawerShortcutType.LOGOUT -> "!"
        RideitDrawerShortcutType.CUSTOM -> "•"
    }
}

private fun RideitDrawerShortcutStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitDrawerShortcutStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitDrawerShortcutStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitDrawerShortcutStyle.WARNING -> Color(0xFFFEF3C7)
        RideitDrawerShortcutStyle.DANGER -> Color(0xFFFEE2E2)
        RideitDrawerShortcutStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitDrawerShortcutStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitDrawerShortcutStyle.borderColor(): Color {
    return when (this) {
        RideitDrawerShortcutStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitDrawerShortcutStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitDrawerShortcutStyle.WARNING -> Color(0xFFFDE68A)
        RideitDrawerShortcutStyle.DANGER -> Color(0xFFFCA5A5)
        RideitDrawerShortcutStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitDrawerShortcutStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitDrawerShortcutStyle.dotColor(): Color {
    return when (this) {
        RideitDrawerShortcutStyle.PRIMARY -> Color(0xFF2563EB)
        RideitDrawerShortcutStyle.SUCCESS -> Color(0xFF22C55E)
        RideitDrawerShortcutStyle.WARNING -> Color(0xFFF59E0B)
        RideitDrawerShortcutStyle.DANGER -> Color(0xFFEF4444)
        RideitDrawerShortcutStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitDrawerShortcutStyle.NEUTRAL -> Color(0xFF64748B)
    }
}

private fun RideitDrawerShortcutStyle.textColor(): Color {
    return when (this) {
        RideitDrawerShortcutStyle.PRIMARY -> Color(0xFF2563EB)
        RideitDrawerShortcutStyle.SUCCESS -> Color(0xFF166534)
        RideitDrawerShortcutStyle.WARNING -> Color(0xFF92400E)
        RideitDrawerShortcutStyle.DANGER -> Color(0xFFB91C1C)
        RideitDrawerShortcutStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitDrawerShortcutStyle.NEUTRAL -> Color(0xFF475569)
    }
}