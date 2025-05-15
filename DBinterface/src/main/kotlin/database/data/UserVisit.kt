package database.data

import kotlinx.serialization.Serializable

@Serializable
data class UserVisit(
    val id: String? = null,
    val userId: String,
    val attractionId: String,
    val visitDate: String? = null
)
