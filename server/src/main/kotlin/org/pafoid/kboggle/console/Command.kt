package org.pafoid.kboggle.console

interface Command {
    val name: String
    val description: String
    fun execute(args: List<String>): String
}