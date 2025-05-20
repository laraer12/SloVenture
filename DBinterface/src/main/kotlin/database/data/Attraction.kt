package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class Attraction(
    val id: String,
    val name: String,
    val regionId: String,
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
    val requiresReservation: Boolean,
    val openingHours: OpeningHours,
    val entryFee: Double,
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

@Serializable
data class OpeningHours(
    val monday: String,
    val tuesday: String,
    val wednesday: String,
    val thursday: String,
    val friday: String,
    val saturday: String,
    val sunday: String
) : DatabaseClass



suspend fun postAttraction(attraction: Attraction): Boolean =
    postToDatabase(attraction, "attractions", Attraction.serializer())



//povezava z web service

val json = Json { ignoreUnknownKeys = true }
val client = OkHttpClient()
/*
suspend fun getAllAttractions(): List<Attraction>? {
    val request = Request.Builder()
        .url("http://localhost:3001/api/attractions")
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("GET failed: ${response.code}")
            return null
        }

        val body = response.body?.string() ?: return null
        return json.decodeFromString<List<Attraction>>(body)
    }
}
*/

suspend fun postAttractionFromApi(attraction: Attraction): String? { //vraca svoj id v bazi
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


suspend fun getAttractionsByRegion(regionId: String): List<Attraction>? {
    val request = Request.Builder()
        .url("http://localhost:3001/api/attractions/region/$regionId")
        .get()
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("GET by region failed: ${response.code}")
            return null
        }

        val body = response.body?.string() ?: return null
        return json.decodeFromString<List<Attraction>>(body)
    }
}

suspend fun updateAttraction(id: String, updated: Attraction): Boolean {
    val jsonAttraction = json.encodeToString(Attraction.serializer(), updated)
    val body = jsonAttraction.toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url("http://localhost:3001/api/attractions/$id")
        .put(body)
        .build()

    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}

suspend fun deleteAttraction(id: String): Boolean {
    val request = Request.Builder()
        .url("http://localhost:3001/api/attractions/$id")
        .delete()
        .build()

    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}
*/


