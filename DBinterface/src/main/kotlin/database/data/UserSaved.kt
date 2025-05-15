package database.data

import kotlinx.serialization.Serializable

@Serializable
data class UserSaved(
    val id: String? = null,
    val userId: String,
    val attractionId: String
)
