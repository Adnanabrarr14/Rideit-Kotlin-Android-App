package com.example.rideit.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitLoadingSkeletonType {
    MAP,
    PROFILE,
    TRIP,
    HISTORY,
    DRIVER_REQUEST,
    EARNINGS,
    PAYMENT,
    SETTINGS,
    VEHICLE,
    CUSTOM
}

enum class RideitLoadingSkeletonStyle {
    LIGHT,
    BLUE,
    GREEN,
    PURPLE,
    DARK
}

@Immutable
data class RideitLoadingStateUiModel(
    val title: String = "Loading Rideit",
    val message: String = "Preparing your premium Rideit experience...",
    val type: RideitLoadingSkeletonType = RideitLoadingSkeletonType.CUSTOM,
    val style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.BLUE
)

@Composable
fun RideitPremiumLoadingCard(
    loadingState: RideitLoadingStateUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    showSpinner: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 16.dp else 22.dp,
                shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
                spotColor = Color.Black.copy(alpha = 0.16f)
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
            ),
        shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = loadingState.style.cardGradient()
                    )
                )
                .padding(if (compact) 16.dp else 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitLoadingPulseIcon(
                type = loadingState.type,
                style = loadingState.style,
                compact = compact,
                showSpinner = showSpinner
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = loadingState.title,
                    color = loadingState.style.titleColor(),
                    fontSize = if (compact) 15.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = loadingState.message,
                    color = loadingState.style.bodyColor(),
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
fun RideitAnimatedPremiumLoadingCard(
    visible: Boolean,
    loadingState: RideitLoadingStateUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    showSpinner: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        RideitPremiumLoadingCard(
            loadingState = loadingState,
            modifier = modifier,
            compact = compact,
            showSpinner = showSpinner
        )
    }
}

@Composable
fun RideitSkeletonBlock(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 16.dp,
    cornerRadius: Dp = 12.dp,
    style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.LIGHT
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_skeleton_block_alpha")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_skeleton_block_alpha_value"
    )

    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction.coerceIn(0.1f, 1f))
            .height(height)
            .graphicsLayer {
                this.alpha = alpha
            }
            .background(
                brush = Brush.linearGradient(
                    colors = style.skeletonColors()
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    )
}

@Composable
fun RideitSkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.LIGHT
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_skeleton_circle_alpha")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_skeleton_circle_alpha_value"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                this.alpha = alpha
            }
            .background(
                brush = Brush.linearGradient(
                    colors = style.skeletonColors()
                ),
                shape = CircleShape
            )
    )
}

@Composable
fun RideitTripSkeletonCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.LIGHT
) {
    RideitSkeletonCardContainer(
        modifier = modifier,
        compact = compact
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSkeletonCircle(
                size = if (compact) 46.dp else 54.dp,
                style = style
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                RideitSkeletonBlock(
                    widthFraction = 0.65f,
                    height = if (compact) 14.dp else 16.dp,
                    style = style
                )

                Spacer(modifier = Modifier.height(8.dp))

                RideitSkeletonBlock(
                    widthFraction = 0.45f,
                    height = if (compact) 11.dp else 12.dp,
                    style = style
                )
            }

            RideitSkeletonBlock(
                widthFraction = 0.18f,
                height = if (compact) 18.dp else 20.dp,
                style = style
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 14.dp else 16.dp))

        RideitSkeletonBlock(
            widthFraction = 1f,
            height = if (compact) 48.dp else 58.dp,
            cornerRadius = 18.dp,
            style = style
        )

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RideitSkeletonBlock(
                modifier = Modifier.weight(1f),
                widthFraction = 1f,
                height = if (compact) 34.dp else 40.dp,
                style = style
            )

            RideitSkeletonBlock(
                modifier = Modifier.weight(1f),
                widthFraction = 1f,
                height = if (compact) 34.dp else 40.dp,
                style = style
            )

            RideitSkeletonBlock(
                modifier = Modifier.weight(1f),
                widthFraction = 1f,
                height = if (compact) 34.dp else 40.dp,
                style = style
            )
        }
    }
}

@Composable
fun RideitProfileSkeletonCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.LIGHT
) {
    RideitSkeletonCardContainer(
        modifier = modifier,
        compact = compact
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSkeletonCircle(
                size = if (compact) 58.dp else 68.dp,
                style = style
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                RideitSkeletonBlock(
                    widthFraction = 0.62f,
                    height = if (compact) 16.dp else 18.dp,
                    style = style
                )

                Spacer(modifier = Modifier.height(8.dp))

                RideitSkeletonBlock(
                    widthFraction = 0.82f,
                    height = if (compact) 11.dp else 12.dp,
                    style = style
                )

                Spacer(modifier = Modifier.height(7.dp))

                RideitSkeletonBlock(
                    widthFraction = 0.42f,
                    height = if (compact) 10.dp else 11.dp,
                    style = style
                )
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 16.dp else 18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                RideitSkeletonBlock(
                    modifier = Modifier.weight(1f),
                    widthFraction = 1f,
                    height = if (compact) 42.dp else 48.dp,
                    style = style
                )
            }
        }
    }
}

