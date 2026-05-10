package com.example.rideit.ui.screens.components

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

enum class RideitSettingsItemType {
    ACCOUNT,
    NOTIFICATIONS,
    LOCATION,
    PRIVACY,
    SECURITY,
    PAYMENT,
    LANGUAGE,
    THEME,
    DRIVER_MODE,
    VEHICLE,
    SUPPORT,
    LOGOUT,
    DELETE_ACCOUNT,
    CUSTOM
}

enum class RideitSettingsItemStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

@Immutable
data class RideitSettingsItemUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val type: RideitSettingsItemType = RideitSettingsItemType.CUSTOM,
    val style: RideitSettingsItemStyle = RideitSettingsItemStyle.NEUTRAL,
    val badgeText: String? = null,
    val enabled: Boolean = true,
    val highlighted: Boolean = false
)

@Composable
fun RideitSettingsHeroCard(
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    title: String? = null,
    message: String? = null,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 16.dp else 22.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = Color.Black.copy(alpha = 0.17f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color.White.copy(alpha = 0.22f)
                    )
                ),
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
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
                .padding(if (compact) 18.dp else 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSettingsIcon(
                type = RideitSettingsItemType.CUSTOM,
                style = RideitSettingsItemStyle.PREMIUM,
                compact = compact,
                forceLight = true,
                pulse = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = title ?: if (isDriverMode) "Driver Settings" else "Rideit Settings",
                    color = Color.White,
                    fontSize = if (compact) 19.sp else 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = message ?: if (isDriverMode) {
                        "Manage driver account, vehicle, notifications, safety, and app preferences."
                    } else {
                        "Manage profile, privacy, location, payments, notifications, and app preferences."
                    },
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = if (compact) 17.sp else 19.sp,
                    modifier = Modifier.padding(top = 5.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RideitSettingsItemCard(
    item: RideitSettingsItemUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    trailingText: String? = null,
    onClick: () -> Unit = {}
) {
    val alpha = if (item.enabled) 1f else 0.55f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (item.highlighted) 14.dp else 8.dp,
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
                spotColor = item.style.dotColor().copy(alpha = 0.16f)
            )
            .border(
                width = if (item.highlighted) 2.dp else 1.dp,
                color = if (item.highlighted) item.style.borderColor() else Color.White.copy(alpha = 0.62f),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable(enabled = item.enabled) {
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
                        colors = if (item.highlighted) {
                            listOf(item.style.softBackgroundColor(), Color.White)
                        } else {
                            listOf(Color.White, Color(0xFFF8FAFC))
                        }
                    )
                )
                .padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSettingsIcon(
                type = item.type,
                style = item.style,
                compact = compact,
                pulse = item.highlighted ||
                        item.type == RideitSettingsItemType.LOGOUT ||
                        item.type == RideitSettingsItemType.DELETE_ACCOUNT
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.subtitle.isNullOrBlank()) {
                    Text(
                        text = item.subtitle,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            when {
                !trailingText.isNullOrBlank() -> {
                    RideitSettingsBadge(
                        text = trailingText,
                        style = item.style,
                        compact = compact
                    )
                }

                !item.badgeText.isNullOrBlank() -> {
                    RideitSettingsBadge(
                        text = item.badgeText,
                        style = item.style,
                        compact = compact
                    )
                }

                else -> {
                    Text(
                        text = "›",
                        color = Color(0xFF94A3B8),
                        fontSize = if (compact) 22.sp else 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun RideitSettingsSwitchCard(
    item: RideitSettingsItemUiModel,
    checked: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    val alpha = if (item.enabled) 1f else 0.55f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (item.highlighted) 14.dp else 8.dp,
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
                spotColor = item.style.dotColor().copy(alpha = 0.16f)
            )
            .border(
                width = if (item.highlighted) 2.dp else 1.dp,
                color = if (item.highlighted) item.style.borderColor() else Color.White.copy(alpha = 0.62f),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
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
                        colors = if (item.highlighted) {
                            listOf(item.style.softBackgroundColor(), Color.White)
                        } else {
                            listOf(Color.White, Color(0xFFF8FAFC))
                        }
                    )
                )
                .clickable(enabled = item.enabled) {
                    onCheckedChange(!checked)
                }
                .padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSettingsIcon(
                type = item.type,
                style = item.style,
                compact = compact,
                pulse = checked
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.subtitle.isNullOrBlank()) {
                    Text(
                        text = item.subtitle,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = {
                    if (item.enabled) onCheckedChange(it)
                },
                enabled = item.enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = item.style.dotColor(),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCBD5E1),
                    uncheckedBorderColor = Color(0xFFE2E8F0)
                )
            )
        }
    }
}

@Composable
fun RideitSettingsSection(
    title: String,
    items: List<RideitSettingsItemUiModel>,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    compact: Boolean = false,
    onItemClick: (RideitSettingsItemUiModel) -> Unit = {}
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

            RideitSettingsBadge(
                text = "${items.size}",
                style = RideitSettingsItemStyle.PRIMARY,
                compact = true
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
        ) {
            items.forEach { item ->
                RideitSettingsItemCard(
                    item = item,
                    compact = compact,
                    onClick = {
                        onItemClick(item)
                    }
                )
            }
        }
    }
}

@Composable
fun RideitSettingsDashboardContent(
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    compact: Boolean = true,
    notificationsEnabled: Boolean = true,
    locationEnabled: Boolean = true,
    darkModeEnabled: Boolean = false,
    onHeroClick: () -> Unit = {},
    onSwitchChange: (id: String, checked: Boolean) -> Unit = { _, _ -> },
    onItemClick: (RideitSettingsItemUiModel) -> Unit = {}
) {
    val accountItems = if (isDriverMode) {
        rideitDriverSettingsAccountItems()
    } else {
        rideitRiderSettingsAccountItems()
    }

    val preferenceItems = if (isDriverMode) {
        rideitDriverSettingsPreferenceItems()
    } else {
        rideitRiderSettingsPreferenceItems()
    }

    val securityItems = rideitSettingsSecurityItems()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 16.dp)
    ) {
        RideitSettingsHeroCard(
            isDriverMode = isDriverMode,
            compact = compact,
            onClick = onHeroClick
        )

        RideitSettingsSection(
            title = "Account",
            subtitle = if (isDriverMode) "Driver profile and vehicle" else "Profile and payment",
            items = accountItems,
            compact = compact,
            onItemClick = onItemClick
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
        ) {
            Text(
                text = "Preferences",
                color = Color(0xFF0F172A),
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            RideitSettingsSwitchCard(
                item = RideitSettingsItemUiModel(
                    id = "notifications",
                    title = "Notifications",
                    subtitle = "Ride updates, driver status, and account alerts",
                    type = RideitSettingsItemType.NOTIFICATIONS,
                    style = RideitSettingsItemStyle.PRIMARY
                ),
                checked = notificationsEnabled,
                compact = compact,
                onCheckedChange = {
                    onSwitchChange("notifications", it)
                }
            )

            RideitSettingsSwitchCard(
                item = RideitSettingsItemUiModel(
                    id = "location",
                    title = "Location services",
                    subtitle = "Pickup accuracy, map movement, and nearby drivers",
                    type = RideitSettingsItemType.LOCATION,
                    style = RideitSettingsItemStyle.SUCCESS
                ),
                checked = locationEnabled,
                compact = compact,
                onCheckedChange = {
                    onSwitchChange("location", it)
                }
            )

            RideitSettingsSwitchCard(
                item = RideitSettingsItemUiModel(
                    id = "dark_mode",
                    title = "Dark mode",
                    subtitle = "Premium Rideit dark interface",
                    type = RideitSettingsItemType.THEME,
                    style = RideitSettingsItemStyle.PREMIUM
                ),
                checked = darkModeEnabled,
                compact = compact,
                onCheckedChange = {
                    onSwitchChange("dark_mode", it)
                }
            )

            preferenceItems.forEach { item ->
                RideitSettingsItemCard(
                    item = item,
                    compact = compact,
                    onClick = {
                        onItemClick(item)
                    }
                )
            }
        }

        RideitSettingsSection(
            title = "Security",
            subtitle = "Privacy, password, safety, and session",
            items = securityItems,
            compact = compact,
            onItemClick = onItemClick
        )
    }
}

@Composable
fun RideitSettingsDangerZone(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
    ) {
        Text(
            text = "Danger zone",
            color = Color(0xFF991B1B),
            fontSize = if (compact) 15.sp else 17.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        RideitSettingsItemCard(
            item = RideitSettingsItemUiModel(
                id = "logout",
                title = "Logout",
                subtitle = "Sign out from this Rideit account",
                type = RideitSettingsItemType.LOGOUT,
                style = RideitSettingsItemStyle.DANGER
            ),
            compact = compact,
            onClick = onLogoutClick
        )

        RideitSettingsItemCard(
            item = RideitSettingsItemUiModel(
                id = "delete_account",
                title = "Delete account",
                subtitle = "Permanently remove this Rideit account",
                type = RideitSettingsItemType.DELETE_ACCOUNT,
                style = RideitSettingsItemStyle.DANGER,
                highlighted = true
            ),
            compact = compact,
            onClick = onDeleteAccountClick
        )
    }
}

@Composable
fun RideitAnimatedSettingsNoticeCard(
    visible: Boolean,
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    style: RideitSettingsItemStyle = RideitSettingsItemStyle.PRIMARY,
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
                RideitSettingsIcon(
                    type = RideitSettingsItemType.CUSTOM,
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
private fun RideitSettingsIcon(
    type: RideitSettingsItemType,
    style: RideitSettingsItemStyle,
    compact: Boolean,
    forceLight: Boolean = false,
    pulse: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_settings_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_settings_icon_pulse_value"
    )

    val backgroundColor = if (forceLight) {
        Color.White.copy(alpha = 0.20f)
    } else {
        style.softBackgroundColor()
    }

    val dotColor = if (forceLight) {
        Color.White
    } else {
        style.dotColor()
    }

    Box(
        modifier = Modifier
            .size(if (compact) 38.dp else 44.dp)
            .background(
                color = backgroundColor,
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
                    color = dotColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.iconText(),
                color = if (forceLight) Color(0xFF2563EB) else Color.White,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun RideitSettingsBadge(
    text: String,
    style: RideitSettingsItemStyle,
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

fun rideitRiderSettingsAccountItems(): List<RideitSettingsItemUiModel> {
    return listOf(
        RideitSettingsItemUiModel(
            id = "profile",
            title = "Profile",
            subtitle = "Name, phone, email, and profile details",
            type = RideitSettingsItemType.ACCOUNT,
            style = RideitSettingsItemStyle.PRIMARY
        ),
        RideitSettingsItemUiModel(
            id = "payment",
            title = "Payment methods",
            subtitle = "Cash, cards, receipts, and fare preferences",
            type = RideitSettingsItemType.PAYMENT,
            style = RideitSettingsItemStyle.SUCCESS
        ),
        RideitSettingsItemUiModel(
            id = "saved_places",
            title = "Saved places",
            subtitle = "Home, work, and favorite destinations",
            type = RideitSettingsItemType.LOCATION,
            style = RideitSettingsItemStyle.PREMIUM
        )
    )
}

fun rideitDriverSettingsAccountItems(): List<RideitSettingsItemUiModel> {
    return listOf(
        RideitSettingsItemUiModel(
            id = "driver_profile",
            title = "Driver profile",
            subtitle = "Name, phone, email, and driver details",
            type = RideitSettingsItemType.ACCOUNT,
            style = RideitSettingsItemStyle.PRIMARY
        ),
        RideitSettingsItemUiModel(
            id = "vehicle",
            title = "Vehicle details",
            subtitle = "Car model, plate number, and availability",
            type = RideitSettingsItemType.VEHICLE,
            style = RideitSettingsItemStyle.PREMIUM
        ),
        RideitSettingsItemUiModel(
            id = "driver_payments",
            title = "Earnings and payouts",
            subtitle = "Trip earnings, receipts, and payout settings",
            type = RideitSettingsItemType.PAYMENT,
            style = RideitSettingsItemStyle.SUCCESS
        )
    )
}

fun rideitRiderSettingsPreferenceItems(): List<RideitSettingsItemUiModel> {
    return listOf(
        RideitSettingsItemUiModel(
            id = "language",
            title = "Language",
            subtitle = "Choose your preferred app language",
            type = RideitSettingsItemType.LANGUAGE,
            style = RideitSettingsItemStyle.NEUTRAL,
            badgeText = "EN"
        ),
        RideitSettingsItemUiModel(
            id = "support",
            title = "Help and support",
            subtitle = "Ride support, payment help, and safety center",
            type = RideitSettingsItemType.SUPPORT,
            style = RideitSettingsItemStyle.PRIMARY
        )
    )
}

fun rideitDriverSettingsPreferenceItems(): List<RideitSettingsItemUiModel> {
    return listOf(
        RideitSettingsItemUiModel(
            id = "driver_mode",
            title = "Driver mode",
            subtitle = "Online status, request preferences, and availability",
            type = RideitSettingsItemType.DRIVER_MODE,
            style = RideitSettingsItemStyle.SUCCESS,
            badgeText = "Active"
        ),
        RideitSettingsItemUiModel(
            id = "language",
            title = "Language",
            subtitle = "Choose your preferred app language",
            type = RideitSettingsItemType.LANGUAGE,
            style = RideitSettingsItemStyle.NEUTRAL,
            badgeText = "EN"
        ),
        RideitSettingsItemUiModel(
            id = "driver_support",
            title = "Driver support",
            subtitle = "Ride help, payout help, and safety center",
            type = RideitSettingsItemType.SUPPORT,
            style = RideitSettingsItemStyle.PRIMARY
        )
    )
}

fun rideitSettingsSecurityItems(): List<RideitSettingsItemUiModel> {
    return listOf(
        RideitSettingsItemUiModel(
            id = "privacy",
            title = "Privacy",
            subtitle = "Location, trip, and account privacy controls",
            type = RideitSettingsItemType.PRIVACY,
            style = RideitSettingsItemStyle.PREMIUM
        ),
        RideitSettingsItemUiModel(
            id = "security",
            title = "Security",
            subtitle = "Password, login safety, and account protection",
            type = RideitSettingsItemType.SECURITY,
            style = RideitSettingsItemStyle.WARNING
        )
    )
}

private fun RideitSettingsItemType.iconText(): String {
    return when (this) {
        RideitSettingsItemType.ACCOUNT -> "A"
        RideitSettingsItemType.NOTIFICATIONS -> "N"
        RideitSettingsItemType.LOCATION -> "L"
        RideitSettingsItemType.PRIVACY -> "P"
        RideitSettingsItemType.SECURITY -> "S"
        RideitSettingsItemType.PAYMENT -> "Rs"
        RideitSettingsItemType.LANGUAGE -> "EN"
        RideitSettingsItemType.THEME -> "T"
        RideitSettingsItemType.DRIVER_MODE -> "D"
        RideitSettingsItemType.VEHICLE -> "V"
        RideitSettingsItemType.SUPPORT -> "?"
        RideitSettingsItemType.LOGOUT -> "!"
        RideitSettingsItemType.DELETE_ACCOUNT -> "×"
        RideitSettingsItemType.CUSTOM -> "•"
    }
}

private fun RideitSettingsItemStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitSettingsItemStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitSettingsItemStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitSettingsItemStyle.WARNING -> Color(0xFFFEF3C7)
        RideitSettingsItemStyle.DANGER -> Color(0xFFFEE2E2)
        RideitSettingsItemStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitSettingsItemStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitSettingsItemStyle.borderColor(): Color {
    return when (this) {
        RideitSettingsItemStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitSettingsItemStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitSettingsItemStyle.WARNING -> Color(0xFFFDE68A)
        RideitSettingsItemStyle.DANGER -> Color(0xFFFCA5A5)
        RideitSettingsItemStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitSettingsItemStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitSettingsItemStyle.dotColor(): Color {
    return when (this) {
        RideitSettingsItemStyle.PRIMARY -> Color(0xFF2563EB)
        RideitSettingsItemStyle.SUCCESS -> Color(0xFF22C55E)
        RideitSettingsItemStyle.WARNING -> Color(0xFFF59E0B)
        RideitSettingsItemStyle.DANGER -> Color(0xFFEF4444)
        RideitSettingsItemStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitSettingsItemStyle.NEUTRAL -> Color(0xFF64748B)
    }
}

private fun RideitSettingsItemStyle.textColor(): Color {
    return when (this) {
        RideitSettingsItemStyle.PRIMARY -> Color(0xFF2563EB)
        RideitSettingsItemStyle.SUCCESS -> Color(0xFF166534)
        RideitSettingsItemStyle.WARNING -> Color(0xFF92400E)
        RideitSettingsItemStyle.DANGER -> Color(0xFFB91C1C)
        RideitSettingsItemStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitSettingsItemStyle.NEUTRAL -> Color(0xFF475569)
    }
}