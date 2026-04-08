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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rideit.map.LocationSuggestion
import com.example.rideit.map.MapViewModel
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
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val defaultLocation = LatLng(33.6844, 73.0479)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 13f)
    }

    LaunchedEffect(uiState.searchMessage) {
        uiState.searchMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            mapViewModel.consumeSearchMessage()
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
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
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

                    OutlinedTextField(
                        value = uiState.dropoffText,
                        onValueChange = { mapViewModel.onDropoffTextChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Dropoff") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                mapViewModel.onSearchClicked()
                            }
                        )
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
                        enabled = uiState.isSearchEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B61FF),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFD1C4E9),
                            disabledContentColor = Color.White
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.White),
            verticalArrangement = Arrangement.Top
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