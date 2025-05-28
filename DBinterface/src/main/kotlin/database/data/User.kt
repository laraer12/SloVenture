package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Request

@Serializable
data class User(
    @SerialName("_id") val id: String? = null,
    val username: String,
    val email: String,
    val password: String,
    val profilePicture: String? = null,
    val isAdmin: Boolean = false,
    val createdAt: String? = null
) : DatabaseClass

fun postUser(user: User): Boolean =
    postToDatabase(user, "users", User.serializer())

fun fetchUsersFromApi(): List<User> {
    val request = Request.Builder()
        .url("http://localhost:3001/users")
        .build()

    val response = client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch users: ${response.code}")
        }
        val body = response.body?.string() ?: throw Exception("Empty response body")

        json.decodeFromString<List<User>>(body)
    }
    return response
}
