package org.pafoid.kboggle.web

import kotlin.time.Duration.Companion.milliseconds

object ServerMetrics {
    private val startTime = System.currentTimeMillis()
    var totalMessagesReceived = 0L
    var totalMessagesSent = 0L
    var peakConnections = 0

    fun getUptime(): String {
        val uptime = (System.currentTimeMillis() - startTime).milliseconds

        val days = uptime.inWholeDays
        val hours = (uptime.inWholeHours % 24)
        val minutes = (uptime.inWholeMinutes % 60)
        val seconds = (uptime.inWholeSeconds % 60)

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0 || days > 0) append("${hours.toString().padStart(2, '0')}h ")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes.toString().padStart(2, '0')}m ")
            append("${seconds.toString().padStart(2, '0')}s")
        }.trim()
    }

    fun recordMessage(sent: Boolean) {
        if (sent) totalMessagesSent++ else totalMessagesReceived++
    }

    fun updatePeakConnections(current: Int) {
        peakConnections = maxOf(peakConnections, current)
    }

    fun getStats(): String {
        return """
            Server Statistics:
            - Uptime: ${getUptime()}
            - Messages sent: $totalMessagesSent
            - Messages received: $totalMessagesReceived
            - Peak connections: $peakConnections
        """.trimIndent()
    }
}