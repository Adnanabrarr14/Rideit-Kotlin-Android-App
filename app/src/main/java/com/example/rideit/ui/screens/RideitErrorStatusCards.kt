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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

enum class RideitStatusCardType {
    ERROR,
    WARNING,
    SUCCESS,
    INFO,
    OFFLINE,
    NETWORK,
    FIREBASE,
    PERMISSION,
    EMPTY,
    CUSTOM
}

enum class RideitStatusCardStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

@Immutable
data class RideitStatusCardUiModel(
    val type: RideitStatusCardType = RideitStatusCardType.ERROR,
    val title: String = "Something went wrong",
    val message: String = "Please try again in a moment.",
    val primaryActionText: String = "Retry",
    val secondaryActionText: String? = null,
    val showPrimaryAction: Boolean = true,
    val showSecondaryAction: Boolean = false,
    val style: RideitStatusCardStyle = RideitStatusCardStyle.DANGER
)

@Immutable
data class RideitInlineStatusUiModel(
    val id: String = "",
    val title: String,
    val message: String? = null,
    val type: RideitStatusCardType = RideitStatusCardType.INFO,
    val style: RideitStatusCardStyle = RideitStatusCardStyle.PRIMARY,
    val actionText: String? = null,
    val enabled: Boolean = true
)

@Composable
fun RideitPremiumStatusCard(
    status: RideitStatusCardUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {}
) {
    RideitStatusCardContainer(
        modifier = modifier,
        compact = compact,
        style = status.style
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 18.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitStatusIcon(
                type = status.type,
                style = status.style,
                compact = compact,
                pulse = true,
                large = true
            )

            Spacer(modifier = Modifier.height(if (compact) 16.dp else 20.dp))

            Text(
                text = status.title,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 19.sp else 23.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = status.message,
                color = Color(0xFF64748B),
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) 18.sp else 20.sp,
                modifier = Modifier.padding(top = 7.dp, start = 8.dp, end = 8.dp),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (status.showPrimaryAction) {
                Spacer(modifier = Modifier.height(if (compact) 18.dp else 22.dp))

                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 52.dp else 56.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = status.style.buttonColor(),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = status.primaryActionText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (status.showSecondaryAction && !status.secondaryActionText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onSecondaryClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = status.secondaryActionText,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun RideitAnimatedPremiumStatusCard(
    visible: Boolean,
    status: RideitStatusCardUiModel,
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
        RideitPremiumStatusCard(
            status = status,
            modifier = modifier,
            compact = compact,
            onPrimaryClick = onPrimaryClick,
            onSecondaryClick = onSecondaryClick
        )
    }
}

@Composable
fun RideitErrorRetryCard(
    modifier: Modifier = Modifier,
    title: String = "Could not load Rideit",
    message: String = "Something went wrong while loading your data. Please try again.",
    compact: Boolean = true,
    onRetryClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    RideitPremiumStatusCard(
        modifier = modifier,
        compact = compact,
        status = RideitStatusCardUiModel(
            type = RideitStatusCardType.ERROR,
            title = title,
            message = message,
            primaryActionText = "Retry",
            secondaryActionText = if (onBackClick != null) "Go back" else null,
            showPrimaryAction = true,
            showSecondaryAction = onBackClick != null,
            style = RideitStatusCardStyle.DANGER
        ),
        onPrimaryClick = onRetryClick,
        onSecondaryClick = {
            onBackClick?.invoke()
        }
    )
}

@Composable
fun RideitNetworkErrorCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onRetryClick: () -> Unit = {},
    onOfflineModeClick: (() -> Unit)? = null
) {
    RideitPremiumStatusCard(
        modifier = modifier,
        compact = compact,
        status = RideitStatusCardUiModel(
            type = RideitStatusCardType.NETWORK,
            title = "Network connection issue",
            message = "Rideit could not connect right now. Check your internet connection and try again.",
            primaryActionText = "Retry connection",
            secondaryActionText = if (onOfflineModeClick != null) "Continue offline" else null,
            showPrimaryAction = true,
            showSecondaryAction = onOfflineModeClick != null,
            style = RideitStatusCardStyle.WARNING
        ),
        onPrimaryClick = onRetryClick,
        onSecondaryClick = {
            onOfflineModeClick?.invoke()
        }
    )
}

@Composable
fun RideitFirebaseErrorCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    message: String = "Firebase request failed. Please retry.",
    onRetryClick: () -> Unit = {},
    onSupportClick: (() -> Unit)? = null
) {
    RideitPremiumStatusCard(
        modifier = modifier,
        compact = compact,
        status = RideitStatusCardUiModel(
            type = RideitStatusCardType.FIREBASE,
            title = "Rideit sync failed",
            message = message,
            primaryActionText = "Retry sync",
            secondaryActionText = if (onSupportClick != null) "Contact support" else null,
            showPrimaryAction = true,
            showSecondaryAction = onSupportClick != null,
            style = RideitStatusCardStyle.DANGER
        ),
        onPrimaryClick = onRetryClick,
        onSecondaryClick = {
            onSupportClick?.invoke()
        }
    )
}

@Composable
fun RideitPermissionStatusCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    title: String = "Permission needed",
    message: String = "Rideit needs this permission to continue safely.",
    primaryActionText: String = "Allow permission",
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: (() -> Unit)? = null
) {
    RideitPremiumStatusCard(
        modifier = modifier,
        compact = compact,
        status = RideitStatusCardUiModel(
            type = RideitStatusCardType.PERMISSION,
            title = title,
            message = message,
            primaryActionText = primaryActionText,
            secondaryActionText = if (onSecondaryClick != null) "Not now" else null,
            showPrimaryAction = true,
            showSecondaryAction = onSecondaryClick != null,
            style = RideitStatusCardStyle.PRIMARY
        ),
        onPrimaryClick = onPrimaryClick,
        onSecondaryClick = {
            onSecondaryClick?.invoke()
        }
    )
}

