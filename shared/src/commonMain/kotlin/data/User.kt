package data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val name: String = "",
    var score: Int = 0,
    val foundWords: MutableList<String> = mutableListOf()
) {
    fun reset() {
        score = 0
        foundWords.clear()
    }
}