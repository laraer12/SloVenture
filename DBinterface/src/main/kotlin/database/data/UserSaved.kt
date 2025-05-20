package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class UserSaved(
    val id: String? = null,
    val userId: String,
    val attractionId: String
) : DatabaseClass

suspend fun postUserSaved(userSaved: UserSaved): Boolean =
    postToDatabase(userSaved, "user-saved", UserSaved.serializer())