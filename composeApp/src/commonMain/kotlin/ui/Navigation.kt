package ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import services.GameService
import services.SocketService

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val socketService = SocketService()
    val gameService = GameService(socketService)

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController, socketService, gameService)
        }
        composable(Screen.Game.route) {
            GameScreen(navController, socketService, gameService)
        }
    }
}