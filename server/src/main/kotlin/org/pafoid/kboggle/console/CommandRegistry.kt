package org.pafoid.kboggle.console

object CommandRegistry {
    private val commands = mutableMapOf<String, Command>()

    fun register(command: Command) {
        commands[command.name] = command
    }

    fun register(commands: List<Command>) {
        commands.map {
            this.commands[it.name] = it
        }
    }

    fun execute(input: String): String {
        val parts = input.trim().split(" ")
        val commandName = parts[0].lowercase()
        val args = parts.drop(1)

        val command = commands[commandName]
        return command?.execute(args) ?: "Unknown command: $commandName"
    }

    fun listCommands(): String {
        return commands.values.joinToString("\n") {
            "${it.name} - ${it.description}"
        }
    }
}