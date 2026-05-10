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

enum class RideitProfileStatStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

@Immutable
data class RideitProfileStatUiModel(
    val label: String,
    val value: String,
    val helperText: String? = null,
    val style: RideitProfileStatStyle = RideitProfileStatStyle.NEUTRAL
)

@Composable
fun RideitProfileSummaryCard(
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    name: String? = null,
    email: String? = null,
    phone: String? = null,
    ratingText: String? = null,
    statusText: String? = null,
    verified: Boolean = false,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 16.dp else 22.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = Color.Black.copy(alpha = 0.18f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.76f),
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
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(if (compact) 16.dp else 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitProfileAvatar(
                    name = name,
                    isDriverMode = isDriverMode,
                    verified = verified,
                    compact = compact
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = name?.takeIf { it.isNotBlank() }
                            ?: if (isDriverMode) "Rideit Driver" else "Rideit Rider",
                        color = Color(0xFF0F172A),
                        fontSize = if (compact) 17.sp else 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = email?.takeIf { it.isNotBlank() }
                            ?: phone?.takeIf { it.isNotBlank() }
                            ?: if (isDriverMode) "Driver account" else "Rider account",
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 12.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!phone.isNullOrBlank() && !email.isNullOrBlank()) {
                        Text(
                            text = phone,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                RideitProfileStatusChip(
                    text = statusText ?: if (verified) "Verified" else "Active",
                    verified = verified,
                    compact = compact
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 14.dp else 18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RideitMiniProfileMetric(
                    label = "Mode",
                    value = if (isDriverMode) "Driver" else "Rider",
                    style = RideitProfileStatStyle.PRIMARY,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )

                RideitMiniProfileMetric(
                    label = "Rating",
                    value = ratingText?.takeIf { it.isNotBlank() } ?: "5.0",
                    style = RideitProfileStatStyle.SUCCESS,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )

                RideitMiniProfileMetric(
                    label = "Status",
                    value = if (verified) "Safe" else "Ready",
                    style = if (verified) RideitProfileStatStyle.SUCCESS else RideitProfileStatStyle.PREMIUM,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )
            }
        }
    }
}

@Composable
fun RideitProfileStatsGrid(
    stats: List<RideitProfileStatUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { stat ->
                    RideitProfileStatCard(
                        stat = stat,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun RideitProfileStatCard(
    stat: RideitProfileStatUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = if (compact) 10.dp else 14.dp,
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.60f),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(if (compact) 13.dp else 15.dp)
        ) {
            RideitProfileStatIcon(
                style = stat.style,
                compact = compact
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

            Text(
                text = stat.value,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 18.sp else 22.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = stat.label,
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!stat.helperText.isNullOrBlank()) {
                Text(
                    text = stat.helperText,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RideitRiderStatsSection(
    modifier: Modifier = Modifier,
    totalTrips: String = "0",
    totalSpent: String = "Rs 0",
    savedPlaces: String = "0",
    ratingText: String = "5.0",
    compact: Boolean = false
) {
    RideitProfileStatsGrid(
        modifier = modifier,
        compact = compact,
        stats = listOf(
            RideitProfileStatUiModel(
                label = "Total trips",
                value = totalTrips,
                helperText = "Completed rides",
                style = RideitProfileStatStyle.PRIMARY
            ),
            RideitProfileStatUiModel(
                label = "Total spent",
                value = totalSpent,
                helperText = "Rideit payments",
                style = RideitProfileStatStyle.SUCCESS
            ),
            RideitProfileStatUiModel(
                label = "Saved places",
                value = savedPlaces,
                helperText = "Quick destinations",
                style = RideitProfileStatStyle.PREMIUM
            ),
            RideitProfileStatUiModel(
                label = "Rating",
                value = ratingText,
                helperText = "Rider profile",
                style = RideitProfileStatStyle.WARNING
            )
        )
    )
}

@Composable
fun RideitDriverStatsSection(
    modifier: Modifier = Modifier,
    totalTrips: String = "0",
    totalEarnings: String = "Rs 0",
    onlineHours: String = "0h",
    ratingText: String = "5.0",
    compact: Boolean = false
) {
    RideitProfileStatsGrid(
        modifier = modifier,
        compact = compact,
        stats = listOf(
            RideitProfileStatUiModel(
                label = "Total trips",
                value = totalTrips,
                helperText = "Completed drives",
                style = RideitProfileStatStyle.PRIMARY
            ),
            RideitProfileStatUiModel(
                label = "Earnings",
                value = totalEarnings,
                helperText = "Driver income",
                style = RideitProfileStatStyle.SUCCESS
            ),
            RideitProfileStatUiModel(
                label = "Online time",
                value = onlineHours,
                helperText = "This period",
                style = RideitProfileStatStyle.PREMIUM
            ),
            RideitProfileStatUiModel(
                label = "Rating",
                value = ratingText,
                helperText = "Driver score",
                style = RideitProfileStatStyle.WARNING
            )
        )
    )
}

@Composable
fun RideitProfileSafetyCard(
    modifier: Modifier = Modifier,
    title: String = "Rideit Safety",
    message: String = "Your safety tools, trusted contacts, and trip support stay ready during every ride.",
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 12.dp else 16.dp,
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
                spotColor = Color.Black.copy(alpha = 0.14f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.65f),
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(if (compact) 15.dp else 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitProfileStatIcon(
                style = RideitProfileStatStyle.PRIMARY,
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
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 15.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = message,
                    color = Color(0xFF64748B),
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
fun RideitProfileDashboardSection(
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    name: String? = null,
    email: String? = null,
    phone: String? = null,
    ratingText: String? = null,
    verified: Boolean = false,
    totalTrips: String = "0",
    totalMoneyText: String = "Rs 0",
    thirdStatText: String = "0",
    compact: Boolean = false,
    onProfileClick: () -> Unit = {},
    onSafetyClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)
    ) {
        RideitProfileSummaryCard(
            isDriverMode = isDriverMode,
            name = name,
            email = email,
            phone = phone,
            ratingText = ratingText,
            verified = verified,
            compact = compact,
            onClick = onProfileClick
        )

        if (isDriverMode) {
            RideitDriverStatsSection(
                totalTrips = totalTrips,
                totalEarnings = totalMoneyText,
                onlineHours = thirdStatText,
                ratingText = ratingText ?: "5.0",
                compact = compact
            )
        } else {
            RideitRiderStatsSection(
                totalTrips = totalTrips,
                totalSpent = totalMoneyText,
                savedPlaces = thirdStatText,
                ratingText = ratingText ?: "5.0",
                compact = compact
            )
        }

        RideitProfileSafetyCard(
            compact = compact,
            onClick = onSafetyClick
        )
    }
}

@Composable
fun RideitAnimatedProfileStatBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    message: String? = null,
    style: RideitProfileStatStyle = RideitProfileStatStyle.PRIMARY,
    compact: Boolean = false
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(200))
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = style.softBackgroundColor(),
                    shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
                )
                .border(
                    width = 1.dp,
                    color = style.borderColor(),
                    shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
                )
                .padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitProfileStatIcon(
                style = style,
                compact = true,
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
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!message.isNullOrBlank()) {
                    Text(
                        text = message,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = value,
                color = style.textColor(),
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RideitProfileAvatar(
    name: String?,
    isDriverMode: Boolean,
    verified: Boolean,
    compact: Boolean
) {
    val initials = name.toRideitInitials()

    Box(
        modifier = Modifier.size(if (compact) 58.dp else 68.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 58.dp else 68.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isDriverMode) {
                            listOf(Color(0xFF0F172A), Color(0xFF2563EB))
                        } else {
                            listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                        }
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = if (compact) 18.sp else 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (verified) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(if (compact) 19.dp else 22.dp)
                    .background(Color(0xFF22C55E), CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = if (compact) 9.sp else 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun RideitProfileStatusChip(
    text: String,
    verified: Boolean,
    compact: Boolean
) {
    val background = if (verified) Color(0xFFDCFCE7) else Color(0xFFEFF6FF)
    val border = if (verified) Color(0xFFBBF7D0) else Color(0xFFBFDBFE)
    val color = if (verified) Color(0xFF166534) else Color(0xFF2563EB)

    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .padding(
                horizontal = if (compact) 9.dp else 11.dp,
                vertical = if (compact) 6.dp else 7.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitMiniProfileMetric(
    label: String,
    value: String,
    style: RideitProfileStatStyle,
    modifier: Modifier = Modifier,
    compact: Boolean
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
            .padding(horizontal = 8.dp, vertical = if (compact) 9.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = value,
            color = style.textColor(),
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitProfileStatIcon(
    style: RideitProfileStatStyle,
    compact: Boolean,
    pulse: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_profile_stat_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_profile_stat_icon_pulse_value"
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
                text = style.iconText(),
                color = Color.White,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun String?.toRideitInitials(): String {
    val clean = this?.trim().orEmpty()

    if (clean.isBlank()) return "R"

    val parts = clean.split(" ").filter { it.isNotBlank() }

    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        clean.length >= 2 -> clean.take(2).uppercase()
        else -> clean.take(1).uppercase()
    }
}

private fun RideitProfileStatStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitProfileStatStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitProfileStatStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitProfileStatStyle.WARNING -> Color(0xFFFEF3C7)
        RideitProfileStatStyle.DANGER -> Color(0xFFFEE2E2)
        RideitProfileStatStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitProfileStatStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitProfileStatStyle.borderColor(): Color {
    return when (this) {
        RideitProfileStatStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitProfileStatStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitProfileStatStyle.WARNING -> Color(0xFFFDE68A)
        RideitProfileStatStyle.DANGER -> Color(0xFFFCA5A5)
        RideitProfileStatStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitProfileStatStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitProfileStatStyle.dotColor(): Color {
    return when (this) {
        RideitProfileStatStyle.PRIMARY -> Color(0xFF2563EB)
        RideitProfileStatStyle.SUCCESS -> Color(0xFF22C55E)
        RideitProfileStatStyle.WARNING -> Color(0xFFF59E0B)
        RideitProfileStatStyle.DANGER -> Color(0xFFEF4444)
        RideitProfileStatStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitProfileStatStyle.NEUTRAL -> Color(0xFF94A3B8)
    }
}

private fun RideitProfileStatStyle.textColor(): Color {
    return when (this) {
        RideitProfileStatStyle.PRIMARY -> Color(0xFF2563EB)
        RideitProfileStatStyle.SUCCESS -> Color(0xFF166534)
        RideitProfileStatStyle.WARNING -> Color(0xFF92400E)
        RideitProfileStatStyle.DANGER -> Color(0xFFB91C1C)
        RideitProfileStatStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitProfileStatStyle.NEUTRAL -> Color(0xFF475569)
    }
}

private fun RideitProfileStatStyle.iconText(): String {
    return when (this) {
        RideitProfileStatStyle.PRIMARY -> "R"
        RideitProfileStatStyle.SUCCESS -> "✓"
        RideitProfileStatStyle.WARNING -> "★"
        RideitProfileStatStyle.DANGER -> "!"
        RideitProfileStatStyle.PREMIUM -> "◆"
        RideitProfileStatStyle.NEUTRAL -> "•"
    }
}