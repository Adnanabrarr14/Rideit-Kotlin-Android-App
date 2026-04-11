package com.example.rideit.map.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rideit.map.model.LocationSuggestion
import com.example.rideit.map.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by mapViewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    val defaultCenter = remember {
        LatLng(33.6844, 73.0479)
    }

    LaunchedEffect(uiState.pickupLatLng, uiState.dropoffLatLng, uiState.routePoints) {
        val pickup = uiState.pickupLatLng
        val dropoff = uiState.dropoffLatLng

        when {
            pickup != null && dropoff != null -> {
                val bounds = LatLngBounds.builder()
                    .include(pickup)
                    .include(dropoff)
                    .build()

                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(bounds, 160),
                    durationMs = 1000
                )
            }

            pickup != null -> {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(pickup, 14f),
                    durationMs = 800
                )
            }

            dropoff != null -> {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(dropoff, 14f),
                    durationMs = 800
                )
            }

            else -> {
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(defaultCenter, 11f)
                )
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false
            )
        ) {
            uiState.pickupLatLng?.let { pickup ->
                Marker(
                    state = MarkerState(position = pickup),
                    title = "Pickup",
                    snippet = uiState.pickupText,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }

            uiState.dropoffLatLng?.let { dropoff ->
                Marker(
                    state = MarkerState(position = dropoff),
                    title = "Dropoff",
                    snippet = uiState.dropoffText,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            if (uiState.routePoints.size >= 2) {
                Polyline(points = uiState.routePoints)
            }
        }

        FloatingActionButton(
            onClick = {
                val target = uiState.pickupLatLng ?: uiState.dropoffLatLng ?: defaultCenter
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(target, 14f)
                )
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Focus location"
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose your ride",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = uiState.pickupText,
                    onValueChange = mapViewModel::onPickupTextChanged,
                    label = { Text("Pickup location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (uiState.pickupSuggestions.isNotEmpty()) {
                    SuggestionList(
                        suggestions = uiState.pickupSuggestions,
                        onSuggestionClick = mapViewModel::onPickupSuggestionSelected
                    )
                }

                OutlinedTextField(
                    value = uiState.dropoffText,
                    onValueChange = mapViewModel::onDropoffTextChanged,
                    label = { Text("Dropoff location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (uiState.dropoffSuggestions.isNotEmpty()) {
                    SuggestionList(
                        suggestions = uiState.dropoffSuggestions,
                        onSuggestionClick = mapViewModel::onDropoffSuggestionSelected
                    )
                }

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = mapViewModel::onSearchClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .height(20.dp)
                                .width(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Searching...")
                    } else {
                        Text("Search")
                    }
                }

                TextButton(
                    onClick = mapViewModel::clearError,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Clear message")
                }
            }
        }
    }
}

@Composable
private fun SuggestionList(
    suggestions: List<LocationSuggestion>,
    onSuggestionClick: (LocationSuggestion) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            items(suggestions) { suggestion ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(14.dp)
                ) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = suggestion.fullAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()
            }
        }
    }
}