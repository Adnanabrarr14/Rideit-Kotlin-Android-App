package com.example.rideit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rideit.ui.screens.HomeScreen
import com.example.rideit.ui.screens.LoginScreen
import com.example.rideit.ui.screens.MapScreen

@Composable
fun RideitNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Login.route,
        modifier = modifier
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onOpenMap = {
                    navController.navigate(Routes.Map.route)
                }
            )
        }

        composable(Routes.Map.route) {
            MapScreen()
        }
    }
}