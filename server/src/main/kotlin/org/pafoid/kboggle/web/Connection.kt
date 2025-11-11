package org.pafoid.kboggle.web

import data.GameMessage
import io.ktor.server.websocket.DefaultWebSocketServerSession
import kotlinx.coroutines.flow.MutableSharedFlow

data class Connection(
    val id: String,
    val session: DefaultWebSocketServerSession?,
    val specificFlow: MutableSharedFlow<GameMessage> = MutableSharedFlow()
)