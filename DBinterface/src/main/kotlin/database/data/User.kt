package database.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val username: String,
    val email: String,
    val password: String,
    val profilePicture: String,
    val role: String,
    val createdAt: String? = null
)
