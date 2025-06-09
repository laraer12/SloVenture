package api

import database.data.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.time.Instant
import java.time.format.DateTimeFormatter

private val client = HttpClient()


suspend fun retrieveAllAttractions(
    maxCount: Int,
    onProgress: (suspend (Attraction) -> Unit)? = null
): List<Attraction> {
    val endpoint = "https://api.kamzavikend.si/public/search"
    val pageSize = 30
    val totalPages = (maxCount + pageSize - 1) / pageSize

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

            for (attractionJsonElement in dataArray) {
                if (globalIndex > maxCount) break
                if (attractionJsonElement !is JsonObject) continue

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
                                    uploadedBy = "Admin",
                                    createdAt = baseAttraction.createdAt
                                )
                            }
                        )
                    } catch (e: Exception) {
                        baseAttraction.copy(
                            address = reverseGeocode(baseAttraction.location),
                            description = "",
                            images = kamzavikendImages
                        )
                    }

                    allAttractions.add(enrichedAttraction)
                    onProgress?.invoke(enrichedAttraction)
                    globalIndex++
                } catch (e: Exception) {
                    println("Warning: Skipping invalid attraction due to exception: ${e.localizedMessage}")
                }
            }

            if (globalIndex > maxCount) break
        }
    } catch (e: Exception) {
        println("Fatal error during bulk fetch: ${e.localizedMessage}")
    }

    return allAttractions
}


fun parseAttraction(json: JsonObject): Attraction {
    val id = json["id"]?.jsonPrimitive?.content ?: ""
    val name = json["name"]?.jsonPrimitive?.content ?: "Unknown"

    val areas = json["areas"]?.jsonArray ?: JsonArray(emptyList())
    val regionName = areas.firstOrNull {
        it.jsonObject["type"]?.jsonObject?.get("key")?.jsonPrimitive?.content == "region"
    }?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""

    val regionId = resolveOrCreateRegion(regionName)

    val region = Region(
        id = regionId,
        name = regionName,
        location = emptyList()
    )

    val locationJson = json["location"]?.jsonObject
    val lat = locationJson?.get("lat")?.jsonPrimitive?.doubleOrNull ?: 0.0
    val lon = locationJson?.get("lng")?.jsonPrimitive?.doubleOrNull ?: 0.0
    val location = Coordinates(lat = lat, lon = lon)

    val attributes = json["attributes"]?.jsonArray ?: JsonArray(emptyList())

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

    val address = Address(
        street = "",
        city = "",
        postalCode = "",
        country = "Slovenia"
    )

    val classification = json["type"]?.jsonObject?.get("default_category")?.jsonObject?.get("name")
        ?.jsonPrimitive?.content ?: ""

    val locationType = json["type"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: ""

    val googleMapsLink = "https://maps.google.com/?q=$lat,$lon"
    val createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

    return Attraction(
        id = id,
        name = name,
        regionId = region.id, // TODO
        location = location,
        address = address,
        description = "",
        classification = classification,
        locationType = locationType,
        elevation = elevation,
        accessibilityOptions = accessibilityOptions,
        ratingFamilyFriendly = 0.0,
        ratingElderlyFriendly = 0.0,
        ratingAccessible = 0.0,
        rating = 0.0,
        googleMapsLink = googleMapsLink,
        createdAt = createdAt
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

