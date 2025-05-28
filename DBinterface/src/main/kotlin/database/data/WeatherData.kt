package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val id: String? = null,
    val location: Coordinates,
    var attractionId: String? = null,
    val forecast:  List<Weather>,
    val lastUpdated: String? = null
) : DatabaseClass


fun postWeatherData(weatherData: WeatherData): Boolean =
    postToDatabase(weatherData, "weather-data", WeatherData.serializer())


@Serializable
data class Weather(
    val date: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val condition: String,
    val precipitationProbabilityMax: Int
) : DatabaseClass
