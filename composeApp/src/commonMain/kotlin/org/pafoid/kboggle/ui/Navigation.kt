package org.pafoid.kboggle.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.compose.koinInject

@Composable
fun Navigation() {
    val navController = koinInject<NavHostController>()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen()
        }
        composable(Screen.Game.route) {
            GameScreen()
        }
    }
}