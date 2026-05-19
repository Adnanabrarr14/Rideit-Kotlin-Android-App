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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rideit.FirebaseManager
import com.example.rideit.RideitNotificationCenter
import com.example.rideit.isRideitRoseTheme
import com.example.rideit.driver.ui.DriverHomeScreen
import com.example.rideit.driver.ui.DriverWalletScreen
import com.example.rideit.map.ui.MapScreen
import com.example.rideit.ui.components.RideitProfilePhotoAvatar
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
    var openRiderDrawerAfterReturn by rememberSaveable { mutableStateOf(false) }
    var openDriverDrawerAfterReturn by rememberSaveable { mutableStateOf(false) }
    val riderDrawerReturnRoutes = remember { mutableSetOf<String>() }
    val driverDrawerReturnRoutes = remember { mutableSetOf<String>() }

    fun returnFromRiderDrawerRoute(route: String) {
        if (riderDrawerReturnRoutes.remove(route)) {
            openRiderDrawerAfterReturn = true
        }

        returnToRiderHome(navController)
    }

    fun returnFromDriverDrawerRoute(route: String) {
        if (driverDrawerReturnRoutes.remove(route)) {
            openDriverDrawerAfterReturn = true
        }

        returnToDriverHome(navController)
    }

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
                accentColor = Color(0xFFFF1212),
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
                accentColor = Color(0xFF8A35F2),
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
                accentColor = Color(0xFFFF1212),
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
                accentColor = Color(0xFF8A35F2),
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
                navController = navController,
                openDrawerAfterReturn = openRiderDrawerAfterReturn,
                onDrawerOpenedAfterReturn = {
                    openRiderDrawerAfterReturn = false
                },
                onDrawerDestinationSelected = { route ->
                    riderDrawerReturnRoutes.add(route)
                }
            )
        }

        composable(Routes.DRIVER_HOME) {
            DriverHomeWithDrawer(
                navController = navController,
                openDrawerAfterReturn = openDriverDrawerAfterReturn,
                onDrawerOpenedAfterReturn = {
                    openDriverDrawerAfterReturn = false
                },
                onDrawerDestinationSelected = { route ->
                    driverDrawerReturnRoutes.add(route)
                }
            )
        }

        composable(Routes.RIDER_PROFILE) {
            BackHandler {
                returnFromRiderDrawerRoute(Routes.RIDER_PROFILE)
            }

            ProfileScreen(
                onBackClick = {
                    returnFromRiderDrawerRoute(Routes.RIDER_PROFILE)
                },
                onLogoutClick = {
                    riderDrawerReturnRoutes.remove(Routes.RIDER_PROFILE)
                    logoutAndReturnToAccountType(navController)
                }
            )
        }

        composable(Routes.RIDER_TRIP_HISTORY) {
            BackHandler {
                returnFromRiderDrawerRoute(Routes.RIDER_TRIP_HISTORY)
            }

            TripHistoryScreen(
                onBackClick = {
                    returnFromRiderDrawerRoute(Routes.RIDER_TRIP_HISTORY)
                }
            )
        }

        composable(Routes.RIDER_PAYMENT) {
            BackHandler {
                returnFromRiderDrawerRoute(Routes.RIDER_PAYMENT)
            }

            PaymentScreen(
                onBackClick = {
                    returnFromRiderDrawerRoute(Routes.RIDER_PAYMENT)
                }
            )
        }

        composable(Routes.RIDER_WALLET) {
            BackHandler {
                returnFromRiderDrawerRoute(Routes.RIDER_WALLET)
            }

            RiderWalletScreen(
                onBackClick = {
                    returnFromRiderDrawerRoute(Routes.RIDER_WALLET)
                },
                onPaymentMethodsClick = {
                    riderDrawerReturnRoutes.remove(Routes.RIDER_WALLET)
                    navController.navigate(Routes.RIDER_PAYMENT) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.RIDER_NOTIFICATIONS) {
            BackHandler {
                returnFromRiderDrawerRoute(Routes.RIDER_NOTIFICATIONS)
            }

            NotificationsScreen(
                accountRole = FirebaseManager.ROLE_RIDER,
                onBackClick = {
                    returnFromRiderDrawerRoute(Routes.RIDER_NOTIFICATIONS)
                }
            )
        }

        composable(Routes.RIDER_SETTINGS) {
            BackHandler {
                returnFromRiderDrawerRoute(Routes.RIDER_SETTINGS)
            }

            SettingsScreen(
                onBackClick = {
                    returnFromRiderDrawerRoute(Routes.RIDER_SETTINGS)
                }
            )
        }

        composable(Routes.DRIVER_PROFILE) {
            BackHandler {
                returnFromDriverDrawerRoute(Routes.DRIVER_PROFILE)
            }

            ProfileScreen(
                onBackClick = {
                    returnFromDriverDrawerRoute(Routes.DRIVER_PROFILE)
                },
                onLogoutClick = {
                    driverDrawerReturnRoutes.remove(Routes.DRIVER_PROFILE)
                    logoutAndReturnToAccountType(navController)
                }
            )
        }

        composable(Routes.DRIVER_WALLET) {
            BackHandler {
                returnFromDriverDrawerRoute(Routes.DRIVER_WALLET)
            }

            DriverWalletScreen(
                onBackClick = {
                    returnFromDriverDrawerRoute(Routes.DRIVER_WALLET)
                }
            )
        }

        composable(Routes.DRIVER_NOTIFICATIONS) {
            BackHandler {
                returnFromDriverDrawerRoute(Routes.DRIVER_NOTIFICATIONS)
            }

            NotificationsScreen(
                accountRole = FirebaseManager.ROLE_DRIVER,
                onBackClick = {
                    returnFromDriverDrawerRoute(Routes.DRIVER_NOTIFICATIONS)
                }
            )
        }

        composable(Routes.DRIVER_SETTINGS) {
            BackHandler {
                returnFromDriverDrawerRoute(Routes.DRIVER_SETTINGS)
            }

            SettingsScreen(
                onBackClick = {
                    returnFromDriverDrawerRoute(Routes.DRIVER_SETTINGS)
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
    navController: NavHostController,
    openDrawerAfterReturn: Boolean,
    onDrawerOpenedAfterReturn: () -> Unit,
    onDrawerDestinationSelected: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var riderName by remember { mutableStateOf(FirebaseManager.currentRiderDisplayName()) }
    var riderPhotoUrl by remember { mutableStateOf(FirebaseManager.currentUserPhotoUrl()) }
    val notificationBadgeCount = rememberVisibleNotificationBadgeCount()

    LaunchedEffect(FirebaseManager.currentUserId()) {
        FirebaseManager.loadCurrentUserProfile(
            onSuccess = { profile ->
                riderName = profile.fullName.ifBlank { FirebaseManager.currentRiderDisplayName() }
                riderPhotoUrl = profile.profilePhotoUrl.ifBlank {
                    FirebaseManager.currentUserPhotoUrl()
                }
            },
            onError = {
                riderName = FirebaseManager.currentRiderDisplayName()
                riderPhotoUrl = FirebaseManager.currentUserPhotoUrl()
            }
        )
    }

    LaunchedEffect(openDrawerAfterReturn) {
        if (openDrawerAfterReturn) {
            drawerState.open()
            onDrawerOpenedAfterReturn()
        }
    }

    fun navigateToDrawerDestination(route: String) {
        onDrawerDestinationSelected(route)
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RiderDrawer(
                riderName = riderName,
                riderPhotoUrl = riderPhotoUrl,
                notificationBadgeCount = notificationBadgeCount,
                onRiderHomeClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.MAP) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navigateToDrawerDestination(Routes.RIDER_PROFILE)
                },
                onTripHistoryClick = {
                    navigateToDrawerDestination(Routes.RIDER_TRIP_HISTORY)
                },
                onPaymentClick = {
                    navigateToDrawerDestination(Routes.RIDER_PAYMENT)
                },
                onWalletClick = {
                    navigateToDrawerDestination(Routes.RIDER_WALLET)
                },
                onNotificationsClick = {
                    navigateToDrawerDestination(Routes.RIDER_NOTIFICATIONS)
                },
                onSettingsClick = {
                    navigateToDrawerDestination(Routes.RIDER_SETTINGS)
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

            DrawerMenuButton(
                compact = true,
                onClick = {
                    scope.launch { drawerState.open() }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 60.dp, start = 10.dp)
                    .zIndex(6f)
            )
        }
    }
}

@Composable
private fun DriverHomeWithDrawer(
    navController: NavHostController,
    openDrawerAfterReturn: Boolean,
    onDrawerOpenedAfterReturn: () -> Unit,
    onDrawerDestinationSelected: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var driverName by remember { mutableStateOf(FirebaseManager.currentDriverDisplayName()) }
    var driverPhotoUrl by remember { mutableStateOf(FirebaseManager.currentUserPhotoUrl()) }
    val notificationBadgeCount = rememberVisibleNotificationBadgeCount()
    var isDriverInnerPage by remember { mutableStateOf(false) }

    LaunchedEffect(FirebaseManager.currentUserId()) {
        FirebaseManager.loadCurrentUserProfile(
            onSuccess = { profile ->
                driverName = profile.fullName.ifBlank { FirebaseManager.currentDriverDisplayName() }
                driverPhotoUrl = profile.profilePhotoUrl.ifBlank {
                    FirebaseManager.currentUserPhotoUrl()
                }
            },
            onError = {
                driverName = FirebaseManager.currentDriverDisplayName()
                driverPhotoUrl = FirebaseManager.currentUserPhotoUrl()
            }
        )
    }

    LaunchedEffect(openDrawerAfterReturn) {
        if (openDrawerAfterReturn) {
            drawerState.open()
            onDrawerOpenedAfterReturn()
        }
    }

    fun navigateToDrawerDestination(route: String) {
        onDrawerDestinationSelected(route)
        scope.launch { drawerState.close() }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DriverDrawer(
                driverName = driverName,
                driverPhotoUrl = driverPhotoUrl,
                notificationBadgeCount = notificationBadgeCount,
                onDriverHomeClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Routes.DRIVER_HOME) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navigateToDrawerDestination(Routes.DRIVER_PROFILE)
                },
                onWalletClick = {
                    navigateToDrawerDestination(Routes.DRIVER_WALLET)
                },
                onNotificationsClick = {
                    navigateToDrawerDestination(Routes.DRIVER_NOTIFICATIONS)
                },
                onSettingsClick = {
                    navigateToDrawerDestination(Routes.DRIVER_SETTINGS)
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    logoutAndReturnToAccountType(navController)
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            DriverHomeScreen(
                onInnerPageChanged = { isInnerPage ->
                    isDriverInnerPage = isInnerPage
                }
            )

            if (!isDriverInnerPage) {
                DrawerMenuButton(
                    compact = false,
                    onClick = {
                        scope.launch { drawerState.open() }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 6.dp, start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun rememberVisibleNotificationBadgeCount(): Int {
    var unreadCount by remember { mutableStateOf(0) }
    var rideAlertsEnabled by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val countRegistration = RideitNotificationCenter.listenToCurrentUserUnreadCount(
            onChange = { count ->
                unreadCount = count
            },
            onError = {
                unreadCount = 0
            }
        )

        val settingsRegistration = RideitNotificationCenter.listenToCurrentUserRideAlertsEnabled(
            onChange = { enabled ->
                rideAlertsEnabled = enabled
            },
            onError = {
                rideAlertsEnabled = true
            }
        )

        onDispose {
            countRegistration?.remove()
            settingsRegistration?.remove()
        }
    }

    return if (rideAlertsEnabled) unreadCount else 0
}

@Composable
private fun DrawerMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val isSoftTheme = scheme.isRideitRoseTheme() || scheme.background.luminance() > 0.5f

    Surface(
        modifier = modifier
            .size(if (compact) 46.dp else 50.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 16.dp else 18.dp),
        color = if (isSoftTheme) {
            scheme.primary.copy(alpha = if (compact) 0.96f else 1f)
        } else {
            Color(0xFF111827).copy(alpha = if (compact) 0.96f else 1f)
        },
        shadowElevation = if (compact) 10.dp else 12.dp,
        tonalElevation = if (compact) 5.dp else 6.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☰",
                color = if (isSoftTheme) scheme.onPrimary else Color.White,
                fontWeight = FontWeight.Black,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                }
            )
        }
    }
}

private data class DrawerThemeColors(
    val sheet: Color,
    val gradient: List<Color>,
    val item: Color,
    val selectedItem: Color,
    val icon: Color,
    val selectedIcon: Color,
    val iconText: Color,
    val selectedIconText: Color,
    val text: Color,
    val subText: Color,
    val primary: Color,
    val selectedCheck: Color,
    val toolCard: Color,
    val logoutBackground: Color,
    val logoutText: Color
)

@Composable
private fun rememberDrawerThemeColors(): DrawerThemeColors {
    val scheme = MaterialTheme.colorScheme
    val isRoseTheme = scheme.isRideitRoseTheme()
    val isLightTheme = scheme.background.luminance() > 0.5f

    return remember(
        scheme.primary,
        scheme.background,
        scheme.surface,
        scheme.surfaceVariant,
        scheme.onSurface,
        scheme.onSurfaceVariant,
        isRoseTheme,
        isLightTheme
    ) {
        when {
            isRoseTheme -> DrawerThemeColors(
                sheet = Color(0xFFFFFBFD),
                gradient = listOf(
                    Color(0xFFFFF7FB),
                    Color(0xFFFFEAF3),
                    Color(0xFFFFFBFD)
                ),
                item = Color.White,
                selectedItem = scheme.primary.copy(alpha = 0.15f),
                icon = Color(0xFFFFEAF3),
                selectedIcon = scheme.primary,
                iconText = scheme.primary,
                selectedIconText = scheme.onPrimary,
                text = Color(0xFF24111A),
                subText = Color(0xFF7A445A),
                primary = scheme.primary,
                selectedCheck = scheme.primary,
                toolCard = Color.White,
                logoutBackground = Color(0xFFFFE4E6),
                logoutText = Color(0xFFE11D48)
            )

            isLightTheme -> DrawerThemeColors(
                sheet = Color.White,
                gradient = listOf(
                    Color(0xFFF8FAFC),
                    Color(0xFFF3F0FF),
                    Color.White
                ),
                item = Color.White,
                selectedItem = scheme.primary.copy(alpha = 0.14f),
                icon = Color(0xFFEDE9FE),
                selectedIcon = scheme.primary,
                iconText = scheme.primary,
                selectedIconText = scheme.onPrimary,
                text = Color(0xFF111827),
                subText = Color(0xFF6B7280),
                primary = scheme.primary,
                selectedCheck = scheme.primary,
                toolCard = Color(0xFFF8FAFC),
                logoutBackground = Color(0xFFFEF2F2),
                logoutText = Color(0xFFDC2626)
            )

            else -> DrawerThemeColors(
                sheet = Color(0xFF111113),
                gradient = listOf(
                    Color(0xFF111113),
                    Color(0xFF141018),
                    Color(0xFF090909)
                ),
                item = Color(0xFF1D1D21),
                selectedItem = scheme.primary.copy(alpha = 0.22f),
                icon = Color(0xFF2A2138),
                selectedIcon = scheme.primary,
                iconText = Color.White,
                selectedIconText = Color.White,
                text = Color.White,
                subText = Color(0xFF9CA3AF),
                primary = scheme.primary,
                selectedCheck = Color(0xFF22C55E),
                toolCard = Color(0xFF1D1D21),
                logoutBackground = Color(0xFF2A1111),
                logoutText = Color(0xFFFF6B6B)
            )
        }
    }
}

@Composable
private fun RiderDrawer(
    riderName: String,
    riderPhotoUrl: String,
    notificationBadgeCount: Int,
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
            .widthIn(max = 304.dp),
        drawerContainerColor = colors.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = colors.gradient
                    )
                )
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 22.dp)
        ) {
            DrawerHeader(
                title = riderName,
                subtitle = "Rider Account",
                avatar = riderName.drawerAvatarLetter(defaultLetter = "R"),
                photoUrl = riderPhotoUrl,
                colors = colors
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                subtitle = "Ride and trip alerts",
                selected = false,
                badgeCount = notificationBadgeCount,
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

            Spacer(modifier = Modifier.height(10.dp))

            LogoutButton(
                onLogout = onLogout,
                colors = colors
            )
        }
    }
}

