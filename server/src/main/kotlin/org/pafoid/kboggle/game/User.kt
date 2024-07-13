package org.pafoid.kboggle.game

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: String? = null, val name: String = "", var score: Int = 25, val foundWords: MutableList<String> = mutableListOf()) {
    fun reset() {
        score = 0
        foundWords.clear()
    }
}