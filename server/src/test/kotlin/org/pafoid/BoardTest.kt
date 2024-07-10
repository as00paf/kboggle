package org.pafoid

import org.pafoid.kboggle.game.Board
import kotlin.test.Test
import kotlin.test.assertNotNull

class BoardTest {

    @Test
    fun testBoardGeneration() {
        val board = Board()
        println("Board generated: $board")
        assertNotNull(board)
    }

}