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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager
import com.example.rideit.RideitThemeTokens
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Immutable
private data class RideitPreferenceOption(
    val code: String,
    val title: String,
    val subtitle: String,
    val icon: String
)

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var rideAlertsEnabled by rememberSaveable { mutableStateOf(true) }
    var promoAlertsEnabled by rememberSaveable { mutableStateOf(true) }
    var locationEnabled by rememberSaveable { mutableStateOf(true) }

    var selectedLanguageCode by rememberSaveable { mutableStateOf("en") }
    var selectedCurrencyCode by rememberSaveable { mutableStateOf("PKR") }
    var selectedThemeMode by rememberSaveable { mutableStateOf(FirebaseManager.THEME_SYSTEM) }

    var isLoading by rememberSaveable { mutableStateOf(true) }
    var isSaving by rememberSaveable { mutableStateOf(false) }

    var activePicker by rememberSaveable { mutableStateOf<String?>(null) }

    val languageOptions = remember {
        listOf(
            RideitPreferenceOption("en", "English", "Default international language", "🇬🇧"),
            RideitPreferenceOption("ur", "Urdu", "پاکستان / اردو", "🇵🇰"),
            RideitPreferenceOption("ar", "Arabic", "العربية", "🇸🇦"),
            RideitPreferenceOption("hi", "Hindi", "हिन्दी", "🇮🇳"),
            RideitPreferenceOption("fr", "French", "Français", "🇫🇷"),
            RideitPreferenceOption("es", "Spanish", "Español", "🇪🇸"),
            RideitPreferenceOption("de", "German", "Deutsch", "🇩🇪"),
            RideitPreferenceOption("tr", "Turkish", "Türkçe", "🇹🇷"),
            RideitPreferenceOption("zh", "Chinese", "中文", "🇨🇳"),
            RideitPreferenceOption("ja", "Japanese", "日本語", "🇯🇵"),
            RideitPreferenceOption("ko", "Korean", "한국어", "🇰🇷"),
            RideitPreferenceOption("pt", "Portuguese", "Português", "🇵🇹"),
            RideitPreferenceOption("ru", "Russian", "Русский", "🇷🇺"),
            RideitPreferenceOption("bn", "Bengali", "বাংলা", "🇧🇩"),
            RideitPreferenceOption("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
            RideitPreferenceOption("ms", "Malay", "Bahasa Melayu", "🇲🇾")
        )
    }

    val currencyOptions = remember {
        listOf(
            RideitPreferenceOption("PKR", "PKR — Pakistani Rupee", "Rs. 580", "🇵🇰"),
            RideitPreferenceOption("USD", "USD — US Dollar", "$2.10", "🇺🇸"),
            RideitPreferenceOption("EUR", "EUR — Euro", "€1.95", "🇪🇺"),
            RideitPreferenceOption("GBP", "GBP — British Pound", "£1.70", "🇬🇧"),
            RideitPreferenceOption("AED", "AED — UAE Dirham", "د.إ 7.70", "🇦🇪"),
            RideitPreferenceOption("SAR", "SAR — Saudi Riyal", "ر.س 7.90", "🇸🇦"),
            RideitPreferenceOption("INR", "INR — Indian Rupee", "₹175", "🇮🇳"),
            RideitPreferenceOption("CAD", "CAD — Canadian Dollar", "C$2.85", "🇨🇦"),
            RideitPreferenceOption("AUD", "AUD — Australian Dollar", "A$3.20", "🇦🇺"),
            RideitPreferenceOption("TRY", "TRY — Turkish Lira", "₺68", "🇹🇷")
        )
    }

    val themeOptions = remember {
        listOf(
            RideitPreferenceOption(FirebaseManager.THEME_SYSTEM, "System Default", "Follow device light/dark mode", "📱"),
            RideitPreferenceOption(FirebaseManager.THEME_LIGHT, "Light Mode", "Use bright app appearance", "☀️"),
            RideitPreferenceOption(FirebaseManager.THEME_DARK, "Dark Mode", "Use dark premium app appearance", "🌙"),
            RideitPreferenceOption(FirebaseManager.THEME_ROSE, "Rose / Pink Theme", "Soft pink theme for a personalized Rideit experience", "🌸")
        )
    }

    val selectedLanguage = languageOptions.firstOrNull { it.code == selectedLanguageCode }
        ?: languageOptions.first()

    val selectedCurrency = currencyOptions.firstOrNull { it.code == selectedCurrencyCode }
        ?: currencyOptions.first()

    val selectedTheme = themeOptions.firstOrNull { it.code == selectedThemeMode }
        ?: themeOptions.first()

    fun saveSettings(
        languageCode: String = selectedLanguageCode,
        currencyCode: String = selectedCurrencyCode,
        themeMode: String = selectedThemeMode,
        rideAlerts: Boolean = rideAlertsEnabled,
        promoAlerts: Boolean = promoAlertsEnabled,
        locationAccess: Boolean = locationEnabled
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            isSaving = false
            return
        }

        isSaving = true

        val safeThemeMode = when (themeMode.trim().lowercase()) {
            FirebaseManager.THEME_LIGHT -> FirebaseManager.THEME_LIGHT
            FirebaseManager.THEME_DARK -> FirebaseManager.THEME_DARK
            FirebaseManager.THEME_ROSE -> FirebaseManager.THEME_ROSE
            else -> FirebaseManager.THEME_SYSTEM
        }

        val settingsData = hashMapOf<String, Any>(
            "preferredLanguageCode" to languageCode,
            "preferredCurrencyCode" to currencyCode,
            "preferredThemeMode" to safeThemeMode,
            "rideAlertsEnabled" to rideAlerts,
            "promoAlertsEnabled" to promoAlerts,
            "locationPreferenceEnabled" to locationAccess,
            "settingsUpdatedAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now()
        )

        firestore.collection("users")
            .document(currentUser.uid)
            .set(settingsData, SetOptions.merge())
            .addOnSuccessListener {
                selectedLanguageCode = languageCode
                selectedCurrencyCode = currencyCode
                selectedThemeMode = safeThemeMode
                rideAlertsEnabled = rideAlerts
                promoAlertsEnabled = promoAlerts
                locationEnabled = locationAccess
                isSaving = false
            }
            .addOnFailureListener {
                isSaving = false
            }
    }

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                selectedLanguageCode = snapshot.getString("preferredLanguageCode") ?: "en"
                selectedCurrencyCode = snapshot.getString("preferredCurrencyCode") ?: "PKR"

                selectedThemeMode = when (
                    snapshot.getString("preferredThemeMode")
                        ?.trim()
                        ?.lowercase()
                ) {
                    FirebaseManager.THEME_LIGHT -> FirebaseManager.THEME_LIGHT
                    FirebaseManager.THEME_DARK -> FirebaseManager.THEME_DARK
                    FirebaseManager.THEME_ROSE -> FirebaseManager.THEME_ROSE
                    else -> FirebaseManager.THEME_SYSTEM
                }

                rideAlertsEnabled = snapshot.getBoolean("rideAlertsEnabled") ?: true
                promoAlertsEnabled = snapshot.getBoolean("promoAlertsEnabled") ?: true
                locationEnabled = snapshot.getBoolean("locationPreferenceEnabled") ?: true

                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    val backgroundColors = when (selectedThemeMode) {
        FirebaseManager.THEME_LIGHT -> listOf(
            Color(0xFFF8FAFC),
            Color(0xFFEDE9FE),
            Color.White
        )

        FirebaseManager.THEME_ROSE -> listOf(
            Color(0xFFFFF7FB),
            Color(0xFFFFEAF3),
            Color(0xFFFFFBFD)
        )

        else -> listOf(
            Color(0xFF050505),
            Color(0xFF15080B),
            Color(0xFF090909)
        )
    }

    val cardColor = when (selectedThemeMode) {
        FirebaseManager.THEME_LIGHT -> Color.White
        FirebaseManager.THEME_ROSE -> Color(0xFFFFFBFD)
        else -> Color(0xFF1B1B1D)
    }

    val textColor = when (selectedThemeMode) {
        FirebaseManager.THEME_LIGHT -> Color(0xFF111827)
        FirebaseManager.THEME_ROSE -> Color(0xFF24111A)
        else -> Color.White
    }

    val subTextColor = when (selectedThemeMode) {
        FirebaseManager.THEME_LIGHT -> Color(0xFF6B7280)
        FirebaseManager.THEME_ROSE -> Color(0xFF7A445A)
        else -> Color(0xFF9CA3AF)
    }

    val iconBackgroundColor = when (selectedThemeMode) {
        FirebaseManager.THEME_LIGHT -> Color(0xFFEBDDFF)
        FirebaseManager.THEME_ROSE -> RideitThemeTokens.RoseSurfaceVariant
        else -> Color(0xFF2A2138)
    }

    val iconTextColor = when (selectedThemeMode) {
        FirebaseManager.THEME_LIGHT -> Color(0xFF8A35F2)
        FirebaseManager.THEME_ROSE -> RideitThemeTokens.RosePrimary
        else -> Color.White
    }

    val isLightLikeMode = selectedThemeMode == FirebaseManager.THEME_LIGHT ||
            selectedThemeMode == FirebaseManager.THEME_ROSE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = backgroundColors
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
            SettingsTopBar(
                titleColor = textColor,
                subtitleColor = subTextColor,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = cardColor,
                shadowElevation = 18.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "International Preferences",
                        color = textColor,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Language, currency and appearance for rider and driver experience.",
                        color = subTextColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    PreferenceNavigationRow(
                        icon = selectedLanguage.icon,
                        title = "Language",
                        subtitle = "${selectedLanguage.title} • ${selectedLanguage.subtitle}",
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor,
                        onClick = {
                            activePicker = "language"
                        }
                    )

                    PreferenceNavigationRow(
                        icon = selectedCurrency.icon,
                        title = "Currency",
                        subtitle = selectedCurrency.title,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor,
                        onClick = {
                            activePicker = "currency"
                        }
                    )

                    PreferenceNavigationRow(
                        icon = selectedTheme.icon,
                        title = "Theme Mode",
                        subtitle = selectedTheme.title,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor,
                        onClick = {
                            activePicker = "theme"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = cardColor,
                shadowElevation = 18.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Notifications & Access",
                        color = textColor,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    ToggleSettingRow(
                        icon = "🔔",
                        title = "Ride Alerts",
                        subtitle = "Driver and trip updates",
                        checked = rideAlertsEnabled,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor,
                        enabled = !isLoading && !isSaving,
                        onCheckedChange = { checked ->
                            saveSettings(
                                rideAlerts = checked
                            )
                        }
                    )

                    ToggleSettingRow(
                        icon = "🎁",
                        title = "Promotions",
                        subtitle = "Discounts and offers",
                        checked = promoAlertsEnabled,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor,
                        enabled = !isLoading && !isSaving,
                        onCheckedChange = { checked ->
                            saveSettings(
                                promoAlerts = checked
                            )
                        }
                    )

                    ToggleSettingRow(
                        icon = "📍",
                        title = "Location Access",
                        subtitle = "Better pickup and dropoff suggestions",
                        checked = locationEnabled,
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor,
                        enabled = !isLoading && !isSaving,
                        onCheckedChange = { checked ->
                            saveSettings(
                                locationAccess = checked
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = cardColor,
                shadowElevation = 18.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Safety & App",
                        color = textColor,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    StaticSettingRow(
                        icon = "🛡️",
                        title = "Privacy",
                        subtitle = "Manage data and permissions",
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor
                    )

                    StaticSettingRow(
                        icon = "🚨",
                        title = "Emergency Contacts",
                        subtitle = "Add trusted contacts",
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor
                    )

                    StaticSettingRow(
                        icon = "ℹ️",
                        title = "About Rideit",
                        subtitle = "Version 1.0.0 • Portfolio Release",
                        textColor = textColor,
                        subTextColor = subTextColor,
                        iconBackgroundColor = iconBackgroundColor,
                        iconTextColor = iconTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsFooterCard(
                textColor = textColor,
                subTextColor = subTextColor,
                lightMode = isLightLikeMode,
                roseMode = selectedThemeMode == FirebaseManager.THEME_ROSE
            )
        }

        activePicker?.let { pickerType ->
            val pickerTitle = when (pickerType) {
                "language" -> "Select Language"
                "currency" -> "Select Currency"
                "theme" -> "Select Theme Mode"
                else -> "Select Preference"
            }

            val pickerOptions = when (pickerType) {
                "language" -> languageOptions
                "currency" -> currencyOptions
                "theme" -> themeOptions
                else -> emptyList()
            }

            val selectedCode = when (pickerType) {
                "language" -> selectedLanguageCode
                "currency" -> selectedCurrencyCode
                "theme" -> selectedThemeMode
                else -> ""
            }

            PreferencePickerOverlay(
                title = pickerTitle,
                options = pickerOptions,
                selectedCode = selectedCode,
                lightMode = isLightLikeMode,
                roseMode = selectedThemeMode == FirebaseManager.THEME_ROSE,
                onDismiss = {
                    activePicker = null
                },
                onOptionSelected = { option ->
                    activePicker = null

                    when (pickerType) {
                        "language" -> {
                            saveSettings(
                                languageCode = option.code
                            )
                        }

                        "currency" -> {
                            saveSettings(
                                currencyCode = option.code
                            )
                        }

                        "theme" -> {
                            saveSettings(
                                themeMode = option.code
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsTopBar(
    titleColor: Color,
    subtitleColor: Color,
    onBackClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = onBackClick,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = titleColor
            )
        ) {
            Text(
                text = "‹ Back",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Settings",
                color = titleColor,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Language, currency and theme",
                color = subtitleColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PreferenceNavigationRow(
    icon: String,
    title: String,
    subtitle: String,
    textColor: Color,
    subTextColor: Color,
    iconBackgroundColor: Color,
    iconTextColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(
            icon = icon,
            backgroundColor = iconBackgroundColor,
            iconTextColor = iconTextColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = subtitle,
                color = subTextColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "›",
            color = subTextColor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ToggleSettingRow(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    textColor: Color,
    subTextColor: Color,
    iconBackgroundColor: Color,
    iconTextColor: Color,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(
            icon = icon,
            backgroundColor = iconBackgroundColor,
            iconTextColor = iconTextColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = subtitle,
                color = subTextColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun StaticSettingRow(
    icon: String,
    title: String,
    subtitle: String,
    textColor: Color,
    subTextColor: Color,
    iconBackgroundColor: Color,
    iconTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(
            icon = icon,
            backgroundColor = iconBackgroundColor,
            iconTextColor = iconTextColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = subtitle,
                color = subTextColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "›",
            color = subTextColor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingIcon(
    icon: String,
    backgroundColor: Color,
    iconTextColor: Color
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            color = iconTextColor
        )
    }
}

@Composable
private fun SettingsFooterCard(
    textColor: Color,
    subTextColor: Color,
    lightMode: Boolean,
    roseMode: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = when {
            roseMode -> Color.White
            lightMode -> Color.White
            else -> Color(0xFF241A35)
        },
        shadowElevation = if (lightMode) 10.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (roseMode) RideitThemeTokens.RosePrimary else Color(0xFF8A35F2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "R",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Rideit",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Build 1.0.0 • Launch-ready preferences",
                    color = subTextColor,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PreferencePickerOverlay(
    title: String,
    options: List<RideitPreferenceOption>,
    selectedCode: String,
    lightMode: Boolean,
    roseMode: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (RideitPreferenceOption) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.54f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onDismiss()
                }
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            shape = RoundedCornerShape(32.dp),
            color = when {
                roseMode -> Color(0xFFFFFBFD)
                lightMode -> Color.White
                else -> Color(0xFF141417)
            },
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD1D5DB))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = if (lightMode) Color(0xFF111827) else Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clickable {
                                onDismiss()
                            },
                        shape = CircleShape,
                        color = if (lightMode) Color(0xFFF3F4F6) else Color.White.copy(alpha = 0.08f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "×",
                                color = if (lightMode) Color(0xFF111827) else Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    options.forEach { option ->
                        PreferenceOptionRow(
                            option = option,
                            selected = option.code == selectedCode,
                            lightMode = lightMode,
                            roseMode = roseMode,
                            onClick = {
                                onOptionSelected(option)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceOptionRow(
    option: RideitPreferenceOption,
    selected: Boolean,
    lightMode: Boolean,
    roseMode: Boolean,
    onClick: () -> Unit
) {
    val selectedColor = if (roseMode) RideitThemeTokens.RosePrimary else Color(0xFF8A35F2)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) selectedColor else Color(0xFF2A2A31),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = when {
            selected && roseMode -> Color(0xFFFFEAF3)
            roseMode -> Color.White
            selected && lightMode -> Color(0xFFF3EEFF)
            selected -> Color(0xFF241A35)
            lightMode -> Color(0xFFF8FAFC)
            else -> Color(0xFF1B1B1D)
        }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (selected) {
                            selectedColor.copy(alpha = 0.20f)
                        } else if (roseMode) {
                            Color(0xFFFFD6E8)
                        } else if (lightMode) {
                            Color(0xFFEBDDFF)
                        } else {
                            Color(0xFF2A2138)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = option.icon)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    color = if (lightMode) Color(0xFF111827) else Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = option.subtitle,
                    color = if (lightMode) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = selectedColor
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
