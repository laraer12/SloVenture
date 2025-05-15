package api

import database.data.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.time.Instant
import java.time.format.DateTimeFormatter

private val client = HttpClient()

val validCategories =
    setOf("pohodnistvo", "kultura", "naravne-lepote", "poletna-osvezitev", "raziskovanje", "feretanje", "supanje")
val validTypes = setOf(
    "hrib",
    "koca",
    "cerkev",
    "planina",
    "drugo",
    "jezero",
    "grad",
    "slap",
    "park",
    "kopalisce",
    "dvorec",
    "muzej-na-prostem",
    "izvir",
    "jama",
    "razgledni-stolp",
    "sup-tocka",
    "bivak",
    "soteska",
    "ferata",
    "pravljicna-pot"
)

sealed class RetrieveResult {
    data class Success(val data: List<EnrichedAttraction>) : RetrieveResult()
    data class Error(val message: String) : RetrieveResult()
}
data class EnrichedAttraction(
    val attraction: Attraction,
    val images: List<AttractionImage>
)

suspend fun retrieveAttractions(
    category: String?,
    type: String?,
    num: Int?,
    page: Int?
): RetrieveResult {
    // vaidacija
    if (num == null || page == null) {
        return RetrieveResult.Error("Missing parameters: please provide num and page")
    }
    if (num < 1 || page < 1) {
        return RetrieveResult.Error("Invalid parameters: num and page should be greater than 0")
    }
    if (num * page > 2696 + num) {
        return RetrieveResult.Error("Invalid parameters: num * page should be less than 2696")
    }
    if (category != null && category !in validCategories) {
        return RetrieveResult.Error("Invalid category: please provide a valid category")
    }
    if (type != null && type !in validTypes) {
        return RetrieveResult.Error("Invalid type: please provide a valid type")
    }

    val endpoint = "https://api.kamzavikend.si/public/search"

    return try {
        val response: HttpResponse = client.get(endpoint) {
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            parameter("page[size]", num)
            parameter("page[number]", page)
            if (type != null) parameter("filter[type.slug]", type)
            if (category != null) parameter("filter[categories.slug]", category)
        }

        val jsonText = response.bodyAsText()

        //test
        println("response: ")
        println(jsonText)

        val json = Json.parseToJsonElement(jsonText).jsonObject
        val dataArray = json["data"]?.jsonArray ?: return RetrieveResult.Error("No data in response")

        val attractions = dataArray.map { attractionJsonElement ->
            val obj = attractionJsonElement.jsonObject
            val attraction = parseAttraction(obj)
            val images = parseAttractionImages(obj)
            EnrichedAttraction(attraction, images)
        }

        RetrieveResult.Success(attractions)
    } catch (e: Exception) {
        RetrieveResult.Error("Error fetching data from kamzavikend.si: ${e.localizedMessage}")
    }
}


fun parseAttraction(json: JsonObject): Attraction {
    val id = json["id"]?.jsonPrimitive?.content ?: "" //zaenkrat puscam njihov id
    val name = json["name"]?.jsonPrimitive?.content ?: "Unknown"

    //pustla bom njihov id za pokrajine ker je zaenkrat tak najlazje...
    val areas = json["areas"]?.jsonArray ?: JsonArray(emptyList())
    val regionId = areas.firstOrNull {
        it.jsonObject["type"]?.jsonObject?.get("key")?.jsonPrimitive?.content == "region"
    }?.jsonObject?.get("id")?.jsonPrimitive?.content ?: ""

    val locationJson = json["location"]?.jsonObject
    val lat = locationJson?.get("lat")?.jsonPrimitive?.doubleOrNull ?: 0.0
    val lon = locationJson?.get("lng")?.jsonPrimitive?.doubleOrNull ?: 0.0
    val location = Coordinates(lat = lat, lon = lon)

    val attributes = json["attributes"]?.jsonArray ?: JsonArray(emptyList())

    // helper ki najde value by slug
    fun findAttributeValue(slug: String): JsonElement? {
        return attributes.firstOrNull {
            it.jsonObject["attribute"]?.jsonObject?.get("slug")?.jsonPrimitive?.content == slug
        }?.jsonObject?.get("value")
    }

    val elevation = findAttributeValue("nadmorska-visina")?.jsonPrimitive?.doubleOrNull
        ?: findAttributeValue("nadmorska-visina")?.jsonPrimitive?.intOrNull?.toDouble()
        ?: 0.0

    val accessibilityOptions = when (val accValue = findAttributeValue("dostopnost")) {
        is JsonArray -> accValue.joinToString(", ") { it.jsonPrimitive.content }
        is JsonPrimitive -> accValue.content
        else -> null
    }
    val address = Address(street = "", city = "", postalCode = "", country = "Slovenia") //se nimamo

    val classification = json["type"]?.jsonObject?.get("default_category")?.jsonObject?.get("name")
        ?.jsonPrimitive?.content ?: ""

    val locationType = json["type"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""


    val parkingInfo = ParkingInfo(
        hasParking = false,
        distanceToParkingMeters = null,
        notes = null
    ) // TODO: No parking info in API response
    val requiresReservation = false // TODO: no info
    val openingHours = OpeningHours(
        monday = "",
        tuesday = "",
        wednesday = "",
        thursday = "",
        friday = "",
        saturday = "",
        sunday = ""
    ) // TODO: no info
    val entryFee = 0.0 // TODO: no info
    val hikingInfo = HikingInfo(
        difficulty = "",
        estimatedDurationMinutes = 0,
        trailType = ""
    ) // TODO: no info


    val googleMapsLink = "https://maps.google.com/?q=$lat,$lon"
    val createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
    val verified = false // TODO

    return Attraction(
        id = id,
        name = name,
        regionId = regionId,
        location = location,
        address = address,
        description = "",
        classification = classification,
        locationType = locationType,
        elevation = elevation,
        accessibilityOptions = accessibilityOptions,
        ratingFamilyFriendly = 0.0, //to dodajamo sami
        ratingElderlyFriendly = 0.0,
        ratingAccessible = 0.0,
        rating = 0.0,
        parkingInfo = parkingInfo,
        requiresReservation = requiresReservation,
        openingHours = openingHours,
        entryFee = entryFee,
        hikingInfo = hikingInfo,
        googleMapsLink = googleMapsLink,
        createdAt = createdAt,
        verified = verified
    )
}

fun parseAttractionImages(json: JsonObject): List<AttractionImage> {
    val attractionId = json["id"]?.jsonPrimitive?.content ?: return emptyList()

    val thumbsArray = json["thumbs"]?.jsonArray ?: return emptyList()

    return thumbsArray.mapNotNull { thumbElement ->
        val thumb = thumbElement.jsonObject

        val url = thumb["large_url"]?.jsonPrimitive?.content ?: return@mapNotNull null

        AttractionImage(
            id = null,
            attractionId = attractionId,
            url = url,
            source = "kamzavikend",
            uploadedBy = "admin",
            createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )
    }
}

