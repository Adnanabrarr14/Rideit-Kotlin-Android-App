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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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

@Immutable
private data class PaymentThemeColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val innerCard: Color,
    val iconCard: Color,
    val primary: Color,
    val secondary: Color,
    val text: Color,
    val subText: Color,
    val border: Color,
    val success: Color,
    val danger: Color,
    val onPrimary: Color,
    val heroEnd: Color
)

@Composable
fun PaymentScreen(
    onBackClick: () -> Unit
) {
    val colors = rememberPaymentThemeColors()

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
                    ""
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
                "Add a demo card for portfolio checkout."
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
                        colors.backgroundTop,
                        colors.backgroundMiddle,
                        colors.backgroundBottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 22.dp, bottom = 22.dp)
        ) {
            PaymentTopBar(
                colors = colors,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                StatusPill(
                    text = "Loading saved payment method...",
                    colors = colors
                )

                Spacer(modifier = Modifier.height(12.dp))
            } else if (statusMessage.isNotBlank()) {
                StatusPill(
                    text = statusMessage,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            PaymentHeroCard(colors = colors)

            Spacer(modifier = Modifier.height(22.dp))

            SelectedPaymentSummary(
                selectedPaymentType = selectedPaymentType,
                hasSavedCard = hasSavedCard,
                savedCardLastFour = savedCardLastFour,
                colors = colors
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Payment Methods",
                color = colors.text,
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
                    colors = colors,
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
                colors = colors,
                onClick = {
                    showCardDialog = true
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            WalletSeparationInfoCard(colors = colors)

            Spacer(modifier = Modifier.height(14.dp))

            PaymentInfoCard(colors = colors)

            Spacer(modifier = Modifier.height(14.dp))

            SecurityCard(colors = colors)
        }

        if (showCardDialog) {
            AddCardDialog(
                existingCardName = savedCardName,
                hasSavedCard = hasSavedCard,
                colors = colors,
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
                }
            )
        }
    }
}

@Composable
private fun PaymentTopBar(
    colors: PaymentThemeColors,
    onBackClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onBackClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.text
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
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Cash and card payment methods",
                color = colors.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    colors: PaymentThemeColors
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = colors.card,
        shadowElevation = 6.dp
    ) {
        Text(
            text = text,
            color = colors.text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PaymentHeroCard(
    colors: PaymentThemeColors
) {
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
                            colors.primary,
                            colors.secondary,
                            colors.heroEnd
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
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "Safe Demo",
                            color = colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Cash or Card",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose how you want to pay for rides",
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
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
    savedCardLastFour: String,
    colors: PaymentThemeColors
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
        color = colors.card,
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
                    .background(colors.success.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = colors.success,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = colors.subText,
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
    colors: PaymentThemeColors,
    onClick: () -> Unit,
    onManageCardClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.primary else colors.border,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (selected && method.type != RiderPaymentType.CASH) {
            colors.primary.copy(alpha = 0.12f)
        } else {
            colors.card
        },
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
                            if (selected && method.type != RiderPaymentType.CASH) {
                                colors.primary.copy(alpha = 0.20f)
                            } else {
                                colors.iconCard
                            }
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
                            color = colors.text,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) colors.primary else colors.innerCard
                        ) {
                            Text(
                                text = method.badge,
                                color = if (selected) colors.onPrimary else colors.text,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = method.subtitle,
                        color = colors.subText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) colors.primary else colors.innerCard
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selected) "✓" else "",
                        color = if (selected) colors.onPrimary else colors.subText,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (method.type == RiderPaymentType.CARD) {
                Spacer(modifier = Modifier.height(12.dp))

                Divider(
                    color = colors.border
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasSavedCard) {
                            "Manage your saved demo card"
                        } else {
                            "Add a demo card to unlock card selection"
                        },
                        color = colors.subText,
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
                            color = colors.primary,
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
    colors: PaymentThemeColors,
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
            contentColor = colors.text,
            disabledContentColor = colors.subText.copy(alpha = 0.4f)
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
private fun WalletSeparationInfoCard(
    colors: PaymentThemeColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.primary.copy(alpha = 0.16f)),
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
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Rideit Wallet is managed from the drawer menu. Add money and choose wallet for rides from the Wallet screen.",
                    color = colors.subText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PaymentInfoCard(
    colors: PaymentThemeColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Connected to ride booking",
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your selected cash or card method is attached automatically when you book a ride. Card payment remains safe demo mode.",
                color = colors.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SecurityCard(
    colors: PaymentThemeColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(colors.success.copy(alpha = 0.14f)),
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
                    text = "Safe demo storage",
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Only selected method and card last 4 digits are saved. Full card numbers and CVV are never stored.",
                    color = colors.subText,
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
    colors: PaymentThemeColors,
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
            color = colors.card,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (hasSavedCard) "Manage Card" else "Add Card",
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Demo card only. Only last 4 digits are saved.",
                    color = colors.subText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(18.dp))

                DemoCardPreview(
                    cardName = cardName,
                    cardNumber = cardNumber,
                    colors = colors
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
                    keyboardType = KeyboardType.Text,
                    colors = colors
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
                    keyboardType = KeyboardType.Number,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row {
                    PaymentTextField(
                        value = expiry,
                        onValueChange = {
                            expiry = it.take(5)
                            errorMessage = ""
                        },
                        label = "Expiry",
                        placeholder = "12/28",
                        keyboardType = KeyboardType.Text,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    PaymentTextField(
                        value = cvv,
                        onValueChange = {
                            cvv = it.filter { char -> char.isDigit() }.take(4)
                            errorMessage = ""
                        },
                        label = "CVV",
                        placeholder = "123",
                        keyboardType = KeyboardType.Number,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = errorMessage,
                        color = colors.danger,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val digitsOnly = cardNumber.filter { it.isDigit() }

                        if (cardName.trim().isBlank()) {
                            errorMessage = "Please enter card holder name"
                            return@Button
                        }

                        if (digitsOnly.length < 12) {
                            errorMessage = "Please enter a valid demo card number"
                            return@Button
                        }

                        onSaveCard(cardName, cardNumber)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text(
                        text = "Save Demo Card",
                        fontWeight = FontWeight.Black
                    )
                }

                if (hasSavedCard) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onRemoveCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.danger
                        )
                    ) {
                        Text(
                            text = "Remove Saved Card",
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = colors.subText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoCardPreview(
    cardName: String,
    cardNumber: String,
    colors: PaymentThemeColors
) {
    val digits = cardNumber.filter { it.isDigit() }
    val lastFour = digits.takeLast(4).ifBlank { "0000" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colors.primary,
                            colors.secondary,
                            colors.heroEnd
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Text(
                    text = "Rideit Demo Card",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "••••  ••••  ••••  $lastFour",
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = cardName.ifBlank { "CARD HOLDER" },
                    color = Color.White.copy(alpha = 0.88f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
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
    colors: PaymentThemeColors,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.text,
            unfocusedTextColor = colors.text,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.border,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.subText,
            cursorColor = colors.primary,
            focusedPlaceholderColor = colors.subText,
            unfocusedPlaceholderColor = colors.subText
        )
    )
}

@Composable
private fun rememberPaymentThemeColors(): PaymentThemeColors {
    val scheme = MaterialTheme.colorScheme

    val isRoseTheme =
        scheme.primary == Color(0xFFFF5CA8) ||
                scheme.primary == Color(0xFFEC4899) ||
                scheme.primaryContainer == Color(0xFFFFD6E8)

    val isLightTheme = scheme.background.luminance() > 0.5f

    return remember(scheme.primary, scheme.background) {
        when {
            isRoseTheme -> PaymentThemeColors(
                backgroundTop = Color(0xFFFFF7FB),
                backgroundMiddle = Color(0xFFFFEAF3),
                backgroundBottom = Color(0xFFFFFBFD),
                card = Color.White,
                innerCard = Color(0xFFFFEAF3),
                iconCard = Color(0xFFFFD6E8),
                primary = Color(0xFFFF5CA8),
                secondary = Color(0xFFEC4899),
                text = Color(0xFF24111A),
                subText = Color(0xFF7A445A),
                border = Color(0xFFF9A8D4),
                success = Color(0xFF16A34A),
                danger = Color(0xFFE11D48),
                onPrimary = Color.White,
                heroEnd = Color(0xFFBE185D)
            )

            isLightTheme -> PaymentThemeColors(
                backgroundTop = Color(0xFFF8FAFC),
                backgroundMiddle = Color(0xFFEDE9FE),
                backgroundBottom = Color.White,
                card = Color.White,
                innerCard = Color(0xFFF1F5F9),
                iconCard = Color(0xFFEBDDFF),
                primary = scheme.primary,
                secondary = Color(0xFF5B21B6),
                text = Color(0xFF111827),
                subText = Color(0xFF6B7280),
                border = Color(0xFFE5E7EB),
                success = Color(0xFF16A34A),
                danger = Color(0xFFEF4444),
                onPrimary = Color.White,
                heroEnd = Color(0xFF111827)
            )

            else -> PaymentThemeColors(
                backgroundTop = Color(0xFF050505),
                backgroundMiddle = Color(0xFF16070A),
                backgroundBottom = Color(0xFF090909),
                card = Color(0xFF1B1B1D),
                innerCard = Color(0xFF252529),
                iconCard = Color(0xFF2A2138),
                primary = Color(0xFF8A35F2),
                secondary = Color(0xFF5B21B6),
                text = Color.White,
                subText = Color(0xFF9CA3AF),
                border = Color(0xFF2A2A31),
                success = Color(0xFF22C55E),
                danger = Color(0xFFEF4444),
                onPrimary = Color.White,
                heroEnd = Color(0xFF111827)
            )
        }
    }
}
