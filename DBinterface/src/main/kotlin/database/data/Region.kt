package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class Region(
    @SerialName("_id") val id: String? = null,
    val name: String,
    val location: List<Coordinates>
) : DatabaseClass

suspend fun postRegion(region: Region): Boolean =
    postToDatabase(region, "regions", Region.serializer())


suspend fun resolveOrCreateRegion(regionName: String): String? {
    if (regionName.isBlank()) return null

    val getRequest = Request.Builder()
        .url("http://localhost:3001/regions/region?name=$regionName")
        .get()
        .build()

    client.newCall(getRequest).execute().use { response ->
        if (response.isSuccessful) {
            val body = response.body?.string()
            if (!body.isNullOrBlank()) {
                val regionJson = Json.parseToJsonElement(body).jsonObject
                return regionJson["_id"]?.jsonPrimitive?.content
            }
        }
    }

    val regionPayload = Json.encodeToString(JsonObject.serializer(), buildJsonObject {
        put("name", JsonPrimitive(regionName))
    })

    val postRequest = Request.Builder()
        .url("http://localhost:3001/regions")
        .post(regionPayload.toRequestBody("application/json".toMediaType()))
        .build()

    client.newCall(postRequest).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to create region: ${response.code}")
            return null
        }

        val body = response.body?.string()
        if (!body.isNullOrBlank()) {
            val regionJson = Json.parseToJsonElement(body).jsonObject
            return regionJson["_id"]?.jsonPrimitive?.content
        }
    }

    return null
}

