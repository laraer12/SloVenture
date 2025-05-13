package database.data

import kotlinx.serialization.Serializable

@Serializable
data class UserViewHistory(
    val id: String? = null,
    val userId: String,
    val attractionId: String,
    val viewedAt: String? = null
)
