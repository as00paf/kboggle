package org.pafoid

import kotlinx.coroutines.runBlocking
import game.Board
import game.Boggle
import org.pafoid.kboggle.game.Solver
import kotlin.test.Test
import kotlin.test.assertEquals

const val WORD_COUNT = 72286
const val SOLVABLE_WORD_COUNT = 91

class SolverTest {

    @Test
    fun testBoardSolving() {
        val board = Board("ELLEPOSETRESBIEN".lowercase().toCharArray().toList())
        println("Board generated: $board")

        val solver = Solver()
        runBlocking {
            solver.dictionary = solver.loadWordsFromResources(Boggle.DICTIONARY)

            println("Solver initialized: ${solver.dictionary.size} words loaded")

            //assertEquals(WORD_COUNT, solver.dictionary.size)

            val wordCount = solver.solve(board).size
            println("Board solved: $wordCount words are accepted for this board")

            assertEquals(wordCount, SOLVABLE_WORD_COUNT)
        }
    }

    @Test
    fun testIndex() {
        val board = Board("ELLEPOSETRESBIEN".lowercase().toCharArray().toList())
        println("Board generated: $board")

        val solver = Solver()
        runBlocking {
            solver.dictionary = solver.loadWordsFromResources(Boggle.DICTIONARY)

            println("Solver initialized: ${solver.dictionary.size} words loaded")

            val word = "f"
            println("Index of word '$word' ${solver.indexOfWord(word)}")
        }
    }

}