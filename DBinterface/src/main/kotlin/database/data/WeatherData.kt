package database.data

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val id: String? = null,
    val location: Coordinates,
    val attractionId: String,
    val currentWeather: Weather,
    val forecast: WeatherForecast,
    val lastUpdated: String? = null
)

@Serializable
data class Weather(
    val temperature: Double,
    val condition: String,
    val windSpeed: Double,
    val humidity: Int
)

@Serializable
data class WeatherForecast(
    val daily: List<Weather>
)