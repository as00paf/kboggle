package org.pafoid.kboggle.console.commands

import org.pafoid.kboggle.console.Command
import org.pafoid.kboggle.web.ServerMetrics

class StatusCommand : Command {
    override val name = "status"
    override val description = "Show server status"

    override fun execute(args: List<String>): String {
        return ServerMetrics.getStats()
    }
}