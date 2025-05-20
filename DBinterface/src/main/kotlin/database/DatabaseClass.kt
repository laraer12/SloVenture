package database
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

interface DatabaseClass {
}

val json = Json { ignoreUnknownKeys = true }
val client = OkHttpClient()

inline fun <reified T : DatabaseClass> postToDatabase(
    obj: T,
    endpoint: String,
    serializer: KSerializer<T>
): Boolean {
    val jsonBody = json.encodeToString(serializer, obj)
    val mediaType = "application/json".toMediaType()
    val body = jsonBody.toRequestBody(mediaType)

    println("JSON payload being sent:\n$jsonBody")

    val request = Request.Builder()
        .url("http://localhost:3001/$endpoint")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("POST failed: ${response.code}")
            return false
        }

        return true
    }
}
