package com.example.rideit.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rideit.isRideitRoseTheme

@Immutable
private data class AccountTypeThemeColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val cardBorder: Color,
    val primary: Color,
    val riderAccent: Color,
    val driverAccent: Color,
    val text: Color,
    val subText: Color,
    val mutedText: Color,
    val onPrimary: Color,
    val choiceCard: Color,
    val choiceCardAlt: Color
)

@Composable
fun AccountTypeScreen(
    onRiderLoginClick: () -> Unit,
    onDriverLoginClick: () -> Unit
) {
    val colors = rememberAccountTypeThemeColors()

    val infiniteTransition = rememberInfiniteTransition(label = "rideit_logo_animation")

    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundTop)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(620.dp)
                .border(
                    width = 1.dp,
                    color = colors.cardBorder,
                    shape = RoundedCornerShape(42.dp)
                ),
            shape = RoundedCornerShape(42.dp),
            color = Color.Transparent,
            shadowElevation = 24.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.backgroundTop,
                                colors.backgroundMiddle,
                                colors.backgroundBottom,
                                colors.backgroundTop
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(260.dp)
                        .blur(22.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.primary.copy(alpha = 0.48f),
                                    colors.riderAccent.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(230.dp)
                        .blur(18.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.riderAccent.copy(alpha = 0.30f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 42.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedRideitLogo(
                        glowScale = glowScale,
                        glowAlpha = glowAlpha,
                        ringScale = ringScale,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "Welcome to\nRideit",
                        color = colors.text,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Choose how you want to continue",
                        color = colors.subText,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(38.dp))

                    AccountChoiceCard(
                        icon = "🚕",
                        title = "Rider Login",
                        badge = "User",
                        subtitle = "Book rides, track drivers\nand manage payments.",
                        accentColor = colors.riderAccent,
                        colors = colors,
                        onClick = onRiderLoginClick
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AccountChoiceCard(
                        icon = "🚘",
                        title = "Driver Login",
                        badge = "Driver",
                        subtitle = "Go online, accept rides\nand view earnings.",
                        accentColor = colors.driverAccent,
                        colors = colors,
                        onClick = onDriverLoginClick
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Secure Rideit access",
                        color = colors.mutedText,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedRideitLogo(
    glowScale: Float,
    glowAlpha: Float,
    ringScale: Float,
    colors: AccountTypeThemeColors
) {
    Box(
        modifier = Modifier.size(132.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(126.dp)
                .scale(glowScale)
                .blur(18.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = glowAlpha),
                            colors.riderAccent.copy(alpha = glowAlpha * 0.45f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(106.dp)
                .scale(ringScale)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            colors.primary,
                            colors.riderAccent,
                            colors.driverAccent,
                            colors.primary
                        )
                    ),
                    shape = CircleShape
                )
        )

        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = 24.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colors.driverAccent,
                                colors.primary,
                                colors.riderAccent
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.18f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}

@Composable
private fun AccountChoiceCard(
    icon: String,
    title: String,
    badge: String,
    subtitle: String,
    accentColor: Color,
    colors: AccountTypeThemeColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(98.dp)
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = colors.cardBorder,
                shape = RoundedCornerShape(26.dp)
            ),
        shape = RoundedCornerShape(26.dp),
        color = colors.choiceCard,
        shadowElevation = 14.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.45f),
                                colors.choiceCardAlt
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = colors.text,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = accentColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            color = accentColor,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = colors.subText,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "›",
                color = accentColor,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun rememberAccountTypeThemeColors(): AccountTypeThemeColors {
    val scheme = MaterialTheme.colorScheme

    val isRoseTheme = scheme.isRideitRoseTheme()
    val isLightTheme = scheme.background.luminance() > 0.5f

    return remember(scheme.primary, scheme.background) {
        when {
            isRoseTheme -> AccountTypeThemeColors(
                backgroundTop = Color(0xFFFFF7FB),
                backgroundMiddle = Color(0xFFFFEAF3),
                backgroundBottom = Color(0xFFFFFBFD),
                card = Color.White,
                cardBorder = Color(0xFFF9A8D4),
                primary = Color(0xFFFF5CA8),
                riderAccent = Color(0xFFEC4899),
                driverAccent = Color(0xFFFF7ABC),
                text = Color(0xFF24111A),
                subText = Color(0xFF7A445A),
                mutedText = Color(0xFF9D5570),
                onPrimary = Color.White,
                choiceCard = Color.White,
                choiceCardAlt = Color(0xFFFFEAF3)
            )

            isLightTheme -> AccountTypeThemeColors(
                backgroundTop = Color(0xFFF8FAFC),
                backgroundMiddle = Color(0xFFEDE9FE),
                backgroundBottom = Color.White,
                card = Color.White,
                cardBorder = Color(0xFFE5E7EB),
                primary = Color(0xFF8A35F2),
                riderAccent = Color(0xFFFF1212),
                driverAccent = Color(0xFF8A35F2),
                text = Color(0xFF111827),
                subText = Color(0xFF6B7280),
                mutedText = Color(0xFF9CA3AF),
                onPrimary = Color.White,
                choiceCard = Color.White,
                choiceCardAlt = Color(0xFFEBDDFF)
            )

            else -> AccountTypeThemeColors(
                backgroundTop = Color(0xFF030307),
                backgroundMiddle = Color(0xFF160522),
                backgroundBottom = Color(0xFF21000A),
                card = Color(0xFF101019),
                cardBorder = Color.White.copy(alpha = 0.08f),
                primary = Color(0xFF8A35F2),
                riderAccent = Color(0xFFFF1212),
                driverAccent = Color(0xFF8A35F2),
                text = Color.White,
                subText = Color(0xFFB8B4C6),
                mutedText = Color.White.copy(alpha = 0.22f),
                onPrimary = Color.White,
                choiceCard = Color(0xFF101019).copy(alpha = 0.92f),
                choiceCardAlt = Color(0xFF1A1222)
            )
        }
    }
}