@Composable
private fun DriverDrawer(
    driverName: String,
    driverPhotoUrl: String,
    notificationBadgeCount: Int,
    onDriverHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    val colors = rememberDrawerThemeColors()

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 304.dp),
        drawerContainerColor = colors.sheet
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = colors.gradient
                    )
                )
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 22.dp)
        ) {
            DrawerHeader(
                title = driverName,
                subtitle = "Driver Account",
                avatar = driverName.drawerAvatarLetter(defaultLetter = "D"),
                photoUrl = driverPhotoUrl,
                colors = colors
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                icon = "N",
                title = "Notifications",
                subtitle = "Ride and trip alerts",
                selected = false,
                badgeCount = notificationBadgeCount,
                colors = colors,
                onClick = onNotificationsClick
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
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(22.dp),
                color = colors.toolCard
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Driver tools",
                        color = colors.text,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Wallet, history, documents, support and active trip tools are available from the dashboard.",
                        color = colors.subText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LogoutButton(
                onLogout = onLogout,
                colors = colors
            )
        }
    }
}

@Composable
private fun DrawerHeader(
    title: String,
    subtitle: String,
    avatar: String,
    photoUrl: String,
    colors: DrawerThemeColors
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RideitProfilePhotoAvatar(
            photoUrl = photoUrl,
            fallbackText = avatar,
            size = 58.dp,
            backgroundColor = colors.primary,
            contentColor = Color.White
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1
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
    badgeCount: Int = 0,
    colors: DrawerThemeColors,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        colors.selectedItem
    } else {
        colors.item
    }

    val iconBackgroundColor = if (selected) {
        colors.selectedIcon
    } else {
        colors.icon
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    color = if (selected) colors.selectedIconText else colors.iconText,
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

            if (badgeCount > 0) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.primary
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else if (selected) {
                Text(
                    text = "✓",
                    color = colors.selectedCheck,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun LogoutButton(
    onLogout: () -> Unit,
    colors: DrawerThemeColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLogout() },
        shape = RoundedCornerShape(20.dp),
        color = colors.logoutBackground
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

private fun String.drawerAvatarLetter(
    defaultLetter: String
): String {
    return trim()
        .firstOrNull()
        ?.uppercase()
        ?: defaultLetter
}
