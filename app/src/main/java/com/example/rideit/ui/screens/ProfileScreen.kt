package com.example.rideit.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rideit.FirebaseManager

@Immutable
private data class ProfileThemeColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val innerCard: Color,
    val iconCard: Color,
    val primary: Color,
    val text: Color,
    val subText: Color,
    val buttonText: Color,
    val logout: Color,
    val success: Color,
    val border: Color
)

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val colors = rememberProfileThemeColors()

    var fullName by rememberSaveable {
        mutableStateOf(FirebaseManager.currentUserDisplayName("Rideit User"))
    }

    var email by rememberSaveable {
        mutableStateOf(FirebaseManager.currentUserEmail() ?: "No email found")
    }

    var phoneNumber by rememberSaveable {
        mutableStateOf("")
    }

    var accountRole by rememberSaveable {
        mutableStateOf("Rider")
    }

    var statusMessage by rememberSaveable {
        mutableStateOf("Loading profile...")
    }

    var isLoading by rememberSaveable {
        mutableStateOf(true)
    }

    var isSaving by rememberSaveable {
        mutableStateOf(false)
    }

    var showEditDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        FirebaseManager.loadCurrentUserProfile(
            onSuccess = { profile ->
                fullName = profile.fullName.ifBlank {
                    FirebaseManager.currentUserDisplayName("Rideit User")
                }
                email = profile.email.ifBlank {
                    FirebaseManager.currentUserEmail() ?: "No email found"
                }
                phoneNumber = profile.phoneNumber
                accountRole = if (profile.role == FirebaseManager.ROLE_DRIVER) {
                    "Driver"
                } else {
                    "Rider"
                }
                isLoading = false
                statusMessage = "Profile loaded"
            },
            onError = { error ->
                isLoading = false
                statusMessage = error
            }
        )
    }

    val avatarLetter = fullName
        .trim()
        .firstOrNull()
        ?.uppercase()
        ?: "R"

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
                .padding(top = 22.dp, bottom = 20.dp)
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

                Text(
                    text = "Profile",
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ProfileStatusPill(
                text = if (isLoading) "Loading profile..." else statusMessage,
                colors = colors
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                color = colors.card,
                shadowElevation = 18.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarLetter,
                            color = colors.buttonText,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = fullName,
                        color = colors.text,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = email,
                        color = colors.subText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showEditDialog = true
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.buttonText
                        )
                    ) {
                        Text(
                            text = "Edit Profile",
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    ProfileInfoRow(
                        title = "Account Type",
                        value = accountRole,
                        colors = colors
                    )

                    ProfileInfoRow(
                        title = "Phone Number",
                        value = phoneNumber.ifBlank { "Not added" },
                        colors = colors
                    )

                    ProfileInfoRow(
                        title = "Member Since",
                        value = "2026",
                        colors = colors
                    )

                    ProfileInfoRow(
                        title = "Rating",
                        value = "⭐ 4.9",
                        colors = colors
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = colors.card,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Account Settings",
                        color = colors.text,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingRow(
                        icon = "👤",
                        title = "Edit Profile",
                        subtitle = "Update your name and phone number",
                        colors = colors,
                        onClick = {
                            showEditDialog = true
                        }
                    )

                    SettingRow(
                        icon = "📱",
                        title = "Phone Number",
                        subtitle = phoneNumber.ifBlank { "Add your phone number" },
                        colors = colors,
                        onClick = {
                            showEditDialog = true
                        }
                    )

                    SettingRow(
                        icon = "📍",
                        title = "Saved Places",
                        subtitle = "Home, Work and favorites",
                        colors = colors,
                        onClick = {}
                    )

                    SettingRow(
                        icon = "🔐",
                        title = "Security",
                        subtitle = "Password and login settings",
                        colors = colors,
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.logout,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Logout",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                initialName = fullName,
                initialPhone = phoneNumber,
                colors = colors,
                onDismiss = {
                    showEditDialog = false
                },
                onSave = { newName, newPhone ->
                    isSaving = true
                    statusMessage = "Saving profile..."

                    FirebaseManager.updateCurrentUserProfile(
                        fullName = newName,
                        phoneNumber = newPhone,
                        onSuccess = {
                            fullName = newName.trim()
                            phoneNumber = newPhone.trim()
                            isSaving = false
                            statusMessage = "Profile updated"
                            showEditDialog = false
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
private fun ProfileStatusPill(
    text: String,
    colors: ProfileThemeColors
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
private fun ProfileInfoRow(
    title: String,
    value: String,
    colors: ProfileThemeColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp),
        color = colors.innerCard
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = colors.subText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SettingRow(
    icon: String,
    title: String,
    subtitle: String,
    colors: ProfileThemeColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.iconCard),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = colors.text,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = subtitle,
                color = colors.subText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    initialName: String,
    initialPhone: String,
    colors: ProfileThemeColors,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by rememberSaveable {
        mutableStateOf(initialName)
    }

    var phone by rememberSaveable {
        mutableStateOf(initialPhone)
    }

    var error by rememberSaveable {
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
                    text = "Edit Profile",
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Update your real name and phone number.",
                    color = colors.subText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProfileTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(40)
                        error = ""
                    },
                    label = "Full Name",
                    placeholder = "Adnan Khan",
                    colors = colors
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileTextField(
                    value = phone,
                    onValueChange = {
                        phone = it.take(24)
                        error = ""
                    },
                    label = "Phone Number",
                    placeholder = "+92 300 1234567",
                    colors = colors
                )

                if (error.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = error,
                        color = colors.logout,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.trim().length < 2) {
                            error = "Please enter a valid full name."
                            return@Button
                        }

                        onSave(name.trim(), phone.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.buttonText
                    )
                ) {
                    Text(
                        text = "Save Profile",
                        fontWeight = FontWeight.Black
                    )
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
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    colors: ProfileThemeColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
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
private fun rememberProfileThemeColors(): ProfileThemeColors {
    val scheme = MaterialTheme.colorScheme

    val isRoseTheme =
        scheme.primary == Color(0xFFFF5CA8) ||
                scheme.primary == Color(0xFFEC4899) ||
                scheme.primaryContainer == Color(0xFFFFD6E8)

    val isLightTheme = scheme.background.luminance() > 0.5f

    return remember(scheme.primary, scheme.background) {
        when {
            isRoseTheme -> ProfileThemeColors(
                backgroundTop = Color(0xFFFFF7FB),
                backgroundMiddle = Color(0xFFFFEAF3),
                backgroundBottom = Color(0xFFFFFBFD),
                card = Color.White,
                innerCard = Color(0xFFFFEAF3),
                iconCard = Color(0xFFFFD6E8),
                primary = Color(0xFFFF5CA8),
                text = Color(0xFF24111A),
                subText = Color(0xFF7A445A),
                buttonText = Color.White,
                logout = Color(0xFFE11D48),
                success = Color(0xFF16A34A),
                border = Color(0xFFF9A8D4)
            )

            isLightTheme -> ProfileThemeColors(
                backgroundTop = Color(0xFFF8FAFC),
                backgroundMiddle = Color(0xFFEDE9FE),
                backgroundBottom = Color.White,
                card = Color.White,
                innerCard = Color(0xFFF1F5F9),
                iconCard = Color(0xFFEBDDFF),
                primary = scheme.primary,
                text = Color(0xFF111827),
                subText = Color(0xFF6B7280),
                buttonText = Color.White,
                logout = Color(0xFFEF4444),
                success = Color(0xFF16A34A),
                border = Color(0xFFE5E7EB)
            )

            else -> ProfileThemeColors(
                backgroundTop = Color(0xFF050505),
                backgroundMiddle = Color(0xFF15080B),
                backgroundBottom = Color(0xFF090909),
                card = Color(0xFF1B1B1D),
                innerCard = Color(0xFF252529),
                iconCard = Color(0xFF2A2138),
                primary = Color(0xFF8A35F2),
                text = Color.White,
                subText = Color(0xFF9CA3AF),
                buttonText = Color.White,
                logout = Color(0xFFEF4444),
                success = Color(0xFF22C55E),
                border = Color(0xFF2A2A31)
            )
        }
    }
}