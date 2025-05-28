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
    @SerialName("regionId") val region: Region?,
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
    val verified: Boolean,
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


suspend fun postAttraction(attraction: Attraction): Boolean =
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

    // raw JSON string
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

        // parse response JSON to extract MongoDB ObjectId
        val responseBody = response.body?.string() ?: return null
        val responseJson = Json.parseToJsonElement(responseBody).jsonObject
        return responseJson["_id"]?.jsonPrimitive?.content
    }
}
/*


suspend fun postAttractionsWithImages(attractions: List<Attraction>): Boolean {
    for (attraction in attractions) {
        val jsonAttraction = json.encodeToString(Attraction.serializer(), attraction)
        val mediaType = "application/json".toMediaType()
        val attractionBody = jsonAttraction.toRequestBody(mediaType)

        val attractionRequest = Request.Builder()
            .url("http://localhost:3001/attractions")
            .post(attractionBody)
            .build()

        println("Posting attraction:\n$jsonAttraction")

        val attractionResponse = client.newCall(attractionRequest).execute()
        if (!attractionResponse.isSuccessful) {
            println("Failed to post attraction: ${attractionResponse.code}")
            attractionResponse.close()
            return false
        }

        // Get the posted attraction ID (assuming backend returns full object)
        val savedAttractionJson = attractionResponse.body?.string()
        attractionResponse.close()

        val savedAttraction = json.decodeFromString(Attraction.serializer(), savedAttractionJson!!)
        val attractionId = savedAttraction.id

        // Post each image associated with the attraction
        for (image in attraction.images) {
            val imageWithAttractionId = image.copy(attractionId = attractionId)
            val jsonImage = json.encodeToString(AttractionImage.serializer(), imageWithAttractionId)
            val imageBody = jsonImage.toRequestBody(mediaType)

            val imageRequest = Request.Builder()
                .url("http://localhost:3001/attractionImages")
                .post(imageBody)
                .build()

            println("Posting image:\n$jsonImage")

            val imageResponse = client.newCall(imageRequest).execute()
            if (!imageResponse.isSuccessful) {
                println("Failed to post image: ${imageResponse.code}")
                imageResponse.close()
                return false
            }
            imageResponse.close()
        }
    }

    return true
}



*/