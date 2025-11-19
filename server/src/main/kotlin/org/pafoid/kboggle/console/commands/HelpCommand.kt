package org.pafoid.kboggle.console.commands

import org.pafoid.kboggle.console.Command
import org.pafoid.kboggle.console.CommandRegistry

class HelpCommand : Command {
    override val name = "help"
    override val description = "Show available commands"

    override fun execute(args: List<String>): String {
        return "Available commands:\n${CommandRegistry.listCommands()}"
    }
}