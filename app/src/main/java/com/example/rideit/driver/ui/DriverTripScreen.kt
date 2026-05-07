package com.example.rideit.driver.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun DriverTripScreen(
    driverName: String = "Shameer Khan",
    onBackToDriverHome: () -> Unit
) {
    var tripStep by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val stepTitle = when (tripStep) {
        0 -> "Go to pickup"
        1 -> "Arrived at pickup"
        2 -> "Trip in progress"
        else -> "Trip completed"
    }

    val stepSubtitle = when (tripStep) {
        0 -> "Navigate to rider pickup point."
        1 -> "Confirm rider pickup and start the trip."
        2 -> "Follow the route to the dropoff location."
        else -> "Trip completed successfully."
    }

    val progress = when (tripStep) {
        0 -> 0.33f
        1 -> 0.58f
        2 -> 0.86f
        else -> 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            DriverTripHeader(
                driverName = driverName,
                stepTitle = stepTitle,
                stepSubtitle = stepSubtitle,
                progress = progress
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripRealMapPreviewCard(
                tripStep = tripStep
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripRiderCard()

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripRouteCard()

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripMetricsCard()

            Spacer(modifier = Modifier.height(16.dp))

            DriverTripActionCard(
                tripStep = tripStep,
                onPrimaryClick = {
                    when (tripStep) {
                        0 -> {
                            tripStep = 1
                            scope.launch {
                                snackbarHostState.showSnackbar("Marked as arrived at pickup.")
                            }
                        }

                        1 -> {
                            tripStep = 2
                            scope.launch {
                                snackbarHostState.showSnackbar("Trip started.")
                            }
                        }

                        2 -> {
                            tripStep = 3
                            scope.launch {
                                snackbarHostState.showSnackbar("Trip completed. Earnings updated demo.")
                            }
                        }

                        else -> {
                            onBackToDriverHome()
                        }
                    }
                },
                onSecondaryClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Driver support will be connected later.")
                    }
                },
                onCancelClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Trip cancellation flow will be connected later.")
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
        )
    }
}

@Composable
private fun DriverTripHeader(
    driverName: String,
    stepTitle: String,
    stepSubtitle: String,
    progress: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF111827),
        shadowElevation = 14.dp,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF111827),
                            Color(0xFF1F2937),
                            Color(0xFF8A35F2)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driverName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Active driver trip",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.76f)
                    )
                }

                LiveDriverDot()
            }

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stepTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stepSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.76f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = Color(0xFF22C55E),
                        trackColor = Color.White.copy(alpha = 0.18f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveDriverDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "driver_trip_live_dot")

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
        label = "driver_trip_live_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .alpha(alpha)
                .background(
                    color = Color(0xFF22C55E),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = "Live",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun DriverTripRealMapPreviewCard(
    tripStep: Int
) {
    val pickupLocation = LatLng(33.6938, 73.0328)
    val dropoffLocation = LatLng(33.7202, 73.0605)

    val driverLocation = when (tripStep) {
        0 -> LatLng(33.6844, 73.0479)
        1 -> pickupLocation
        2 -> LatLng(33.7067, 73.0469)
        else -> dropoffLocation
    }

    val routePoints = listOf(
        driverLocation,
        pickupLocation,
        LatLng(33.7048, 73.0437),
        dropoffLocation
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pickupLocation, 13.4f)
    }

    LaunchedEffect(tripStep) {
        val focusPoint = when (tripStep) {
            0 -> pickupLocation
            1 -> pickupLocation
            2 -> dropoffLocation
            else -> dropoffLocation
        }

        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(focusPoint, 13.8f),
            durationMs = 700
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    compassEnabled = false
                )
            ) {
                Marker(
                    state = MarkerState(driverLocation),
                    title = "Driver",
                    snippet = "Shameer Khan",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )

                Marker(
                    state = MarkerState(pickupLocation),
                    title = "Pickup",
                    snippet = "F-10 Markaz, Islamabad",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )

                Marker(
                    state = MarkerState(dropoffLocation),
                    title = "Dropoff",
                    snippet = "Blue Area, Islamabad",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
                )

                Polyline(
                    points = routePoints,
                    color = Color(0xFF8A35F2),
                    width = 8f
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.96f),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = when (tripStep) {
                        0 -> "3 min to pickup"
                        1 -> "At pickup"
                        2 -> "12 min to dropoff"
                        else -> "Completed"
                    },
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(50),
                color = Color(0xFF8A35F2),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "Live map",
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DriverTripRiderCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8A35F2).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8A35F2)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Adnan Rider",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "⭐ 4.8 • Verified rider",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280)
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF16A34A).copy(alpha = 0.12f)
            ) {
                Text(
                    text = "Call",
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF16A34A)
                )
            }
        }
    }
}

@Composable
private fun DriverTripRouteCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Trip route",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverRoutePoint(
                dotColor = Color(0xFF16A34A),
                label = "Pickup",
                value = "F-10 Markaz, Islamabad"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DriverRoutePoint(
                dotColor = Color(0xFF8A35F2),
                label = "Dropoff",
                value = "Blue Area, Islamabad"
            )
        }
    }
}

@Composable
private fun DriverRoutePoint(
    dotColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(13.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DriverTripMetricsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DriverTripMetric(
                title = "Fare",
                value = "₨ 850"
            )

            DriverTripMetric(
                title = "Distance",
                value = "8.4 km"
            )

            DriverTripMetric(
                title = "ETA",
                value = "15 min"
            )
        }
    }
}

@Composable
private fun DriverTripMetric(
    title: String,
    value: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111827)
        )
    }
}

@Composable
private fun DriverTripActionCard(
    tripStep: Int,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (tripStep) {
                        0 -> Color(0xFF8A35F2)
                        1 -> Color(0xFF16A34A)
                        2 -> Color(0xFF111827)
                        else -> Color(0xFF8A35F2)
                    }
                )
            ) {
                Text(
                    text = when (tripStep) {
                        0 -> "Arrived at Pickup"
                        1 -> "Start Trip"
                        2 -> "Complete Trip"
                        else -> "Back to Driver Home"
                    },
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(11.dp))

            OutlinedButton(
                onClick = onSecondaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Contact Support",
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = tripStep < 3,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(11.dp))

                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Text(
                            text = "Cancel Trip Demo",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}