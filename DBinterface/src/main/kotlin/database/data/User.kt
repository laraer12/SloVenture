package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.util.Base64

fun hashPassword(password: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val hashed = md.digest(password.toByteArray())
    return Base64.getEncoder().encodeToString(hashed)
}


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
    postToDatabase(user, "users/Kotlin", User.serializer())

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

fun deleteUser(id: String): Boolean {
    val request = Request.Builder()
        .delete()
        .url("http://localhost:3001/users/$id")
        .build()

    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}

fun updateUser(user: User): Boolean {
    val requestBody = json.encodeToString(User.serializer(), user)
        .toRequestBody("application/json".toMediaType()) //TODO

    val request = Request.Builder()
        .put(requestBody)
        .url("http://localhost:3001/users/${user.id}")
        .build()

    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}
