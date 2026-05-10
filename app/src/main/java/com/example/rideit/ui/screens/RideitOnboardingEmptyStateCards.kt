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

enum class RideitOnboardingEmptyStateType {
    RIDER_HOME,
    DRIVER_HOME,
    NO_TRIPS,
    NO_HISTORY,
    NO_SAVED_PLACES,
    NO_PAYMENTS,
    NO_EARNINGS,
    NO_VEHICLE,
    NO_SUPPORT_ITEMS,
    CUSTOM
}

enum class RideitOnboardingStepStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

@Immutable
data class RideitOnboardingStepUiModel(
    val id: String,
    val title: String,
    val message: String,
    val actionText: String? = null,
    val style: RideitOnboardingStepStyle = RideitOnboardingStepStyle.PRIMARY,
    val completed: Boolean = false
)

@Immutable
data class RideitOnboardingEmptyStateUiModel(
    val type: RideitOnboardingEmptyStateType,
    val title: String,
    val message: String,
    val primaryActionText: String = "Continue",
    val secondaryActionText: String? = null,
    val showPrimaryAction: Boolean = true,
    val showSecondaryAction: Boolean = false
)

@Composable
fun RideitPremiumEmptyStateCard(
    state: RideitOnboardingEmptyStateUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {}
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
            ),
        shape = RoundedCornerShape(if (compact) 30.dp else 36.dp),
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
                .padding(if (compact) 18.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitEmptyStateIcon(
                type = state.type,
                compact = compact,
                pulse = true
            )

            Spacer(modifier = Modifier.height(if (compact) 16.dp else 20.dp))

            Text(
                text = state.title,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 20.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = state.message,
                color = Color(0xFF64748B),
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) 18.sp else 20.sp,
                modifier = Modifier.padding(top = 7.dp, start = 8.dp, end = 8.dp),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (state.showPrimaryAction) {
                Spacer(modifier = Modifier.height(if (compact) 18.dp else 22.dp))

                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 52.dp else 56.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = state.type.primaryColor(),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = state.primaryActionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (state.showSecondaryAction && !state.secondaryActionText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = state.secondaryActionText,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun RideitAnimatedPremiumEmptyStateCard(
    visible: Boolean,
    state: RideitOnboardingEmptyStateUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(240)) + expandVertically(animationSpec = tween(260)),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = tween(200))
    ) {
        RideitPremiumEmptyStateCard(
            state = state,
            modifier = modifier,
            compact = compact,
            onPrimaryClick = onPrimaryClick,
            onSecondaryClick = onSecondaryClick
        )
    }
}

@Composable
fun RideitRiderOnboardingEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onBookRideClick: () -> Unit = {},
    onSavedPlacesClick: () -> Unit = {}
) {
    RideitPremiumEmptyStateCard(
        modifier = modifier,
        compact = compact,
        state = RideitOnboardingEmptyStateUiModel(
            type = RideitOnboardingEmptyStateType.RIDER_HOME,
            title = "Ready for your first Rideit trip?",
            message = "Search your destination, choose a ride option, and book your first premium Rideit journey.",
            primaryActionText = "Book a ride",
            secondaryActionText = "Add saved place",
            showPrimaryAction = true,
            showSecondaryAction = true
        ),
        onPrimaryClick = onBookRideClick,
        onSecondaryClick = onSavedPlacesClick
    )
}

@Composable
fun RideitDriverOnboardingEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onGoOnlineClick: () -> Unit = {},
    onVehicleClick: () -> Unit = {}
) {
    RideitPremiumEmptyStateCard(
        modifier = modifier,
        compact = compact,
        state = RideitOnboardingEmptyStateUiModel(
            type = RideitOnboardingEmptyStateType.DRIVER_HOME,
            title = "Start earning with Rideit",
            message = "Complete your vehicle setup, go online, and begin receiving rider requests.",
            primaryActionText = "Go online",
            secondaryActionText = "Vehicle setup",
            showPrimaryAction = true,
            showSecondaryAction = true
        ),
        onPrimaryClick = onGoOnlineClick,
        onSecondaryClick = onVehicleClick
    )
}