@Composable
fun RideitSuccessStatusCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    title: String = "Success",
    message: String = "Your Rideit action was completed successfully.",
    primaryActionText: String = "Done",
    onDoneClick: () -> Unit = {}
) {
    RideitPremiumStatusCard(
        modifier = modifier,
        compact = compact,
        status = RideitStatusCardUiModel(
            type = RideitStatusCardType.SUCCESS,
            title = title,
            message = message,
            primaryActionText = primaryActionText,
            showPrimaryAction = true,
            showSecondaryAction = false,
            style = RideitStatusCardStyle.SUCCESS
        ),
        onPrimaryClick = onDoneClick
    )
}

@Composable
fun RideitWarningStatusCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    title: String = "Check this first",
    message: String = "Please review this Rideit status before continuing.",
    primaryActionText: String = "Continue",
    secondaryActionText: String? = "Cancel",
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {}
) {
    RideitPremiumStatusCard(
        modifier = modifier,
        compact = compact,
        status = RideitStatusCardUiModel(
            type = RideitStatusCardType.WARNING,
            title = title,
            message = message,
            primaryActionText = primaryActionText,
            secondaryActionText = secondaryActionText,
            showPrimaryAction = true,
            showSecondaryAction = !secondaryActionText.isNullOrBlank(),
            style = RideitStatusCardStyle.WARNING
        ),
        onPrimaryClick = onPrimaryClick,
        onSecondaryClick = onSecondaryClick
    )
}

@Composable
fun RideitOfflineStatusCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onRetryClick: () -> Unit = {},
    onSettingsClick: (() -> Unit)? = null
) {
    RideitPremiumStatusCard(
        modifier = modifier,
        compact = compact,
        status = RideitStatusCardUiModel(
            type = RideitStatusCardType.OFFLINE,
            title = "You are offline",
            message = "Rideit needs internet to update trips, driver requests, and live ride status.",
            primaryActionText = "Retry",
            secondaryActionText = if (onSettingsClick != null) "Open settings" else null,
            showPrimaryAction = true,
            showSecondaryAction = onSettingsClick != null,
            style = RideitStatusCardStyle.NEUTRAL
        ),
        onPrimaryClick = onRetryClick,
        onSecondaryClick = {
            onSettingsClick?.invoke()
        }
    )
}

