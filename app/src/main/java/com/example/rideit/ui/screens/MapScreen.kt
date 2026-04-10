package com.example.rideit.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rideit.map.LocationSuggestion
import com.example.rideit.map.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
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
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by mapViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val defaultLocation = remember { LatLng(33.6844, 73.0479) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 11.8f)
    }

    LaunchedEffect(uiState.searchMessage) {
        uiState.searchMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            mapViewModel.consumeSearchMessage()
        }
    }

    LaunchedEffect(uiState.searchRequestId, uiState.pickupLatLng, uiState.dropoffLatLng, uiState.routePoints) {
        val pickup = uiState.pickupLatLng
        val dropoff = uiState.dropoffLatLng

        if (uiState.searchRequestId != 0L && pickup != null && dropoff != null) {
            val builder = LatLngBounds.builder()
            builder.include(pickup)
            builder.include(dropoff)

            uiState.routePoints.forEach { point ->
                builder.include(point)
            }

            val bounds = builder.build()

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 140),
                durationMs = 900
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false)
        ) {
            Marker(
                state = MarkerState(position = defaultLocation),
                title = "Islamabad"
            )

            val pickupMarker = uiState.pickupLatLng
            if (uiState.showSearchMarkers && pickupMarker != null) {
                Marker(
                    state = MarkerState(position = pickupMarker),
                    title = "Pickup",
                    snippet = uiState.pickupText
                )
            }

            val dropoffMarker = uiState.dropoffLatLng
            if (uiState.showSearchMarkers && dropoffMarker != null) {
                Marker(
                    state = MarkerState(position = dropoffMarker),
                    title = "Dropoff",
                    snippet = uiState.dropoffText
                )
            }

            if (uiState.routePoints.isNotEmpty()) {
                Polyline(
                    points = uiState.routePoints,
                    color = Color(0xFF7B61FF),
                    width = 12f,
                    geodesic = true,
                    jointType = JointType.ROUND
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(5.dp)
                                .background(
                                    color = Color(0xFFD8D8D8),
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Choose your ride location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Search sectors, streets, landmarks, malls, hospitals and airports",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    RideLocationField(
                        value = uiState.pickupText,
                        onValueChange = { mapViewModel.onPickupTextChanged(it) },
                        label = "Pickup",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Pickup",
                                tint = Color(0xFF7B61FF)
                            )
                        },
                        imeAction = ImeAction.Next
                    )

                    if (uiState.pickupSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SuggestionList(
                            suggestions = uiState.pickupSuggestions,
                            onClick = {
                                mapViewModel.onPickupSuggestionSelected(it)
                                keyboardController?.hide()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    RideLocationField(
                        value = uiState.dropoffText,
                        onValueChange = { mapViewModel.onDropoffTextChanged(it) },
                        label = "Dropoff",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Dropoff",
                                tint = Color(0xFFFF5A5F)
                            )
                        },
                        imeAction = ImeAction.Search,
                        onSearch = {
                            keyboardController?.hide()
                            mapViewModel.onSearchClicked()
                        }
                    )

                    if (uiState.dropoffSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SuggestionList(
                            suggestions = uiState.dropoffSuggestions,
                            onClick = {
                                mapViewModel.onDropoffSuggestionSelected(it)
                                keyboardController?.hide()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            mapViewModel.onSearchClicked()
                        },
                        enabled = uiState.isSearchEnabled && !uiState.isRouteLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B61FF),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFD7CCFF),
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (uiState.isRouteLoading) "Finding Route..." else "Search Route",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RideLocationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit,
    imeAction: ImeAction,
    onSearch: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch?.invoke() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7B61FF),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedLabelColor = Color(0xFF7B61FF),
            unfocusedLabelColor = Color.Gray,
            cursorColor = Color(0xFF7B61FF),
            focusedContainerColor = Color(0xFFFDFDFF),
            unfocusedContainerColor = Color(0xFFFDFDFF)
        )
    )
}

@Composable
fun SuggestionList(
    suggestions: List<LocationSuggestion>,
    onClick: (LocationSuggestion) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(Color.White),
            verticalArrangement = Arrangement.Top
        ) {
            items(suggestions) { item ->
                SuggestionItem(
                    suggestion = item,
                    onClick = { onClick(item) }
                )
                HorizontalDivider(color = Color(0xFFF0F0F0))
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: LocationSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFFF3F0FF),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Place",
                tint = Color(0xFF7B61FF)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            if (suggestion.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = suggestion.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}