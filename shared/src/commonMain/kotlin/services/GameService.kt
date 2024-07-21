package services


import data.Chat
import data.GameJoined
import data.GameMessage
import data.LeaveGame
import data.Sync
import data.WordGuess
import data.dtos.GameMessageDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameService(private val socketService: SocketService) {

    fun handleMessage(message: GameMessage) {
        try {
            when (message) {
                is GameJoined -> handleGameJoined(message)
                is WordGuess -> handleWordGuess(message)
                is Chat -> handleChatMessage(message)
                is LeaveGame -> handleLeaveGame(message)
                is Sync -> handleSync(message)
                else -> {
                    println("Error: $message")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error: $e")
        }
    }

    private fun handleSync(data: Sync) {
        println("handleGameJoined: $data")
    }

    fun joinGame(username: String) {
        val message = GameMessageDTO.JoinGame(username)
        println("sending join game message : $message")
        CoroutineScope(Dispatchers.Default).launch {
            socketService.send(message)
        }
    }

    fun handleGameJoined(data: GameJoined) {
        println("handleGameJoined: $data")
    }

    fun handleLeaveGame(data: LeaveGame) {
        sync()
    }

    fun sync() {
        println("sync")
    }

    fun handleWordGuess(data: WordGuess) {
        println("handleWordGuess")
    }

    fun handleChatMessage(data: Chat) {
        println("handleChatMessage")
    }
}