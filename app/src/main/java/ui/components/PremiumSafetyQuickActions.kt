package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PremiumSafetyQuickActions(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onSafetyClick: () -> Unit,
    onShareTripClick: () -> Unit,
    onSupportClick: () -> Unit,
    onEmergencyInfoClick: () -> Unit
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
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            shadowElevation = 14.dp,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
                horizontalAlignment = Alignment.End
            ) {
                PremiumQuickActionButton(
                    emoji = "🛡",
                    label = "Safety",
                    accentColor = Color(0xFF16A34A),
                    onClick = onSafetyClick
                )

                PremiumQuickActionButton(
                    emoji = "↗",
                    label = "Share",
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = onShareTripClick
                )

                PremiumQuickActionButton(
                    emoji = "?",
                    label = "Support",
                    accentColor = Color(0xFF2563EB),
                    onClick = onSupportClick
                )

                PremiumQuickActionButton(
                    emoji = "!",
                    label = "Emergency",
                    accentColor = Color(0xFFEF4444),
                    onClick = onEmergencyInfoClick
                )
            }
        }
    }
}

@Composable
private fun PremiumQuickActionButton(
    emoji: String,
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = accentColor.copy(alpha = 0.09f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.width(8.dp))

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }
        }
    }
}