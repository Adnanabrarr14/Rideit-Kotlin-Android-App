package com.example.rideit.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class RideitFilterChipStyle {
    PRIMARY,
    SUCCESS,
    WARNING,
    DANGER,
    PREMIUM,
    NEUTRAL
}

enum class RideitSortDirection {
    ASCENDING,
    DESCENDING
}

@Immutable
data class RideitFilterChipUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val style: RideitFilterChipStyle = RideitFilterChipStyle.NEUTRAL,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val countText: String? = null
)

@Immutable
data class RideitSortOptionUiModel(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val direction: RideitSortDirection = RideitSortDirection.DESCENDING,
    val selected: Boolean = false
)

@Composable
fun RideitPremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search Rideit",
    compact: Boolean = true,
    enabled: Boolean = true,
    showClearButton: Boolean = true,
    onClearClick: () -> Unit = {
        onQueryChange("")
    }
) {
    val alpha = if (enabled) 1f else 0.55f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (compact) 12.dp else 16.dp,
                shape = RoundedCornerShape(if (compact) 24.dp else 28.dp),
                spotColor = Color.Black.copy(alpha = 0.13f)
            )
            .background(
                color = Color.White.copy(alpha = 0.97f),
                shape = RoundedCornerShape(if (compact) 24.dp else 28.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color(0xFFE2E8F0)
                    )
                ),
                shape = RoundedCornerShape(if (compact) 24.dp else 28.dp)
            )
            .graphicsLayer {
                this.alpha = alpha
            }
            .padding(horizontal = if (compact) 13.dp else 15.dp, vertical = if (compact) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RideitSearchFilterIcon(
            text = "S",
            style = RideitFilterChipStyle.PRIMARY,
            compact = compact,
            pulse = query.isNotBlank()
        )

        Spacer(modifier = Modifier.width(11.dp))

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isBlank()) {
                Text(
                    text = placeholder,
                    color = Color(0xFF94A3B8),
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            BasicTextField(
                value = query,
                onValueChange = {
                    if (enabled) onQueryChange(it)
                },
                enabled = enabled,
                textStyle = TextStyle(
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(Color(0xFF2563EB)),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = showClearButton && query.isNotBlank(),
            enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(140)) + shrinkHorizontally(animationSpec = tween(160))
        ) {
            Text(
                text = "×",
                color = Color(0xFF64748B),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(
                        color = Color(0xFFF1F5F9),
                        shape = CircleShape
                    )
                    .clickable(enabled = enabled) {
                        onClearClick()
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
fun RideitFilterChip(
    filter: RideitFilterChipUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onClick: () -> Unit = {}
) {
    val alpha = if (filter.enabled) 1f else 0.55f

    Row(
        modifier = modifier
            .background(
                color = if (filter.selected) filter.style.softBackgroundColor() else Color.White,
                shape = RoundedCornerShape(50)
            )
            .border(
                width = if (filter.selected) 2.dp else 1.dp,
                color = if (filter.selected) filter.style.borderColor() else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(50)
            )
            .clickable(enabled = filter.enabled) {
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
                    color = if (filter.selected) filter.style.dotColor() else Color(0xFF94A3B8),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = filter.title,
            color = if (filter.selected) filter.style.textColor() else Color(0xFF64748B),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!filter.countText.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = filter.countText,
                color = if (filter.selected) filter.style.textColor() else Color(0xFF94A3B8),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RideitHorizontalFilterChips(
    filters: List<RideitFilterChipUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onFilterClick: (RideitFilterChipUiModel) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { filter ->
            RideitFilterChip(
                filter = filter,
                compact = compact,
                onClick = {
                    onFilterClick(filter)
                }
            )
        }
    }
}

@Composable
fun RideitSortDropdownCard(
    selectedSort: RideitSortOptionUiModel,
    sortOptions: List<RideitSortOptionUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    onSortSelected: (RideitSortOptionUiModel) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(50),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(50)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(50)
                )
                .clickable {
                    expanded = true
                }
                .padding(
                    horizontal = if (compact) 11.dp else 13.dp,
                    vertical = if (compact) 9.dp else 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSearchFilterIcon(
                text = if (selectedSort.direction == RideitSortDirection.DESCENDING) "↓" else "↑",
                style = RideitFilterChipStyle.PREMIUM,
                compact = true,
                pulse = false
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = selectedSort.title,
                color = Color(0xFF0F172A),
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(18.dp))
        ) {
            sortOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = option.title,
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            if (!option.subtitle.isNullOrBlank()) {
                                Text(
                                    text = option.subtitle,
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onSortSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
fun RideitSearchFilterSortBar(
    query: String,
    onQueryChange: (String) -> Unit,
    filters: List<RideitFilterChipUiModel>,
    selectedSort: RideitSortOptionUiModel,
    sortOptions: List<RideitSortOptionUiModel>,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    placeholder: String = "Search trips, receipts, places",
    onFilterClick: (RideitFilterChipUiModel) -> Unit = {},
    onSortSelected: (RideitSortOptionUiModel) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
    ) {
        RideitPremiumSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder,
            compact = compact
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitHorizontalFilterChips(
                filters = filters,
                compact = compact,
                modifier = Modifier.weight(1f),
                onFilterClick = onFilterClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            RideitSortDropdownCard(
                selectedSort = selectedSort,
                sortOptions = sortOptions,
                compact = compact,
                onSortSelected = onSortSelected
            )
        }
    }
}

@Composable
fun RideitFilterSummaryCard(
    visible: Boolean,
    resultCountText: String,
    modifier: Modifier = Modifier,
    query: String? = null,
    selectedFilterText: String? = null,
    selectedSortText: String? = null,
    compact: Boolean = true,
    onClearClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFBFDBFE),
                    shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
                )
                .padding(if (compact) 13.dp else 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RideitSearchFilterIcon(
                text = "F",
                style = RideitFilterChipStyle.PRIMARY,
                compact = true,
                pulse = true
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 11.dp)
            ) {
                Text(
                    text = resultCountText,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = listOfNotNull(
                        query?.takeIf { it.isNotBlank() }?.let { "Search: $it" },
                        selectedFilterText?.takeIf { it.isNotBlank() },
                        selectedSortText?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ").ifBlank {
                        "Showing current Rideit results"
                    },
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "Clear",
                color = Color(0xFF2563EB),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFBFDBFE),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable {
                        onClearClick()
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
fun RideitSearchEmptyResultCard(
    visible: Boolean,
    modifier: Modifier = Modifier,
    title: String = "No results found",
    message: String = "Try changing your search, filter, or sort options.",
    compact: Boolean = true,
    onClearClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(240)),
        exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(180))
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
                    .padding(if (compact) 18.dp else 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RideitSearchFilterIcon(
                    text = "S",
                    style = RideitFilterChipStyle.NEUTRAL,
                    compact = false,
                    pulse = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    color = Color(0xFF0F172A),
                    fontSize = if (compact) 16.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = message,
                    color = Color(0xFF64748B),
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = if (compact) 17.sp else 19.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Clear filters",
                    color = Color(0xFF2563EB),
                    fontSize = 12.sp,
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
                        .clickable {
                            onClearClick()
                        }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RideitSearchFilterIcon(
    text: String,
    style: RideitFilterChipStyle,
    compact: Boolean,
    pulse: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rideit_search_filter_icon_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 880),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rideit_search_filter_icon_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(if (compact) 34.dp else 44.dp)
            .background(
                color = style.softBackgroundColor(),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 15.dp else 19.dp)
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

fun rideitDefaultTripFilters(
    selectedId: String = "all"
): List<RideitFilterChipUiModel> {
    return listOf(
        RideitFilterChipUiModel(
            id = "all",
            title = "All",
            style = RideitFilterChipStyle.PRIMARY,
            selected = selectedId == "all"
        ),
        RideitFilterChipUiModel(
            id = "completed",
            title = "Completed",
            style = RideitFilterChipStyle.SUCCESS,
            selected = selectedId == "completed"
        ),
        RideitFilterChipUiModel(
            id = "cancelled",
            title = "Cancelled",
            style = RideitFilterChipStyle.DANGER,
            selected = selectedId == "cancelled"
        ),
        RideitFilterChipUiModel(
            id = "active",
            title = "Active",
            style = RideitFilterChipStyle.WARNING,
            selected = selectedId == "active"
        )
    )
}

fun rideitDefaultDriverRequestFilters(
    selectedId: String = "all"
): List<RideitFilterChipUiModel> {
    return listOf(
        RideitFilterChipUiModel(
            id = "all",
            title = "All",
            style = RideitFilterChipStyle.PRIMARY,
            selected = selectedId == "all"
        ),
        RideitFilterChipUiModel(
            id = "nearby",
            title = "Nearby",
            style = RideitFilterChipStyle.SUCCESS,
            selected = selectedId == "nearby"
        ),
        RideitFilterChipUiModel(
            id = "high_fare",
            title = "High fare",
            style = RideitFilterChipStyle.PREMIUM,
            selected = selectedId == "high_fare"
        ),
        RideitFilterChipUiModel(
            id = "urgent",
            title = "Urgent",
            style = RideitFilterChipStyle.WARNING,
            selected = selectedId == "urgent"
        )
    )
}

fun rideitDefaultReceiptFilters(
    selectedId: String = "all"
): List<RideitFilterChipUiModel> {
    return listOf(
        RideitFilterChipUiModel(
            id = "all",
            title = "All",
            style = RideitFilterChipStyle.PRIMARY,
            selected = selectedId == "all"
        ),
        RideitFilterChipUiModel(
            id = "cash",
            title = "Cash",
            style = RideitFilterChipStyle.SUCCESS,
            selected = selectedId == "cash"
        ),
        RideitFilterChipUiModel(
            id = "card",
            title = "Card",
            style = RideitFilterChipStyle.PREMIUM,
            selected = selectedId == "card"
        ),
        RideitFilterChipUiModel(
            id = "support",
            title = "Support",
            style = RideitFilterChipStyle.WARNING,
            selected = selectedId == "support"
        )
    )
}

fun rideitDefaultSortOptions(
    selectedId: String = "newest"
): List<RideitSortOptionUiModel> {
    return listOf(
        RideitSortOptionUiModel(
            id = "newest",
            title = "Newest",
            subtitle = "Latest first",
            direction = RideitSortDirection.DESCENDING,
            selected = selectedId == "newest"
        ),
        RideitSortOptionUiModel(
            id = "oldest",
            title = "Oldest",
            subtitle = "Oldest first",
            direction = RideitSortDirection.ASCENDING,
            selected = selectedId == "oldest"
        ),
        RideitSortOptionUiModel(
            id = "fare_high",
            title = "Fare high",
            subtitle = "Highest fare first",
            direction = RideitSortDirection.DESCENDING,
            selected = selectedId == "fare_high"
        ),
        RideitSortOptionUiModel(
            id = "fare_low",
            title = "Fare low",
            subtitle = "Lowest fare first",
            direction = RideitSortDirection.ASCENDING,
            selected = selectedId == "fare_low"
        )
    )
}

fun rideitSelectedSortOption(
    selectedId: String,
    options: List<RideitSortOptionUiModel> = rideitDefaultSortOptions(selectedId)
): RideitSortOptionUiModel {
    return options.firstOrNull { it.id == selectedId }
        ?: options.firstOrNull()
        ?: RideitSortOptionUiModel(
            id = "newest",
            title = "Newest",
            subtitle = "Latest first",
            direction = RideitSortDirection.DESCENDING,
            selected = true
        )
}

private fun RideitFilterChipStyle.softBackgroundColor(): Color {
    return when (this) {
        RideitFilterChipStyle.PRIMARY -> Color(0xFFEFF6FF)
        RideitFilterChipStyle.SUCCESS -> Color(0xFFDCFCE7)
        RideitFilterChipStyle.WARNING -> Color(0xFFFEF3C7)
        RideitFilterChipStyle.DANGER -> Color(0xFFFEE2E2)
        RideitFilterChipStyle.PREMIUM -> Color(0xFFF3E8FF)
        RideitFilterChipStyle.NEUTRAL -> Color(0xFFF8FAFC)
    }
}

private fun RideitFilterChipStyle.borderColor(): Color {
    return when (this) {
        RideitFilterChipStyle.PRIMARY -> Color(0xFFBFDBFE)
        RideitFilterChipStyle.SUCCESS -> Color(0xFFBBF7D0)
        RideitFilterChipStyle.WARNING -> Color(0xFFFDE68A)
        RideitFilterChipStyle.DANGER -> Color(0xFFFCA5A5)
        RideitFilterChipStyle.PREMIUM -> Color(0xFFD8B4FE)
        RideitFilterChipStyle.NEUTRAL -> Color(0xFFE2E8F0)
    }
}

private fun RideitFilterChipStyle.dotColor(): Color {
    return when (this) {
        RideitFilterChipStyle.PRIMARY -> Color(0xFF2563EB)
        RideitFilterChipStyle.SUCCESS -> Color(0xFF22C55E)
        RideitFilterChipStyle.WARNING -> Color(0xFFF59E0B)
        RideitFilterChipStyle.DANGER -> Color(0xFFEF4444)
        RideitFilterChipStyle.PREMIUM -> Color(0xFF7C3AED)
        RideitFilterChipStyle.NEUTRAL -> Color(0xFF64748B)
    }
}

private fun RideitFilterChipStyle.textColor(): Color {
    return when (this) {
        RideitFilterChipStyle.PRIMARY -> Color(0xFF2563EB)
        RideitFilterChipStyle.SUCCESS -> Color(0xFF166534)
        RideitFilterChipStyle.WARNING -> Color(0xFF92400E)
        RideitFilterChipStyle.DANGER -> Color(0xFFB91C1C)
        RideitFilterChipStyle.PREMIUM -> Color(0xFF6D28D9)
        RideitFilterChipStyle.NEUTRAL -> Color(0xFF475569)
    }
}