package com.example.rideit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rideit.FirebaseManager

private enum class RiderPaymentType {
    CASH,
    CARD
}

@Immutable
private data class RiderPaymentMethodUi(
    val type: RiderPaymentType,
    val icon: String,
    val title: String,
    val subtitle: String,
    val badge: String
)

@Composable
fun PaymentScreen(
    onBackClick: () -> Unit
) {
    var selectedPaymentType by rememberSaveable {
        mutableStateOf(RiderPaymentType.CASH)
    }

    var showCardDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var savedCardLastFour by rememberSaveable {
        mutableStateOf("")
    }

    var savedCardName by rememberSaveable {
        mutableStateOf("")
    }

    var walletBalance by rememberSaveable {
        mutableStateOf(1250L)
    }

    var isLoading by rememberSaveable {
        mutableStateOf(true)
    }

    var isSaving by rememberSaveable {
        mutableStateOf(false)
    }

    var statusMessage by rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        FirebaseManager.loadRiderPaymentProfile(
            onSuccess = { profile ->
                selectedPaymentType = when (profile.selectedPaymentMethod) {
                    FirebaseManager.PAYMENT_CARD -> RiderPaymentType.CARD
                    else -> RiderPaymentType.CASH
                }

                savedCardLastFour = profile.cardLastFour
                savedCardName = profile.cardHolderName
                walletBalance = profile.walletBalance
                isLoading = false

                statusMessage = if (profile.selectedPaymentMethod == FirebaseManager.PAYMENT_WALLET) {
                    "Wallet is managed from Rideit Wallet"
                } else {
                    "Payment method loaded"
                }
            },
            onError = { error ->
                isLoading = false
                statusMessage = error
            }
        )
    }

    val hasSavedCard = savedCardLastFour.isNotBlank()

    val methods = listOf(
        RiderPaymentMethodUi(
            type = RiderPaymentType.CASH,
            icon = "💵",
            title = "Cash",
            subtitle = "Pay directly to your driver after the ride",
            badge = "Default"
        ),
        RiderPaymentMethodUi(
            type = RiderPaymentType.CARD,
            icon = "💳",
            title = "Debit / Credit Card",
            subtitle = if (hasSavedCard) {
                "Card ending •••• $savedCardLastFour"
            } else {
                "Add a simulated card for portfolio checkout flow"
            },
            badge = if (hasSavedCard) "Saved" else "Add"
        )
    )

    fun savePaymentSelection(
        type: RiderPaymentType,
        cardLastFour: String = savedCardLastFour,
        cardHolderName: String = savedCardName
    ) {
        isSaving = true
        statusMessage = "Saving payment method..."

        FirebaseManager.saveRiderPaymentProfile(
            selectedPaymentMethod = when (type) {
                RiderPaymentType.CASH -> FirebaseManager.PAYMENT_CASH
                RiderPaymentType.CARD -> FirebaseManager.PAYMENT_CARD
            },
            cardLastFour = cardLastFour,
            cardHolderName = cardHolderName,
            walletBalance = walletBalance,
            onSuccess = {
                selectedPaymentType = type
                savedCardLastFour = cardLastFour
                savedCardName = cardHolderName
                isSaving = false
                statusMessage = when (type) {
                    RiderPaymentType.CASH -> "Cash selected and saved"
                    RiderPaymentType.CARD -> "Card selected and saved"
                }
            },
            onError = { error ->
                isSaving = false
                statusMessage = error
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050505),
                        Color(0xFF16070A),
                        Color(0xFF090909)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 34.dp, bottom = 22.dp)
        ) {
            PaymentTopBar(
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                StatusPill(
                    text = "Loading saved payment method..."
                )

                Spacer(modifier = Modifier.height(12.dp))
            } else if (statusMessage.isNotBlank()) {
                StatusPill(
                    text = statusMessage
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            PaymentHeroCard()

            Spacer(modifier = Modifier.height(22.dp))

            SelectedPaymentSummary(
                selectedPaymentType = selectedPaymentType,
                hasSavedCard = hasSavedCard,
                savedCardLastFour = savedCardLastFour
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Payment Methods",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            methods.forEach { method ->
                RiderPaymentMethodCard(
                    method = method,
                    selected = selectedPaymentType == method.type,
                    hasSavedCard = hasSavedCard,
                    enabled = !isLoading && !isSaving,
                    onClick = {
                        when (method.type) {
                            RiderPaymentType.CASH -> {
                                savePaymentSelection(RiderPaymentType.CASH)
                            }

                            RiderPaymentType.CARD -> {
                                if (hasSavedCard) {
                                    savePaymentSelection(RiderPaymentType.CARD)
                                } else {
                                    showCardDialog = true
                                }
                            }
                        }
                    },
                    onManageCardClick = {
                        showCardDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            AddOrManageCardButton(
                hasSavedCard = hasSavedCard,
                enabled = !isLoading && !isSaving,
                onClick = {
                    showCardDialog = true
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            WalletSeparationInfoCard()

            Spacer(modifier = Modifier.height(14.dp))

            PaymentInfoCard()

            Spacer(modifier = Modifier.height(14.dp))

            SecurityCard()
        }

        if (showCardDialog) {
            AddCardDialog(
                existingCardName = savedCardName,
                onDismiss = {
                    showCardDialog = false
                },
                onSaveCard = { cardName, cardNumber ->
                    val digitsOnly = cardNumber.filter { it.isDigit() }
                    val lastFour = digitsOnly.takeLast(4).ifBlank { "0000" }
                    val cleanName = cardName.trim().ifBlank { "Rideit Rider" }

                    showCardDialog = false
                    isSaving = true
                    statusMessage = "Saving card..."

                    FirebaseManager.saveRiderPaymentProfile(
                        selectedPaymentMethod = FirebaseManager.PAYMENT_CARD,
                        cardLastFour = lastFour,
                        cardHolderName = cleanName,
                        walletBalance = walletBalance,
                        onSuccess = {
                            savedCardLastFour = lastFour
                            savedCardName = cleanName
                            selectedPaymentType = RiderPaymentType.CARD
                            isSaving = false
                            statusMessage = "Card saved and selected"
                        },
                        onError = { error ->
                            isSaving = false
                            statusMessage = error
                        }
                    )
                },
                onRemoveCard = {
                    showCardDialog = false
                    isSaving = true
                    statusMessage = "Removing card..."

                    FirebaseManager.removeRiderSavedCard(
                        onSuccess = {
                            savedCardLastFour = ""
                            savedCardName = ""
                            selectedPaymentType = RiderPaymentType.CASH
                            isSaving = false
                            statusMessage = "Card removed. Cash selected"
                        },
                        onError = { error ->
                            isSaving = false
                            statusMessage = error
                        }
                    )
                },
                hasSavedCard = hasSavedCard
            )
        }
    }
}

@Composable
private fun PaymentTopBar(
    onBackClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onBackClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text(
                text = "‹ Back",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = "Payment",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Cash and card payment methods",
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Text(
            text = text,
            color = Color(0xFFE5E7EB),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PaymentHeroCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        shadowElevation = 18.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8A35F2),
                            Color(0xFF5B21B6),
                            Color(0xFF111827)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ride Payment",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "Safe Demo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Cash or Card",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose how you want to pay for rides",
                    color = Color(0xFFE5E7EB),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💳",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Composable
private fun SelectedPaymentSummary(
    selectedPaymentType: RiderPaymentType,
    hasSavedCard: Boolean,
    savedCardLastFour: String
) {
    val title = when (selectedPaymentType) {
        RiderPaymentType.CASH -> "Cash selected"
        RiderPaymentType.CARD -> if (hasSavedCard) {
            "Card ending •••• $savedCardLastFour selected"
        } else {
            "Card selected"
        }
    }

    val subtitle = when (selectedPaymentType) {
        RiderPaymentType.CASH -> "This method will be attached to your next ride request."
        RiderPaymentType.CARD -> "Only the last 4 card digits are saved for demo mode."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF17171A),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RiderPaymentMethodCard(
    method: RiderPaymentMethodUi,
    selected: Boolean,
    hasSavedCard: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onManageCardClick: () -> Unit
) {
    val purple = Color(0xFF8A35F2)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) purple else Color(0xFF2A2A31),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (selected) Color(0xFF241A35) else Color(0xFF1B1B1D),
        shadowElevation = if (selected) 10.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (selected) purple.copy(alpha = 0.22f)
                            else Color(0xFF252529)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = method.icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = method.title,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) purple else Color(0xFF2A2A31)
                        ) {
                            Text(
                                text = method.badge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = method.subtitle,
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) purple else Color(0xFF2A2A31)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selected) "✓" else "",
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (method.type == RiderPaymentType.CARD) {
                Spacer(modifier = Modifier.height(12.dp))

                Divider(
                    color = Color.White.copy(alpha = 0.08f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasSavedCard) {
                            "Manage your saved simulated card"
                        } else {
                            "Add a demo card to unlock card selection"
                        },
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    TextButton(
                        onClick = onManageCardClick,
                        enabled = enabled
                    ) {
                        Text(
                            text = if (hasSavedCard) "Manage" else "Add Card",
                            color = purple,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddOrManageCardButton(
    hasSavedCard: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            disabledContentColor = Color.White.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = if (hasSavedCard) "Manage Saved Card" else "+ Add Debit / Credit Card",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun WalletSeparationInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF17171A)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF8A35F2).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👛",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Wallet is separate",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Rideit Wallet is managed from the drawer menu. Add money and choose wallet for rides from the Wallet screen.",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PaymentInfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF121216)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Connected to ride booking",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your selected cash or card method is saved in Firebase and attached automatically when you book a ride. Card payment remains safe demo mode.",
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SecurityCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1B1B1D)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔒",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Safe Firebase storage",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Only selected method and card last 4 digits are saved. Full card numbers and CVV are never stored.",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AddCardDialog(
    existingCardName: String,
    hasSavedCard: Boolean,
    onDismiss: () -> Unit,
    onSaveCard: (String, String) -> Unit,
    onRemoveCard: () -> Unit
) {
    var cardName by rememberSaveable {
        mutableStateOf(existingCardName.ifBlank { "" })
    }

    var cardNumber by rememberSaveable {
        mutableStateOf("")
    }

    var expiry by rememberSaveable {
        mutableStateOf("")
    }

    var cvv by rememberSaveable {
        mutableStateOf("")
    }

    var errorMessage by rememberSaveable {
        mutableStateOf("")
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF141417),
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (hasSavedCard) "Manage Card" else "Add Card",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Demo card only. Only last 4 digits are saved.",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(18.dp))

                DemoCardPreview(
                    cardName = cardName,
                    cardNumber = cardNumber
                )

                Spacer(modifier = Modifier.height(18.dp))

                PaymentTextField(
                    value = cardName,
                    onValueChange = {
                        cardName = it.take(26)
                        errorMessage = ""
                    },
                    label = "Card holder name",
                    placeholder = "Adnan Khan",
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(12.dp))

                PaymentTextField(
                    value = cardNumber,
                    onValueChange = { value ->
                        cardNumber = value.filter { it.isDigit() }.take(16)
                        errorMessage = ""
                    },
                    label = "Card number",
                    placeholder = "4242 4242 4242 4242",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaymentTextField(
                        value = expiry,
                        onValueChange = { value ->
                            val clean = value.filter { it.isDigit() }.take(4)
                            expiry = when {
                                clean.length <= 2 -> clean
                                else -> clean.take(2) + "/" + clean.drop(2)
                            }
                            errorMessage = ""
                        },
                        label = "Expiry",
                        placeholder = "12/28",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )

                    PaymentTextField(
                        value = cvv,
                        onValueChange = { value ->
                            cvv = value.filter { it.isDigit() }.take(3)
                            errorMessage = ""
                        },
                        label = "CVV",
                        placeholder = "123",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val digitsOnly = cardNumber.filter { it.isDigit() }

                        when {
                            cardName.trim().length < 2 -> {
                                errorMessage = "Please enter card holder name."
                            }

                            digitsOnly.length < 12 -> {
                                errorMessage = "Please enter a valid demo card number."
                            }

                            expiry.length < 5 -> {
                                errorMessage = "Please enter expiry date."
                            }

                            cvv.length < 3 -> {
                                errorMessage = "Please enter CVV."
                            }

                            else -> {
                                onSaveCard(cardName, cardNumber)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8A35F2),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (hasSavedCard) "Update Card" else "Save Card",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasSavedCard) {
                        TextButton(
                            onClick = onRemoveCard
                        ) {
                            Text(
                                text = "Remove Card",
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DemoCardPreview(
    cardName: String,
    cardNumber: String
) {
    val digitsOnly = cardNumber.filter { it.isDigit() }
    val lastFour = digitsOnly.takeLast(4).ifBlank { "0000" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF111827),
                            Color(0xFF37235C),
                            Color(0xFF8A35F2)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rideit Card",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "DEMO",
                        color = Color.White.copy(alpha = 0.86f),
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "••••  ••••  ••••  $lastFour",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = cardName.trim().ifBlank { "CARD HOLDER" }.uppercase(),
                    color = Color.White.copy(alpha = 0.86f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun PaymentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(label)
        },
        placeholder = {
            Text(placeholder)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF8A35F2),
            unfocusedBorderColor = Color(0xFF34343A),
            cursorColor = Color(0xFF8A35F2),
            focusedLabelColor = Color(0xFFB58CFF),
            unfocusedLabelColor = Color(0xFF9CA3AF),
            focusedPlaceholderColor = Color(0xFF6B7280),
            unfocusedPlaceholderColor = Color(0xFF6B7280)
        )
    )
}