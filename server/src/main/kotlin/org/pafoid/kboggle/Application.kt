package org.pafoid.kboggle

import SERVER_PORT
import data.ChatMessage
import data.GameMessage
import data.JoinGameMessage
import data.LeaveGameMessage
import data.SyncMessage
import data.User
import data.WordGuessMessage
import data.WordGuessedMessage
import game.BoggleConfig
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondFile
import io.ktor.server.routing.get
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
import java.io.File
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
                    subclass(JoinGameMessage::class)
                    subclass(WordGuessMessage::class)
                    subclass(ChatMessage::class)
                    subclass(LeaveGameMessage::class)
                }
            }
        })

        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        staticFiles("/", File("server/src/main/resources/www"))

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
            }
        }

        get("/") {
            call.respondFile(File("server/src/main/resources/www/index.html"))
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
            is JoinGameMessage -> handleJoinGame(connection, message)
            is WordGuessMessage -> handleWordGuess(connection, message)
            is ChatMessage -> handleChatMessage(connection, message)
            is LeaveGameMessage -> handleLeaveGame(connection, message)
            else -> {
                println("received unhandled message : $message")
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error: ${e.cause}")
    }
}

suspend fun handleLeaveGame(connection: Connection, data: LeaveGameMessage) {
    connections.remove(connection)
    gameServer.leave(data.userId)
    sync()
}

suspend fun sync() {
    val userIds = gameServer.users.map { it.id }
    val gameConnections = connections.filter { userIds.contains(it.id) }

    gameConnections.forEach {
        it.session?.sendSerialized(SyncMessage("sync", gameServer.data()))
    }
}

suspend fun handleJoinGame(connection: Connection, data: JoinGameMessage) {
    println("handleJoinGame")
    val newUser = gameServer.join(User(connection.id, data.name, 1, mutableListOf()))
    if (newUser == null) {
        // TODO: will change because of outcome class
        println("error joining game")
    } else {
        println("${newUser.name} joined the game!")
        connection.session?.sendSerialized(SyncMessage("game_joined", gameServer.data()))
        sync()
    }
}

suspend fun handleWordGuess(connection: Connection, data: WordGuessMessage) {
    val points = gameServer.guessWord(data)
    connection.session?.sendSerialized(WordGuessedMessage("word_guess", data.word, points, gameServer.data()))
    if(points != null) { sync() }
}

suspend fun handleChatMessage(connection: Connection, data: ChatMessage) {

}

data class Connection(val id: String, val session: DefaultWebSocketServerSession?)