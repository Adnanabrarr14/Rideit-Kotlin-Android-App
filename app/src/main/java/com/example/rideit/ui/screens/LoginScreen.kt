package com.example.rideit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager

@Composable
fun LoginScreen(
    accountRole: String,
    accountTitle: String,
    accountSubtitle: String,
    primaryButtonText: String,
    createAccountText: String,
    accentColor: Color,
    onBackClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val isDriver = accountRole == FirebaseManager.ROLE_DRIVER
    val purple = Color(0xFF8A35F2)
    val red = Color(0xFFFF1212)
    val glowColor = if (isDriver) purple else red
    val darkCard = Color(0xFF101019)
    val fieldBg = Color(0xFF1A1A25)

    BackHandler {
        if (!isLoading) {
            onBackClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030307))
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(690.dp)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(42.dp)
                ),
            shape = RoundedCornerShape(42.dp),
            color = Color.Transparent,
            shadowElevation = 24.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF030307),
                                glowColor.copy(alpha = if (isDriver) 0.28f else 0.22f),
                                Color(0xFF07020A),
                                Color(0xFF030307)
                            )
                        )
                    )
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(250.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    glowColor.copy(alpha = 0.52f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 26.dp, top = 24.dp)
                        .size(54.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.10f),
                    shadowElevation = 12.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !isLoading) {
                                onBackClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "‹",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(76.dp))

                    Text(
                        text = "R I D E I T",
                        color = Color.White.copy(alpha = 0.50f),
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = accountSubtitle,
                        color = Color(0xFFC5C0CF),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        shape = RoundedCornerShape(28.dp),
                        color = darkCard.copy(alpha = 0.96f),
                        shadowElevation = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = accountTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Use your email and password to sign in",
                                color = Color(0xFFAAA6B6),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            RoleWarningPill(
                                text = if (isDriver) {
                                    "🚘 Driver accounts open Driver Dashboard only."
                                } else {
                                    "🚕 Rider accounts open Rider Map only."
                                },
                                accentColor = glowColor
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            LoginLabel("EMAIL")

                            Spacer(modifier = Modifier.height(7.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    errorMessage = null
                                    successMessage = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                placeholder = {
                                    Text(
                                        text = "you@example.com",
                                        color = Color(0xFF6F6B78)
                                    )
                                },
                                leadingIcon = {
                                    Text(
                                        text = "✉",
                                        color = Color(0xFF6F6B78)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = fieldBg,
                                    unfocusedContainerColor = fieldBg,
                                    disabledContainerColor = fieldBg,
                                    focusedBorderColor = Color.White.copy(alpha = 0.16f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    cursorColor = glowColor
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            LoginLabel("PASSWORD")

                            Spacer(modifier = Modifier.height(7.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    errorMessage = null
                                    successMessage = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                placeholder = {
                                    Text(
                                        text = "••••••••",
                                        color = Color(0xFF6F6B78)
                                    )
                                },
                                leadingIcon = {
                                    Text(
                                        text = "🔒",
                                        color = Color(0xFF6F6B78)
                                    )
                                },
                                trailingIcon = {
                                    TextButton(
                                        onClick = {
                                            passwordVisible = !passwordVisible
                                        }
                                    ) {
                                        Text(
                                            text = if (passwordVisible) "Hide" else "Show",
                                            color = Color(0xFFB8B4C6),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = fieldBg,
                                    unfocusedContainerColor = fieldBg,
                                    disabledContainerColor = fieldBg,
                                    focusedBorderColor = Color.White.copy(alpha = 0.16f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                    cursorColor = glowColor
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            TextButton(
                                onClick = {
                                    if (email.isBlank()) {
                                        errorMessage = "Enter your email first"
                                        successMessage = null
                                        return@TextButton
                                    }

                                    FirebaseManager.resetPassword(
                                        email = email.trim(),
                                        onSuccess = {
                                            successMessage = "Password reset email sent"
                                            errorMessage = null
                                        },
                                        onError = {
                                            errorMessage = it
                                            successMessage = null
                                        }
                                    )
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "Forgot password?",
                                    color = Color(0xFFB8B4C6),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            GlowButton(
                                text = if (isLoading) "Signing In..." else primaryButtonText,
                                accentColor = glowColor,
                                enabled = !isLoading,
                                onClick = {
                                    if (email.isBlank() || password.isBlank()) {
                                        errorMessage = "Please enter email and password"
                                        successMessage = null
                                        return@GlowButton
                                    }

                                    isLoading = true

                                    FirebaseManager.login(
                                        email = email.trim(),
                                        password = password,
                                        expectedRole = accountRole,
                                        onSuccess = {
                                            isLoading = false
                                            onLoginSuccess()
                                        },
                                        onError = {
                                            isLoading = false
                                            errorMessage = it
                                            successMessage = null
                                        }
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable(enabled = !isLoading) {
                                        onCreateAccountClick()
                                    },
                                shape = RoundedCornerShape(22.dp),
                                color = Color.White.copy(alpha = 0.05f),
                                shadowElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = createAccountText,
                                        color = Color.White.copy(alpha = 0.88f),
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }

                            errorMessage?.let {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = it,
                                    color = Color(0xFFFF6B6B),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            successMessage?.let {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = it,
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginLabel(
    text: String
) {
    Text(
        text = text,
        color = Color(0xFFAAA6B6),
        fontWeight = FontWeight.Black,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RoleWarningPill(
    text: String,
    accentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.18f),
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (accentColor == Color(0xFFFF1212)) {
                Color(0xFFFFB4B4)
            } else {
                Color(0xFFD6C2FF)
            },
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun GlowButton(
    text: String,
    accentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = enabled) {
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 18.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (enabled) 1f else 0.55f),
                            if (accentColor == Color(0xFFFF1212)) {
                                Color(0xFFC90013).copy(alpha = if (enabled) 1f else 0.55f)
                            } else {
                                Color(0xFF6D19E8).copy(alpha = if (enabled) 1f else 0.55f)
                            }
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}