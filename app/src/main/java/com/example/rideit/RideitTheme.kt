package com.example.rideit

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object RideitThemeTokens {
    val RosePrimary = Color(0xFFFF5CA8)
    val RoseSecondary = Color(0xFFEC4899)
    val RosePrimaryContainer = Color(0xFFFFD6E8)
    val RoseSecondaryContainer = Color(0xFFFFE4F1)
    val RoseBackground = Color(0xFFFFF7FB)
    val RoseSurface = Color(0xFFFFFBFD)
    val RoseSurfaceVariant = Color(0xFFFFEAF3)
    val RoseText = Color(0xFF24111A)
    val RoseSubText = Color(0xFF7A445A)
    val RoseOutline = Color(0xFFF9A8D4)
}

fun ColorScheme.isRideitRoseTheme(): Boolean {
    return primary == RideitThemeTokens.RosePrimary ||
            primary == Color(0xFFE45A8A) ||
            primary == RideitThemeTokens.RoseSecondary ||
            primaryContainer == RideitThemeTokens.RosePrimaryContainer ||
            background == RideitThemeTokens.RoseBackground
}

private val RideitLightColorScheme = lightColorScheme(
    primary = Color(0xFF8A35F2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDDFF),
    onPrimaryContainer = Color(0xFF2B064F),

    secondary = Color(0xFFFF1212),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410002),

    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF111827),

    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),

    outline = Color(0xFFD1D5DB),
    error = Color(0xFFEF4444),
    onError = Color.White
)

private val RideitDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB98CFF),
    onPrimary = Color(0xFF23004D),
    primaryContainer = Color(0xFF5B21B6),
    onPrimaryContainer = Color(0xFFF1E7FF),

    secondary = Color(0xFFFF8A8A),
    onSecondary = Color(0xFF4A0000),
    secondaryContainer = Color(0xFF8B0000),
    onSecondaryContainer = Color(0xFFFFDAD6),

    background = Color(0xFF050505),
    onBackground = Color(0xFFE5E7EB),

    surface = Color(0xFF111113),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),

    outline = Color(0xFF374151),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF450A0A)
)

private val RideitRoseColorScheme = lightColorScheme(
    primary = RideitThemeTokens.RosePrimary,
    onPrimary = Color.White,
    primaryContainer = RideitThemeTokens.RosePrimaryContainer,
    onPrimaryContainer = Color(0xFF4A0A27),

    secondary = RideitThemeTokens.RoseSecondary,
    onSecondary = Color.White,
    secondaryContainer = RideitThemeTokens.RoseSecondaryContainer,
    onSecondaryContainer = Color(0xFF4A0A27),

    background = RideitThemeTokens.RoseBackground,
    onBackground = RideitThemeTokens.RoseText,

    surface = RideitThemeTokens.RoseSurface,
    onSurface = RideitThemeTokens.RoseText,
    surfaceVariant = RideitThemeTokens.RoseSurfaceVariant,
    onSurfaceVariant = RideitThemeTokens.RoseSubText,

    outline = RideitThemeTokens.RoseOutline,
    error = Color(0xFFEF4444),
    onError = Color.White
)

@Composable
fun RideitTheme(
    content: @Composable () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    var selectedThemeMode by remember {
        mutableStateOf(FirebaseManager.THEME_SYSTEM)
    }

    DisposableEffect(Unit) {
        var userSettingsListener: ListenerRegistration? = null

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            userSettingsListener?.remove()
            userSettingsListener = null

            val currentUser = firebaseAuth.currentUser

            if (currentUser == null) {
                selectedThemeMode = FirebaseManager.THEME_SYSTEM
                return@AuthStateListener
            }

            userSettingsListener = firestore.collection("users")
                .document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    val savedThemeMode = snapshot
                        ?.getString("preferredThemeMode")
                        ?.trim()
                        ?.lowercase()

                    val savedGender = snapshot
                        ?.getString("gender")
                        ?.trim()
                        ?.lowercase()
                        .orEmpty()

                    selectedThemeMode = when (savedThemeMode) {
                        FirebaseManager.THEME_SYSTEM -> FirebaseManager.THEME_SYSTEM
                        FirebaseManager.THEME_LIGHT -> FirebaseManager.THEME_LIGHT
                        FirebaseManager.THEME_DARK -> FirebaseManager.THEME_DARK
                        FirebaseManager.THEME_ROSE -> FirebaseManager.THEME_ROSE
                        else -> {
                            if (savedGender == FirebaseManager.GENDER_WOMAN) {
                                FirebaseManager.THEME_ROSE
                            } else {
                                FirebaseManager.THEME_SYSTEM
                            }
                        }
                    }
                }
        }

        auth.addAuthStateListener(authStateListener)

        onDispose {
            userSettingsListener?.remove()
            auth.removeAuthStateListener(authStateListener)
        }
    }

    val systemDarkTheme = isSystemInDarkTheme()

    val colorScheme = when (selectedThemeMode) {
        FirebaseManager.THEME_LIGHT -> RideitLightColorScheme
        FirebaseManager.THEME_DARK -> RideitDarkColorScheme
        FirebaseManager.THEME_ROSE -> RideitRoseColorScheme
        else -> {
            if (systemDarkTheme) {
                RideitDarkColorScheme
            } else {
                RideitLightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
