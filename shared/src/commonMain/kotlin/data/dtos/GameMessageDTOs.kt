@file:OptIn(ExperimentalSerializationApi::class)

package data.dtos

import data.Data
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
sealed class GameMessageDTO(val type: String) {
    @Serializable
    @SerialName("JoinGame")
    data class JoinGame(val name: String) : GameMessageDTO("JoinGame")

    @Serializable
    @SerialName("GameJoined")
    data class GameJoined(val name: String, val gameData: Data) : GameMessageDTO("GameJoined")

    @Serializable
    @SerialName("LeaveGame")
    data class LeaveGame(val userId: String) : GameMessageDTO("LeaveGame")

    @Serializable
    @SerialName("WordGuess")
    data class WordGuess(val userId: String, val word: String) : GameMessageDTO("WordGuess")

    @Serializable
    @SerialName("Chat")
    data class Chat(val userId: String, val chat: String) : GameMessageDTO("Chat")

    @Serializable
    @SerialName("Sync")
    data class Sync(val gameData: Data): GameMessageDTO("Sync")

    @Serializable
    @SerialName("WordGuessed")
    data class WordGuessed(val word: String?, val points:Int?, val gameData: Data): GameMessageDTO("WordGuessed")
}
