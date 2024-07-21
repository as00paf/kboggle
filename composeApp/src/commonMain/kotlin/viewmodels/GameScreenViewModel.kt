package viewmodels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import services.GameService
import services.SocketService

class GameScreenViewModel(
    private val navController: NavController,
    private val socketService:SocketService,
    private val gameService:GameService
): ViewModel() {

}