@Composable
fun RideitMapSkeletonOverlay(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.BLUE
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)
    ) {
        RideitSkeletonCardContainer(
            compact = compact
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitSkeletonCircle(
                    size = if (compact) 42.dp else 48.dp,
                    style = style
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    RideitSkeletonBlock(
                        widthFraction = 0.45f,
                        height = if (compact) 14.dp else 16.dp,
                        style = style
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RideitSkeletonBlock(
                        widthFraction = 0.72f,
                        height = if (compact) 11.dp else 12.dp,
                        style = style
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                repeat(4) {
                    RideitSkeletonCircle(
                        size = if (compact) 44.dp else 50.dp,
                        style = style
                    )
                }
            }
        }

        RideitSkeletonCardContainer(
            compact = compact
        ) {
            RideitSkeletonBlock(
                widthFraction = 0.56f,
                height = if (compact) 16.dp else 18.dp,
                style = style
            )

            Spacer(modifier = Modifier.height(12.dp))

            RideitSkeletonBlock(
                widthFraction = 1f,
                height = if (compact) 44.dp else 52.dp,
                cornerRadius = 18.dp,
                style = style
            )

            Spacer(modifier = Modifier.height(10.dp))

            RideitSkeletonBlock(
                widthFraction = 0.82f,
                height = if (compact) 44.dp else 52.dp,
                cornerRadius = 18.dp,
                style = style
            )
        }
    }
}

@Composable
fun RideitDriverRequestSkeletonCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.LIGHT
) {
    RideitSkeletonCardContainer(
        modifier = modifier,
        compact = compact
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSkeletonCircle(
                size = if (compact) 48.dp else 56.dp,
                style = style
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                RideitSkeletonBlock(
                    widthFraction = 0.52f,
                    height = if (compact) 15.dp else 17.dp,
                    style = style
                )

                Spacer(modifier = Modifier.height(8.dp))

                RideitSkeletonBlock(
                    widthFraction = 0.70f,
                    height = if (compact) 11.dp else 12.dp,
                    style = style
                )
            }

            RideitSkeletonBlock(
                widthFraction = 0.20f,
                height = if (compact) 20.dp else 22.dp,
                style = style
            )
        }

        Spacer(modifier = Modifier.height(if (compact) 14.dp else 16.dp))

        RideitSkeletonBlock(
            widthFraction = 1f,
            height = if (compact) 52.dp else 60.dp,
            cornerRadius = 18.dp,
            style = style
        )

        Spacer(modifier = Modifier.height(if (compact) 12.dp else 14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RideitSkeletonBlock(
                modifier = Modifier.weight(1f),
                widthFraction = 1f,
                height = if (compact) 46.dp else 52.dp,
                style = style
            )

            RideitSkeletonBlock(
                modifier = Modifier.weight(1f),
                widthFraction = 1f,
                height = if (compact) 46.dp else 52.dp,
                style = style
            )
        }
    }
}

@Composable
fun RideitEarningsSkeletonCard(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.DARK
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
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(if (compact) 30.dp else 36.dp)
            ),
        shape = RoundedCornerShape(if (compact) 30.dp else 36.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF2563EB))
                    )
                )
                .padding(if (compact) 18.dp else 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RideitSkeletonCircle(
                    size = if (compact) 42.dp else 50.dp,
                    style = style
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    RideitSkeletonBlock(
                        widthFraction = 0.46f,
                        height = if (compact) 15.dp else 17.dp,
                        style = style
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RideitSkeletonBlock(
                        widthFraction = 0.30f,
                        height = if (compact) 11.dp else 12.dp,
                        style = style
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 18.dp else 22.dp))

            RideitSkeletonBlock(
                widthFraction = 0.58f,
                height = if (compact) 34.dp else 40.dp,
                style = style
            )

            Spacer(modifier = Modifier.height(if (compact) 16.dp else 18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    RideitSkeletonBlock(
                        modifier = Modifier.weight(1f),
                        widthFraction = 1f,
                        height = if (compact) 44.dp else 50.dp,
                        style = style
                    )
                }
            }
        }
    }
}

