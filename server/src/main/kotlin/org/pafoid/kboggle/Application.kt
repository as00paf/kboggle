package org.pafoid.kboggle

import game.BoggleConfig
import org.pafoid.kboggle.console.ConsoleService
import org.pafoid.kboggle.game.GameServer
import org.pafoid.kboggle.web.SocketService

val socketService = SocketService()
val gameServer = GameServer(BoggleConfig.default, socketService)
val consoleService = ConsoleService(gameServer)

fun main() {
    consoleService.start()
    gameServer.initGame()
    socketService.start()
}