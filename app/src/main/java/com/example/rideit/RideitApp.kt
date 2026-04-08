package com.example.rideit

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.rideit.navigation.RideitNavGraph

@Composable
fun RideitApp() {
    val navController = rememberNavController()
    RideitNavGraph(navController = navController)
}