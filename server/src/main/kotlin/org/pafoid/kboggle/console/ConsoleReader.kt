package org.pafoid.kboggle.console

import kotlin.concurrent.thread

class ConsoleReader {
    private var running = true

    fun start() {
        thread(name = "ConsoleReader") {
            println("Console ready. Type 'help' for commands.")

            while (running) {
                try {
                    print("> ")
                    val input = readlnOrNull() ?: break

                    if (input.isBlank()) continue

                    val result = CommandRegistry.execute(input)
                    println(result)
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }
        }
    }

    fun stop() {
        running = false
    }
}