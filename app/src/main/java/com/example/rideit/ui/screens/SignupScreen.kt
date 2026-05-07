package com.example.rideit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager

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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cardColor = Color(0xFF1B1B1D)
    val fieldBorder = Color(0xFF6B6A76)

    val isDriver = accountRole == FirebaseManager.ROLE_DRIVER

    val roleHelperText = if (isDriver) {
        "This will create a Driver account and open Driver Dashboard."
    } else {
        "This will create a Rider account and open Rider Map."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF070000),
                        Color(0xFF210000),
                        Color(0xFF000000)
                    )
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
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Rideit",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = accountSubtitle,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(30.dp))

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
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create your account with email and password",
                        color = Color(0xFF9CA3AF)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = accentColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = roleHelperText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = Color(0xFFE5E7EB),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        leadingIcon = {
                            Text(
                                text = "✉",
                                color = Color(0xFF6B6A76)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
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
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        leadingIcon = {
                            Text(
                                text = "🔒",
                                color = Color(0xFF6B6A76)
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                enabled = !isLoading,
                                onClick = {
                                    passwordVisible = !passwordVisible
                                }
                            ) {
                                Text(
                                    text = if (passwordVisible) "Hide" else "Show",
                                    color = Color(0xFF8B8A96)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
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
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Text(
                                text = "🔒",
                                color = Color(0xFF6B6A76)
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                enabled = !isLoading,
                                onClick = {
                                    confirmPasswordVisible = !confirmPasswordVisible
                                }
                            ) {
                                Text(
                                    text = if (confirmPasswordVisible) "Hide" else "Show",
                                    color = Color(0xFF8B8A96)
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
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
                    )

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
                                color = accentColor,
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
                                "Creating driver account and saving role..."
                            } else {
                                "Creating rider account and saving role..."
                            }
                            errorMessage = null

                            FirebaseManager.signup(
                                email = email,
                                password = password,
                                role = accountRole,
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
                            containerColor = accentColor,
                            contentColor = Color.White,
                            disabledContainerColor = accentColor.copy(alpha = 0.55f),
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
                            color = Color(0xFFFF6B6B)
                        )
                    }
                }
            }
        }
    }
}