@Composable
fun RideitSkeletonList(
    count: Int,
    modifier: Modifier = Modifier,
    type: RideitLoadingSkeletonType = RideitLoadingSkeletonType.TRIP,
    compact: Boolean = true,
    style: RideitLoadingSkeletonStyle = RideitLoadingSkeletonStyle.LIGHT
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)
    ) {
        repeat(count.coerceAtLeast(0)) {
            when (type) {
                RideitLoadingSkeletonType.PROFILE -> {
                    RideitProfileSkeletonCard(
                        compact = compact,
                        style = style
                    )
                }

                RideitLoadingSkeletonType.DRIVER_REQUEST -> {
                    RideitDriverRequestSkeletonCard(
                        compact = compact,
                        style = style
                    )
                }

                RideitLoadingSkeletonType.EARNINGS -> {
                    RideitEarningsSkeletonCard(
                        compact = compact
                    )
                }

                else -> {
                    RideitTripSkeletonCard(
                        compact = compact,
                        style = style
                    )
                }
            }
        }
    }
}

@Composable
fun RideitLoadingStateContainer(
    loading: Boolean,
    modifier: Modifier = Modifier,
    loadingState: RideitLoadingStateUiModel = RideitLoadingStateUiModel(),
    skeletonType: RideitLoadingSkeletonType = RideitLoadingSkeletonType.TRIP,
    skeletonCount: Int = 3,
    compact: Boolean = true,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = loading,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 14.dp)
        ) {
            RideitPremiumLoadingCard(
                loadingState = loadingState,
                compact = compact
            )

            RideitSkeletonList(
                count = skeletonCount,
                type = skeletonType,
                compact = compact
            )
        }
    }

    AnimatedVisibility(
        visible = !loading,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        content()
    }
}

@Composable
private fun RideitSkeletonCardContainer(
    modifier: Modifier = Modifier,
    compact: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 12.dp else 16.dp,
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.62f),
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp)
            ),
        shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
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
                .padding(if (compact) 16.dp else 20.dp),
            content = content
        )
    }
}

