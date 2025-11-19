package data.dtos

import kotlinx.serialization.Serializable

@Serializable
data class SetUsernameRequest(val username: String)