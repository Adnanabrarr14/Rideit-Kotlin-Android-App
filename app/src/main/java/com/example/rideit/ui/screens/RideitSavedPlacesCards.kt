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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitSavedPlaceType {
    HOME,
    WORK,
    AIRPORT,
    RECENT,
    FAVORITE,
    CUSTOM,
    ADD
}

@Immutable
data class RideitSavedPlaceUiModel(
    val id: String = "",
    val title: String,
    val address: String? = null,
    val distanceText: String? = null,
    val type: RideitSavedPlaceType = RideitSavedPlaceType.CUSTOM,
    val isDefault: Boolean = false
)

@Composable
fun RideitSavedPlacesSection(
    places: List<RideitSavedPlaceUiModel>,
    modifier: Modifier = Modifier,
    title: String = "Saved places",
    subtitle: String = "Quick access to your favorite destinations",
    compact: Boolean = false,
    showAddPlace: Boolean = true,
    onPlaceClick: (RideitSavedPlaceUiModel) -> Unit = {},
    onAddPlaceClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        RideitSavedPlacesHeader(
            title = title,
            subtitle = subtitle,
            count = places.size,
            compact = compact
        )

        Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

        if (places.isEmpty() && !showAddPlace) {
            RideitSavedPlacesEmptyCard(
                compact = compact,
                onAddClick = onAddPlaceClick
            )
        } else {
            RideitSavedPlacesList(
                places = places,
                compact = compact,
                showAddPlace = showAddPlace,
                onPlaceClick = onPlaceClick,
                onAddPlaceClick = onAddPlaceClick
            )
        }
    }
}

@Composable
fun RideitSavedPlacesList(
    places: List<RideitSavedPlaceUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showAddPlace: Boolean = true,
    onPlaceClick: (RideitSavedPlaceUiModel) -> Unit = {},
    onAddPlaceClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
    ) {
        places.forEach { place ->
            RideitSavedPlaceCard(
                place = place,
                compact = compact,
                onClick = {
                    onPlaceClick(place)
                }
            )
        }

        if (showAddPlace) {
            RideitAddSavedPlaceCard(
                compact = compact,
                onClick = onAddPlaceClick
            )
        }

        if (places.isEmpty() && showAddPlace) {
            RideitSavedPlacesTipCard(
                compact = compact
            )
        }
    }
}

