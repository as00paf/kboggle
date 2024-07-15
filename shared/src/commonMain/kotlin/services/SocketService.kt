package services

import data.ChatMessage
import data.GameMessage
import data.JoinGameMessage
import data.LeaveGameMessage
import data.WordGuessMessage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass


class SocketService(private val url: String = "ws://10.0.0.213:8080/comms") {
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
                        subclass(JoinGameMessage::class)
                        subclass(WordGuessMessage::class)
                        subclass(ChatMessage::class)
                        subclass(LeaveGameMessage::class)
                    }
                    prettyPrint = true
                }
            })

            pingInterval = 15000L
            maxFrameSize = Long.MAX_VALUE
        }
    }

    var session: DefaultClientWebSocketSession? = null

    suspend fun connect(onConnected: suspend () -> Unit, onMessage: (GameMessage) -> Unit) {
        try {
            client.webSocket(host = "10.0.0.213", port = 8080, path = "comms") {
                println("Socket connected!")
                session = this
                isConnected = true
                onConnected()
                try {
                    send(Frame.Text("First comm"))
                    for (frame in incoming) {
                        println((frame as? Frame.Text)?.readText())
                        //onMessage(receiveDeserialized())
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

    suspend inline fun <reified T:GameMessage> send(message: T) {
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