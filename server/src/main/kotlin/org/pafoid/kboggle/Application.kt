package org.pafoid.kboggle

import SERVER_PORT
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.logging.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.pafoid.kboggle.game.BoggleConfig
import org.pafoid.kboggle.game.GameServer
import org.pafoid.kboggle.game.User
import java.io.File

val gameServer = GameServer(BoggleConfig.default)

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        post("/join-game") {
            try {
                val player = call.receive<User>()
                gameServer.join(player)
            } catch (e: Exception) {
                println("Error : ${e}")
            }

            call.respond(gameServer.data)
        }

        post("/submit-word") {
            // Handle word submission logic
        }

        get("/") {
            call.respondFile(File("server/src/main/resources/www/index.html"))
        }
    }

    gameServer.initGame()
}