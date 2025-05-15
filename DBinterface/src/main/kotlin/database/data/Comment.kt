package database.data

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String? = null,
    val userId: String,
    val attractionId: String,
    val text: String,
    val createdAt: String? = null
)