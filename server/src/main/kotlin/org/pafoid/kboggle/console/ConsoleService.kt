package org.pafoid.kboggle.console

import org.pafoid.kboggle.console.commands.HelpCommand
import org.pafoid.kboggle.console.commands.ListPlayersCommand
import org.pafoid.kboggle.console.commands.StatusCommand
import org.pafoid.kboggle.console.commands.StopCommand
import org.pafoid.kboggle.game.GameServer

class ConsoleService(private val gameServer: GameServer) {

    private val consoleReader = ConsoleReader()

    init {
        registerCommands()
    }

    private fun registerCommands() {
        val commands = listOf(
            HelpCommand(),
            ListPlayersCommand(gameServer),
            StatusCommand(),
            StopCommand()
        )
        CommandRegistry.register(commands)
    }

    fun start(){
        consoleReader.start()
    }

}