@Composable
fun RideitNoTripsEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    isDriverMode: Boolean = false,
    onPrimaryClick: () -> Unit = {}
) {
    RideitPremiumEmptyStateCard(
        modifier = modifier,
        compact = compact,
        state = RideitOnboardingEmptyStateUiModel(
            type = RideitOnboardingEmptyStateType.NO_TRIPS,
            title = if (isDriverMode) "No active driver trips" else "No active trips",
            message = if (isDriverMode) {
                "When you accept a rider request, your active trip will appear here."
            } else {
                "Book a ride and your active trip status will appear here."
            },
            primaryActionText = if (isDriverMode) "Go to requests" else "Book ride",
            showPrimaryAction = true
        ),
        onPrimaryClick = onPrimaryClick
    )
}

@Composable
fun RideitNoHistoryEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    isDriverMode: Boolean = false,
    onPrimaryClick: () -> Unit = {}
) {
    RideitPremiumEmptyStateCard(
        modifier = modifier,
        compact = compact,
        state = RideitOnboardingEmptyStateUiModel(
            type = RideitOnboardingEmptyStateType.NO_HISTORY,
            title = "No history yet",
            message = if (isDriverMode) {
                "Completed driver trips and earnings history will appear here."
            } else {
                "Completed rides, receipts, and ratings will appear here."
            },
            primaryActionText = if (isDriverMode) "Driver home" else "Start riding",
            showPrimaryAction = true
        ),
        onPrimaryClick = onPrimaryClick
    )
}

@Composable
fun RideitNoSavedPlacesEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onAddPlaceClick: () -> Unit = {}
) {
    RideitPremiumEmptyStateCard(
        modifier = modifier,
        compact = compact,
        state = RideitOnboardingEmptyStateUiModel(
            type = RideitOnboardingEmptyStateType.NO_SAVED_PLACES,
            title = "No saved places yet",
            message = "Save home, work, or favorite destinations to book faster next time.",
            primaryActionText = "Add saved place",
            showPrimaryAction = true
        ),
        onPrimaryClick = onAddPlaceClick
    )
}

@Composable
fun RideitNoPaymentsEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onAddPaymentClick: () -> Unit = {}
) {
    RideitPremiumEmptyStateCard(
        modifier = modifier,
        compact = compact,
        state = RideitOnboardingEmptyStateUiModel(
            type = RideitOnboardingEmptyStateType.NO_PAYMENTS,
            title = "No payment method",
            message = "Cash is available by default. Add a card or wallet method for faster checkout.",
            primaryActionText = "Add payment method",
            showPrimaryAction = true
        ),
        onPrimaryClick = onAddPaymentClick
    )
}

@Composable
fun RideitNoEarningsEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onDriverHomeClick: () -> Unit = {}
) {
    RideitPremiumEmptyStateCard(
        modifier = modifier,
        compact = compact,
        state = RideitOnboardingEmptyStateUiModel(
            type = RideitOnboardingEmptyStateType.NO_EARNINGS,
            title = "No earnings yet",
            message = "Complete your first Rideit trip and your earnings will appear here.",
            primaryActionText = "Driver home",
            showPrimaryAction = true
        ),
        onPrimaryClick = onDriverHomeClick
    )
}

@Composable
fun RideitNoVehicleEmptyState(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onAddVehicleClick: () -> Unit = {}
) {
    RideitPremiumEmptyStateCard(
        modifier = modifier,
        compact = compact,
        state = RideitOnboardingEmptyStateUiModel(
            type = RideitOnboardingEmptyStateType.NO_VEHICLE,
            title = "Add your vehicle",
            message = "Driver vehicle details are required before accepting Rideit trips.",
            primaryActionText = "Add vehicle",
            showPrimaryAction = true
        ),
        onPrimaryClick = onAddVehicleClick
    )
}