@Composable
fun RideitInlineStatusBanner(
    status: RideitInlineStatusUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    val alpha = if (status.enabled) 1f else 0.55f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = status.style.softBackgroundColor(),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .border(
                width = 1.dp,
                color = status.style.borderColor(),
                shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
            )
            .clickable(enabled = status.enabled) {
                onClick()
            }
            .graphicsLayer {
                this.alpha = alpha
            }
            .padding(if (compact) 13.dp else 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitStatusIcon(
            type = status.type,
            style = status.style,
            compact = true,
            pulse = status.type == RideitStatusCardType.ERROR ||
                    status.type == RideitStatusCardType.WARNING ||
                    status.type == RideitStatusCardType.NETWORK ||
                    status.type == RideitStatusCardType.FIREBASE,
            large = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 11.dp)
        ) {
            Text(
                text = status.title,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!status.message.isNullOrBlank()) {
                Text(
                    text = status.message,
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (!status.actionText.isNullOrBlank()) {
            Text(
                text = status.actionText,
                color = status.style.textColor(),
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.84f),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        color = status.style.borderColor(),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RideitAnimatedInlineStatusBanner(
    visible: Boolean,
    status: RideitInlineStatusUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
    ) {
        RideitInlineStatusBanner(
            status = status,
            modifier = modifier,
            compact = compact,
            onClick = onClick
        )
    }
}

@Composable
fun RideitStatusChip(
    type: RideitStatusCardType,
    modifier: Modifier = Modifier,
    text: String? = null,
    style: RideitStatusCardStyle = type.defaultStyle(),
    compact: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val alpha = if (enabled) 1f else 0.55f

    Row(
        modifier = modifier
            .background(
                color = style.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = style.borderColor(),
                shape = RoundedCornerShape(50)
            )
            .clickable(enabled = enabled) {
                onClick()
            }
            .graphicsLayer {
                this.alpha = alpha
            }
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
                    color = style.dotColor(),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.padding(start = 7.dp))

        Text(
            text = text ?: type.shortLabel(),
            color = style.textColor(),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitStatusList(
    statuses: List<RideitInlineStatusUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onStatusClick: (RideitInlineStatusUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)
    ) {
        statuses.forEach { status ->
            RideitInlineStatusBanner(
                status = status,
                compact = compact,
                onClick = {
                    onStatusClick(status)
                }
            )
        }
    }
}

@Composable
fun RideitScreenStatusHost(
    loading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    loadingContent: @Composable (() -> Unit)? = null,
    onRetryClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    when {
        loading -> {
            if (loadingContent != null) {
                loadingContent()
            } else {
                RideitPremiumLoadingCard(
                    loadingState = RideitLoadingStateUiModel(
                        title = "Loading Rideit",
                        message = "Please wait while Rideit prepares your screen...",
                        type = RideitLoadingSkeletonType.CUSTOM,
                        style = RideitLoadingSkeletonStyle.BLUE
                    ),
                    modifier = modifier,
                    compact = compact
                )
            }
        }

        !errorMessage.isNullOrBlank() -> {
            RideitErrorRetryCard(
                modifier = modifier,
                compact = compact,
                message = errorMessage,
                onRetryClick = onRetryClick
            )
        }

        else -> {
            content()
        }
    }
}

@Composable
private fun RideitStatusCardContainer(
    modifier: Modifier,
    compact: Boolean,
    style: RideitStatusCardStyle,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 16.dp else 22.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = style.dotColor().copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        style.borderColor().copy(alpha = 0.68f)
                    )
                ),
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp)
            ),
        shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(style.softBackgroundColor(), Color.White)
                )
            ),
            content = content
        )
    }
}

