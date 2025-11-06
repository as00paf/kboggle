package org.pafoid.kboggle.viewmodels

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import org.pafoid.kboggle.services.GameService

class GameScreenViewModel(
    private val navController: NavHostController,
    private val gameService: GameService
): ViewModel() {

    val letters = "KOLOSSALBOGGLEFR".split("").filterNot { it.isEmpty() }

}