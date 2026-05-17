package com.example.rideit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager

/*
    Important:
    This file is the older rider payment receipt card file.
    It must NOT define PremiumTripReceiptPreviewSheet because the active map receipt
    already uses ui.components.PremiumTripReceiptPreviewSheet.

    This renamed component is kept safely for future use and to avoid duplicate
    conflicting overload errors.
*/

@Composable
fun RideitPaymentReceiptPreviewSheet(
    visible: Boolean,
    driverName: String?,
    pickupText: String,
    dropoffText: String,
    rideTitle: String?,
    fareText: String?,
    rating: Int?,
    modifier: Modifier = Modifier,
    onDoneClick: () -> Unit,
    onViewHistoryClick: () -> Unit
) {
    var paymentDisplayText by rememberSaveable {
        mutableStateOf("Cash")
    }

    LaunchedEffect(visible) {
        if (visible) {
            FirebaseManager.loadRiderPaymentProfile(
                onSuccess = { profile ->
                    paymentDisplayText = when (profile.selectedPaymentMethod) {
                        FirebaseManager.PAYMENT_CARD -> {
                            if (profile.cardLastFour.isNotBlank()) {
                                "Debit / Credit Card •••• ${profile.cardLastFour}"
                            } else {
                                "Debit / Credit Card"
                            }
                        }

                        FirebaseManager.PAYMENT_WALLET -> {
                            "Rideit Wallet"
                        }

                        else -> {
                            "Cash"
                        }
                    }
                },
                onError = {
                    paymentDisplayText = "Cash"
                }
            )
        }
    }

    if (!visible) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD1D5DB))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rs",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trip receipt",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Your Rideit trip summary is ready.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF16A34A).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Paid",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xFFF8FAFC)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ReceiptInfoRow(
                            title = "Total fare",
                            value = fareText?.takeIf { it.isNotBlank() } ?: "Rs 180"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(color = Color(0xFFE5E7EB))

                        Spacer(modifier = Modifier.height(12.dp))

                        ReceiptInfoRow(
                            title = "Ride type",
                            value = rideTitle?.takeIf { it.isNotBlank() } ?: "Mini"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ReceiptInfoRow(
                            title = "Driver",
                            value = driverName ?: "Rideit Driver"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ReceiptInfoRow(
                            title = "Payment",
                            value = paymentDisplayText
                        )

                        if (rating != null) {
                            Spacer(modifier = Modifier.height(10.dp))

                            ReceiptInfoRow(
                                title = "Your rating",
                                value = "$rating ★"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Route",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ReceiptRoutePoint(
                            dotColor = Color(0xFF16A34A),
                            label = "Pickup",
                            value = pickupText.ifBlank { "Pickup location" }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ReceiptRoutePoint(
                            dotColor = MaterialTheme.colorScheme.primary,
                            label = "Dropoff",
                            value = dropoffText.ifBlank { "Dropoff location" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDoneClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Done",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onViewHistoryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "View in Trip History",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B7280),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReceiptRoutePoint(
    dotColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 34.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
