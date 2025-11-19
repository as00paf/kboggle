package org.pafoid.kboggle.web

import data.Chat
import data.GameJoined
import data.JoinGame
import data.LeaveGame
import data.RejoinGame
import data.Sync
import data.User
import data.WordGuess
import data.WordGuessed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.pafoid.kboggle.game.GameServer
import java.util.*

class MessageHandler(private val socketService: SocketService, private val gameServer: GameServer) {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            socketService.sharedIncomingMessageFlow.collectLatest { clientMessage ->
                when(clientMessage.message){
                    is JoinGame -> handleJoinGame(clientMessage.sessionId, clientMessage.message)
                    is RejoinGame -> handleRejoinGame(clientMessage.sessionId, clientMessage.message)
                    is WordGuess -> handleWordGuess(clientMessage.sessionId, clientMessage.message)
                    is Chat -> handleChatMessage(clientMessage.message)
                    is LeaveGame -> handleLeaveGame(clientMessage.message)
                    else -> {
                        println("received unhandled message : ${clientMessage.message}")
                    }
                }
            }
        }
    }

    private fun sync() {
        socketService.sendToAll(Sync(gameServer.data()))
    }

    private fun handleLeaveGame(data: LeaveGame) {
        gameServer.leave(data.userId)
        sync()
    }

    private fun handleJoinGame(sessionId: String, data: JoinGame) {
        println("handleJoinGame")
        val userId = UUID.randomUUID().toString()
        val newUser = gameServer.join(User(userId, data.name))
        if (newUser == null) {
            // TODO: will change because of outcome class
            println("error joining game")
        } else {
            println("${newUser.name} joined the game!")
            sync()
            socketService.sendToConnection(sessionId, GameJoined(newUser.name, gameServer.data()))
        }
    }

    private fun handleRejoinGame(sessionId: String, data: RejoinGame) {
        println("handleRejoinGame")
        handleJoinGame(sessionId, JoinGame(data.name))
    }

    private fun handleWordGuess(sessionId: String, data: WordGuess) {
        val points = gameServer.guessWord(data)
        socketService.sendToConnection(sessionId, WordGuessed(data.word, points, gameServer.data()))
        if(points != null) { sync() }
    }

    private fun handleChatMessage(data: Chat) {

    }

}