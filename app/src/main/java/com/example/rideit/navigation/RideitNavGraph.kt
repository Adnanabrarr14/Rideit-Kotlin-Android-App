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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rideit.FirebaseManager
import com.example.rideit.driver.ui.DriverHomeScreen
import com.example.rideit.map.ui.MapScreen
import com.example.rideit.ui.screens.AccountTypeScreen
import com.example.rideit.ui.screens.LoginScreen
import com.example.rideit.ui.screens.NotificationsScreen
import com.example.rideit.ui.screens.PaymentScreen
import com.example.rideit.ui.screens.ProfileScreen
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

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    logoutAndReturnToAccountType(navController)
                }
            )
        }

        composable(Routes.TRIP_HISTORY) {
            TripHistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PAYMENT) {
            PaymentScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            RiderDrawer(
                onRiderHomeClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.MAP) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.RIDER_PROFILE) {
                        launchSingleTop = true
                    }
                },
                onTripHistoryClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.RIDER_TRIP_HISTORY) {
                        launchSingleTop = true
                    }
                },
                onPaymentClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.RIDER_PAYMENT) {
                        launchSingleTop = true
                    }
                },
                onNotificationsClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.RIDER_NOTIFICATIONS) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.RIDER_SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                    }

                    logoutAndReturnToAccountType(navController)
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MapScreen()

            Button(
                onClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 46.dp, start = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111827),
                    contentColor = Color.White
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DriverDrawer(
                onDriverHomeClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.DRIVER_HOME) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.DRIVER_PROFILE) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(Routes.DRIVER_SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                    }

                    logoutAndReturnToAccountType(navController)
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            DriverHomeScreen()

            Button(
                onClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 46.dp, start = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111827),
                    contentColor = Color.White
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
    onRiderHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onTripHistoryClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 310.dp),
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
                .padding(22.dp)
        ) {
            DrawerHeader(
                title = "Rideit",
                subtitle = "Rider Account",
                avatar = "R"
            )

            Spacer(modifier = Modifier.height(30.dp))

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
                subtitle = "Cards, cash and wallet",
                selected = false,
                onClick = onPaymentClick
            )

            DrawerItem(
                icon = "🔔",
                title = "Notifications",
                subtitle = "Alerts, offers and promos",
                selected = false,
                onClick = onNotificationsClick
            )

            DrawerItem(
                icon = "⚙️",
                title = "Settings",
                subtitle = "App preferences",
                selected = false,
                onClick = onSettingsClick
            )

            Spacer(modifier = Modifier.weight(1f))

            LogoutButton(
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun DriverDrawer(
    onDriverHomeClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 310.dp),
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
                .padding(22.dp)
        ) {
            DrawerHeader(
                title = "Rideit Driver",
                subtitle = "Driver Account",
                avatar = "D"
            )

            Spacer(modifier = Modifier.height(30.dp))

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
                icon = "⚙️",
                title = "Driver Settings",
                subtitle = "Driver app preferences",
                selected = false,
                onClick = onSettingsClick
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF1D1D21)
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Text(
                        text = "Driver tools",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Wallet, trip history, vehicle documents, support and active trip tools are inside Driver Dashboard.",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            LogoutButton(
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun DrawerHeader(
    title: String,
    subtitle: String,
    avatar: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Column {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
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
            .padding(bottom = 12.dp)
            .clickable {
                onClick()
            },
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
                Text(icon)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
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

            if (selected) {
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
            .clickable {
                onLogout()
            },
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