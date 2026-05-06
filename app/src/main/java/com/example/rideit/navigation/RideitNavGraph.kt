package com.example.rideit.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rideit.map.ui.MapScreen
import com.example.rideit.ui.screens.LoginScreen
import com.example.rideit.ui.screens.NotificationsScreen
import com.example.rideit.ui.screens.PaymentScreen
import com.example.rideit.ui.screens.ProfileScreen
import com.example.rideit.ui.screens.SettingsScreen
import com.example.rideit.ui.screens.TripHistoryScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun RideitNavGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAP) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    RideitDrawer(
                        onProfileClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.PROFILE)
                        },
                        onTripHistoryClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.TRIP_HISTORY)
                        },
                        onPaymentClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.PAYMENT)
                        },
                        onNotificationsClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.NOTIFICATIONS)
                        },
                        onSettingsClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.SETTINGS)
                        },
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.MAP) { inclusive = true }
                            }
                        }
                    )
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MapScreen()

                    Button(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 46.dp, start = 16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF111827),
                            contentColor = Color.White
                        )
                    ) {
                        Text("☰", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.TRIP_HISTORY) {
            TripHistoryScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.PAYMENT) {
            PaymentScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

@Composable
private fun RideitDrawer(
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8A35F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "R",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Rideit",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Premium Ride App",
                        color = Color(0xFF9CA3AF),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            DrawerItem("👤", "Profile", "Manage your account", onProfileClick)
            DrawerItem("🧾", "Trip History", "View previous rides", onTripHistoryClick)
            DrawerItem("💳", "Payment Methods", "Cards, cash and wallet", onPaymentClick)
            DrawerItem("🔔", "Notifications", "Alerts, offers and promos", onNotificationsClick)
            DrawerItem("⚙️", "Settings", "App preferences", onSettingsClick)

            Spacer(modifier = Modifier.weight(1f))

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
                    Text("🚪", style = MaterialTheme.typography.titleLarge)

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
    }
}

@Composable
private fun DrawerItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1D1D21)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2A2138)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
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
        }
    }
}