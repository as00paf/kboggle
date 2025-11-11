package org.pafoid.kboggle.web

import data.GameMessage

data class ClientMessage(val sessionId: String, val message:GameMessage)