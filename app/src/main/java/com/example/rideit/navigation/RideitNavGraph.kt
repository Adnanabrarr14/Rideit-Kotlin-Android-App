package com.example.rideit.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rideit.FirebaseManager
import com.example.rideit.driver.ui.DriverHomeScreen
import com.example.rideit.driver.ui.DriverWalletScreen
import com.example.rideit.map.ui.MapScreen
import com.example.rideit.ui.screens.AccountTypeScreen
import com.example.rideit.ui.screens.LoginScreen
import com.example.rideit.ui.screens.NotificationsScreen
import com.example.rideit.ui.screens.PaymentScreen
import com.example.rideit.ui.screens.ProfileScreen
import com.example.rideit.ui.screens.RiderWalletScreen
import com.example.rideit.ui.screens.SettingsScreen
import com.example.rideit.ui.screens.SignupScreen
import com.example.rideit.ui.screens.TripHistoryScreen
import kotlinx.coroutines.launch

@Composable
fun RideitNavGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.ACCOUNT_TYPE,
        modifier = modifier
    ) {
        composable(Routes.ACCOUNT_TYPE) {
            AccountTypeScreen(
                onRiderLoginClick = {
                    navController.navigate(Routes.RIDER_LOGIN) {
                        launchSingleTop = true
                    }
                },
                onDriverLoginClick = {
                    navController.navigate(Routes.DRIVER_LOGIN) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.RIDER_LOGIN) {
            LoginScreen(
                accountRole = FirebaseManager.ROLE_RIDER,
                accountTitle = "Rider Login",
                accountSubtitle = "Sign in to book your ride instantly",
                primaryButtonText = "Sign In as Rider",
                createAccountText = "Create Rider Account",
                accentColor = MaterialTheme.colorScheme.secondary,
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateAccountClick = {
                    navController.navigate(Routes.RIDER_SIGNUP) {
                        launchSingleTop = true
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Routes.MAP) {
                        popUpTo(Routes.ACCOUNT_TYPE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.DRIVER_LOGIN) {
            LoginScreen(
                accountRole = FirebaseManager.ROLE_DRIVER,
                accountTitle = "Driver Login",
                accountSubtitle = "Sign in to drive, earn and manage trips",
                primaryButtonText = "Sign In as Driver",
                createAccountText = "Create Driver Account",
                accentColor = MaterialTheme.colorScheme.primary,
                onBackClick = {
                    navController.popBackStack()
                },
                onCreateAccountClick = {
                    navController.navigate(Routes.DRIVER_SIGNUP) {
                        launchSingleTop = true
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Routes.DRIVER_HOME) {
                        popUpTo(Routes.ACCOUNT_TYPE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.RIDER_SIGNUP) {
            SignupScreen(
                accountRole = FirebaseManager.ROLE_RIDER,
                accountTitle = "Create Rider Account",
                accountSubtitle = "Create your rider account to book rides",
                primaryButtonText = "Create Rider Account",
                accentColor = MaterialTheme.colorScheme.secondary,
                onBackClick = {
                    navController.popBackStack()
                },
                onSignupSuccess = {
                    navController.navigate(Routes.MAP) {
                        popUpTo(Routes.ACCOUNT_TYPE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.DRIVER_SIGNUP) {
            SignupScreen(
                accountRole = FirebaseManager.ROLE_DRIVER,
                accountTitle = "Create Driver Account",
                accountSubtitle = "Create your driver account to start earning",
                primaryButtonText = "Create Driver Account",
                accentColor = MaterialTheme.colorScheme.primary,
                onBackClick = {
                    navController.popBackStack()
                },
                onSignupSuccess = {
                    navController.navigate(Routes.DRIVER_HOME) {
                        popUpTo(Routes.ACCOUNT_TYPE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.MAP) {
            RiderMapWithDrawer(
                navController = navController
            )
        }

        composable(Routes.DRIVER_HOME) {
            DriverHomeWithDrawer(
                navController = navController
            )
        }

        composable(Routes.RIDER_PROFILE) {
            BackHandler {
                returnToRiderHome(navController)
            }

            ProfileScreen(
                onBackClick = {
                    returnToRiderHome(navController)
                },
                onLogoutClick = {
                    logoutAndReturnToAccountType(navController)
                }
            )
        }

        composable(Routes.RIDER_TRIP_HISTORY) {
            BackHandler {
                returnToRiderHome(navController)
            }

            TripHistoryScreen(
                onBackClick = {
                    returnToRiderHome(navController)
                }
            )
        }

        composable(Routes.RIDER_PAYMENT) {
            BackHandler {
                returnToRiderHome(navController)
            }

            PaymentScreen(
                onBackClick = {
                    returnToRiderHome(navController)
                }
            )
        }

        composable(Routes.RIDER_WALLET) {
            BackHandler {
                returnToRiderHome(navController)
            }

            RiderWalletScreen(
                onBackClick = {
                    returnToRiderHome(navController)
                },
                onPaymentMethodsClick = {
                    navController.navigate(Routes.RIDER_PAYMENT) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.RIDER_NOTIFICATIONS) {
            BackHandler {
                returnToRiderHome(navController)
            }

            NotificationsScreen(
                onBackClick = {
                    returnToRiderHome(navController)
                }
            )
        }

        composable(Routes.RIDER_SETTINGS) {
            BackHandler {
                returnToRiderHome(navController)
            }

            SettingsScreen(
                onBackClick = {
                    returnToRiderHome(navController)
                }
            )
        }

        composable(Routes.DRIVER_PROFILE) {
            BackHandler {
                returnToDriverHome(navController)
            }

            ProfileScreen(
                onBackClick = {
                    returnToDriverHome(navController)
                },
                onLogoutClick = {
                    logoutAndReturnToAccountType(navController)
                }
            )
        }

        composable(Routes.DRIVER_WALLET) {
            BackHandler {
                returnToDriverHome(navController)
            }

            DriverWalletScreen(
                onBackClick = {
                    returnToDriverHome(navController)
                }
            )
        }

        composable(Routes.DRIVER_SETTINGS) {
            BackHandler {
                returnToDriverHome(navController)
            }

            SettingsScreen(
                onBackClick = {
                    returnToDriverHome(navController)
                }
            )
        }
    }
}

private fun returnToRiderHome(
    navController: NavHostController
) {
    navController.navigate(Routes.MAP) {
        popUpTo(Routes.MAP) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

private fun returnToDriverHome(
    navController: NavHostController
) {
    navController.navigate(Routes.DRIVER_HOME) {
        popUpTo(Routes.DRIVER_HOME) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

private fun logoutAndReturnToAccountType(
    navController: NavHostController
) {
    FirebaseManager.logout()

    navController.navigate(Routes.ACCOUNT_TYPE) {
        popUpTo(0)
        launchSingleTop = true
    }
}

@Composable
private fun RiderMapWithDrawer(
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val riderName = FirebaseManager.currentRiderDisplayName()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RiderDrawer(
                riderName = riderName,
                onRiderHomeClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.MAP) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.RIDER_PROFILE) {
                        launchSingleTop = true
                    }
                },
                onTripHistoryClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.RIDER_TRIP_HISTORY) {
                        launchSingleTop = true
                    }
                },
                onPaymentClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.RIDER_PAYMENT) {
                        launchSingleTop = true
                    }
                },
                onWalletClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.RIDER_WALLET) {
                        launchSingleTop = true
                    }
                },
                onNotificationsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.RIDER_NOTIFICATIONS) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.RIDER_SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    logoutAndReturnToAccountType(navController)
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MapScreen()

            Button(
                onClick = {
                    scope.launch { drawerState.open() }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 14.dp, start = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "☰",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DriverHomeWithDrawer(
    navController: NavHostController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val driverName = FirebaseManager.currentDriverDisplayName()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DriverDrawer(
                driverName = driverName,
                onDriverHomeClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.DRIVER_HOME) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.DRIVER_PROFILE) {
                        launchSingleTop = true
                    }
                },
                onWalletClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.DRIVER_WALLET) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.DRIVER_SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    logoutAndReturnToAccountType(navController)
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            DriverHomeScreen()

            Button(
                onClick = {
                    scope.launch { drawerState.open() }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 14.dp, start = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "☰",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RiderDrawer(
    riderName: String,
    onRiderHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTripHistoryClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    val colors = rememberDrawerThemeColors()

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 310.dp),
        drawerContainerColor = colors.drawerContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.drawerContainer,
                            colors.drawerMiddle,
                            colors.drawerContainer
                        )
                    )
                )
                .padding(22.dp)
        ) {
            DrawerHeader(
                title = riderName,
                subtitle = "Rider Account",
                avatar = riderName.drawerAvatarLetter(defaultLetter = "R"),
                colors = colors
            )

            Spacer(modifier = Modifier.height(30.dp))

            DrawerItem(
                icon = "🚕",
                title = "Rider Home",
                subtitle = "Book and track your rides",
                selected = true,
                colors = colors,
                onClick = onRiderHomeClick
            )

            DrawerItem(
                icon = "👤",
                title = "Profile",
                subtitle = "Manage your rider account",
                selected = false,
                colors = colors,
                onClick = onProfileClick
            )

            DrawerItem(
                icon = "🧾",
                title = "Trip History",
                subtitle = "View previous rides",
                selected = false,
                colors = colors,
                onClick = onTripHistoryClick
            )

            DrawerItem(
                icon = "💳",
                title = "Payment Methods",
                subtitle = "Cash and card",
                selected = false,
                colors = colors,
                onClick = onPaymentClick
            )

            DrawerItem(
                icon = "👛",
                title = "Rideit Wallet",
                subtitle = "Balance and demo top-up",
                selected = false,
                colors = colors,
                onClick = onWalletClick
            )

            DrawerItem(
                icon = "🔔",
                title = "Notifications",
                subtitle = "Alerts, offers and promos",
                selected = false,
                colors = colors,
                onClick = onNotificationsClick
            )

            DrawerItem(
                icon = "⚙️",
                title = "Settings",
                subtitle = "App preferences",
                selected = false,
                colors = colors,
                onClick = onSettingsClick
            )

            Spacer(modifier = Modifier.weight(1f))

            LogoutButton(
                colors = colors,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun DriverDrawer(
    driverName: String,
    onDriverHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    val colors = rememberDrawerThemeColors()

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 310.dp),
        drawerContainerColor = colors.drawerContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.drawerContainer,
                            colors.drawerMiddle,
                            colors.drawerContainer
                        )
                    )
                )
                .padding(22.dp)
        ) {
            DrawerHeader(
                title = driverName,
                subtitle = "Driver Account",
                avatar = driverName.drawerAvatarLetter(defaultLetter = "D"),
                colors = colors
            )

            Spacer(modifier = Modifier.height(30.dp))

            DrawerItem(
                icon = "🚘",
                title = "Driver Dashboard",
                subtitle = "Online status, rides and earnings",
                selected = true,
                colors = colors,
                onClick = onDriverHomeClick
            )

            DrawerItem(
                icon = "👤",
                title = "Driver Profile",
                subtitle = "Manage your driver account",
                selected = false,
                colors = colors,
                onClick = onProfileClick
            )

            DrawerItem(
                icon = "₨",
                title = "Driver Wallet",
                subtitle = "Earnings and demo payout",
                selected = false,
                colors = colors,
                onClick = onWalletClick
            )

            DrawerItem(
                icon = "⚙️",
                title = "Driver Settings",
                subtitle = "Driver app preferences",
                selected = false,
                colors = colors,
                onClick = onSettingsClick
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(22.dp),
                color = colors.card
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Text(
                        text = "Driver tools",
                        color = colors.text,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Wallet, trip history, vehicle documents, support and active trip tools remain inside Driver Dashboard too.",
                        color = colors.subText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            LogoutButton(
                colors = colors,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun DrawerHeader(
    title: String,
    subtitle: String,
    avatar: String,
    colors: DrawerThemeColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(colors.avatar),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatar,
                color = colors.onPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                color = colors.text,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = subtitle,
                color = colors.subText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    colors: DrawerThemeColors,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        colors.primary.copy(alpha = 0.20f)
    } else {
        colors.card
    }

    val iconBackgroundColor = if (selected) {
        colors.primary
    } else {
        colors.iconCard
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    color = colors.onPrimary,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = subtitle,
                    color = colors.subText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (selected) {
                Text(
                    text = "✓",
                    color = colors.success,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun LogoutButton(
    colors: DrawerThemeColors,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLogout() },
        shape = RoundedCornerShape(20.dp),
        color = colors.logoutCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🚪",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Logout",
                color = colors.logoutText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun rememberDrawerThemeColors(): DrawerThemeColors {
    val scheme = MaterialTheme.colorScheme
    val isLight = scheme.background.luminance() > 0.5f
    val isRoseTheme =
        scheme.primary == Color(0xFFFF5CA8) ||
                scheme.primary == Color(0xFFEC4899) ||
                scheme.primaryContainer == Color(0xFFFFD6E8)

    return when {
        isRoseTheme -> DrawerThemeColors(
            drawerContainer = Color(0xFFFFF7FB),
            drawerMiddle = Color(0xFFFFEAF3),
            card = Color.White,
            iconCard = Color(0xFFFFD6E8),
            primary = Color(0xFFFF5CA8),
            avatar = Color(0xFFFF5CA8),
            text = Color(0xFF24111A),
            subText = Color(0xFF7A445A),
            onPrimary = Color.White,
            success = Color(0xFF16A34A),
            logoutCard = Color(0xFFFFE4E6),
            logoutText = Color(0xFFE11D48)
        )

        isLight -> DrawerThemeColors(
            drawerContainer = Color(0xFFF8FAFC),
            drawerMiddle = Color(0xFFEDE9FE),
            card = Color.White,
            iconCard = Color(0xFFEBDDFF),
            primary = scheme.primary,
            avatar = scheme.primary,
            text = Color(0xFF111827),
            subText = Color(0xFF6B7280),
            onPrimary = Color.White,
            success = Color(0xFF16A34A),
            logoutCard = Color(0xFFFFE4E6),
            logoutText = Color(0xFFE11D48)
        )

        else -> DrawerThemeColors(
            drawerContainer = Color(0xFF111113),
            drawerMiddle = Color(0xFF1A1018),
            card = Color(0xFF1D1D21),
            iconCard = Color(0xFF2A2138),
            primary = scheme.primary,
            avatar = scheme.primary,
            text = Color.White,
            subText = Color(0xFF9CA3AF),
            onPrimary = Color.White,
            success = Color(0xFF22C55E),
            logoutCard = Color(0xFF2A1111),
            logoutText = Color(0xFFFF6B6B)
        )
    }
}

private data class DrawerThemeColors(
    val drawerContainer: Color,
    val drawerMiddle: Color,
    val card: Color,
    val iconCard: Color,
    val primary: Color,
    val avatar: Color,
    val text: Color,
    val subText: Color,
    val onPrimary: Color,
    val success: Color,
    val logoutCard: Color,
    val logoutText: Color
)

private fun String.drawerAvatarLetter(
    defaultLetter: String
): String {
    return trim()
        .firstOrNull()
        ?.uppercase()
        ?: defaultLetter
}