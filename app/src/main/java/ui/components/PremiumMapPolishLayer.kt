package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PremiumMapPolishLayer(
    visible: Boolean,
    isMapMoving: Boolean,
    hasRoute: Boolean,
    hasDriver: Boolean,
    tripStatusText: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(160)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PremiumMapMovingFocus(
                visible = isMapMoving,
                modifier = Modifier.align(Alignment.Center)
            )

            PremiumLiveMapChip(
                visible = hasRoute || hasDriver || isMapMoving,
                isMapMoving = isMapMoving,
                hasRoute = hasRoute,
                hasDriver = hasDriver,
                tripStatusText = tripStatusText,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, bottom = 112.dp)
            )
        }
    }
}

@Composable
private fun PremiumMapMovingFocus(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(180)) + scaleIn(
            animationSpec = tween(220),
            initialScale = 0.86f
        ),
        exit = fadeOut(animationSpec = tween(160)) + scaleOut(
            animationSpec = tween(160),
            targetScale = 0.86f
        ),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "map_focus_pulse")

        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "map_focus_scale"
        )

        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.80f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "map_focus_alpha"
        )

        Box(
            modifier = Modifier.size(82.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun PremiumLiveMapChip(
    visible: Boolean,
    isMapMoving: Boolean,
    hasRoute: Boolean,
    hasDriver: Boolean,
    tripStatusText: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) + scaleIn(
            animationSpec = tween(220),
            initialScale = 0.94f
        ),
        exit = fadeOut(animationSpec = tween(160)) + scaleOut(
            animationSpec = tween(160),
            targetScale = 0.94f
        ),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.wrapContentWidth(),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            shadowElevation = 12.dp,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                PremiumPulseDot(
                    active = hasDriver || hasRoute || isMapMoving
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when {
                        isMapMoving -> "Exploring map"
                        hasDriver -> tripStatusText
                        hasRoute -> "Route optimized"
                        else -> "Map ready"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 1
                )

                if (hasRoute || hasDriver) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumPulseDot(
    active: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_dot_pulse")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 850,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    Box(
        modifier = Modifier
            .size(9.dp)
            .alpha(if (active) alpha else 1f)
            .background(
                color = if (active) Color(0xFF16A34A) else Color(0xFF9CA3AF),
                shape = CircleShape
            )
    )
}