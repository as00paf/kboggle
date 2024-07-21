@file:OptIn(ExperimentalSerializationApi::class)

package data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
sealed class GameMessage
@Serializable
@SerialName("JoinGame")
data class JoinGame(val name: String) : GameMessage()

@Serializable
@SerialName("LeaveGame")
data class LeaveGame(val userId: String) : GameMessage()

@Serializable
@SerialName("GameJoined")
data class GameJoined(val name: String, val gameData: Data): GameMessage()

@Serializable
@SerialName("WordGuess")
data class WordGuess(val userId: String, val word: String) : GameMessage()

@Serializable
@SerialName("Chat")
data class Chat(val userId: String, val chat: String) : GameMessage()

@Serializable
@SerialName("Sync")
data class Sync(val gameData: Data): GameMessage()

@Serializable
@SerialName("WordGuessed")
data class WordGuessed(val type: String, val word: String?, val points:Int?, val gameData: Data)

