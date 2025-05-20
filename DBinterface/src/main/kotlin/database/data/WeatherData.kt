package database.data

import database.DatabaseClass
import database.postToDatabase
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val id: String? = null,
    val location: Coordinates,
    val attractionId: String? = null,
    val currentWeather: Weather,
    val forecast: WeatherForecast,
    val lastUpdated: String? = null
) : DatabaseClass


suspend fun postWeatherData(weatherData: WeatherData): Boolean =
    postToDatabase(weatherData, "weather-data", WeatherData.serializer())


@Serializable
data class Weather(
    val date: String,
    val temperature: Double?,
    val condition: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val precipitationProbability: Int
) : DatabaseClass

@Serializable
data class WeatherForecast(
    val daily: List<Weather>
) : DatabaseClass