@Composable
private fun RideitStatusIcon(
    type: RideitStatusCardType,
    style: RideitStatusCardStyle,
    compact: Boolean,
    pulse: Boolean,
    large: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_status_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_status_icon_pulse_value"
    )

    val outerSize = when {
        large && compact -> 72.dp
        large -> 84.dp
        compact -> 38.dp
        else -> 44.dp
    }

    val innerSize = when {
        large && compact -> 32.dp
        large -> 40.dp
        compact -> 16.dp
        else -> 19.dp
    }

    Box(
        modifier = Modifier
            .size(outerSize)
            .background(
                color = style.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
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
                fontSize = when {
                    large && compact -> 13.sp
                    large -> 15.sp
                    compact -> 8.sp
                    else -> 9.sp
                },
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

fun rideitErrorStatus(
    message: String,
    title: String = "Something went wrong"
): RideitStatusCardUiModel {
    return RideitStatusCardUiModel(
        type = RideitStatusCardType.ERROR,
        title = title,
        message = message,
        primaryActionText = "Retry",
        showPrimaryAction = true,
        style = RideitStatusCardStyle.DANGER
    )
}

fun rideitNetworkStatus(): RideitStatusCardUiModel {
    return RideitStatusCardUiModel(
        type = RideitStatusCardType.NETWORK,
        title = "Network issue",
        message = "Please check your internet connection and try again.",
        primaryActionText = "Retry",
        showPrimaryAction = true,
        style = RideitStatusCardStyle.WARNING
    )
}

fun rideitSuccessInlineStatus(
    title: String,
    message: String? = null
): RideitInlineStatusUiModel {
    return RideitInlineStatusUiModel(
        title = title,
        message = message,
        type = RideitStatusCardType.SUCCESS,
        style = RideitStatusCardStyle.SUCCESS,
        actionText = null
    )
}

fun rideitErrorInlineStatus(
    title: String,
    message: String? = null,
    actionText: String? = "Retry"
): RideitInlineStatusUiModel {
    return RideitInlineStatusUiModel(
        title = title,
        message = message,
        type = RideitStatusCardType.ERROR,
        style = RideitStatusCardStyle.DANGER,
        actionText = actionText
    )
}

fun rideitWarningInlineStatus(
    title: String,
    message: String? = null,
    actionText: String? = null
): RideitInlineStatusUiModel {
    return RideitInlineStatusUiModel(
        title = title,
        message = message,
        type = RideitStatusCardType.WARNING,
        style = RideitStatusCardStyle.WARNING,
        actionText = actionText
    )
}

private fun RideitStatusCardType.defaultStyle(): RideitStatusCardStyle {
    return when (this) {
        RideitStatusCardType.ERROR,
        RideitStatusCardType.FIREBASE -> RideitStatusCardStyle.DANGER

        RideitStatusCardType.WARNING,
        RideitStatusCardType.NETWORK -> RideitStatusCardStyle.WARNING

        RideitStatusCardType.SUCCESS -> RideitStatusCardStyle.SUCCESS
        RideitStatusCardType.INFO,
        RideitStatusCardType.PERMISSION -> RideitStatusCardStyle.PRIMARY

        RideitStatusCardType.OFFLINE,
        RideitStatusCardType.EMPTY,
        RideitStatusCardType.CUSTOM -> RideitStatusCardStyle.NEUTRAL
    }
}

private fun RideitStatusCardType.shortLabel(): String {
    return when (this) {
        RideitStatusCardType.ERROR -> "Error"
        RideitStatusCardType.WARNING -> "Warning"
        RideitStatusCardType.SUCCESS -> "Success"
        RideitStatusCardType.INFO -> "Info"
        RideitStatusCardType.OFFLINE -> "Offline"
        RideitStatusCardType.NETWORK -> "Network"
        RideitStatusCardType.FIREBASE -> "Sync"
        RideitStatusCardType.PERMISSION -> "Permission"
        RideitStatusCardType.EMPTY -> "Empty"
        RideitStatusCardType.CUSTOM -> "Status"
    }
}

private fun RideitStatusCardType.iconText(): String {
    return when (this) {
        RideitStatusCardType.ERROR -> "!"
        RideitStatusCardType.WARNING -> "!"
        RideitStatusCardType.SUCCESS -> "✓"
        RideitStatusCardType.INFO -> "i"
        RideitStatusCardType.OFFLINE -> "•"
        RideitStatusCardType.NETWORK -> "N"
        RideitStatusCardType.FIREBASE -> "F"
        RideitStatusCardType.PERMISSION -> "P"
        RideitStatusCardType.EMPTY -> "Ø"
        RideitStatusCardType.CUSTOM -> "•"
    }
}

private fun RideitStatusCardStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitStatusCardStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitStatusCardStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitStatusCardStyle.WARNING -> Color(0xFFFEF3C7)
        RideitStatusCardStyle.DANGER -> Color(0xFFFEE2E2)
        RideitStatusCardStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitStatusCardStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitStatusCardStyle.borderColor(): Color {
    return when (this) {
        RideitStatusCardStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitStatusCardStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitStatusCardStyle.WARNING -> Color(0xFFFDE68A)
        RideitStatusCardStyle.DANGER -> Color(0xFFFCA5A5)
        RideitStatusCardStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitStatusCardStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitStatusCardStyle.dotColor(): Color {
    return when (this) {
        RideitStatusCardStyle.PRIMARY -> Color(0xFF2563EB)
        RideitStatusCardStyle.SUCCESS -> Color(0xFF22C55E)
        RideitStatusCardStyle.WARNING -> Color(0xFFF59E0B)
        RideitStatusCardStyle.DANGER -> Color(0xFFEF4444)
        RideitStatusCardStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitStatusCardStyle.NEUTRAL -> Color(0xFF64748B)
    }
}

private fun RideitStatusCardStyle.textColor(): Color {
    return when (this) {
        RideitStatusCardStyle.PRIMARY -> Color(0xFF2563EB)
        RideitStatusCardStyle.SUCCESS -> Color(0xFF166534)
        RideitStatusCardStyle.WARNING -> Color(0xFF92400E)
        RideitStatusCardStyle.DANGER -> Color(0xFFB91C1C)
        RideitStatusCardStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitStatusCardStyle.NEUTRAL -> Color(0xFF475569)
    }
}

private fun RideitStatusCardStyle.buttonColor(): Color {
    return when (this) {
        RideitStatusCardStyle.PRIMARY -> Color(0xFF2563EB)
        RideitStatusCardStyle.SUCCESS -> Color(0xFF16A34A)
        RideitStatusCardStyle.WARNING -> Color(0xFFF59E0B)
        RideitStatusCardStyle.DANGER -> Color(0xFFEF4444)
        RideitStatusCardStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitStatusCardStyle.NEUTRAL -> Color(0xFF0F172A)
    }
}