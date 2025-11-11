package org.pafoid.kboggle

import game.BoggleConfig
import org.pafoid.kboggle.game.GameServer
import org.pafoid.kboggle.web.MessageHandler
import org.pafoid.kboggle.web.SocketService

val socketService = SocketService()
val gameServer = GameServer(BoggleConfig.default, socketService)
val messageHandler = MessageHandler(socketService, gameServer)

fun main() {
    gameServer.initGame()
    socketService.start()
}