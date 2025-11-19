package org.pafoid.kboggle.console.commands

import org.pafoid.kboggle.console.Command
import org.pafoid.kboggle.game.GameServer

class ListPlayersCommand(private val gameServer: GameServer) : Command {
    override val name = "players"
    override val description = "List connected players"

    override fun execute(args: List<String>): String {
        val players = gameServer.users.map { it.name }

        return "Connected players (${players.size}):\n" +
                players.joinToString("\n") {
                    "- $it"
                }
    }
}