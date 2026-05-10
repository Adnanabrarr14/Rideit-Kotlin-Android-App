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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun RideitTripRatingCard(
    visible: Boolean,
    modifier: Modifier = Modifier,
    isDriverMode: Boolean = false,
    personName: String? = null,
    initialRating: Int = 5,
    initialComment: String = "",
    loading: Boolean = false,
    showSkipButton: Boolean = true,
    onSubmitRating: (rating: Int, comment: String) -> Unit,
    onSkip: () -> Unit = {}
) {
    var selectedRating by remember(initialRating) {
        mutableIntStateOf(initialRating.coerceIn(1, 5))
    }

    var comment by remember(initialComment) {
        mutableStateOf(initialComment)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(260)) +
                expandVertically(animationSpec = tween(280)),
        exit = fadeOut(animationSpec = tween(180)) +
                shrinkVertically(animationSpec = tween(220))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(36.dp),
                    spotColor = Color.Black.copy(alpha = 0.22f)
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
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF8FAFC)
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RideitRatingHeaderIcon(
                    rating = selectedRating
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isDriverMode) {
                        "Rate your rider"
                    } else {
                        "Rate your driver"
                    },
                    color = Color(0xFF0F172A),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = buildRatingSubtitle(
                        isDriverMode = isDriverMode,
                        personName = personName
                    ),
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                RideitStarRatingSelector(
                    rating = selectedRating,
                    enabled = !loading,
                    onRatingChange = { selectedRating = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = selectedRating.ratingLabel(),
                    color = selectedRating.ratingColor(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = {
                        if (it.length <= 180) comment = it
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    minLines = 3,
                    maxLines = 4,
                    placeholder = {
                        Text(
                            text = if (isDriverMode) {
                                "Add a short note about this rider..."
                            } else {
                                "Add a short note about your driver..."
                            },
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        disabledBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color(0xFFF8FAFC),
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        disabledTextColor = Color(0xFF64748B),
                        cursorColor = Color(0xFF2563EB)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${comment.length}/180",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!loading) {
                            onSubmitRating(selectedRating, comment.trim())
                        }
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE2E8F0),
                        disabledContentColor = Color(0xFF94A3B8)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.size(10.dp))

                        Text(
                            text = "Submitting...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Text(
                            text = "Submit rating",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (showSkipButton) {
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            if (!loading) onSkip()
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Skip for now",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RideitCompactRatingPreviewCard(
    modifier: Modifier = Modifier,
    rating: Int,
    title: String = "Trip rating",
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.72f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitRatingMiniIcon(rating = rating)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    color = Color(0xFF0F172A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle ?: rating.coerceIn(1, 5).ratingLabel(),
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${rating.coerceIn(1, 5)}.0",
                color = rating.coerceIn(1, 5).ratingColor(),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun RideitStarRatingSelector(
    rating: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val starNumber = index + 1
            val selected = starNumber <= rating

            RideitRatingStar(
                selected = selected,
                enabled = enabled,
                onClick = {
                    onRatingChange(starNumber)
                }
            )
        }
    }
}

@Composable
private fun RideitRatingStar(
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_rating_star_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_rating_star_pulse_value"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(45.dp)
            .graphicsLayer {
                scaleX = if (selected) pulse else 1f
                scaleY = if (selected) pulse else 1f
                alpha = if (enabled) 1f else 0.55f
            }
            .background(
                color = if (selected) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFFFDE68A) else Color(0xFFE2E8F0),
                shape = CircleShape
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "★",
            color = if (selected) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RideitRatingHeaderIcon(
    rating: Int
) {
    val safeRating = rating.coerceIn(1, 5)

    val infiniteTransition = rememberInfiniteTransition(label = "rideit_rating_header_pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_rating_header_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(76.dp)
            .background(
                color = safeRating.ratingColor().copy(alpha = 0.14f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .background(
                    color = safeRating.ratingColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = safeRating.toString(),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun RideitRatingMiniIcon(
    rating: Int
) {
    val safeRating = rating.coerceIn(1, 5)

    Box(
        modifier = Modifier
            .size(46.dp)
            .background(
                color = safeRating.ratingColor().copy(alpha = 0.14f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    color = safeRating.ratingColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = safeRating.toString(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private fun buildRatingSubtitle(
    isDriverMode: Boolean,
    personName: String?
): String {
    val cleanName = personName?.trim().orEmpty()

    return when {
        isDriverMode && cleanName.isNotBlank() -> "How was your ride with $cleanName?"
        !isDriverMode && cleanName.isNotBlank() -> "How was your trip with $cleanName?"
        isDriverMode -> "Help Rideit keep the rider experience safe and respectful."
        else -> "Help Rideit keep driver quality high for every trip."
    }
}

private fun Int.ratingLabel(): String {
    return when (this.coerceIn(1, 5)) {
        1 -> "Poor experience"
        2 -> "Could be better"
        3 -> "Good ride"
        4 -> "Great ride"
        else -> "Excellent ride"
    }
}

private fun Int.ratingColor(): Color {
    return when (this.coerceIn(1, 5)) {
        1 -> Color(0xFFEF4444)
        2 -> Color(0xFFF97316)
        3 -> Color(0xFFF59E0B)
        4 -> Color(0xFF2563EB)
        else -> Color(0xFF16A34A)
    }
}