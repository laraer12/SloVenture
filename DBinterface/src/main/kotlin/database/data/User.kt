package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
    val createdAt: String? = null,
    val isFakeData: Boolean
) : DatabaseClass

fun postUser(user: User): Boolean =
    postToDatabase(user, "users/Kotlin", User.serializer())

fun fetchUsers(): List<User> {
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

fun getUserIdByUsername(username: String): String? {
    val url = "http://localhost:3001/users/getIdByUsername/$username"
    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("Failed to fetch user: ${response.code}")
                return null
            }

            val body = response.body?.string() ?: return null
            val json = Json.parseToJsonElement(body).jsonObject

            return json["_id"]?.jsonPrimitive?.content
        }
    } catch (e: Exception) {
        println("Exception fetching user ID: ${e.localizedMessage}")
        null
    }
}

@Serializable
data class AdminUser(val id: String, val username: String)
fun fetchAdminUsers(): List<AdminUser> {
    val request = Request.Builder()
        .url("http://localhost:3001/users/getAdmins")
        .build()

    val adminUsers: List<AdminUser> = client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch admin users: ${response.code}")
        }

        val body = response.body?.string() ?: throw Exception("Empty response body")

        println("Raw response body: $body")

        json.decodeFromString(body)
    }

    println("Parsed admin users: $adminUsers")
    return adminUsers
}

