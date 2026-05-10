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
import androidx.compose.foundation.layout.width
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

enum class RideitPaymentMethodType {
    CASH,
    CARD,
    WALLET,
    BANK,
    PROMO,
    CUSTOM
}

enum class RideitReceiptActionType {
    VIEW,
    DOWNLOAD,
    SHARE,
    REPORT,
    SUPPORT,
    CUSTOM
}

@Immutable
data class RideitPaymentMethodUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val type: RideitPaymentMethodType = RideitPaymentMethodType.CASH,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val badgeText: String? = null
)

@Immutable
data class RideitFareBreakdownUiModel(
    val baseFareText: String? = null,
    val distanceFareText: String? = null,
    val timeFareText: String? = null,
    val serviceFeeText: String? = null,
    val discountText: String? = null,
    val totalFareText: String? = null,
    val paymentMethodText: String? = null
)

@Immutable
data class RideitReceiptActionUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val type: RideitReceiptActionType = RideitReceiptActionType.CUSTOM,
    val enabled: Boolean = true
)

@Composable
fun RideitPaymentMethodCard(
    paymentMethod: RideitPaymentMethodUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    val alpha = if (paymentMethod.enabled) 1f else 0.55f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (paymentMethod.selected) 16.dp else 9.dp,
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
                spotColor = paymentMethod.type.dotColor().copy(alpha = 0.16f)
            )
            .border(
                width = if (paymentMethod.selected) 2.dp else 1.dp,
                color = if (paymentMethod.selected) {
                    paymentMethod.type.borderColor()
                } else {
                    Color.White.copy(alpha = 0.62f)
                },
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable(enabled = paymentMethod.enabled) {
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
                        colors = if (paymentMethod.selected) {
                            listOf(paymentMethod.type.softBackgroundColor(), Color.White)
                        } else {
                            listOf(Color.White, Color(0xFFF8FAFC))
                        }
                    )
                )
                .padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitPaymentMethodIcon(
                type = paymentMethod.type,
                compact = compact,
                pulse = paymentMethod.selected
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = paymentMethod.title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = paymentMethod.subtitle?.takeIf { it.isNotBlank() }
                        ?: paymentMethod.type.defaultSubtitle(),
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            when {
                !paymentMethod.badgeText.isNullOrBlank() -> {
                    RideitPaymentMiniBadge(
                        text = paymentMethod.badgeText,
                        type = paymentMethod.type,
                        compact = compact
                    )
                }

                paymentMethod.selected -> {
                    RideitPaymentMiniBadge(
                        text = "Selected",
                        type = paymentMethod.type,
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
fun RideitPaymentMethodList(
    paymentMethods: List<RideitPaymentMethodUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onPaymentMethodClick: (RideitPaymentMethodUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
    ) {
        paymentMethods.forEach { paymentMethod ->
            RideitPaymentMethodCard(
                paymentMethod = paymentMethod,
                compact = compact,
                onClick = {
                    onPaymentMethodClick(paymentMethod)
                }
            )
        }
    }
}

@Composable
fun RideitPaymentMethodSection(
    paymentMethods: List<RideitPaymentMethodUiModel>,
    modifier: Modifier = Modifier,
    title: String = "Payment method",
    subtitle: String = "Choose how you want to pay",
    compact: Boolean = true,
    onPaymentMethodClick: (RideitPaymentMethodUiModel) -> Unit = {},
    onAddPaymentClick: () -> Unit = {}
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
                text = "Add",
                color = Color(0xFF2563EB),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFBFDBFE),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onAddPaymentClick() }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        if (paymentMethods.isEmpty()) {
            RideitEmptyPaymentMethodCard(
                compact = compact,
                onClick = onAddPaymentClick
            )
        } else {
            RideitPaymentMethodList(
                paymentMethods = paymentMethods,
                compact = compact,
                onPaymentMethodClick = onPaymentMethodClick
            )
        }
    }
}

@Composable
fun RideitFareBreakdownCard(
    fare: RideitFareBreakdownUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    expanded: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 14.dp else 20.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = Color.Black.copy(alpha = 0.16f)
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
                        colors = listOf(Color.White, Color(0xFFF8FAFC))
                    )
                )
                .padding(if (compact) 16.dp else 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitPaymentMethodIcon(
                    type = RideitPaymentMethodType.CASH,
                    compact = compact,
                    pulse = true
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "Fare breakdown",
                        color = Color(0xFF0F172A),
                        fontSize = if (compact) 16.sp else 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = fare.paymentMethodText?.takeIf { it.isNotBlank() }
                            ?: "Rideit fare summary",
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = fare.totalFareText?.takeIf { it.isNotBlank() } ?: "Rs 0",
                    color = Color(0xFF166534),
                    fontSize = if (compact) 17.sp else 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(if (compact) 14.dp else 16.dp))

                    RideitFareLine(
                        label = "Base fare",
                        value = fare.baseFareText?.takeIf { it.isNotBlank() } ?: "Rs 0",
                        compact = compact
                    )

                    RideitFareLine(
                        label = "Distance fare",
                        value = fare.distanceFareText?.takeIf { it.isNotBlank() } ?: "Rs 0",
                        compact = compact
                    )

                    RideitFareLine(
                        label = "Time fare",
                        value = fare.timeFareText?.takeIf { it.isNotBlank() } ?: "Rs 0",
                        compact = compact
                    )

                    RideitFareLine(
                        label = "Service fee",
                        value = fare.serviceFeeText?.takeIf { it.isNotBlank() } ?: "Rs 0",
                        compact = compact
                    )

                    if (!fare.discountText.isNullOrBlank()) {
                        RideitFareLine(
                            label = "Discount",
                            value = fare.discountText,
                            compact = compact,
                            success = true
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = if (compact) 10.dp else 12.dp),
                        color = Color(0xFFE2E8F0)
                    )

                    RideitFareLine(
                        label = "Total",
                        value = fare.totalFareText?.takeIf { it.isNotBlank() } ?: "Rs 0",
                        compact = compact,
                        bold = true
                    )
                }
            }
        }
    }
}

