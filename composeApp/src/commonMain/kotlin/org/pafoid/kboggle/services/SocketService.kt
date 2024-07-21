@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.pafoid.kboggle.services

import data.GameMessage
import data.dtos.GameMessageDTO

expect class SocketService() {
    val url: String
    suspend fun connect(onConnected: suspend () -> Unit, onMessage: (GameMessage) -> Unit)
    suspend inline fun <reified T: GameMessageDTO> send(message: T)
}