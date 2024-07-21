package org.pafoid.kboggle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import org.pafoid.kboggle.services.GameService
import org.pafoid.kboggle.services.SocketService

class GameScreenViewModel(
    private val navController: NavHostController,
    private val socketService: SocketService,
    private val gameService: GameService
): ViewModel() {

}