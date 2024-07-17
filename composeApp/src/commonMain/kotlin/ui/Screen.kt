package ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Game : Screen("game")
}