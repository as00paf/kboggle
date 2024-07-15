package game

import kotlinx.serialization.Serializable
import utils.selectRandomLetter

@Serializable
class Board(var letters:List<Char> = listOf()){

    init {
        generateLetters()
    }

    private fun generateLetters(): List<Char> {
        if(letters.isNotEmpty()) return letters

        val shuffledDice = Boggle.DICE.shuffled()
        letters = shuffledDice.map { it.selectRandomLetter() }
        println("Board generated: \n$this")
        return letters
    }

    override fun toString(): String {
        val boardString = letters.chunked(4) { it.joinToString("") }.joinToString("\n").uppercase()
        return "Board(\n$boardString\n)"
    }
}