package org.pafoid.kboggle.game

data class BoggleConfig(
    val gameLength: Int,
    val endScreenLength: Int,
    val restartScreenLength: Int,
    val interval: Long = 1000L
){
    companion object{
        val default by lazy { BoggleConfig(180, 30, 3) }
        val debug by lazy { BoggleConfig(45, 20, 3) }
        val speed by lazy { BoggleConfig(180, 30, 3, 100) }
    }
}

