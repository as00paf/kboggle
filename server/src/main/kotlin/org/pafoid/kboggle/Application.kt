package org.pafoid.kboggle

import SERVER_PORT
import data.Chat
import data.GameJoined
import data.GameMessage
import data.JoinGame
import data.LeaveGame
import data.Sync
import data.User
import data.WordGuess
import data.dtos.GameMessageDTO
import game.BoggleConfig
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.pafoid.kboggle.game.GameServer
import java.time.Duration
import java.util.*

val gameServer = GameServer(BoggleConfig.default, ::sync)
val connections = Collections.synchronizedSet<Connection>(LinkedHashSet())

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json {
            serializersModule = SerializersModule {
                polymorphic(GameMessage::class) {
                    subclass(JoinGame::class)
                    subclass(GameJoined::class)
                    subclass(WordGuess::class)
                    subclass(Sync::class)
                    subclass(Chat::class)
                    subclass(LeaveGame::class)
                }
            }
        })

        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        staticResources("/", "www") {
            default("www/index.html")
        }

        webSocket("/comms") {
            val connection = Connection(UUID.randomUUID().toString(), this)
            connections += connection
            try {
                for (frame in incoming) {
                    onReceiveMessage(connection)
                }
            } catch (e: ClosedReceiveChannelException) {
                e.printStackTrace()
                println("onClose ${closeReason.await()}")
            } catch (e: Throwable) {
                println("onError ${closeReason.await()}")
                e.printStackTrace()
            } catch (e: Exception) {
                println("onError $e")
                e.printStackTrace()
            } finally {
                println("Removing $connection!")
                connections -= connection
                sync()
            }
        }
    }

    gameServer.initGame()
}

// Todo: move
suspend fun onReceiveMessage(connection: Connection) {
    try {
        val session = connection.session ?: return
        /*for (frame in session.incoming) {
            val txt = (frame as? Frame.Text)?.readText()
            println("received message: $txt")
        }
        return*/
        val message = session.receiveDeserialized<GameMessage>()
        when (message) {
            is JoinGame -> handleJoinGame(connection, message)
            is WordGuess -> handleWordGuess(connection, message)
            is Chat -> handleChatMessage(connection, message)
            is LeaveGame -> handleLeaveGame(connection, message)
            else -> {
                println("received unhandled message : $message")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error: ${e.cause}")
    }
}

suspend fun handleLeaveGame(connection: Connection, data: LeaveGame) {
    connections.remove(connection)
    gameServer.leave(data.userId)
    sync()
}

suspend fun sync() {
    val userIds = gameServer.users.map { it.id }
    val gameConnections = connections.filter { userIds.contains(it.id) }

    gameConnections.forEach {
        it.session?.sendSerialized(GameMessageDTO.Sync(gameServer.data()))
    }
}

suspend fun handleJoinGame(connection: Connection, data: JoinGame) {
    println("handleJoinGame")
    val newUser = gameServer.join(User(connection.id, data.name, 1, mutableListOf()))
    if (newUser == null) {
        // TODO: will change because of outcome class
        println("error joining game")
    } else {
        println("${newUser.name} joined the game!")
        sync()
        connection.session?.sendSerialized(GameMessageDTO.GameJoined(newUser.name, gameServer.data()))
    }
}

suspend fun handleWordGuess(connection: Connection, data: WordGuess) {
    val points = gameServer.guessWord(data)
    connection.session?.sendSerialized(GameMessageDTO.WordGuessed(data.word, points, gameServer.data()))
    if(points != null) { sync() }
}

suspend fun handleChatMessage(connection: Connection, data: Chat) {

}

data class Connection(val id: String, val session: DefaultWebSocketServerSession?)