@Composable
private fun RideitLoadingPulseIcon(
    type: RideitLoadingSkeletonType,
    style: RideitLoadingSkeletonStyle,
    compact: Boolean,
    showSpinner: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_loading_pulse_icon")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_loading_pulse_icon_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 46.dp else 54.dp)
            .background(
                color = style.iconBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showSpinner) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (compact) 24.dp else 28.dp),
                strokeWidth = 3.dp,
                color = style.iconColor()
            )
        } else {
            Box(
                modifier = Modifier
                    .size(if (compact) 20.dp else 24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .background(
                        color = style.iconColor(),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = type.iconText(),
                    color = Color.White,
                    fontSize = if (compact) 8.sp else 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun rideitLoadingStateForType(
    type: RideitLoadingSkeletonType
): RideitLoadingStateUiModel {
    return when (type) {
        RideitLoadingSkeletonType.MAP -> RideitLoadingStateUiModel(
            title = "Loading map",
            message = "Preparing location, route, and Rideit map controls...",
            type = type,
            style = RideitLoadingSkeletonStyle.BLUE
        )

        RideitLoadingSkeletonType.PROFILE -> RideitLoadingStateUiModel(
            title = "Loading profile",
            message = "Getting your Rideit profile and stats ready...",
            type = type,
            style = RideitLoadingSkeletonStyle.PURPLE
        )

        RideitLoadingSkeletonType.TRIP -> RideitLoadingStateUiModel(
            title = "Loading trip",
            message = "Checking your latest Rideit trip status...",
            type = type,
            style = RideitLoadingSkeletonStyle.BLUE
        )

        RideitLoadingSkeletonType.HISTORY -> RideitLoadingStateUiModel(
            title = "Loading history",
            message = "Fetching your trips, receipts, and ride summaries...",
            type = type,
            style = RideitLoadingSkeletonStyle.LIGHT
        )

        RideitLoadingSkeletonType.DRIVER_REQUEST -> RideitLoadingStateUiModel(
            title = "Loading requests",
            message = "Finding rider requests near your driver location...",
            type = type,
            style = RideitLoadingSkeletonStyle.BLUE
        )

        RideitLoadingSkeletonType.EARNINGS -> RideitLoadingStateUiModel(
            title = "Loading earnings",
            message = "Preparing driver earnings, payouts, and trip stats...",
            type = type,
            style = RideitLoadingSkeletonStyle.DARK
        )

        RideitLoadingSkeletonType.PAYMENT -> RideitLoadingStateUiModel(
            title = "Loading payment",
            message = "Preparing fare, receipt, and payment information...",
            type = type,
            style = RideitLoadingSkeletonStyle.GREEN
        )

        RideitLoadingSkeletonType.SETTINGS -> RideitLoadingStateUiModel(
            title = "Loading settings",
            message = "Preparing your Rideit preferences...",
            type = type,
            style = RideitLoadingSkeletonStyle.PURPLE
        )

        RideitLoadingSkeletonType.VEHICLE -> RideitLoadingStateUiModel(
            title = "Loading vehicle",
            message = "Checking vehicle details and driver availability...",
            type = type,
            style = RideitLoadingSkeletonStyle.BLUE
        )

        RideitLoadingSkeletonType.CUSTOM -> RideitLoadingStateUiModel()
    }
}

private fun RideitLoadingSkeletonType.iconText(): String {
    return when (this) {
        RideitLoadingSkeletonType.MAP -> "M"
        RideitLoadingSkeletonType.PROFILE -> "P"
        RideitLoadingSkeletonType.TRIP -> "R"
        RideitLoadingSkeletonType.HISTORY -> "H"
        RideitLoadingSkeletonType.DRIVER_REQUEST -> "D"
        RideitLoadingSkeletonType.EARNINGS -> "Rs"
        RideitLoadingSkeletonType.PAYMENT -> "Rs"
        RideitLoadingSkeletonType.SETTINGS -> "S"
        RideitLoadingSkeletonType.VEHICLE -> "V"
        RideitLoadingSkeletonType.CUSTOM -> "•"
    }
}

private fun RideitLoadingSkeletonStyle.cardGradient(): List<Color> {
    return when (this) {
        RideitLoadingSkeletonStyle.LIGHT -> listOf(Color.White, Color(0xFFF8FAFC))
        RideitLoadingSkeletonStyle.BLUE -> listOf(Color(0xFFEFF6FF), Color.White)
        RideitLoadingSkeletonStyle.GREEN -> listOf(Color(0xFFF0FDF4), Color.White)
        RideitLoadingSkeletonStyle.PURPLE -> listOf(Color(0xFFF3E8FF), Color.White)
        RideitLoadingSkeletonStyle.DARK -> listOf(Color(0xFF0F172A), Color(0xFF2563EB))
    }
}

private fun RideitLoadingSkeletonStyle.skeletonColors(): List<Color> {
    return when (this) {
        RideitLoadingSkeletonStyle.LIGHT -> listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF8FAFC),
            Color(0xFFE2E8F0)
        )

        RideitLoadingSkeletonStyle.BLUE -> listOf(
            Color(0xFFBFDBFE),
            Color(0xFFEFF6FF),
            Color(0xFFBFDBFE)
        )

        RideitLoadingSkeletonStyle.GREEN -> listOf(
            Color(0xFFBBF7D0),
            Color(0xFFF0FDF4),
            Color(0xFFBBF7D0)
        )

        RideitLoadingSkeletonStyle.PURPLE -> listOf(
            Color(0xFFD8B4FE),
            Color(0xFFF3E8FF),
            Color(0xFFD8B4FE)
        )

        RideitLoadingSkeletonStyle.DARK -> listOf(
            Color.White.copy(alpha = 0.18f),
            Color.White.copy(alpha = 0.34f),
            Color.White.copy(alpha = 0.18f)
        )
    }
}

private fun RideitLoadingSkeletonStyle.iconBackgroundColor(): Color {
    return when (this) {
        RideitLoadingSkeletonStyle.LIGHT -> Color(0xFFF8FAFC)
        RideitLoadingSkeletonStyle.BLUE -> Color(0xFFEFF6FF)
        RideitLoadingSkeletonStyle.GREEN -> Color(0xFFDCFCE7)
        RideitLoadingSkeletonStyle.PURPLE -> Color(0xFFF3E8FF)
        RideitLoadingSkeletonStyle.DARK -> Color.White.copy(alpha = 0.18f)
    }
}

private fun RideitLoadingSkeletonStyle.iconColor(): Color {
    return when (this) {
        RideitLoadingSkeletonStyle.LIGHT -> Color(0xFF64748B)
        RideitLoadingSkeletonStyle.BLUE -> Color(0xFF2563EB)
        RideitLoadingSkeletonStyle.GREEN -> Color(0xFF16A34A)
        RideitLoadingSkeletonStyle.PURPLE -> Color(0xFF7C3AED)
        RideitLoadingSkeletonStyle.DARK -> Color.White
    }
}

private fun RideitLoadingSkeletonStyle.titleColor(): Color {
    return when (this) {
        RideitLoadingSkeletonStyle.DARK -> Color.White
        else -> Color(0xFF0F172A)
    }
}

private fun RideitLoadingSkeletonStyle.bodyColor(): Color {
    return when (this) {
        RideitLoadingSkeletonStyle.DARK -> Color.White.copy(alpha = 0.78f)
        else -> Color(0xFF64748B)
    }
}