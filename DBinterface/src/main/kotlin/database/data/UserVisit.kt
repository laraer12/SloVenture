package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class UserVisit(
    val id: String? = null,
    val userId: String,
    val attractionId: String,
    val visitDate: String? = null
) : DatabaseClass

suspend fun postUserVisit(userVisit: UserVisit): Boolean =
    postToDatabase(userVisit, "user-visit", UserVisit.serializer())
