package viewmodels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import data.GameJoined
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import services.GameService
import services.SocketService
import ui.Screen

class LoginScreenViewModel(
    private val navController: NavController,
    private val socketService:SocketService,
    private val gameService:GameService
): ViewModel() {
    fun isUserNameValid(username: String): Boolean {
        return username.isNotEmpty()
    }

    fun joinGame(username: String) {
        CoroutineScope(Dispatchers.Default).launch {
            socketService.connect ({
                println("Socket connected!")
                gameService.joinGame(username)
            }, {
                gameService.handleMessage(it)
                if(it is GameJoined) {
                    println("Game joined!")
                    CoroutineScope(Dispatchers.Main).launch {
                        navController.navigate(Screen.Game.route)
                    }
                }
            })
        }
    }
}