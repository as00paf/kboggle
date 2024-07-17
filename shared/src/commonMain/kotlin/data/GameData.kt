package data

import game.Board
import kotlinx.serialization.Serializable

@Serializable
data class Data(val users:List<User>, val prevUsers: List<User>, val isGameStarted: Boolean, val board: Board? = null, val currentWords: List<String>, val currentMaxScore: Int, val currentTime:Int, val currentState: String, val winners: List<User>)