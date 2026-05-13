package com.example.rideit.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rideit.FirebaseManager

@Immutable
private data class LoginThemeColors(
    val backgroundTop: Color,
    val backgroundMiddle: Color,
    val backgroundBottom: Color,
    val card: Color,
    val softField: Color,
    val selectedTab: Color,
    val unselectedTab: Color,
    val accent: Color,
    val accentBottom: Color,
    val text: Color,
    val subText: Color,
    val mutedText: Color,
    val onAccent: Color,
    val error: Color,
    val success: Color
)

private enum class LoginMethod {
    Email,
    Phone
}

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
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    var selectedMethod by remember { mutableStateOf(LoginMethod.Email) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val isDriver = accountRole == FirebaseManager.ROLE_DRIVER

    val colors = rememberLoginThemeColors(
        isDriver = isDriver,
        fallbackAccent = accentColor
    )

    BackHandler {
        if (!isLoading) {
            onBackClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundTop)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(720.dp)
                .clip(RoundedCornerShape(42.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.backgroundTop,
                            colors.backgroundMiddle,
                            colors.backgroundBottom,
                            colors.backgroundTop
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(260.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colors.accent.copy(alpha = 0.46f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 24.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(colors.card.copy(alpha = 0.82f))
                    .clickable(enabled = !isLoading) {
                        onBackClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "‹",
                    color = colors.text,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(64.dp))

                Text(
                    text = "R I D E I T",
                    color = colors.text.copy(alpha = 0.58f),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = accountSubtitle,
                    color = colors.subText,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(colors.card)
                        .padding(horizontal = 18.dp, vertical = 22.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = accountTitle,
                            color = colors.text,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = if (selectedMethod == LoginMethod.Email) {
                                "Use email and password to sign in"
                            } else {
                                "Use phone number and OTP to sign in"
                            },
                            color = colors.subText,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        LoginMethodTabs(
                            selectedMethod = selectedMethod,
                            colors = colors,
                            onEmailClick = {
                                selectedMethod = LoginMethod.Email
                                errorMessage = null
                                successMessage = null
                            },
                            onPhoneClick = {
                                selectedMethod = LoginMethod.Phone
                                errorMessage = null
                                successMessage = null
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (selectedMethod == LoginMethod.Email) {
                            EmailLoginContent(
                                email = email,
                                password = password,
                                passwordVisible = passwordVisible,
                                isLoading = isLoading,
                                colors = colors,
                                onEmailChange = {
                                    email = it
                                    errorMessage = null
                                    successMessage = null
                                },
                                onPasswordChange = {
                                    password = it
                                    errorMessage = null
                                    successMessage = null
                                },
                                onPasswordVisibilityClick = {
                                    passwordVisible = !passwordVisible
                                },
                                onForgotPasswordClick = {
                                    if (email.isBlank()) {
                                        errorMessage = "Enter your email first."
                                        successMessage = null
                                        return@EmailLoginContent
                                    }

                                    FirebaseManager.resetPassword(
                                        email = email.trim(),
                                        onSuccess = {
                                            successMessage = "Password reset email sent."
                                            errorMessage = null
                                        },
                                        onError = {
                                            errorMessage = it
                                            successMessage = null
                                        }
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            GlowButton(
                                text = if (isLoading) "Signing In..." else primaryButtonText,
                                colors = colors,
                                enabled = !isLoading,
                                onClick = {
                                    if (email.isBlank() || password.isBlank()) {
                                        errorMessage = "Please enter email and password."
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
                        } else {
                            PhoneLoginContent(
                                phoneNumber = phoneNumber,
                                otpCode = otpCode,
                                otpSent = otpSent,
                                isLoading = isLoading,
                                colors = colors,
                                onPhoneChange = {
                                    phoneNumber = it
                                    errorMessage = null
                                    successMessage = null
                                },
                                onOtpChange = {
                                    otpCode = it.filter { char -> char.isDigit() }.take(6)
                                    errorMessage = null
                                    successMessage = null
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            GlowButton(
                                text = when {
                                    isLoading -> "Please wait..."
                                    otpSent -> "Verify OTP"
                                    else -> "Send OTP"
                                },
                                colors = colors,
                                enabled = !isLoading,
                                onClick = {
                                    if (activity == null) {
                                        errorMessage = "Phone login requires an Android activity."
                                        successMessage = null
                                        return@GlowButton
                                    }

                                    if (!otpSent) {
                                        isLoading = true
                                        errorMessage = null
                                        successMessage = null

                                        FirebaseManager.sendPhoneLoginOtp(
                                            activity = activity,
                                            phoneNumber = phoneNumber,
                                            expectedRole = accountRole,
                                            onCodeSent = { newVerificationId ->
                                                isLoading = false
                                                verificationId = newVerificationId
                                                otpSent = true
                                                successMessage = "OTP sent. Please check your SMS."
                                            },
                                            onAutoVerified = {
                                                isLoading = false
                                                onLoginSuccess()
                                            },
                                            onError = {
                                                isLoading = false
                                                errorMessage = it
                                                successMessage = null
                                            }
                                        )
                                    } else {
                                        isLoading = true
                                        errorMessage = null
                                        successMessage = null

                                        FirebaseManager.verifyPhoneLoginOtp(
                                            verificationId = verificationId,
                                            otpCode = otpCode,
                                            expectedRole = accountRole,
                                            phoneNumber = phoneNumber,
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
                                }
                            )

                            if (otpSent) {
                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = {
                                        otpSent = false
                                        verificationId = ""
                                        otpCode = ""
                                        successMessage = "You can request a new OTP."
                                        errorMessage = null
                                    }
                                ) {
                                    Text(
                                        text = "Change phone number",
                                        color = colors.subText,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        CreateAccountButton(
                            text = if (isDriver) {
                                "New driver? Create Driver Account"
                            } else {
                                "New rider? Create Rider Account"
                            },
                            colors = colors,
                            enabled = !isLoading,
                            onClick = onCreateAccountClick
                        )

                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = colors.error,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        successMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = colors.success,
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

@Composable
private fun LoginMethodTabs(
    selectedMethod: LoginMethod,
    colors: LoginThemeColors,
    onEmailClick: () -> Unit,
    onPhoneClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.softField)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoginTabButton(
            text = "Email",
            selected = selectedMethod == LoginMethod.Email,
            colors = colors,
            modifier = Modifier.weight(1f),
            onClick = onEmailClick
        )

        LoginTabButton(
            text = "Phone",
            selected = selectedMethod == LoginMethod.Phone,
            colors = colors,
            modifier = Modifier.weight(1f),
            onClick = onPhoneClick
        )
    }
}

@Composable
private fun LoginTabButton(
    text: String,
    selected: Boolean,
    colors: LoginThemeColors,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(15.dp))
            .background(
                if (selected) {
                    colors.selectedTab
                } else {
                    colors.unselectedTab
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) colors.onAccent else colors.subText,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmailLoginContent(
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    colors: LoginThemeColors,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    LoginLabel(
        text = "EMAIL",
        colors = colors
    )

    Spacer(modifier = Modifier.height(10.dp))

    CleanLoginInputField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = "you@example.com",
        leadingIcon = "✉",
        keyboardType = KeyboardType.Email,
        enabled = !isLoading,
        colors = colors
    )

    Spacer(modifier = Modifier.height(18.dp))

    LoginLabel(
        text = "PASSWORD",
        colors = colors
    )

    Spacer(modifier = Modifier.height(10.dp))

    CleanLoginInputField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = "••••••••",
        leadingIcon = "🔒",
        keyboardType = KeyboardType.Password,
        enabled = !isLoading,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingContent = {
            TextButton(
                onClick = onPasswordVisibilityClick,
                enabled = !isLoading
            ) {
                Text(
                    text = if (passwordVisible) "Hide" else "Show",
                    color = colors.subText,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        colors = colors
    )

    Spacer(modifier = Modifier.height(6.dp))

    TextButton(
        onClick = onForgotPasswordClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Forgot password?",
            color = colors.subText,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PhoneLoginContent(
    phoneNumber: String,
    otpCode: String,
    otpSent: Boolean,
    isLoading: Boolean,
    colors: LoginThemeColors,
    onPhoneChange: (String) -> Unit,
    onOtpChange: (String) -> Unit
) {
    LoginLabel(
        text = "PHONE NUMBER",
        colors = colors
    )

    Spacer(modifier = Modifier.height(10.dp))

    CleanLoginInputField(
        value = phoneNumber,
        onValueChange = onPhoneChange,
        placeholder = "+923001234567",
        leadingIcon = "📱",
        keyboardType = KeyboardType.Phone,
        enabled = !isLoading && !otpSent,
        colors = colors
    )

    if (otpSent) {
        Spacer(modifier = Modifier.height(18.dp))

        LoginLabel(
            text = "OTP CODE",
            colors = colors
        )

        Spacer(modifier = Modifier.height(10.dp))

        CleanLoginInputField(
            value = otpCode,
            onValueChange = onOtpChange,
            placeholder = "6-digit code",
            leadingIcon = "🔐",
            keyboardType = KeyboardType.Number,
            enabled = !isLoading,
            colors = colors
        )
    }
}

@Composable
private fun LoginLabel(
    text: String,
    colors: LoginThemeColors
) {
    Text(
        text = text,
        color = colors.subText,
        fontWeight = FontWeight.Black,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CleanLoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
    colors: LoginThemeColors,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.softField)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leadingIcon,
                color = colors.mutedText
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        color = colors.mutedText,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType
                    ),
                    cursorBrush = SolidColor(colors.accent),
                    textStyle = TextStyle(
                        color = colors.text,
                        fontWeight = FontWeight.Medium,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            trailingContent?.invoke()
        }
    }
}

@Composable
private fun GlowButton(
    text: String,
    colors: LoginThemeColors,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.accent.copy(alpha = if (enabled) 1f else 0.55f),
                        colors.accentBottom.copy(alpha = if (enabled) 1f else 0.55f)
                    )
                )
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.onAccent,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun CreateAccountButton(
    text: String,
    colors: LoginThemeColors,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.accent.copy(alpha = 0.14f))
            .clickable(enabled = enabled) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.text,
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun rememberLoginThemeColors(
    isDriver: Boolean,
    fallbackAccent: Color
): LoginThemeColors {
    val scheme = MaterialTheme.colorScheme

    val isRoseTheme =
        scheme.primary == Color(0xFFFF5CA8) ||
                scheme.primary == Color(0xFFEC4899) ||
                scheme.primaryContainer == Color(0xFFFFD6E8)

    val isLightTheme = scheme.background.luminance() > 0.5f

    return remember(scheme.primary, scheme.background, isDriver, fallbackAccent) {
        when {
            isRoseTheme -> {
                val accent = if (isDriver) Color(0xFFEC4899) else Color(0xFFFF5CA8)

                LoginThemeColors(
                    backgroundTop = Color(0xFFFFF7FB),
                    backgroundMiddle = Color(0xFFFFEAF3),
                    backgroundBottom = Color(0xFFFFFBFD),
                    card = Color.White,
                    softField = Color(0xFFFFF7FB),
                    selectedTab = accent,
                    unselectedTab = Color.Transparent,
                    accent = accent,
                    accentBottom = Color(0xFFBE185D),
                    text = Color(0xFF24111A),
                    subText = Color(0xFF7A445A),
                    mutedText = Color(0xFF9D5570),
                    onAccent = Color.White,
                    error = Color(0xFFE11D48),
                    success = Color(0xFF16A34A)
                )
            }

            isLightTheme -> {
                val accent = if (isDriver) Color(0xFF8A35F2) else Color(0xFFFF1212)

                LoginThemeColors(
                    backgroundTop = Color(0xFFF8FAFC),
                    backgroundMiddle = Color(0xFFEDE9FE),
                    backgroundBottom = Color.White,
                    card = Color.White,
                    softField = Color(0xFFF8FAFC),
                    selectedTab = accent,
                    unselectedTab = Color.Transparent,
                    accent = accent,
                    accentBottom = if (isDriver) Color(0xFF6D19E8) else Color(0xFFC90013),
                    text = Color(0xFF111827),
                    subText = Color(0xFF6B7280),
                    mutedText = Color(0xFF9CA3AF),
                    onAccent = Color.White,
                    error = Color(0xFFEF4444),
                    success = Color(0xFF16A34A)
                )
            }

            else -> {
                val accent = if (isDriver) Color(0xFF8A35F2) else Color(0xFFFF1212)

                LoginThemeColors(
                    backgroundTop = Color(0xFF030307),
                    backgroundMiddle = accent.copy(alpha = if (isDriver) 0.28f else 0.22f),
                    backgroundBottom = Color(0xFF07020A),
                    card = Color(0xFF101019).copy(alpha = 0.96f),
                    softField = Color(0xFF1A1A25),
                    selectedTab = accent,
                    unselectedTab = Color.Transparent,
                    accent = accent,
                    accentBottom = if (isDriver) Color(0xFF6D19E8) else Color(0xFFC90013),
                    text = Color.White,
                    subText = Color(0xFFB8B4C6),
                    mutedText = Color(0xFF6F6B78),
                    onAccent = Color.White,
                    error = Color(0xFFFF6B6B),
                    success = Color(0xFF22C55E)
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}