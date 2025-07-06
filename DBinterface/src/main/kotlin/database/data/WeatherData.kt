package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Request

@Serializable
data class WeatherData(
    @SerialName("_id") val id: String? = null,
    val location: Coordinates,
    var attractionId: String? = null,
    val forecast:  List<Weather>,
    val lastUpdated: String? = null
) : DatabaseClass

@Serializable
data class Weather(
    val date: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val condition: String,
    val precipitationProbabilityMax: Int
) : DatabaseClass

fun postWeatherData(weatherData: WeatherData): Boolean {
    val existing = getWeatherDataByAttractionId(weatherData.attractionId ?: return false)
    if (existing != null) {
        deleteWeatherData(existing.id ?: return false)
    }
    return postToDatabase(weatherData, "weather-data", WeatherData.serializer())
}

fun getWeatherDataByAttractionId(attractionId: String): WeatherData? {
    val url = "http://localhost:3001/weather-data/findByAttractionId/$attractionId"
    val request = Request.Builder().url(url).build()

    val response = client.newCall(request).execute()
    if (!response.isSuccessful) return null

    val body = response.body?.string() ?: return null
    return json.decodeFromString(WeatherData.serializer(), body)
}

fun deleteWeatherData(id: String): Boolean {
    val url = "http://localhost:3001/weather-data/$id"
    val request = Request.Builder().url(url).delete().build()

    val response = client.newCall(request).execute()
    return response.isSuccessful
}

