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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rideit.FirebaseManager
import com.example.rideit.RideitNotificationCenter
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
    val riderName = FirebaseManager.currentRiderDisplayName()
    val notificationBadgeCount = rememberVisibleNotificationBadgeCount()

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
                    .padding(top = 76.dp, start = 12.dp)
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
    val driverName = FirebaseManager.currentDriverDisplayName()
    val notificationBadgeCount = rememberVisibleNotificationBadgeCount()
    var isDriverInnerPage by remember { mutableStateOf(false) }

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
    Surface(
        modifier = modifier
            .size(if (compact) 46.dp else 50.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compact) 16.dp else 18.dp),
        color = Color(0xFF111827).copy(alpha = if (compact) 0.96f else 1f),
        shadowElevation = if (compact) 10.dp else 12.dp,
        tonalElevation = if (compact) 5.dp else 6.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☰",
                color = Color.White,
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

@Composable
private fun RiderDrawer(
    riderName: String,
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
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 304.dp),
        drawerContainerColor = Color(0xFF111113)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF111113),
                            Color(0xFF1A1018),
                            Color(0xFF090909)
                        )
                    )
                )
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 22.dp)
        ) {
            DrawerHeader(
                title = riderName,
                subtitle = "Rider Account",
                avatar = riderName.drawerAvatarLetter(defaultLetter = "R")
            )

            Spacer(modifier = Modifier.height(24.dp))

            DrawerItem(
                icon = "🚕",
                title = "Rider Home",
                subtitle = "Book and track your rides",
                selected = true,
                onClick = onRiderHomeClick
            )

            DrawerItem(
                icon = "👤",
                title = "Profile",
                subtitle = "Manage your rider account",
                selected = false,
                onClick = onProfileClick
            )

            DrawerItem(
                icon = "🧾",
                title = "Trip History",
                subtitle = "View previous rides",
                selected = false,
                onClick = onTripHistoryClick
            )

            DrawerItem(
                icon = "💳",
                title = "Payment Methods",
                subtitle = "Cash and card",
                selected = false,
                onClick = onPaymentClick
            )

            DrawerItem(
                icon = "👛",
                title = "Rideit Wallet",
                subtitle = "Balance and demo top-up",
                selected = false,
                onClick = onWalletClick
            )

            DrawerItem(
                icon = "🔔",
                title = "Notifications",
                subtitle = "Ride and trip alerts",
                selected = false,
                badgeCount = notificationBadgeCount,
                onClick = onNotificationsClick
            )

            DrawerItem(
                icon = "⚙️",
                title = "Settings",
                subtitle = "App preferences",
                selected = false,
                onClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            LogoutButton(onLogout = onLogout)
        }
    }
}

@Composable
private fun DriverDrawer(
    driverName: String,
    notificationBadgeCount: Int,
    onDriverHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 304.dp),
        drawerContainerColor = Color(0xFF111113)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF111113),
                            Color(0xFF111827),
                            Color(0xFF090909)
                        )
                    )
                )
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 22.dp)
        ) {
            DrawerHeader(
                title = driverName,
                subtitle = "Driver Account",
                avatar = driverName.drawerAvatarLetter(defaultLetter = "D")
            )

            Spacer(modifier = Modifier.height(24.dp))

            DrawerItem(
                icon = "🚘",
                title = "Driver Dashboard",
                subtitle = "Online status, rides and earnings",
                selected = true,
                onClick = onDriverHomeClick
            )

            DrawerItem(
                icon = "👤",
                title = "Driver Profile",
                subtitle = "Manage your driver account",
                selected = false,
                onClick = onProfileClick
            )

            DrawerItem(
                icon = "₨",
                title = "Driver Wallet",
                subtitle = "Earnings and demo payout",
                selected = false,
                onClick = onWalletClick
            )

            DrawerItem(
                icon = "N",
                title = "Notifications",
                subtitle = "Ride and trip alerts",
                selected = false,
                badgeCount = notificationBadgeCount,
                onClick = onNotificationsClick
            )

            DrawerItem(
                icon = "⚙️",
                title = "Driver Settings",
                subtitle = "Driver app preferences",
                selected = false,
                onClick = onSettingsClick
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF1D1D21)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Driver tools",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Wallet, trip history, vehicle documents, support, and active trip tools are available from the dashboard.",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LogoutButton(onLogout = onLogout)
        }
    }
}

@Composable
private fun DrawerHeader(
    title: String,
    subtitle: String,
    avatar: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Color(0xFF8A35F2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatar,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1
            )

            Text(
                text = subtitle,
                color = Color(0xFF9CA3AF),
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
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        Color(0xFF8A35F2).copy(alpha = 0.22f)
    } else {
        Color(0xFF1D1D21)
    }

    val iconBackgroundColor = if (selected) {
        Color(0xFF8A35F2)
    } else {
        Color(0xFF2A2138)
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
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = subtitle,
                    color = Color(0xFF9CA3AF),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (badgeCount > 0) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF8A35F2)
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
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun LogoutButton(
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLogout() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF2A1111)
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
                color = Color(0xFFFF6B6B),
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
