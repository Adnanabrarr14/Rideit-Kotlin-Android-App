package com.example.rideit.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by mapViewModel.uiState.collectAsState()

    val defaultLocation = remember { LatLng(33.6844, 73.0479) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = false
            )
        ) {
            Marker(
                state = MarkerState(position = defaultLocation),
                title = "Islamabad"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "Choose your ride location",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.pickupText,
                        onValueChange = { mapViewModel.onPickupTextChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Pickup") },
                        singleLine = true
                    )

                    if (uiState.pickupSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SuggestionList(
                            suggestions = uiState.pickupSuggestions,
                            onClick = { mapViewModel.onPickupSuggestionSelected(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.dropoffText,
                        onValueChange = { mapViewModel.onDropoffTextChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Dropoff") },
                        singleLine = true
                    )

                    if (uiState.dropoffSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SuggestionList(
                            suggestions = uiState.dropoffSuggestions,
                            onClick = { mapViewModel.onDropoffSuggestionSelected(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { mapViewModel.clearSuggestions() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B61FF),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Search")
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionList(
    suggestions: List<LocationSuggestion>,
    onClick: (LocationSuggestion) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.White)
        ) {
            items(suggestions) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(item) }
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = item.title,
                            color = Color.Black
                        )
                        if (item.subtitle.isNotEmpty()) {
                            Text(
                                text = item.subtitle,
                                color = Color.Gray
                            )
                        }
                    }
                }
                Divider()
            }
        }
    }
}