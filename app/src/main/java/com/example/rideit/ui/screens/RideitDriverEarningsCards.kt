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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

enum class RideitEarningsPeriodType {
    TODAY,
    WEEK,
    MONTH,
    ALL_TIME,
    CUSTOM
}

enum class RideitPayoutStatusType {
    AVAILABLE,
    PENDING,
    PAID,
    FAILED,
    NOT_READY
}

enum class RideitEarningsStatStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

@Immutable
data class RideitDriverEarningsSummaryUiModel(
    val totalEarningsText: String = "Rs 0",
    val availableBalanceText: String = "Rs 0",
    val pendingPayoutText: String = "Rs 0",
    val completedTripsText: String = "0",
    val onlineHoursText: String = "0h",
    val acceptanceRateText: String = "100%",
    val selectedPeriod: RideitEarningsPeriodType = RideitEarningsPeriodType.TODAY
)

@Immutable
data class RideitDriverEarningsStatUiModel(
    val label: String,
    val value: String,
    val helperText: String? = null,
    val style: RideitEarningsStatStyle = RideitEarningsStatStyle.NEUTRAL
)

@Immutable
data class RideitDriverPayoutUiModel(
    val id: String = "",
    val amountText: String,
    val dateText: String? = null,
    val methodText: String? = null,
    val status: RideitPayoutStatusType = RideitPayoutStatusType.AVAILABLE,
    val referenceText: String? = null
)

@Composable
fun RideitDriverEarningsHeroCard(
    summary: RideitDriverEarningsSummaryUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
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
                        Color.White.copy(alpha = 0.80f),
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
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF2563EB)
                        )
                    )
                )
                .padding(if (compact) 18.dp else 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitEarningsIcon(
                    style = RideitEarningsStatStyle.SUCCESS,
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
                        text = "Driver earnings",
                        color = Color.White,
                        fontSize = if (compact) 17.sp else 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = summary.selectedPeriod.label(),
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                RideitEarningsHeroBadge(
                    text = summary.completedTripsText + " trips",
                    compact = compact
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 16.dp else 20.dp))

            Text(
                text = summary.totalEarningsText,
                color = Color.White,
                fontSize = if (compact) 34.sp else 40.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Total earnings",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(if (compact) 16.dp else 18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RideitEarningsHeroMiniTile(
                    label = "Available",
                    value = summary.availableBalanceText,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )

                RideitEarningsHeroMiniTile(
                    label = "Pending",
                    value = summary.pendingPayoutText,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )

                RideitEarningsHeroMiniTile(
                    label = "Hours",
                    value = summary.onlineHoursText,
                    modifier = Modifier.weight(1f),
                    compact = compact
                )
            }
        }
    }
}

@Composable
fun RideitDriverEarningsStatsGrid(
    summary: RideitDriverEarningsSummaryUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    val stats = listOf(
        RideitDriverEarningsStatUiModel(
            label = "Completed trips",
            value = summary.completedTripsText,
            helperText = "Finished rides",
            style = RideitEarningsStatStyle.PRIMARY
        ),
        RideitDriverEarningsStatUiModel(
            label = "Online hours",
            value = summary.onlineHoursText,
            helperText = "Driver time",
            style = RideitEarningsStatStyle.PREMIUM
        ),
        RideitDriverEarningsStatUiModel(
            label = "Acceptance",
            value = summary.acceptanceRateText,
            helperText = "Ride requests",
            style = RideitEarningsStatStyle.SUCCESS
        ),
        RideitDriverEarningsStatUiModel(
            label = "Pending",
            value = summary.pendingPayoutText,
            helperText = "Payout balance",
            style = RideitEarningsStatStyle.WARNING
        )
    )

    RideitDriverEarningsCustomStatsGrid(
        stats = stats,
        modifier = modifier,
        compact = compact
    )
}

