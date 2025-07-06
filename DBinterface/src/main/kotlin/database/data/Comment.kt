package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String? = null,
    val userId: String,
    val attractionId: String,
    val text: String,
    val createdAt: String? = null
) : DatabaseClass

suspend fun postComment(comment: Comment): Boolean =
    postToDatabase(comment, "comments", Comment.serializer())