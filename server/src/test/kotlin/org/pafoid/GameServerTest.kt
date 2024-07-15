package org.pafoid

import kotlinx.coroutines.test.runTest
import game.BoggleConfig
import org.pafoid.kboggle.game.GameServer
import org.pafoid.kboggle.game.state.GameState
import kotlin.test.Test
import kotlin.test.assertEquals

class GameServerTest {

    @Test
    fun testGameServer() = runTest {
        val gameServer = GameServer(BoggleConfig.default, {sync()})
        assertEquals(gameServer.gameState, GameState.INIT)
        gameServer.initGame()
        assertEquals(gameServer.gameState, GameState.STARTED)
    }

    suspend fun sync(){

    }
}