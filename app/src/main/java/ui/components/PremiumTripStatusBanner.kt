package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class RideitTripStatus {
    Idle,
    SearchingDriver,
    DriverFound,
    DriverArriving,
    TripInProgress,
    TripCompleted,
    Cancelled
}

@Composable
fun PremiumTripStatusBanner(
    status: RideitTripStatus,
    modifier: Modifier = Modifier,
    driverName: String? = null,
    etaText: String? = null,
    vehicleText: String? = null
) {
    AnimatedVisibility(
        visible = status != RideitTripStatus.Idle,
        enter = fadeIn(animationSpec = tween(280)) + expandVertically(
            animationSpec = tween(280),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(animationSpec = tween(220)) + shrinkVertically(
            animationSpec = tween(220),
            shrinkTowards = Alignment.Top
        ),
        modifier = modifier
    ) {
        val bannerData = rememberBannerData(
            status = status,
            driverName = driverName,
            etaText = etaText,
            vehicleText = vehicleText
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumStatusEmoji(
                        emoji = bannerData.emoji,
                        emojiColor = bannerData.emojiTint,
                        backgroundColor = bannerData.emojiBackground,
                        animated = bannerData.animated
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = bannerData.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = bannerData.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (bannerData.trailingText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bannerData.trailingText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (bannerData.showProgress) {
                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
                    )
                }

                if (bannerData.bottomText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            text = bannerData.bottomText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumStatusEmoji(
    emoji: String,
    emojiColor: Color,
    backgroundColor: Color,
    animated: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_emoji_pulse")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "status_emoji_alpha"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            color = emojiColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.alpha(if (animated) pulseAlpha else 1f)
        )
    }
}

@Composable
private fun rememberBannerData(
    status: RideitTripStatus,
    driverName: String?,
    etaText: String?,
    vehicleText: String?
): PremiumBannerData {
    val primary = MaterialTheme.colorScheme.primary
    val success = Color(0xFF16A34A)
    val error = MaterialTheme.colorScheme.error

    return when (status) {
        RideitTripStatus.Idle -> PremiumBannerData(
            title = "",
            subtitle = "",
            trailingText = "",
            bottomText = "",
            emoji = "•",
            emojiTint = primary,
            emojiBackground = primary.copy(alpha = 0.12f),
            showProgress = false,
            animated = false
        )

        RideitTripStatus.SearchingDriver -> PremiumBannerData(
            title = "Finding your driver",
            subtitle = "Searching for the best nearby driver for your ride.",
            trailingText = "Live",
            bottomText = "We are matching you with a verified Rideit driver.",
            emoji = "⌕",
            emojiTint = primary,
            emojiBackground = primary.copy(alpha = 0.13f),
            showProgress = true,
            animated = true
        )

        RideitTripStatus.DriverFound -> PremiumBannerData(
            title = "Driver found",
            subtitle = buildString {
                append(driverName ?: "Your driver")
                append(" accepted your ride")
                if (!vehicleText.isNullOrBlank()) {
                    append(" • ")
                    append(vehicleText)
                }
            },
            trailingText = etaText ?: "Soon",
            bottomText = "Driver details are verified for a safer pickup.",
            emoji = "✓",
            emojiTint = success,
            emojiBackground = success.copy(alpha = 0.13f),
            showProgress = false,
            animated = false
        )

        RideitTripStatus.DriverArriving -> PremiumBannerData(
            title = "Driver is arriving",
            subtitle = buildString {
                append(driverName ?: "Your driver")
                append(" is heading to your pickup location.")
            },
            trailingText = etaText ?: "3 min",
            bottomText = "Please stay near your pickup point.",
            emoji = "→",
            emojiTint = primary,
            emojiBackground = primary.copy(alpha = 0.13f),
            showProgress = true,
            animated = true
        )

        RideitTripStatus.TripInProgress -> PremiumBannerData(
            title = "Trip in progress",
            subtitle = "Relax and enjoy your Rideit journey.",
            trailingText = "Active",
            bottomText = "Your route is being tracked for safety.",
            emoji = "↗",
            emojiTint = primary,
            emojiBackground = primary.copy(alpha = 0.13f),
            showProgress = true,
            animated = false
        )

        RideitTripStatus.TripCompleted -> PremiumBannerData(
            title = "Trip completed",
            subtitle = "Thanks for riding with Rideit.",
            trailingText = "Done",
            bottomText = "Your receipt and trip history have been updated.",
            emoji = "✓",
            emojiTint = success,
            emojiBackground = success.copy(alpha = 0.13f),
            showProgress = false,
            animated = false
        )

        RideitTripStatus.Cancelled -> PremiumBannerData(
            title = "Ride cancelled",
            subtitle = "Your ride request has been cancelled.",
            trailingText = "",
            bottomText = "You can request another ride anytime.",
            emoji = "×",
            emojiTint = error,
            emojiBackground = error.copy(alpha = 0.12f),
            showProgress = false,
            animated = false
        )
    }
}

private data class PremiumBannerData(
    val title: String,
    val subtitle: String,
    val trailingText: String,
    val bottomText: String,
    val emoji: String,
    val emojiTint: Color,
    val emojiBackground: Color,
    val showProgress: Boolean,
    val animated: Boolean
)