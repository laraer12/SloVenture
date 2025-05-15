package api

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
data class LocationResult(
    val display_name: String,
    val lat: String,
    val lon: String
)

suspend fun searchLocation(attractionName: String): List<LocationResult> {
    val apiKey = System.getenv("LOCATIONIQ_API_KEY") ?: error("LOCATIONIQ_API_KEY missing")
    val url =
        "https://us1.locationiq.com/v1/search.php?key=$apiKey&q=${attractionName}&countrycodes=si&format=json"

    return HttpClientProvider.client.get(url).body()
}
