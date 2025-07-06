package api

import database.data.Address
import database.data.Coordinates
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File
import io.ktor.client.call.body

@Serializable
private data class LocationIQResponse(
    val address: Map<String, String>
)

suspend fun reverseGeocode(coordinates: Coordinates): Address {
    val json = File("src/main/kotlin/api/api_keys.json").readText()
    val apiKey = Json.decodeFromString<Map<String, String>>(json)["location"]
        ?: error("API key 'location' not found in JSON")

    val url =
        "https://us1.locationiq.com/v1/reverse.php?key=$apiKey&lat=${coordinates.lat}&lon=${coordinates.lon}&format=json"


    val response: LocationIQResponse = HttpClientProvider.client.get(url).body()

    val street = listOfNotNull(
        response.address["house_number"],
        response.address["road"]
    ).joinToString(" ")

    val city = response.address["city"] ?: response.address["town"] ?: response.address["village"] ?: ""
    val postalCode = response.address["postcode"] ?: ""
    val country = response.address["country"] ?: ""

    return Address(
        street = street,
        city = city,
        postalCode = postalCode,
        country = country
    )
}

