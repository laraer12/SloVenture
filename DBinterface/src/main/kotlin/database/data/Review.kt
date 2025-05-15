package database.data

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String? = null,
    val userId: String,
    val attractionId: String,
    val rating: Int,
    val ratingFamilyFriendly: Int,
    val ratingElderlyFriendly: Int,
    val ratingAccessible: Int,
    val createdAt: String? = null
)
