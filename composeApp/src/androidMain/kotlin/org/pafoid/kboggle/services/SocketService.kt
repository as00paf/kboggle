package org.pafoid.kboggle.services

import data.Chat
import data.GameJoined
import data.GameMessage
import data.LeaveGame
import data.Sync
import data.WordGuess
import data.dtos.GameMessageDTO
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


actual class SocketService {
    actual val url: String = "ws://10.0.0.213:8080/comms"
    var session: DefaultClientWebSocketSession? = null

    var isConnected: Boolean = false

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json {
                serializersModule = SerializersModule {
                    polymorphic(GameMessage::class) {
                        subclass(GameJoined::class)
                        subclass(Sync::class)
                        subclass(WordGuess::class)
                        subclass(Chat::class)
                        subclass(LeaveGame::class)
                    }

                    prettyPrint = true
                }
            })

            pingInterval = 15_000L
            maxFrameSize = Long.MAX_VALUE
        }
    }

    actual suspend fun connect(onConnected: suspend () -> Unit, onMessage: (GameMessage) -> Unit) {
        try {
            client.webSocket(url) {
                session = this
                isConnected = true
                onConnected()
                try {
                    send(Frame.Text("First comm"))
                    for (frame in incoming) {
                        val message = receiveDeserialized<GameMessage>()
                        println("Message received: $message")
                        onMessage(message)
                    }
                } catch (e: ClosedReceiveChannelException) {
                    println("onClose ${closeReason.await()}")
                } catch (e: Throwable) {
                    println("onError ${closeReason.await()}")
                    e.printStackTrace()
                } catch (e: Exception) {
                    println("onError $e")
                    e.printStackTrace()
                } finally {
                    isConnected = false
                    println("Disconnected")
                }
            }
        } catch (e: Exception) {
            println("Error connecting: $e")
            e.printStackTrace()
        }
        finally {
            client.close()
        }
    }

    actual suspend inline fun <reified T: GameMessageDTO> send(message: T) {
        if (session == null) {
            println("Session is null")
            return
        }
        try {
            session?.sendSerialized(message)
        } catch (e: Exception) {
            println("Error: $e")
        }
    }
}