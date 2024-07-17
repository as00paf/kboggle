package services

import data.ChatMessage
import data.GameMessage
import data.JoinGameMessage
import data.LeaveGameMessage
import data.TestMessage
import data.WordGuessMessage
import io.ktor.client.plugins.websocket.sendSerialized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameService(private val socketService: SocketService) {

    fun handleMessage(message: GameMessage) {
        println("received message : $message")
        try {
            when (message) {
                is JoinGameMessage -> handleJoinGame(message)
                is WordGuessMessage -> handleWordGuess(message)
                is ChatMessage -> handleChatMessage(message)
                is LeaveGameMessage -> handleLeaveGame(message)
                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error: $e")
        }
    }

    fun joinGame(username: String) {
        println("sending join game message")
        CoroutineScope(Dispatchers.Default).launch {
            try {
                //socketService.send(JoinGameMessage(username))
                socketService.session?.sendSerialized(TestMessage.TestJoinMessage("test"))
            } catch (e: Exception) {
                println("Error: $e")
            }
        }
    }

    fun handleJoinGame(data: JoinGameMessage) {
        println("handleJoinGame: $data")
    }

    fun handleLeaveGame(data: LeaveGameMessage) {
        sync()
    }

    fun sync() {
        println("sync")
    }

    fun handleWordGuess(data: WordGuessMessage) {
        println("handleWordGuess")
    }

    fun handleChatMessage(data: ChatMessage) {
        println("handleChatMessage")
    }
}