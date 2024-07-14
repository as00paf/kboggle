package org.pafoid.kboggle.game.data

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.pafoid.kboggle.game.GameServer

@Serializable
@Polymorphic
sealed class GameMessage

@Serializable
@SerialName("JoinGameMessage")
data class JoinGameMessage(val name: String) : GameMessage()

@Serializable
@SerialName("LeaveGameMessage")
data class LeaveGameMessage(val userId: String) : GameMessage()

@Serializable
@SerialName("WordGuessMessage")
data class WordGuessMessage(val userId: String, val word: String) : GameMessage()

@Serializable
@SerialName("ChatMessage")
data class ChatMessage(val userId: String, val chat: String) : GameMessage()

@Serializable
data class SyncMessage(val type: String, val gameData: GameServer.Data)

@Serializable
data class WordGuessedMessage(val type: String, val word: String?, val points:Int?, val gameData: GameServer.Data)