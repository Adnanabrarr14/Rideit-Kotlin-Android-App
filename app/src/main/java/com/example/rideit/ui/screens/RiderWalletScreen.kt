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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager

@Immutable
private data class WalletTopUpOption(
    val amount: Long,
    val label: String,
    val subtitle: String
)

@Immutable
private data class WalletThemeColors(
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
fun RiderWalletScreen(
    onBackClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit
) {
    val colors = rememberWalletThemeColors()

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
            WalletTopBar(
                colors = colors,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                WalletStatusPill(
                    text = "Loading wallet...",
                    colors = colors
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else if (statusMessage.isNotBlank()) {
                WalletStatusPill(
                    text = statusMessage,
                    colors = colors
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            WalletHeroCard(
                walletBalance = walletBalance,
                selectedPaymentMethod = selectedPaymentMethod,
                colors = colors
            )

            Spacer(modifier = Modifier.height(18.dp))

            WalletActionCard(
                selectedPaymentMethod = selectedPaymentMethod,
                enabled = !isLoading && !isSaving,
                colors = colors,
                onSelectWalletClick = {
                    selectWalletForRides()
                },
                onPaymentMethodsClick = onPaymentMethodsClick
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "Demo Top-up",
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            topUpOptions.forEach { option ->
                WalletTopUpCard(
                    option = option,
                    enabled = !isLoading && !isSaving,
                    colors = colors,
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
                transactionThree = transactionThree,
                colors = colors
            )

            Spacer(modifier = Modifier.height(14.dp))

            WalletSafetyCard(colors = colors)
        }
    }
}

@Composable
private fun WalletTopBar(
    colors: WalletThemeColors,
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
                text = "Rideit Wallet",
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Balance, top-up and wallet payment",
                color = colors.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WalletStatusPill(
    text: String,
    colors: WalletThemeColors
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
private fun WalletHeroCard(
    walletBalance: Long,
    selectedPaymentMethod: String,
    colors: WalletThemeColors
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
                        text = "Available Balance",
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
                    text = "Rs. $walletBalance",
                    color = colors.onPrimary,
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
    colors: WalletThemeColors,
    onSelectWalletClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Wallet Payment",
                color = colors.text,
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
                color = colors.subText,
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
                        contentColor = colors.text,
                        disabledContentColor = colors.subText.copy(alpha = 0.4f)
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
                        contentColor = colors.text,
                        disabledContentColor = colors.subText.copy(alpha = 0.4f)
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
    colors: WalletThemeColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        color = colors.card,
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
                    .background(colors.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = colors.primary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = option.subtitle,
                    color = colors.subText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = colors.primary
            ) {
                Text(
                    text = "Top up",
                    color = colors.onPrimary,
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
    transactionThree: String,
    colors: WalletThemeColors
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
                text = "Wallet Activity",
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(14.dp))

            WalletTransactionRow(
                icon = "💰",
                text = transactionOne,
                colors = colors
            )

            WalletTransactionRow(
                icon = "🧾",
                text = transactionTwo,
                colors = colors
            )

            WalletTransactionRow(
                icon = "✅",
                text = transactionThree,
                colors = colors
            )
        }
    }
}

@Composable
private fun WalletTransactionRow(
    icon: String,
    text: String,
    colors: WalletThemeColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.iconCard),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = colors.subText,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WalletSafetyCard(
    colors: WalletThemeColors
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
                    text = "🛡️",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Safe demo wallet",
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "This wallet is connected to Firebase for portfolio testing. No real money is charged or transferred yet.",
                    color = colors.subText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun rememberWalletThemeColors(): WalletThemeColors {
    val scheme = MaterialTheme.colorScheme

    val isRoseTheme =
        scheme.primary == Color(0xFFFF5CA8) ||
                scheme.primary == Color(0xFFEC4899) ||
                scheme.primaryContainer == Color(0xFFFFD6E8)

    val isLightTheme = scheme.background.luminance() > 0.5f

    return remember(scheme.primary, scheme.background) {
        when {
            isRoseTheme -> WalletThemeColors(
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

            isLightTheme -> WalletThemeColors(
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

            else -> WalletThemeColors(
                backgroundTop = Color(0xFF050505),
                backgroundMiddle = Color(0xFF130817),
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