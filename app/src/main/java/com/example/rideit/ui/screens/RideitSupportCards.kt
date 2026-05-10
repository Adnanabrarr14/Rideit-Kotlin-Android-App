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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitSupportCardStyle {
    HELP,
    SAFETY,
    PAYMENT,
    TRIP,
    ACCOUNT,
    PREMIUM,
    WARNING,
    EMERGENCY
}

@Immutable
data class RideitSupportItemUiModel(
    val title: String,
    val message: String,
    val actionText: String = "Open",
    val style: RideitSupportCardStyle = RideitSupportCardStyle.HELP
)

@Composable
fun RideitSupportHeroCard(
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
                elevation = if (compact) 18.dp else 24.dp,
                shape = RoundedCornerShape(if (compact) 30.dp else 36.dp),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color.White.copy(alpha = 0.22f)
                    )
                ),
                shape = RoundedCornerShape(if (compact) 30.dp else 36.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 30.dp else 36.dp),
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
            RideitSupportIcon(
                style = RideitSupportCardStyle.PREMIUM,
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
                    text = title ?: if (isDriverMode) "Driver Support" else "Rideit Help Center",
                    color = Color.White,
                    fontSize = if (compact) 19.sp else 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = message ?: if (isDriverMode) {
                        "Get help with trips, earnings, safety, and driver account support."
                    } else {
                        "Get help with rides, payments, safety, receipts, and account support."
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
fun RideitSupportItemCard(
    item: RideitSupportItemUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 11.dp else 15.dp,
                shape = RoundedCornerShape(if (compact) 24.dp else 28.dp),
                spotColor = Color.Black.copy(alpha = 0.13f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.62f),
                shape = RoundedCornerShape(if (compact) 24.dp else 28.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 24.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(if (compact) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSupportIcon(
                style = item.style,
                compact = compact,
                pulse = item.style == RideitSupportCardStyle.EMERGENCY ||
                        item.style == RideitSupportCardStyle.WARNING
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 14.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.message,
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = if (compact) 16.sp else 18.sp,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = item.style.softBackgroundColor(),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        color = item.style.borderColor(),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.actionText,
                    color = item.style.textColor(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RideitSupportGrid(
    items: List<RideitSupportItemUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onItemClick: (RideitSupportItemUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
    ) {
        items.forEach { item ->
            RideitSupportItemCard(
                item = item,
                compact = compact,
                onClick = {
                    onItemClick(item)
                }
            )
        }
    }
}

@Composable
fun RideitSupportDashboardSection(
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    compact: Boolean = false,
    onHeroClick: () -> Unit = {},
    onItemClick: (RideitSupportItemUiModel) -> Unit = {}
) {
    val items = if (isDriverMode) {
        rideitDriverSupportItems()
    } else {
        rideitRiderSupportItems()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)
    ) {
        RideitSupportHeroCard(
            isDriverMode = isDriverMode,
            compact = compact,
            onClick = onHeroClick
        )

        RideitSupportGrid(
            items = items,
            compact = compact,
            onItemClick = onItemClick
        )
    }
}

@Composable
fun RideitEmergencySupportCard(
    modifier: Modifier = Modifier,
    title: String = "Emergency support",
    message: String = "Use this only when you need urgent help during a Rideit trip.",
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 16.dp else 22.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 32.dp),
                spotColor = Color(0xFFEF4444).copy(alpha = 0.20f)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFFCA5A5),
                shape = RoundedCornerShape(if (compact) 28.dp else 32.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 28.dp else 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFEF2F2),
                            Color(0xFFFFFBFB)
                        )
                    )
                )
                .padding(if (compact) 16.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSupportIcon(
                style = RideitSupportCardStyle.EMERGENCY,
                compact = compact,
                pulse = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Text(
                    text = title,
                    color = Color(0xFF991B1B),
                    fontSize = if (compact) 16.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = message,
                    color = Color(0xFF7F1D1D).copy(alpha = 0.75f),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = if (compact) 16.sp else 18.sp,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RideitSupportFaqCard(
    modifier: Modifier = Modifier,
    question: String,
    answer: String,
    expanded: Boolean,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFE2E8F0),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 14.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (expanded) "−" else "+",
                    color = Color(0xFF2563EB),
                    fontSize = if (compact) 18.sp else 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(180))
            ) {
                Column {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        color = Color(0xFFE2E8F0)
                    )

                    Text(
                        text = answer,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 12.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = if (compact) 17.sp else 19.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RideitSupportQuickActionRow(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onTripHelpClick: () -> Unit = {},
    onPaymentHelpClick: () -> Unit = {},
    onSafetyClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RideitSupportQuickActionTile(
            title = "Trip",
            style = RideitSupportCardStyle.TRIP,
            modifier = Modifier.weight(1f),
            compact = compact,
            onClick = onTripHelpClick
        )

        RideitSupportQuickActionTile(
            title = "Payment",
            style = RideitSupportCardStyle.PAYMENT,
            modifier = Modifier.weight(1f),
            compact = compact,
            onClick = onPaymentHelpClick
        )

        RideitSupportQuickActionTile(
            title = "Safety",
            style = RideitSupportCardStyle.SAFETY,
            modifier = Modifier.weight(1f),
            compact = compact,
            onClick = onSafetyClick
        )
    }
}

@Composable
private fun RideitSupportQuickActionTile(
    title: String,
    style: RideitSupportCardStyle,
    modifier: Modifier = Modifier,
    compact: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = style.softBackgroundColor(),
                shape = RoundedCornerShape(if (compact) 18.dp else 20.dp)
            )
            .border(
                width = 1.dp,
                color = style.borderColor(),
                shape = RoundedCornerShape(if (compact) 18.dp else 20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = if (compact) 11.dp else 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RideitSupportIcon(
            style = style,
            compact = true
        )

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = title,
            color = style.textColor(),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitSupportIcon(
    style: RideitSupportCardStyle,
    compact: Boolean,
    forceLight: Boolean = false,
    pulse: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_support_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_support_icon_pulse_value"
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
            .size(if (compact) 42.dp else 50.dp)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 18.dp else 22.dp)
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
                text = style.iconText(),
                color = if (forceLight) Color(0xFF2563EB) else Color.White,
                fontSize = if (compact) 8.sp else 10.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun rideitRiderSupportItems(): List<RideitSupportItemUiModel> {
    return listOf(
        RideitSupportItemUiModel(
            title = "Trip help",
            message = "Get help with pickup, destination, driver arrival, or active ride issues.",
            actionText = "Trips",
            style = RideitSupportCardStyle.TRIP
        ),
        RideitSupportItemUiModel(
            title = "Payment and receipts",
            message = "Review fare, payment method, receipt, refund, or billing questions.",
            actionText = "Payments",
            style = RideitSupportCardStyle.PAYMENT
        ),
        RideitSupportItemUiModel(
            title = "Safety support",
            message = "Access trip safety, trusted contacts, and Rideit safety guidance.",
            actionText = "Safety",
            style = RideitSupportCardStyle.SAFETY
        ),
        RideitSupportItemUiModel(
            title = "Account help",
            message = "Manage login, profile, phone number, password, and account access.",
            actionText = "Account",
            style = RideitSupportCardStyle.ACCOUNT
        )
    )
}

fun rideitDriverSupportItems(): List<RideitSupportItemUiModel> {
    return listOf(
        RideitSupportItemUiModel(
            title = "Active ride help",
            message = "Get help with accepting, pickup, trip progress, or completing rides.",
            actionText = "Trips",
            style = RideitSupportCardStyle.TRIP
        ),
        RideitSupportItemUiModel(
            title = "Earnings support",
            message = "Review fare, earnings, payout, and completed ride questions.",
            actionText = "Earnings",
            style = RideitSupportCardStyle.PAYMENT
        ),
        RideitSupportItemUiModel(
            title = "Driver safety",
            message = "Access driver safety guidance and support during active rides.",
            actionText = "Safety",
            style = RideitSupportCardStyle.SAFETY
        ),
        RideitSupportItemUiModel(
            title = "Driver account",
            message = "Manage driver profile, vehicle details, login, and account access.",
            actionText = "Account",
            style = RideitSupportCardStyle.ACCOUNT
        )
    )
}

private fun RideitSupportCardStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitSupportCardStyle.HELP -> Color(0xFFEFF6FF)
        RideitSupportCardStyle.SAFETY -> Color(0xFFDCFCE7)
        RideitSupportCardStyle.PAYMENT -> Color(0xFFF0FDF4)
        RideitSupportCardStyle.TRIP -> Color(0xFFEFF6FF)
        RideitSupportCardStyle.ACCOUNT -> Color(0xFFF8FAFC)
        RideitSupportCardStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitSupportCardStyle.WARNING -> Color(0xFFFEF3C7)
        RideitSupportCardStyle.EMERGENCY -> Color(0xFFFEE2E2)
    }
}

private fun RideitSupportCardStyle.borderColor(): Color {
    return when (this) {
        RideitSupportCardStyle.HELP -> Color(0xFFBFDBFE)
        RideitSupportCardStyle.SAFETY -> Color(0xFFBBF7D0)
        RideitSupportCardStyle.PAYMENT -> Color(0xFFBBF7D0)
        RideitSupportCardStyle.TRIP -> Color(0xFFBFDBFE)
        RideitSupportCardStyle.ACCOUNT -> Color(0xFFE2E8F0)
        RideitSupportCardStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitSupportCardStyle.WARNING -> Color(0xFFFDE68A)
        RideitSupportCardStyle.EMERGENCY -> Color(0xFFFCA5A5)
    }
}

private fun RideitSupportCardStyle.dotColor(): Color {
    return when (this) {
        RideitSupportCardStyle.HELP -> Color(0xFF2563EB)
        RideitSupportCardStyle.SAFETY -> Color(0xFF22C55E)
        RideitSupportCardStyle.PAYMENT -> Color(0xFF16A34A)
        RideitSupportCardStyle.TRIP -> Color(0xFF2563EB)
        RideitSupportCardStyle.ACCOUNT -> Color(0xFF64748B)
        RideitSupportCardStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitSupportCardStyle.WARNING -> Color(0xFFF59E0B)
        RideitSupportCardStyle.EMERGENCY -> Color(0xFFEF4444)
    }
}

private fun RideitSupportCardStyle.textColor(): Color {
    return when (this) {
        RideitSupportCardStyle.HELP -> Color(0xFF2563EB)
        RideitSupportCardStyle.SAFETY -> Color(0xFF166534)
        RideitSupportCardStyle.PAYMENT -> Color(0xFF166534)
        RideitSupportCardStyle.TRIP -> Color(0xFF2563EB)
        RideitSupportCardStyle.ACCOUNT -> Color(0xFF475569)
        RideitSupportCardStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitSupportCardStyle.WARNING -> Color(0xFF92400E)
        RideitSupportCardStyle.EMERGENCY -> Color(0xFFB91C1C)
    }
}

private fun RideitSupportCardStyle.iconText(): String {
    return when (this) {
        RideitSupportCardStyle.HELP -> "?"
        RideitSupportCardStyle.SAFETY -> "✓"
        RideitSupportCardStyle.PAYMENT -> "Rs"
        RideitSupportCardStyle.TRIP -> "R"
        RideitSupportCardStyle.ACCOUNT -> "A"
        RideitSupportCardStyle.PREMIUM -> "◆"
        RideitSupportCardStyle.WARNING -> "!"
        RideitSupportCardStyle.EMERGENCY -> "!"
    }
}