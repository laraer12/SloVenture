package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class UserViewHistory(
    val id: String? = null,
    val userId: String,
    val attractionId: String,
    val viewedAt: String? = null
) : DatabaseClass

suspend fun postUserViewHistory(userViewHistory: UserViewHistory): Boolean =
    postToDatabase(userViewHistory, "user-view-history", UserViewHistory.serializer())
