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

suspend fun retrieveAttractions(
    category: String?,
    type: String?,
    num: Int?,
    page: Int?
): List<Attraction> {
    if (num == null || page == null) {
        error("Missing parameters: please provide num and page")
    }
    if (num < 1 || page < 1) {
        error("Invalid parameters: num and page should be greater than 0")
    }
    if (num * page > 2696 + num) {
        error("Invalid parameters: num * page should be less than 2696")
    }
    if (category != null && category !in validCategories) {
        error("Invalid category: please provide a valid category")
    }
    if (type != null && type !in validTypes) {
        error("Invalid type: please provide a valid type")
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
        val json = Json.parseToJsonElement(jsonText).jsonObject
        val dataArray = json["data"]?.jsonArray ?: return emptyList()

        dataArray.map { attractionJsonElement ->
            val obj = attractionJsonElement.jsonObject
            val baseAttraction = parseAttraction(obj)
            val kamzavikendImages = parseAttractionImages(obj)

            val slug = obj["slug"]?.jsonPrimitive?.content ?: ""
            val scraped = webScraper.fetchAttractionDetails(slug)

            val address = reverseGeocode(baseAttraction.location)
            val enrichedAttraction = baseAttraction.copy(
                address = address,
                description = scraped.descriptionParagraphs.joinToString("\n\n"),
                images = kamzavikendImages + scraped.imageLinks.map { url ->
                    AttractionImage(
                        id = null,
                        attractionId = baseAttraction.id,
                        url = url,
                        source = "webScraper",
                        uploadedBy = "admin",
                        createdAt = baseAttraction.createdAt
                    )
                }
            )
            enrichedAttraction
        }
    } catch (e: Exception) {
        println("Error fetching or enriching attractions: ${e.localizedMessage}")
        emptyList()
    }
}

suspend fun retrieveAllAttractions(): List<Attraction> {
    val endpoint = "https://api.kamzavikend.si/public/search"
    val totalAttractions = 120 //TODO 2696
    val pageSize = 30
    val totalPages = (totalAttractions + pageSize - 1) / pageSize

    val allAttractions = mutableListOf<Attraction>()
    var globalIndex = 1

    try {
        for (page in 1..totalPages) {
            val response: HttpResponse = client.get(endpoint) {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
                parameter("page[number]", page)
            }

            val jsonText = response.bodyAsText()
            val json = Json.parseToJsonElement(jsonText).jsonObject
            val dataArray = json["data"]?.jsonArray ?: continue

            val attractions = dataArray.mapNotNull { attractionJsonElement ->
                if (attractionJsonElement !is JsonObject) {
                    println("Warning: Skipping non-object element in data array: $attractionJsonElement")
                    return@mapNotNull null
                }

                println("Processing attraction $globalIndex of $totalAttractions")
                globalIndex++

                try {
                    val baseAttraction = parseAttraction(attractionJsonElement)
                    val kamzavikendImages = parseAttractionImages(attractionJsonElement)

                    val enrichedAttraction = try {
                        val slug = attractionJsonElement["slug"]?.jsonPrimitive?.content ?: ""
                        val scraped = webScraper.fetchAttractionDetails(slug)

                        baseAttraction.copy(
                            address = reverseGeocode(baseAttraction.location),
                            description = scraped.descriptionParagraphs.joinToString("\n\n"),
                            images = kamzavikendImages + scraped.imageLinks.map { url ->
                                AttractionImage(
                                    id = null,
                                    attractionId = baseAttraction.id,
                                    url = url,
                                    source = "webScraper",
                                    uploadedBy = "admin",
                                    createdAt = baseAttraction.createdAt
                                )
                            }
                        )
                    } catch (e: Exception) {
                        println("Warning: Failed to enrich attraction ${baseAttraction.id}: ${e.localizedMessage}")
                        baseAttraction.copy(
                            address = reverseGeocode(baseAttraction.location),
                            description = "",
                            images = kamzavikendImages
                        )
                    }

                    enrichedAttraction
                } catch (e: Exception) {
                    println("Warning: Skipping invalid attraction due to exception: ${e.localizedMessage}")
                    null
                }
            }

            allAttractions.addAll(attractions)
        }
    } catch (e: Exception) {
        println("Fatal error during bulk fetch: ${e.localizedMessage}")
    }

    return allAttractions
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
        requiresReservation = requiresReservation,
        openingHours = openingHours,
        entryFee = entryFee,
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
            uploadedBy = "admin", //TODO spremeni
            createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        )
    }
}