@Composable
fun RideitSavedPlaceCard(
    place: RideitSavedPlaceUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 10.dp else 14.dp,
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
                        colors = listOf(
                            Color.White,
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(if (compact) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSavedPlaceIcon(
                type = place.type,
                compact = compact,
                pulse = place.isDefault
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = place.title,
                        color = Color(0xFF0F172A),
                        fontSize = if (compact) 14.sp else 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (place.isDefault) {
                        Spacer(modifier = Modifier.width(7.dp))

                        RideitSavedPlaceMiniBadge(
                            text = "Default",
                            compact = compact
                        )
                    }
                }

                Text(
                    text = place.address?.takeIf { it.isNotBlank() }
                        ?: place.type.defaultAddressHint(),
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = if (compact) 16.sp else 18.sp,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!place.distanceText.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .background(
                            color = place.type.softBackgroundColor(),
                            shape = RoundedCornerShape(50)
                        )
                        .border(
                            width = 1.dp,
                            color = place.type.borderColor(),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = place.distanceText,
                        color = place.type.textColor(),
                        fontSize = 10.sp,
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
fun RideitSavedPlacesHorizontalChips(
    places: List<RideitSavedPlaceUiModel>,
    modifier: Modifier = Modifier,
    showAddChip: Boolean = true,
    compact: Boolean = true,
    onPlaceClick: (RideitSavedPlaceUiModel) -> Unit = {},
    onAddPlaceClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        places.forEach { place ->
            RideitSavedPlaceChip(
                place = place,
                compact = compact,
                onClick = {
                    onPlaceClick(place)
                }
            )
        }

        if (showAddChip) {
            RideitAddSavedPlaceChip(
                compact = compact,
                onClick = onAddPlaceClick
            )
        }
    }
}

@Composable
fun RideitSavedPlaceChip(
    place: RideitSavedPlaceUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(
                color = place.type.softBackgroundColor(),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = place.type.borderColor(),
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
                .size(if (compact) 22.dp else 24.dp)
                .background(
                    color = place.type.dotColor(),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = place.type.iconText(),
                color = Color.White,
                fontSize = if (compact) 8.sp else 9.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = place.title,
            color = place.type.textColor(),
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RideitAddSavedPlaceCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    title: String = "Add saved place",
    message: String = "Save home, work, or favorite destinations for faster booking.",
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 10.dp else 14.dp,
                shape = RoundedCornerShape(if (compact) 24.dp else 28.dp),
                spotColor = Color.Black.copy(alpha = 0.11f)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFBFDBFE),
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
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
                .padding(if (compact) 14.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSavedPlaceIcon(
                type = RideitSavedPlaceType.ADD,
                compact = compact,
                pulse = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 13.dp)
            ) {
                Text(
                    text = title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 14.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = message,
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = if (compact) 16.sp else 18.sp,
                    modifier = Modifier.padding(top = 3.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            RideitSavedPlaceMiniBadge(
                text = "Add",
                compact = compact
            )
        }
    }
}

@Composable
fun RideitAddSavedPlaceChip(
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .background(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(50)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFBFDBFE),
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
                .size(if (compact) 22.dp else 24.dp)
                .background(
                    color = Color(0xFF2563EB),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Add place",
            color = Color(0xFF2563EB),
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun RideitSavedPlacesQuickPanel(
    visible: Boolean,
    places: List<RideitSavedPlaceUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onPlaceClick: (RideitSavedPlaceUiModel) -> Unit = {},
    onAddPlaceClick: () -> Unit = {}
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
                    elevation = 18.dp,
                    shape = RoundedCornerShape(30.dp),
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
                    shape = RoundedCornerShape(30.dp)
                ),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFF8FAFC)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                RideitSavedPlacesHeader(
                    title = "Quick destinations",
                    subtitle = "Tap a saved place to set destination",
                    count = places.size,
                    compact = compact
                )

                Spacer(modifier = Modifier.height(12.dp))

                RideitSavedPlacesHorizontalChips(
                    places = places,
                    showAddChip = true,
                    compact = compact,
                    onPlaceClick = onPlaceClick,
                    onAddPlaceClick = onAddPlaceClick
                )

                if (places.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    RideitSavedPlacesTipCard(
                        compact = compact
                    )
                }
            }
        }
    }
}

@Composable
private fun RideitSavedPlacesHeader(
    title: String,
    subtitle: String,
    count: Int,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 17.sp else 21.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = Color(0xFF64748B),
                fontSize = if (compact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

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
                .padding(horizontal = 10.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count saved",
                color = Color(0xFF2563EB),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun RideitSavedPlaceIcon(
    type: RideitSavedPlaceType,
    compact: Boolean,
    pulse: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_saved_place_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_saved_place_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 42.dp else 50.dp)
            .background(
                color = type.softBackgroundColor(),
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RideitSavedPlaceMiniBadge(
    text: String,
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
                horizontal = if (compact) 7.dp else 8.dp,
                vertical = if (compact) 4.dp else 5.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFF2563EB),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RideitSavedPlacesEmptyCard(
    compact: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.62f),
                shape = RoundedCornerShape(if (compact) 26.dp else 30.dp)
            )
            .clickable { onAddClick() },
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
                .padding(if (compact) 18.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RideitSavedPlaceIcon(
                type = RideitSavedPlaceType.ADD,
                compact = compact,
                pulse = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No saved places yet",
                color = Color(0xFF0F172A),
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Save your most used destinations to book rides faster.",
                color = Color(0xFF64748B),
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) 17.sp else 19.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun RideitSavedPlacesTipCard(
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(if (compact) 20.dp else 22.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFFDE68A),
                shape = RoundedCornerShape(if (compact) 20.dp else 22.dp)
            )
            .padding(if (compact) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitSavedPlaceIcon(
            type = RideitSavedPlaceType.FAVORITE,
            compact = true
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 11.dp)
        ) {
            Text(
                text = "Tip",
                color = Color(0xFF92400E),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Add Home and Work first for a faster Rideit booking flow.",
                color = Color(0xFF92400E).copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun rideitDefaultSavedPlaceShortcuts(): List<RideitSavedPlaceUiModel> {
    return listOf(
        RideitSavedPlaceUiModel(
            id = "home",
            title = "Home",
            address = "Add your home address",
            type = RideitSavedPlaceType.HOME,
            isDefault = true
        ),
        RideitSavedPlaceUiModel(
            id = "work",
            title = "Work",
            address = "Add your work address",
            type = RideitSavedPlaceType.WORK
        ),
        RideitSavedPlaceUiModel(
            id = "airport",
            title = "Airport",
            address = "Quick airport ride",
            type = RideitSavedPlaceType.AIRPORT
        )
    )
}

private fun RideitSavedPlaceType.defaultAddressHint(): String {
    return when (this) {
        RideitSavedPlaceType.HOME -> "Home address"
        RideitSavedPlaceType.WORK -> "Work address"
        RideitSavedPlaceType.AIRPORT -> "Airport destination"
        RideitSavedPlaceType.RECENT -> "Recent destination"
        RideitSavedPlaceType.FAVORITE -> "Favorite destination"
        RideitSavedPlaceType.CUSTOM -> "Saved destination"
        RideitSavedPlaceType.ADD -> "Add new destination"
    }
}

private fun RideitSavedPlaceType.softBackgroundColor(): Color {
    return when (this) {
        RideitSavedPlaceType.HOME -> Color(0xFFEFF6FF)
        RideitSavedPlaceType.WORK -> Color(0xFFF3E8FF)
        RideitSavedPlaceType.AIRPORT -> Color(0xFFECFEFF)
        RideitSavedPlaceType.RECENT -> Color(0xFFF8FAFC)
        RideitSavedPlaceType.FAVORITE -> Color(0xFFFEF3C7)
        RideitSavedPlaceType.CUSTOM -> Color(0xFFF8FAFC)
        RideitSavedPlaceType.ADD -> Color(0xFFEFF6FF)
    }
}

private fun RideitSavedPlaceType.borderColor(): Color {
    return when (this) {
        RideitSavedPlaceType.HOME -> Color(0xFFBFDBFE)
        RideitSavedPlaceType.WORK -> Color(0xFFD8B4FE)
        RideitSavedPlaceType.AIRPORT -> Color(0xFFA5F3FC)
        RideitSavedPlaceType.RECENT -> Color(0xFFE2E8F0)
        RideitSavedPlaceType.FAVORITE -> Color(0xFFFDE68A)
        RideitSavedPlaceType.CUSTOM -> Color(0xFFE2E8F0)
        RideitSavedPlaceType.ADD -> Color(0xFFBFDBFE)
    }
}

private fun RideitSavedPlaceType.dotColor(): Color {
    return when (this) {
        RideitSavedPlaceType.HOME -> Color(0xFF2563EB)
        RideitSavedPlaceType.WORK -> Color(0xFF7C3AED)
        RideitSavedPlaceType.AIRPORT -> Color(0xFF0891B2)
        RideitSavedPlaceType.RECENT -> Color(0xFF64748B)
        RideitSavedPlaceType.FAVORITE -> Color(0xFFF59E0B)
        RideitSavedPlaceType.CUSTOM -> Color(0xFF0F172A)
        RideitSavedPlaceType.ADD -> Color(0xFF2563EB)
    }
}

private fun RideitSavedPlaceType.textColor(): Color {
    return when (this) {
        RideitSavedPlaceType.HOME -> Color(0xFF2563EB)
        RideitSavedPlaceType.WORK -> Color(0xFF6D28D9)
        RideitSavedPlaceType.AIRPORT -> Color(0xFF0E7490)
        RideitSavedPlaceType.RECENT -> Color(0xFF475569)
        RideitSavedPlaceType.FAVORITE -> Color(0xFF92400E)
        RideitSavedPlaceType.CUSTOM -> Color(0xFF0F172A)
        RideitSavedPlaceType.ADD -> Color(0xFF2563EB)
    }
}

private fun RideitSavedPlaceType.iconText(): String {
    return when (this) {
        RideitSavedPlaceType.HOME -> "H"
        RideitSavedPlaceType.WORK -> "W"
        RideitSavedPlaceType.AIRPORT -> "A"
        RideitSavedPlaceType.RECENT -> "R"
        RideitSavedPlaceType.FAVORITE -> "★"
        RideitSavedPlaceType.CUSTOM -> "•"
        RideitSavedPlaceType.ADD -> "+"
    }
}