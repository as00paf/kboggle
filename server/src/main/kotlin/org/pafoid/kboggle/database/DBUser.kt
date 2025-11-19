package org.pafoid.kboggle.database

import kotlinx.serialization.Serializable

@Serializable
data class DBUser(
    val id: String,
    val email: String,
    val username: String,
    val authProvider: AuthProvider,
    val createdAt: Long = System.currentTimeMillis()
)