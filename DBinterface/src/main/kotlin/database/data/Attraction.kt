package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class Attraction(
    @SerialName("_id") val id: String,
    val name: String,
    @SerialName("regionId") val regionId: String?, //TODO
    val location: Coordinates,
    val address: Address?,
    val description: String?,
    val classification: String,
    val locationType: String,
    val elevation: Double,
    val accessibilityOptions: String?,
    val ratingFamilyFriendly: Double,
    val ratingElderlyFriendly: Double,
    val ratingAccessible: Double,
    val rating: Double,
    val googleMapsLink: String,
    val createdAt: String? = null,
    val images: List<AttractionImage> = emptyList()  //to sem dodala
) : DatabaseClass

@Serializable
data class Coordinates(
    val lat: Double,
    val lon: Double
) : DatabaseClass

@Serializable
data class Address(
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String
) : DatabaseClass


fun postAttraction(attraction: Attraction): Boolean =
    postToDatabase(attraction, "attractions", Attraction.serializer())


val json = Json { ignoreUnknownKeys = true }
val client = OkHttpClient()



@Serializable
data class FullAttractionData(
    val attraction: Attraction,
    val weatherData: WeatherData? = null,
    val nearbyAttractions: List<NearbyAttraction> = emptyList()
)

fun fetchFullAttractionData(id: String): FullAttractionData {
    val request = Request.Builder()
        .url("http://localhost:3001/attractions/fullKotlin/$id")
        .build()

    val response = client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch full attraction data: HTTP ${response.code}")
        }
        val responseBody = response.body?.string() ?: throw Exception("Empty response body")
        json.decodeFromString<FullAttractionData>(responseBody)
    }
    return response
}

fun postAttractionFromApi(attraction: Attraction): String? { //vraca svoj id v bazi
    val jsonAttraction = json.encodeToString(Attraction.serializer(), attraction)
    val mediaType = "application/json".toMediaType()
    val body = jsonAttraction.toRequestBody(mediaType)

    println("JSON payload being sent:\n$jsonAttraction")

    val request = Request.Builder()
        .url("http://localhost:3001/attractions")
        .post(body)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("POST failed: ${response.code}")
            return null
        }

        val responseBody = response.body?.string() ?: return null
        val responseJson = Json.parseToJsonElement(responseBody).jsonObject
        return responseJson["_id"]?.jsonPrimitive?.content
    }
}

fun updateAttraction(attraction: Attraction): Boolean {
    val jsonAttraction = json.encodeToString(Attraction.serializer(), attraction)
    val body = jsonAttraction.toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("http://localhost:3001/attractions/${attraction.id}")
        .put(body)
        .build()

    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}

fun deleteAttraction(id: String): Boolean {
    val request = Request.Builder()
        .url("http://localhost:3001/attractions/$id")
        .delete()
        .build()

    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}

@Serializable
data class AttractionMinimal(
    @SerialName("_id") val id: String,
    val name: String,
    val location: Coordinates?
)

fun getAllAttractions(): List<AttractionMinimal> {
    val request = Request.Builder()
        .url("http://localhost:3001/attractions/getAllAttractionsKotlin")
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch attractions: HTTP ${response.code}")
            return emptyList()
        }

        val responseBody = response.body?.string() ?: return emptyList()

        return try {
            json.decodeFromString<List<AttractionMinimal>>(responseBody)
        } catch (e: Exception) {
            println("Failed to parse attractions: ${e.message}")
            emptyList()
        }
    }
}



