package org.pafoid.kboggle.web

import Constants.SERVER_PORT
import data.Chat
import data.GameJoined
import data.GameMessage
import data.JoinGame
import data.LeaveGame
import data.RejoinGame
import data.Sync
import data.WordGuess
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

class SocketService {
    private val incomingMessageFlow = MutableSharedFlow<ClientMessage>()
    val sharedIncomingMessageFlow = incomingMessageFlow.asSharedFlow()

    private val broadcastFlow = MutableSharedFlow<GameMessage>()
    private val connections = ConcurrentHashMap<String, Connection>()

    private val context = CoroutineScope(Dispatchers.IO)

    fun start() {
        val port = System.getenv("BOGGLE_SERVER_PORT")?.toInt() ?: SERVER_PORT
        embeddedServer(Netty, port = port, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json()
            }

            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json {
                    serializersModule = SerializersModule {
                        polymorphic(GameMessage::class) {
                            subclass(JoinGame::class)
                            subclass(RejoinGame::class)
                            subclass(GameJoined::class)
                            subclass(WordGuess::class)
                            subclass(Sync::class)
                            subclass(Chat::class)
                            subclass(LeaveGame::class)
                        }
                    }
                })

                pingPeriod = 15.seconds
                timeout = 15.seconds
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                staticResources("/", "www") {
                    default("www/index.html")
                }

                webSocket("/comms") {
                    val connectionId = UUID.randomUUID().toString()
                    val connection = Connection(connectionId, this)
                    connections[connectionId] = connection
                    ServerMetrics.updatePeakConnections(connections.size)

                    val broadcastJob = launch {
                        broadcastFlow.collect { message ->
                            sendSerialized(message)
                            ServerMetrics.recordMessage(true)
                        }
                    }

                    val specificJob = launch {
                        connection.specificFlow.collect { message ->
                            sendSerialized(message)
                            ServerMetrics.recordMessage(true)
                        }
                    }

                    runCatching {
                        incoming.consumeEach { frame ->
                            if (frame is Frame.Text) {
                                val messageResponse = receiveDeserialized<GameMessage>()
                                incomingMessageFlow.emit(ClientMessage(connectionId, messageResponse))
                                ServerMetrics.recordMessage(false)
                            }
                        }
                    }.onFailure { exception ->
                        println("WebSocket exception: ${exception.localizedMessage}")
                    }.also {
                        broadcastJob.cancel()
                        specificJob.cancel()
                        connections.remove(connectionId)
                    }
                }
            }
        }
            .start(wait = true)
    }

    fun sendToAll(message: GameMessage) = context.launch {
        broadcastFlow.emit(message)
    }

    fun sendToConnection(connectionId: String, message: GameMessage) = context.launch {
        val connection = connections[connectionId]
        connection?.specificFlow?.emit(message)
    }

    fun sendToMultiple(connectionIds: List<String>, message: GameMessage) = context.launch {
        connectionIds.forEach { id ->
            connections[id]?.specificFlow?.emit(message)
        }
    }
}
