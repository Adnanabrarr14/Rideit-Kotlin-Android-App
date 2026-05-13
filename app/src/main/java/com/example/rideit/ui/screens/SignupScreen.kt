package com.example.rideit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager

@Immutable
private data class SignupChoice(
    val code: String,
    val title: String,
    val icon: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignupScreen(
    accountRole: String,
    accountTitle: String,
    accountSubtitle: String,
    primaryButtonText: String,
    accentColor: Color,
    onBackClick: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var selectedGender by rememberSaveable {
        mutableStateOf(FirebaseManager.GENDER_PREFER_NOT_TO_SAY)
    }

    var selectedThemeMode by rememberSaveable {
        mutableStateOf(FirebaseManager.THEME_SYSTEM)
    }

    var userChangedTheme by rememberSaveable {
        mutableStateOf(false)
    }

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var loadingMessage by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val cardColor = Color(0xFF1B1B1D)
    val fieldBorder = Color(0xFF6B6A76)
    val isDriver = accountRole == FirebaseManager.ROLE_DRIVER

    val roleHelperText = if (isDriver) {
        "This will create a Driver account and open Driver Dashboard."
    } else {
        "This will create a Rider account and open Rider Map."
    }

    val genderChoices = remember {
        listOf(
            SignupChoice(FirebaseManager.GENDER_WOMAN, "Woman", "👩"),
            SignupChoice(FirebaseManager.GENDER_MAN, "Man", "👨"),
            SignupChoice(FirebaseManager.GENDER_PREFER_NOT_TO_SAY, "Prefer not to say", "✨"),
            SignupChoice(FirebaseManager.GENDER_OTHER, "Other", "🌍")
        )
    }

    val themeChoices = remember {
        listOf(
            SignupChoice(FirebaseManager.THEME_SYSTEM, "System", "📱"),
            SignupChoice(FirebaseManager.THEME_LIGHT, "Light", "☀️"),
            SignupChoice(FirebaseManager.THEME_DARK, "Dark", "🌙"),
            SignupChoice(FirebaseManager.THEME_ROSE, "Rose", "🌸")
        )
    }

    val signupAccentColor = when (selectedThemeMode) {
        FirebaseManager.THEME_ROSE -> Color(0xFFFF5CA8)
        FirebaseManager.THEME_DARK -> Color(0xFFB98CFF)
        FirebaseManager.THEME_LIGHT -> Color(0xFF8A35F2)
        else -> accentColor
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (selectedThemeMode == FirebaseManager.THEME_ROSE) {
                        listOf(
                            Color(0xFF1A0610),
                            Color(0xFF3B1023),
                            Color(0xFFFF5CA8),
                            Color(0xFF0B0507)
                        )
                    } else {
                        listOf(
                            Color(0xFF000000),
                            Color(0xFF070000),
                            Color(0xFF210000),
                            Color(0xFF000000)
                        )
                    }
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 18.dp)
                .clickable(enabled = !isLoading) {
                    onBackClick()
                },
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.12f)
        ) {
            Text(
                text = "‹",
                modifier = Modifier.padding(horizontal = 17.dp, vertical = 8.dp),
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 760.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Rideit",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = accountSubtitle,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = cardColor,
                shadowElevation = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = accountTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create your account with name, email, gender and preferred theme",
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = signupAccentColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = roleHelperText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = Color(0xFFE5E7EB),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    SignupTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it.take(40)
                            errorMessage = null
                        },
                        enabled = !isLoading,
                        label = "Full Name",
                        leadingIcon = "👤",
                        fieldBorder = fieldBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SignupTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        enabled = !isLoading,
                        label = "Email",
                        leadingIcon = "✉",
                        fieldBorder = fieldBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SignupPasswordField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        enabled = !isLoading,
                        label = "Password",
                        visible = passwordVisible,
                        onVisibilityChange = {
                            passwordVisible = !passwordVisible
                        },
                        fieldBorder = fieldBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SignupPasswordField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        enabled = !isLoading,
                        label = "Confirm Password",
                        visible = confirmPasswordVisible,
                        onVisibilityChange = {
                            confirmPasswordVisible = !confirmPasswordVisible
                        },
                        fieldBorder = fieldBorder
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    SignupSectionTitle(
                        title = "Gender",
                        subtitle = "Used for personalization and future safety features"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genderChoices.forEach { choice ->
                            SignupChoiceChip(
                                choice = choice,
                                selected = selectedGender == choice.code,
                                selectedColor = signupAccentColor,
                                enabled = !isLoading,
                                onClick = {
                                    selectedGender = choice.code
                                    errorMessage = null

                                    if (
                                        choice.code == FirebaseManager.GENDER_WOMAN &&
                                        !userChangedTheme
                                    ) {
                                        selectedThemeMode = FirebaseManager.THEME_ROSE
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    SignupSectionTitle(
                        title = "Preferred App Theme",
                        subtitle = "Rose is suggested for women, but anyone can choose it"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themeChoices.forEach { choice ->
                            SignupChoiceChip(
                                choice = choice,
                                selected = selectedThemeMode == choice.code,
                                selectedColor = if (choice.code == FirebaseManager.THEME_ROSE) {
                                    Color(0xFFFF5CA8)
                                } else {
                                    signupAccentColor
                                },
                                enabled = !isLoading,
                                onClick = {
                                    userChangedTheme = true
                                    selectedThemeMode = choice.code
                                    errorMessage = null
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    AnimatedVisibility(
                        visible = isLoading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = signupAccentColor,
                                trackColor = Color(0xFF2D2D32)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = loadingMessage.ifBlank { "Creating account..." },
                                color = Color(0xFFB8BBC7),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    Button(
                        enabled = !isLoading,
                        onClick = {
                            val cleanName = fullName.trim()

                            if (cleanName.length < 2) {
                                errorMessage = "Please enter your real full name"
                                return@Button
                            }

                            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                errorMessage = "Please fill all fields"
                                return@Button
                            }

                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                                return@Button
                            }

                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                return@Button
                            }

                            isLoading = true
                            loadingMessage = if (isDriver) {
                                "Creating driver account and saving profile..."
                            } else {
                                "Creating rider account and saving profile..."
                            }
                            errorMessage = null

                            FirebaseManager.signup(
                                fullName = cleanName,
                                email = email,
                                password = password,
                                role = accountRole,
                                gender = selectedGender,
                                preferredThemeMode = selectedThemeMode,
                                onSuccess = {
                                    isLoading = false
                                    loadingMessage = ""
                                    onSignupSuccess()
                                },
                                onError = {
                                    isLoading = false
                                    loadingMessage = ""
                                    errorMessage = it
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = signupAccentColor,
                            contentColor = Color.White,
                            disabledContainerColor = signupAccentColor.copy(alpha = 0.55f),
                            disabledContentColor = Color.White.copy(alpha = 0.72f)
                        )
                    ) {
                        Text(
                            text = if (isLoading) "Please wait..." else primaryButtonText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = it,
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

@Composable
private fun SignupSectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = subtitle,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SignupChoiceChip(
    choice: SignupChoice,
    selected: Boolean,
    selectedColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) selectedColor else Color(0xFF3B3B42),
                shape = RoundedCornerShape(50)
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        shape = RoundedCornerShape(50),
        color = if (selected) {
            selectedColor.copy(alpha = 0.22f)
        } else {
            Color(0xFF111113)
        }
    ) {
        Text(
            text = "${choice.icon} ${choice.title}",
            color = Color.White,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun SignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    label: String,
    leadingIcon: String,
    fieldBorder: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Text(
                text = leadingIcon,
                color = Color(0xFF6B6A76)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = signupTextFieldColors(fieldBorder = fieldBorder)
    )
}

@Composable
private fun SignupPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    label: String,
    visible: Boolean,
    onVisibilityChange: () -> Unit,
    fieldBorder: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Text(
                text = "🔒",
                color = Color(0xFF6B6A76)
            )
        },
        trailingIcon = {
            TextButton(
                enabled = enabled,
                onClick = onVisibilityChange
            ) {
                Text(
                    text = if (visible) "Hide" else "Show",
                    color = Color(0xFF8B8A96)
                )
            }
        },
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = signupTextFieldColors(fieldBorder = fieldBorder)
    )
}

@Composable
private fun signupTextFieldColors(
    fieldBorder: Color
) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Color(0xFF9CA3AF),
    focusedBorderColor = fieldBorder,
    unfocusedBorderColor = fieldBorder,
    disabledBorderColor = fieldBorder.copy(alpha = 0.55f),
    focusedLabelColor = Color(0xFF9CA3AF),
    unfocusedLabelColor = Color(0xFF6B7280),
    disabledLabelColor = Color(0xFF6B7280),
    cursorColor = Color.White
)