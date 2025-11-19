package org.pafoid.kboggle.console.commands

import org.pafoid.kboggle.console.Command
import kotlin.system.exitProcess

class StopCommand : Command {
    override val name = "stop"
    override val description = "Stop the server"

    override fun execute(args: List<String>): String {
        println("Shutting down server...")
        exitProcess(0)
    }
}