@Composable
fun RideitReceiptActionCard(
    action: RideitReceiptActionUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    val alpha = if (action.enabled) 1f else 0.55f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = action.type.borderColor(),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable(enabled = action.enabled) {
                onClick()
            }
            .graphicsLayer {
                this.alpha = alpha
            },
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = action.type.softBackgroundColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitReceiptActionIcon(
                type = action.type,
                compact = compact,
                pulse = action.type == RideitReceiptActionType.DOWNLOAD ||
                        action.type == RideitReceiptActionType.SHARE
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = action.title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!action.subtitle.isNullOrBlank()) {
                    Text(
                        text = action.subtitle,
                        color = Color(0xFF64748B),
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "›",
                color = action.type.textColor(),
                fontSize = if (compact) 22.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun RideitReceiptActionGrid(
    actions: List<RideitReceiptActionUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onActionClick: (RideitReceiptActionUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
    ) {
        actions.forEach { action ->
            RideitReceiptActionCard(
                action = action,
                compact = compact,
                onClick = {
                    onActionClick(action)
                }
            )
        }
    }
}

@Composable
fun RideitReceiptActionSection(
    actions: List<RideitReceiptActionUiModel>,
    modifier: Modifier = Modifier,
    title: String = "Receipt actions",
    subtitle: String = "View, share, download, or get help",
    compact: Boolean = true,
    onActionClick: (RideitReceiptActionUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
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

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        RideitReceiptActionGrid(
            actions = actions,
            compact = compact,
            onActionClick = onActionClick
        )
    }
}

@Composable
fun RideitPaymentReceiptSummaryPanel(
    visible: Boolean,
    fare: RideitFareBreakdownUiModel,
    paymentMethods: List<RideitPaymentMethodUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    receiptActions: List<RideitReceiptActionUiModel> = rideitDefaultReceiptActions(),
    primaryButtonText: String = "Done",
    showPrimaryButton: Boolean = true,
    onPaymentMethodClick: (RideitPaymentMethodUiModel) -> Unit = {},
    onReceiptActionClick: (RideitReceiptActionUiModel) -> Unit = {},
    onPrimaryClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(200))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.20f)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.80f),
                            Color.White.copy(alpha = 0.22f)
                        )
                    ),
                    shape = RoundedCornerShape(36.dp)
                ),
            shape = RoundedCornerShape(36.dp),
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
                    .padding(18.dp)
            ) {
                RideitFareBreakdownCard(
                    fare = fare,
                    compact = compact,
                    expanded = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                RideitPaymentMethodSection(
                    paymentMethods = paymentMethods,
                    compact = compact,
                    onPaymentMethodClick = onPaymentMethodClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                RideitReceiptActionSection(
                    actions = receiptActions,
                    compact = compact,
                    onActionClick = onReceiptActionClick
                )

                if (showPrimaryButton) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onPrimaryClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(19.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = primaryButtonText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RideitCompactPaymentSummaryCard(
    modifier: Modifier = Modifier,
    fareText: String? = null,
    paymentMethodText: String? = null,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
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
            RideitPaymentMethodIcon(
                type = RideitPaymentMethodType.CASH,
                compact = compact,
                pulse = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "Payment summary",
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 14.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = paymentMethodText?.takeIf { it.isNotBlank() } ?: "Cash payment",
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = fareText?.takeIf { it.isNotBlank() } ?: "Rs 0",
                color = Color(0xFF166534),
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RideitFareLine(
    label: String,
    value: String,
    compact: Boolean,
    bold: Boolean = false,
    success: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 4.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (bold) Color(0xFF0F172A) else Color(0xFF64748B),
            fontSize = if (compact) 12.sp else 13.sp,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = value,
            color = when {
                success -> Color(0xFF166534)
                bold -> Color(0xFF0F172A)
                else -> Color(0xFF475569)
            },
            fontSize = if (compact) 12.sp else 13.sp,
            fontWeight = if (bold || success) FontWeight.ExtraBold else FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitPaymentMethodIcon(
    type: RideitPaymentMethodType,
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_payment_method_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_payment_method_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 40.dp else 46.dp)
            .background(
                color = type.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 17.dp else 20.dp)
                .graphicsLayer {
                    scaleX = if (pulse) scale else 1f
                    scaleY = if (pulse) scale else 1f
                }
                .background(
                    color = type.dotColor(),
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
private fun RideitReceiptActionIcon(
    type: RideitReceiptActionType,
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_receipt_action_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_receipt_action_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 38.dp else 44.dp)
            .background(
                color = type.softBackgroundColor(),
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
                    color = type.dotColor(),
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
private fun RideitPaymentMiniBadge(
    text: String,
    type: RideitPaymentMethodType,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = type.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = type.borderColor(),
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
            color = type.textColor(),
            fontSize = if (compact) 9.sp else 10.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitEmptyPaymentMethodCard(
    compact: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFFBFDBFE),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitPaymentMethodIcon(
                type = RideitPaymentMethodType.CASH,
                compact = compact,
                pulse = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No payment method yet",
                color = Color(0xFF0F172A),
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Add cash, card, or wallet method for faster Rideit checkout.",
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

fun rideitDefaultPaymentMethods(
    selectedId: String = "cash"
): List<RideitPaymentMethodUiModel> {
    return listOf(
        RideitPaymentMethodUiModel(
            id = "cash",
            title = "Cash",
            subtitle = "Pay directly to the driver",
            type = RideitPaymentMethodType.CASH,
            selected = selectedId == "cash",
            badgeText = if (selectedId == "cash") "Selected" else null
        ),
        RideitPaymentMethodUiModel(
            id = "card",
            title = "Card",
            subtitle = "Credit or debit card",
            type = RideitPaymentMethodType.CARD,
            selected = selectedId == "card"
        ),
        RideitPaymentMethodUiModel(
            id = "wallet",
            title = "Rideit Wallet",
            subtitle = "Fast wallet checkout",
            type = RideitPaymentMethodType.WALLET,
            selected = selectedId == "wallet"
        )
    )
}

fun rideitDefaultReceiptActions(): List<RideitReceiptActionUiModel> {
    return listOf(
        RideitReceiptActionUiModel(
            id = "view_receipt",
            title = "View receipt",
            subtitle = "Open full trip receipt",
            type = RideitReceiptActionType.VIEW
        ),
        RideitReceiptActionUiModel(
            id = "download_receipt",
            title = "Download receipt",
            subtitle = "Save receipt for your records",
            type = RideitReceiptActionType.DOWNLOAD
        ),
        RideitReceiptActionUiModel(
            id = "share_receipt",
            title = "Share receipt",
            subtitle = "Send receipt to someone",
            type = RideitReceiptActionType.SHARE
        ),
        RideitReceiptActionUiModel(
            id = "receipt_support",
            title = "Get receipt help",
            subtitle = "Report fare or payment issue",
            type = RideitReceiptActionType.SUPPORT
        )
    )
}

private fun RideitPaymentMethodType.defaultSubtitle(): String {
    return when (this) {
        RideitPaymentMethodType.CASH -> "Pay directly to the driver"
        RideitPaymentMethodType.CARD -> "Credit or debit card"
        RideitPaymentMethodType.WALLET -> "Fast wallet checkout"
        RideitPaymentMethodType.BANK -> "Bank transfer or linked account"
        RideitPaymentMethodType.PROMO -> "Promo or discount applied"
        RideitPaymentMethodType.CUSTOM -> "Custom payment method"
    }
}

private fun RideitPaymentMethodType.iconText(): String {
    return when (this) {
        RideitPaymentMethodType.CASH -> "Rs"
        RideitPaymentMethodType.CARD -> "C"
        RideitPaymentMethodType.WALLET -> "W"
        RideitPaymentMethodType.BANK -> "B"
        RideitPaymentMethodType.PROMO -> "%"
        RideitPaymentMethodType.CUSTOM -> "•"
    }
}

private fun RideitPaymentMethodType.softBackgroundColor(): Color {
    return when (this) {
        RideitPaymentMethodType.CASH -> Color(0xFFF0FDF4)
        RideitPaymentMethodType.CARD -> Color(0xFFEFF6FF)
        RideitPaymentMethodType.WALLET -> Color(0xFFF3E8FF)
        RideitPaymentMethodType.BANK -> Color(0xFFECFEFF)
        RideitPaymentMethodType.PROMO -> Color(0xFFFEF3C7)
        RideitPaymentMethodType.CUSTOM -> Color(0xFFF8FAFC)
    }
}

private fun RideitPaymentMethodType.borderColor(): Color {
    return when (this) {
        RideitPaymentMethodType.CASH -> Color(0xFFBBF7D0)
        RideitPaymentMethodType.CARD -> Color(0xFFBFDBFE)
        RideitPaymentMethodType.WALLET -> Color(0xFFD8B4FE)
        RideitPaymentMethodType.BANK -> Color(0xFFA5F3FC)
        RideitPaymentMethodType.PROMO -> Color(0xFFFDE68A)
        RideitPaymentMethodType.CUSTOM -> Color(0xFFE2E8F0)
    }
}

private fun RideitPaymentMethodType.dotColor(): Color {
    return when (this) {
        RideitPaymentMethodType.CASH -> Color(0xFF16A34A)
        RideitPaymentMethodType.CARD -> Color(0xFF2563EB)
        RideitPaymentMethodType.WALLET -> Color(0xFF7C3AED)
        RideitPaymentMethodType.BANK -> Color(0xFF0891B2)
        RideitPaymentMethodType.PROMO -> Color(0xFFF59E0B)
        RideitPaymentMethodType.CUSTOM -> Color(0xFF64748B)
    }
}

private fun RideitPaymentMethodType.textColor(): Color {
    return when (this) {
        RideitPaymentMethodType.CASH -> Color(0xFF166534)
        RideitPaymentMethodType.CARD -> Color(0xFF2563EB)
        RideitPaymentMethodType.WALLET -> Color(0xFF6D28D9)
        RideitPaymentMethodType.BANK -> Color(0xFF0E7490)
        RideitPaymentMethodType.PROMO -> Color(0xFF92400E)
        RideitPaymentMethodType.CUSTOM -> Color(0xFF475569)
    }
}

private fun RideitReceiptActionType.iconText(): String {
    return when (this) {
        RideitReceiptActionType.VIEW -> "R"
        RideitReceiptActionType.DOWNLOAD -> "↓"
        RideitReceiptActionType.SHARE -> "S"
        RideitReceiptActionType.REPORT -> "!"
        RideitReceiptActionType.SUPPORT -> "?"
        RideitReceiptActionType.CUSTOM -> "•"
    }
}

private fun RideitReceiptActionType.softBackgroundColor(): Color {
    return when (this) {
        RideitReceiptActionType.VIEW -> Color(0xFFEFF6FF)
        RideitReceiptActionType.DOWNLOAD -> Color(0xFFDCFCE7)
        RideitReceiptActionType.SHARE -> Color(0xFFF3E8FF)
        RideitReceiptActionType.REPORT -> Color(0xFFFEE2E2)
        RideitReceiptActionType.SUPPORT -> Color(0xFFFFFBEB)
        RideitReceiptActionType.CUSTOM -> Color(0xFFF8FAFC)
    }
}

private fun RideitReceiptActionType.borderColor(): Color {
    return when (this) {
        RideitReceiptActionType.VIEW -> Color(0xFFBFDBFE)
        RideitReceiptActionType.DOWNLOAD -> Color(0xFFBBF7D0)
        RideitReceiptActionType.SHARE -> Color(0xFFD8B4FE)
        RideitReceiptActionType.REPORT -> Color(0xFFFCA5A5)
        RideitReceiptActionType.SUPPORT -> Color(0xFFFDE68A)
        RideitReceiptActionType.CUSTOM -> Color(0xFFE2E8F0)
    }
}

private fun RideitReceiptActionType.dotColor(): Color {
    return when (this) {
        RideitReceiptActionType.VIEW -> Color(0xFF2563EB)
        RideitReceiptActionType.DOWNLOAD -> Color(0xFF22C55E)
        RideitReceiptActionType.SHARE -> Color(0xFF7C3AED)
        RideitReceiptActionType.REPORT -> Color(0xFFEF4444)
        RideitReceiptActionType.SUPPORT -> Color(0xFFF59E0B)
        RideitReceiptActionType.CUSTOM -> Color(0xFF64748B)
    }
}

private fun RideitReceiptActionType.textColor(): Color {
    return when (this) {
        RideitReceiptActionType.VIEW -> Color(0xFF2563EB)
        RideitReceiptActionType.DOWNLOAD -> Color(0xFF166534)
        RideitReceiptActionType.SHARE -> Color(0xFF6D28D9)
        RideitReceiptActionType.REPORT -> Color(0xFFB91C1C)
        RideitReceiptActionType.SUPPORT -> Color(0xFF92400E)
        RideitReceiptActionType.CUSTOM -> Color(0xFF475569)
    }
}