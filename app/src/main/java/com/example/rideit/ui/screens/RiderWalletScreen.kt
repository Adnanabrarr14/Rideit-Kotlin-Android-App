package com.example.rideit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager

@Immutable
private data class WalletTopUpOption(
    val amount: Long,
    val label: String,
    val subtitle: String
)

@Composable
fun RiderWalletScreen(
    onBackClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit
) {
    var walletBalance by rememberSaveable {
        mutableStateOf(1250L)
    }

    var selectedPaymentMethod by rememberSaveable {
        mutableStateOf(FirebaseManager.PAYMENT_CASH)
    }

    var savedCardLastFour by rememberSaveable {
        mutableStateOf("")
    }

    var savedCardName by rememberSaveable {
        mutableStateOf("")
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

    var transactionOne by rememberSaveable {
        mutableStateOf("Welcome bonus • +Rs. 1,250 • Demo credit")
    }

    var transactionTwo by rememberSaveable {
        mutableStateOf("Wallet created • Safe demo mode • No real money")
    }

    var transactionThree by rememberSaveable {
        mutableStateOf("Payment system ready • Cash/Card/Wallet")
    }

    LaunchedEffect(Unit) {
        FirebaseManager.loadRiderPaymentProfile(
            onSuccess = { profile ->
                walletBalance = profile.walletBalance
                selectedPaymentMethod = profile.selectedPaymentMethod
                savedCardLastFour = profile.cardLastFour
                savedCardName = profile.cardHolderName
                isLoading = false
                statusMessage = "Wallet loaded from Firebase"
            },
            onError = { error ->
                isLoading = false
                statusMessage = error
            }
        )
    }

    val topUpOptions = listOf(
        WalletTopUpOption(
            amount = 500L,
            label = "Rs. 500",
            subtitle = "Small city rides"
        ),
        WalletTopUpOption(
            amount = 1000L,
            label = "Rs. 1,000",
            subtitle = "Daily wallet boost"
        ),
        WalletTopUpOption(
            amount = 2500L,
            label = "Rs. 2,500",
            subtitle = "Weekly ride balance"
        )
    )

    fun saveWalletBalance(
        newBalance: Long,
        topUpAmount: Long
    ) {
        isSaving = true
        statusMessage = "Adding demo wallet balance..."

        FirebaseManager.saveRiderPaymentProfile(
            selectedPaymentMethod = selectedPaymentMethod,
            cardLastFour = savedCardLastFour,
            cardHolderName = savedCardName,
            walletBalance = newBalance,
            onSuccess = {
                walletBalance = newBalance
                isSaving = false
                statusMessage = "Rs. $topUpAmount added to Rideit Wallet"

                transactionThree = transactionTwo
                transactionTwo = transactionOne
                transactionOne = "Demo top-up • +Rs. $topUpAmount • Balance Rs. $newBalance"
            },
            onError = { error ->
                isSaving = false
                statusMessage = error
            }
        )
    }

    fun selectWalletForRides() {
        isSaving = true
        statusMessage = "Selecting Rideit Wallet..."

        FirebaseManager.saveRiderPaymentProfile(
            selectedPaymentMethod = FirebaseManager.PAYMENT_WALLET,
            cardLastFour = savedCardLastFour,
            cardHolderName = savedCardName,
            walletBalance = walletBalance,
            onSuccess = {
                selectedPaymentMethod = FirebaseManager.PAYMENT_WALLET
                isSaving = false
                statusMessage = "Rideit Wallet selected for rides"

                transactionThree = transactionTwo
                transactionTwo = transactionOne
                transactionOne = "Payment selected • Rideit Wallet • Ready for booking"
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
                        Color(0xFF130817),
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
            WalletTopBar(
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                WalletStatusPill(text = "Loading wallet...")
                Spacer(modifier = Modifier.height(12.dp))
            } else if (statusMessage.isNotBlank()) {
                WalletStatusPill(text = statusMessage)
                Spacer(modifier = Modifier.height(12.dp))
            }

            WalletHeroCard(
                walletBalance = walletBalance,
                selectedPaymentMethod = selectedPaymentMethod
            )

            Spacer(modifier = Modifier.height(18.dp))

            WalletActionCard(
                selectedPaymentMethod = selectedPaymentMethod,
                enabled = !isLoading && !isSaving,
                onSelectWalletClick = {
                    selectWalletForRides()
                },
                onPaymentMethodsClick = onPaymentMethodsClick
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Demo Top-up",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            topUpOptions.forEach { option ->
                WalletTopUpCard(
                    option = option,
                    enabled = !isLoading && !isSaving,
                    onClick = {
                        saveWalletBalance(
                            newBalance = walletBalance + option.amount,
                            topUpAmount = option.amount
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            WalletTransactionHistory(
                transactionOne = transactionOne,
                transactionTwo = transactionTwo,
                transactionThree = transactionThree
            )

            Spacer(modifier = Modifier.height(14.dp))

            WalletSafetyCard()
        }
    }
}

@Composable
private fun WalletTopBar(
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
                text = "Rideit Wallet",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Balance, top-up and wallet payment",
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WalletStatusPill(
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
private fun WalletHeroCard(
    walletBalance: Long,
    selectedPaymentMethod: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.Transparent,
        shadowElevation = 20.dp
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
                        text = "Available Balance",
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
                    text = "Rs. $walletBalance",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (selectedPaymentMethod == FirebaseManager.PAYMENT_WALLET) {
                        "Wallet is selected for your next ride"
                    } else {
                        "Select wallet to use this balance for bookings"
                    },
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
                    text = "👛",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

@Composable
private fun WalletActionCard(
    selectedPaymentMethod: String,
    enabled: Boolean,
    onSelectWalletClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF17171A),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Wallet Payment",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedPaymentMethod == FirebaseManager.PAYMENT_WALLET) {
                    "Rideit Wallet is currently selected. New ride bookings will attach wallet as the payment method."
                } else {
                    "You can select Rideit Wallet as your active payment method for upcoming rides."
                },
                color = Color(0xFF9CA3AF),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onSelectWalletClick,
                    enabled = enabled,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (selectedPaymentMethod == FirebaseManager.PAYMENT_WALLET) {
                            "Wallet Selected"
                        } else {
                            "Use Wallet"
                        },
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                OutlinedButton(
                    onClick = onPaymentMethodsClick,
                    enabled = enabled,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = "Payment Methods",
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletTopUpCard(
    option: WalletTopUpOption,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val purple = Color(0xFF8A35F2)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF2A2A31),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1B1B1D),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(purple.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = option.subtitle,
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = purple
            ) {
                Text(
                    text = "Top up",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun WalletTransactionHistory(
    transactionOne: String,
    transactionTwo: String,
    transactionThree: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF121216)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Recent Activity",
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            WalletTransactionRow(
                icon = "⬆️",
                text = transactionOne
            )

            Spacer(modifier = Modifier.height(12.dp))

            WalletTransactionRow(
                icon = "✨",
                text = transactionTwo
            )

            Spacer(modifier = Modifier.height(12.dp))

            WalletTransactionRow(
                icon = "🔒",
                text = transactionThree
            )
        }
    }
}

@Composable
private fun WalletTransactionRow(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = Color(0xFFE5E7EB),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WalletSafetyCard() {
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
                    text = "Safe wallet demo",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Top-ups update the demo wallet balance in Firebase. No real money, bank account, payment gateway or card charging is connected yet.",
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}