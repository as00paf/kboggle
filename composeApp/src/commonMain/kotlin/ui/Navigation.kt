package ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import services.GameService
import services.SocketService
import viewmodels.GameScreenViewModel
import viewmodels.LoginScreenViewModel

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val socketService = SocketService()
    val gameService = GameService(socketService)

    val loginScreenVm = LoginScreenViewModel(navController, socketService, gameService)
    val gameScreenVm = GameScreenViewModel(navController, socketService, gameService)

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(loginScreenVm)
        }
        composable(Screen.Game.route) {
            GameScreen(gameScreenVm)
        }
    }
}