@Composable
fun RideitDriverEarningsCustomStatsGrid(
    stats: List<RideitDriverEarningsStatUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true
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
                    RideitDriverEarningsStatCard(
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
fun RideitDriverEarningsStatCard(
    stat: RideitDriverEarningsStatUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
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
                color = Color.White.copy(alpha = 0.62f),
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
                        colors = listOf(Color.White, Color(0xFFF8FAFC))
                    )
                )
                .padding(if (compact) 13.dp else 15.dp)
        ) {
            RideitEarningsIcon(
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
fun RideitEarningsPeriodChips(
    selectedPeriod: RideitEarningsPeriodType,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onPeriodClick: (RideitEarningsPeriodType) -> Unit = {}
) {
    val periods = listOf(
        RideitEarningsPeriodType.TODAY,
        RideitEarningsPeriodType.WEEK,
        RideitEarningsPeriodType.MONTH,
        RideitEarningsPeriodType.ALL_TIME
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        periods.forEach { period ->
            RideitEarningsPeriodChip(
                period = period,
                selected = selectedPeriod == period,
                compact = compact,
                onClick = {
                    onPeriodClick(period)
                }
            )
        }
    }
}

@Composable
fun RideitEarningsPeriodChip(
    period: RideitEarningsPeriodType,
    selected: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(
                color = if (selected) Color(0xFFEFF6FF) else Color.White,
                shape = RoundedCornerShape(50)
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color(0xFFBFDBFE) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 8.dp else 9.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 9.dp else 10.dp)
                .background(
                    color = if (selected) Color(0xFF2563EB) else Color(0xFF94A3B8),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = period.shortLabel(),
            color = if (selected) Color(0xFF2563EB) else Color(0xFF64748B),
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitDriverPayoutCard(
    payout: RideitDriverPayoutUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 10.dp else 14.dp,
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
                spotColor = payout.status.dotColor().copy(alpha = 0.14f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.62f),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFF8FAFC))
                    )
                )
                .padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitPayoutStatusIcon(
                status = payout.status,
                compact = compact
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = payout.amountText,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 15.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = listOfNotNull(
                        payout.methodText?.takeIf { it.isNotBlank() },
                        payout.dateText?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank {
                        "Driver payout"
                    },
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!payout.referenceText.isNullOrBlank()) {
                    Text(
                        text = payout.referenceText,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 3.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            RideitPayoutStatusBadge(
                status = payout.status,
                compact = compact
            )
        }
    }
}

@Composable
fun RideitDriverPayoutSection(
    payouts: List<RideitDriverPayoutUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    title: String = "Payouts",
    subtitle: String = "Track available, pending, and paid earnings",
    onPayoutClick: (RideitDriverPayoutUiModel) -> Unit = {},
    onRequestPayoutClick: () -> Unit = {}
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
                    fontSize = if (compact) 16.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Request",
                color = Color(0xFF166534),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFBBF7D0),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onRequestPayoutClick() }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        if (payouts.isEmpty()) {
            RideitEmptyPayoutCard(
                compact = compact,
                onClick = onRequestPayoutClick
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
            ) {
                payouts.forEach { payout ->
                    RideitDriverPayoutCard(
                        payout = payout,
                        compact = compact,
                        onClick = {
                            onPayoutClick(payout)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RideitDriverEarningsDashboardPanel(
    visible: Boolean,
    summary: RideitDriverEarningsSummaryUiModel,
    payouts: List<RideitDriverPayoutUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onHeroClick: () -> Unit = {},
    onPeriodClick: (RideitEarningsPeriodType) -> Unit = {},
    onPayoutClick: (RideitDriverPayoutUiModel) -> Unit = {},
    onRequestPayoutClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(200))
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 16.dp)
        ) {
            RideitDriverEarningsHeroCard(
                summary = summary,
                compact = compact,
                onClick = onHeroClick
            )

            RideitEarningsPeriodChips(
                selectedPeriod = summary.selectedPeriod,
                compact = compact,
                onPeriodClick = onPeriodClick
            )

            RideitDriverEarningsStatsGrid(
                summary = summary,
                compact = compact
            )

            RideitDriverPayoutSection(
                payouts = payouts,
                compact = compact,
                onPayoutClick = onPayoutClick,
                onRequestPayoutClick = onRequestPayoutClick
            )
        }
    }
}

@Composable
fun RideitCompactDriverEarningsCard(
    modifier: Modifier = Modifier,
    earningsText: String = "Rs 0",
    tripsText: String = "0 trips",
    periodText: String = "Today",
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 12.dp else 16.dp,
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
                        colors = listOf(Color.White, Color(0xFFF8FAFC))
                    )
                )
                .padding(if (compact) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitEarningsIcon(
                style = RideitEarningsStatStyle.SUCCESS,
                compact = compact,
                pulse = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = earningsText,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 16.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$periodText • $tripsText",
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "View",
                color = Color(0xFF166534),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFBBF7D0),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun RideitEarningsHeroMiniTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    Column(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.16f),
                shape = RoundedCornerShape(if (compact) 18.dp else 20.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(if (compact) 18.dp else 20.dp)
            )
            .padding(horizontal = 8.dp, vertical = if (compact) 9.dp else 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.74f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitEarningsHeroBadge(
    text: String,
    compact: Boolean
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
            .padding(
                horizontal = if (compact) 9.dp else 11.dp,
                vertical = if (compact) 6.dp else 7.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitEarningsIcon(
    style: RideitEarningsStatStyle,
    compact: Boolean,
    forceLight: Boolean = false,
    pulse: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_earnings_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_earnings_icon_pulse_value"
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
                text = "Rs",
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
private fun RideitPayoutStatusIcon(
    status: RideitPayoutStatusType,
    compact: Boolean
) {
    RideitEarningsIcon(
        style = status.toStatStyle(),
        compact = compact,
        pulse = status == RideitPayoutStatusType.AVAILABLE ||
                status == RideitPayoutStatusType.PENDING
    )
}

@Composable
private fun RideitPayoutStatusBadge(
    status: RideitPayoutStatusType,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = status.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = status.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = if (compact) 8.dp else 9.dp,
                vertical = if (compact) 5.dp else 6.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.label(),
            color = status.textColor(),
            fontSize = if (compact) 9.sp else 10.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitEmptyPayoutCard(
    compact: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFBBF7D0),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitEarningsIcon(
                style = RideitEarningsStatStyle.SUCCESS,
                compact = compact,
                pulse = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No payouts yet",
                color = Color(0xFF0F172A),
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Completed ride earnings and payout activity will appear here.",
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) 16.sp else 18.sp,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}

fun rideitDefaultDriverEarningsSummary(): RideitDriverEarningsSummaryUiModel {
    return RideitDriverEarningsSummaryUiModel(
        totalEarningsText = "Rs 0",
        availableBalanceText = "Rs 0",
        pendingPayoutText = "Rs 0",
        completedTripsText = "0",
        onlineHoursText = "0h",
        acceptanceRateText = "100%",
        selectedPeriod = RideitEarningsPeriodType.TODAY
    )
}

fun rideitDefaultDriverPayouts(): List<RideitDriverPayoutUiModel> {
    return listOf(
        RideitDriverPayoutUiModel(
            id = "available",
            amountText = "Rs 0",
            dateText = "Available now",
            methodText = "Rideit balance",
            status = RideitPayoutStatusType.AVAILABLE,
            referenceText = "Ready when earnings are available"
        )
    )
}

private fun RideitEarningsPeriodType.label(): String {
    return when (this) {
        RideitEarningsPeriodType.TODAY -> "Today"
        RideitEarningsPeriodType.WEEK -> "This week"
        RideitEarningsPeriodType.MONTH -> "This month"
        RideitEarningsPeriodType.ALL_TIME -> "All time"
        RideitEarningsPeriodType.CUSTOM -> "Custom period"
    }
}

private fun RideitEarningsPeriodType.shortLabel(): String {
    return when (this) {
        RideitEarningsPeriodType.TODAY -> "Today"
        RideitEarningsPeriodType.WEEK -> "Week"
        RideitEarningsPeriodType.MONTH -> "Month"
        RideitEarningsPeriodType.ALL_TIME -> "All"
        RideitEarningsPeriodType.CUSTOM -> "Custom"
    }
}

private fun RideitPayoutStatusType.label(): String {
    return when (this) {
        RideitPayoutStatusType.AVAILABLE -> "Available"
        RideitPayoutStatusType.PENDING -> "Pending"
        RideitPayoutStatusType.PAID -> "Paid"
        RideitPayoutStatusType.FAILED -> "Failed"
        RideitPayoutStatusType.NOT_READY -> "Not ready"
    }
}

private fun RideitPayoutStatusType.toStatStyle(): RideitEarningsStatStyle {
    return when (this) {
        RideitPayoutStatusType.AVAILABLE -> RideitEarningsStatStyle.SUCCESS
        RideitPayoutStatusType.PENDING -> RideitEarningsStatStyle.WARNING
        RideitPayoutStatusType.PAID -> RideitEarningsStatStyle.PRIMARY
        RideitPayoutStatusType.FAILED -> RideitEarningsStatStyle.DANGER
        RideitPayoutStatusType.NOT_READY -> RideitEarningsStatStyle.NEUTRAL
    }
}

private fun RideitPayoutStatusType.softBackgroundColor(): Color {
    return toStatStyle().softBackgroundColor()
}

private fun RideitPayoutStatusType.borderColor(): Color {
    return toStatStyle().borderColor()
}

private fun RideitPayoutStatusType.dotColor(): Color {
    return toStatStyle().dotColor()
}

private fun RideitPayoutStatusType.textColor(): Color {
    return toStatStyle().textColor()
}

private fun RideitEarningsStatStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitEarningsStatStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitEarningsStatStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitEarningsStatStyle.WARNING -> Color(0xFFFEF3C7)
        RideitEarningsStatStyle.DANGER -> Color(0xFFFEE2E2)
        RideitEarningsStatStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitEarningsStatStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitEarningsStatStyle.borderColor(): Color {
    return when (this) {
        RideitEarningsStatStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitEarningsStatStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitEarningsStatStyle.WARNING -> Color(0xFFFDE68A)
        RideitEarningsStatStyle.DANGER -> Color(0xFFFCA5A5)
        RideitEarningsStatStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitEarningsStatStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitEarningsStatStyle.dotColor(): Color {
    return when (this) {
        RideitEarningsStatStyle.PRIMARY -> Color(0xFF2563EB)
        RideitEarningsStatStyle.SUCCESS -> Color(0xFF22C55E)
        RideitEarningsStatStyle.WARNING -> Color(0xFFF59E0B)
        RideitEarningsStatStyle.DANGER -> Color(0xFFEF4444)
        RideitEarningsStatStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitEarningsStatStyle.NEUTRAL -> Color(0xFF64748B)
    }
}

private fun RideitEarningsStatStyle.textColor(): Color {
    return when (this) {
        RideitEarningsStatStyle.PRIMARY -> Color(0xFF2563EB)
        RideitEarningsStatStyle.SUCCESS -> Color(0xFF166534)
        RideitEarningsStatStyle.WARNING -> Color(0xFF92400E)
        RideitEarningsStatStyle.DANGER -> Color(0xFFB91C1C)
        RideitEarningsStatStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitEarningsStatStyle.NEUTRAL -> Color(0xFF475569)
    }
}