package database.data

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val id: String? = null,
    val location: Coordinates,
    val attractionId: String? = null,
    val currentWeather: Weather,
    val forecast: WeatherForecast,
    val lastUpdated: String? = null
)

@Serializable
data class Weather(
    val date: String,
    val temperature: Double?,
    val condition: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val precipitationProbability: Int
)

@Serializable
data class WeatherForecast(
    val daily: List<Weather>
)