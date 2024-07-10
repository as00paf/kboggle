package org.pafoid.kboggle

import SERVER_PORT
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.pafoid.kboggle.game.BoggleConfig
import org.pafoid.kboggle.game.GameServer
import org.pafoid.kboggle.game.UserDTO
import org.pafoid.kboggle.game.data
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
            val player = call.receive<UserDTO>().data()
            gameServer.join(player)

            call.respond(gameServer.data)
        }

        post("/submit-word") {
            // Handle word submission logic
        }

        get("/") {
            //call.respondText("Welcome to the Boggle Game Server!")
            call.respondFile(File("server/src/main/resources/www/index.html"))
        }
    }

    gameServer.initGame()
}