@Composable
fun RideitOnboardingStepCard(
    step: RideitOnboardingStepUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (step.completed) 8.dp else 12.dp,
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
                spotColor = step.style.dotColor().copy(alpha = 0.14f)
            )
            .border(
                width = if (step.completed) 1.dp else 2.dp,
                color = if (step.completed) Color.White.copy(alpha = 0.62f) else step.style.borderColor(),
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
                        colors = if (step.completed) {
                            listOf(Color.White, Color(0xFFF8FAFC))
                        } else {
                            listOf(step.style.softBackgroundColor(), Color.White)
                        }
                    )
                )
                .padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitOnboardingStepIcon(
                style = step.style,
                completed = step.completed,
                compact = compact
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = step.title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = step.message,
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!step.actionText.isNullOrBlank() && !step.completed) {
                Text(
                    text = step.actionText,
                    color = step.style.textColor(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .background(
                            color = step.style.softBackgroundColor(),
                            shape = RoundedCornerShape(50)
                        )
                        .border(
                            width = 1.dp,
                            color = step.style.borderColor(),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RideitOnboardingStepsSection(
    steps: List<RideitOnboardingStepUiModel>,
    modifier: Modifier = Modifier,
    title: String = "Get started",
    subtitle: String = "Complete these steps to unlock the best Rideit experience",
    compact: Boolean = true,
    onStepClick: (RideitOnboardingStepUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

            RideitOnboardingProgressBadge(
                completed = steps.count { it.completed },
                total = steps.size,
                compact = compact
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
        ) {
            steps.forEach { step ->
                RideitOnboardingStepCard(
                    step = step,
                    compact = compact,
                    onClick = {
                        onStepClick(step)
                    }
                )
            }
        }
    }
}

@Composable
fun RideitOnboardingQuickTipsRow(
    tips: List<RideitOnboardingStepUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onTipClick: (RideitOnboardingStepUiModel) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tips.forEach { tip ->
            RideitOnboardingTipChip(
                step = tip,
                compact = compact,
                onClick = {
                    onTipClick(tip)
                }
            )
        }
    }
}

@Composable
fun RideitOnboardingTipChip(
    step: RideitOnboardingStepUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(
                color = step.style.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = step.style.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 8.dp else 9.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 9.dp else 10.dp)
                .background(
                    color = step.style.dotColor(),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = step.title,
            color = step.style.textColor(),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitDashboardOnboardingPanel(
    visible: Boolean,
    isDriverMode: Boolean,
    steps: List<RideitOnboardingStepUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {},
    onStepClick: (RideitOnboardingStepUiModel) -> Unit = {}
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
            if (isDriverMode) {
                RideitDriverOnboardingEmptyState(
                    compact = compact,
                    onGoOnlineClick = onPrimaryClick,
                    onVehicleClick = onSecondaryClick
                )
            } else {
                RideitRiderOnboardingEmptyState(
                    compact = compact,
                    onBookRideClick = onPrimaryClick,
                    onSavedPlacesClick = onSecondaryClick
                )
            }

            RideitOnboardingStepsSection(
                steps = steps,
                compact = compact,
                title = if (isDriverMode) "Driver setup" else "Rider setup",
                subtitle = if (isDriverMode) {
                    "Complete your driver tools"
                } else {
                    "Set up faster booking"
                },
                onStepClick = onStepClick
            )
        }
    }
}

@Composable
private fun RideitEmptyStateIcon(
    type: RideitOnboardingEmptyStateType,
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_empty_state_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_empty_state_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 74.dp else 84.dp)
            .background(
                color = type.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 34.dp else 40.dp)
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
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun RideitOnboardingStepIcon(
    style: RideitOnboardingStepStyle,
    completed: Boolean,
    compact: Boolean
) {
    RideitStepIcon(
        style = if (completed) RideitOnboardingStepStyle.SUCCESS else style,
        text = if (completed) "✓" else style.iconText(),
        compact = compact,
        pulse = !completed
    )
}

@Composable
private fun RideitStepIcon(
    style: RideitOnboardingStepStyle,
    text: String,
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_step_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_step_icon_pulse_value"
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
                text = text,
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
private fun RideitOnboardingProgressBadge(
    completed: Int,
    total: Int,
    compact: Boolean
) {
    Box(
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
            .padding(
                horizontal = if (compact) 9.dp else 11.dp,
                vertical = if (compact) 6.dp else 7.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$completed/$total",
            color = Color(0xFF2563EB),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun rideitDefaultRiderOnboardingSteps(): List<RideitOnboardingStepUiModel> {
    return listOf(
        RideitOnboardingStepUiModel(
            id = "book_first_ride",
            title = "Book first ride",
            message = "Search a destination and try Rideit booking.",
            actionText = "Book",
            style = RideitOnboardingStepStyle.PRIMARY
        ),
        RideitOnboardingStepUiModel(
            id = "add_saved_place",
            title = "Add saved place",
            message = "Save home or work for faster booking.",
            actionText = "Add",
            style = RideitOnboardingStepStyle.PREMIUM
        ),
        RideitOnboardingStepUiModel(
            id = "setup_payment",
            title = "Set payment",
            message = "Choose cash, card, or wallet method.",
            actionText = "Pay",
            style = RideitOnboardingStepStyle.SUCCESS
        )
    )
}

fun rideitDefaultDriverOnboardingSteps(): List<RideitOnboardingStepUiModel> {
    return listOf(
        RideitOnboardingStepUiModel(
            id = "add_vehicle",
            title = "Add vehicle",
            message = "Complete vehicle details before trips.",
            actionText = "Add",
            style = RideitOnboardingStepStyle.PRIMARY
        ),
        RideitOnboardingStepUiModel(
            id = "go_online",
            title = "Go online",
            message = "Start receiving rider requests.",
            actionText = "Online",
            style = RideitOnboardingStepStyle.SUCCESS
        ),
        RideitOnboardingStepUiModel(
            id = "check_earnings",
            title = "Track earnings",
            message = "Review earnings after completed trips.",
            actionText = "View",
            style = RideitOnboardingStepStyle.PREMIUM
        )
    )
}

private fun RideitOnboardingEmptyStateType.iconText(): String {
    return when (this) {
        RideitOnboardingEmptyStateType.RIDER_HOME -> "R"
        RideitOnboardingEmptyStateType.DRIVER_HOME -> "D"
        RideitOnboardingEmptyStateType.NO_TRIPS -> "T"
        RideitOnboardingEmptyStateType.NO_HISTORY -> "H"
        RideitOnboardingEmptyStateType.NO_SAVED_PLACES -> "★"
        RideitOnboardingEmptyStateType.NO_PAYMENTS -> "Rs"
        RideitOnboardingEmptyStateType.NO_EARNINGS -> "Rs"
        RideitOnboardingEmptyStateType.NO_VEHICLE -> "V"
        RideitOnboardingEmptyStateType.NO_SUPPORT_ITEMS -> "?"
        RideitOnboardingEmptyStateType.CUSTOM -> "•"
    }
}

private fun RideitOnboardingEmptyStateType.softBackgroundColor(): Color {
    return when (this) {
        RideitOnboardingEmptyStateType.RIDER_HOME -> Color(0xFFEFF6FF)
        RideitOnboardingEmptyStateType.DRIVER_HOME -> Color(0xFFF3E8FF)
        RideitOnboardingEmptyStateType.NO_TRIPS -> Color(0xFFEFF6FF)
        RideitOnboardingEmptyStateType.NO_HISTORY -> Color(0xFFF8FAFC)
        RideitOnboardingEmptyStateType.NO_SAVED_PLACES -> Color(0xFFFEF3C7)
        RideitOnboardingEmptyStateType.NO_PAYMENTS -> Color(0xFFF0FDF4)
        RideitOnboardingEmptyStateType.NO_EARNINGS -> Color(0xFFDCFCE7)
        RideitOnboardingEmptyStateType.NO_VEHICLE -> Color(0xFFEFF6FF)
        RideitOnboardingEmptyStateType.NO_SUPPORT_ITEMS -> Color(0xFFFFFBEB)
        RideitOnboardingEmptyStateType.CUSTOM -> Color(0xFFF8FAFC)
    }
}

private fun RideitOnboardingEmptyStateType.dotColor(): Color {
    return when (this) {
        RideitOnboardingEmptyStateType.RIDER_HOME -> Color(0xFF2563EB)
        RideitOnboardingEmptyStateType.DRIVER_HOME -> Color(0xFF7C3AED)
        RideitOnboardingEmptyStateType.NO_TRIPS -> Color(0xFF2563EB)
        RideitOnboardingEmptyStateType.NO_HISTORY -> Color(0xFF64748B)
        RideitOnboardingEmptyStateType.NO_SAVED_PLACES -> Color(0xFFF59E0B)
        RideitOnboardingEmptyStateType.NO_PAYMENTS -> Color(0xFF16A34A)
        RideitOnboardingEmptyStateType.NO_EARNINGS -> Color(0xFF22C55E)
        RideitOnboardingEmptyStateType.NO_VEHICLE -> Color(0xFF2563EB)
        RideitOnboardingEmptyStateType.NO_SUPPORT_ITEMS -> Color(0xFFF59E0B)
        RideitOnboardingEmptyStateType.CUSTOM -> Color(0xFF64748B)
    }
}

private fun RideitOnboardingEmptyStateType.primaryColor(): Color {
    return when (this) {
        RideitOnboardingEmptyStateType.RIDER_HOME -> Color(0xFF0F172A)
        RideitOnboardingEmptyStateType.DRIVER_HOME -> Color(0xFF0F172A)
        RideitOnboardingEmptyStateType.NO_TRIPS -> Color(0xFF2563EB)
        RideitOnboardingEmptyStateType.NO_HISTORY -> Color(0xFF0F172A)
        RideitOnboardingEmptyStateType.NO_SAVED_PLACES -> Color(0xFFF59E0B)
        RideitOnboardingEmptyStateType.NO_PAYMENTS -> Color(0xFF16A34A)
        RideitOnboardingEmptyStateType.NO_EARNINGS -> Color(0xFF16A34A)
        RideitOnboardingEmptyStateType.NO_VEHICLE -> Color(0xFF2563EB)
        RideitOnboardingEmptyStateType.NO_SUPPORT_ITEMS -> Color(0xFFF59E0B)
        RideitOnboardingEmptyStateType.CUSTOM -> Color(0xFF0F172A)
    }
}

private fun RideitOnboardingStepStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitOnboardingStepStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitOnboardingStepStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitOnboardingStepStyle.WARNING -> Color(0xFFFEF3C7)
        RideitOnboardingStepStyle.DANGER -> Color(0xFFFEE2E2)
        RideitOnboardingStepStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitOnboardingStepStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitOnboardingStepStyle.borderColor(): Color {
    return when (this) {
        RideitOnboardingStepStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitOnboardingStepStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitOnboardingStepStyle.WARNING -> Color(0xFFFDE68A)
        RideitOnboardingStepStyle.DANGER -> Color(0xFFFCA5A5)
        RideitOnboardingStepStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitOnboardingStepStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitOnboardingStepStyle.dotColor(): Color {
    return when (this) {
        RideitOnboardingStepStyle.PRIMARY -> Color(0xFF2563EB)
        RideitOnboardingStepStyle.SUCCESS -> Color(0xFF22C55E)
        RideitOnboardingStepStyle.WARNING -> Color(0xFFF59E0B)
        RideitOnboardingStepStyle.DANGER -> Color(0xFFEF4444)
        RideitOnboardingStepStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitOnboardingStepStyle.NEUTRAL -> Color(0xFF64748B)
    }
}

private fun RideitOnboardingStepStyle.textColor(): Color {
    return when (this) {
        RideitOnboardingStepStyle.PRIMARY -> Color(0xFF2563EB)
        RideitOnboardingStepStyle.SUCCESS -> Color(0xFF166534)
        RideitOnboardingStepStyle.WARNING -> Color(0xFF92400E)
        RideitOnboardingStepStyle.DANGER -> Color(0xFFB91C1C)
        RideitOnboardingStepStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitOnboardingStepStyle.NEUTRAL -> Color(0xFF475569)
    }
}

private fun RideitOnboardingStepStyle.iconText(): String {
    return when (this) {
        RideitOnboardingStepStyle.PRIMARY -> "R"
        RideitOnboardingStepStyle.SUCCESS -> "✓"
        RideitOnboardingStepStyle.WARNING -> "!"
        RideitOnboardingStepStyle.DANGER -> "!"
        RideitOnboardingStepStyle.PREMIUM -> "◆"
        RideitOnboardingStepStyle.NEUTRAL -> "•"
    }
}