package org.pafoid.kboggle.game

import kotlinx.serialization.Serializable

@Serializable data class UserDTO(val name: String)

fun UserDTO.data():User {
    return User(null, name)
}