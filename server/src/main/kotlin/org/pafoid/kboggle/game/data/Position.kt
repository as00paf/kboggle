package org.pafoid.kboggle.game.data

data class Position(val row: Int, val col: Int)

fun Position.isValid(board: List<List<Char>>): Boolean {
    return this.row in board.indices && this.col in board[0].indices
}