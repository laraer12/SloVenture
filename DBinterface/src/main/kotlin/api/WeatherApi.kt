package api

import database.data.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*
import java.time.Instant
import io.ktor.client.call.body

suspend fun fetchWeatherData(lat: Double, lon: Double): WeatherData {
    val url =
        "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                ",precipitation_probability_max&current=temperature_2m" +
                ",weather_code,precipitation&timezone=Europe%2FBerlin"

    val response: String= HttpClientProvider.client.get(url).body()
    val json = Json.parseToJsonElement(response).jsonObject

    val current = json["current"]!!.jsonObject
    val daily = json["daily"]!!.jsonObject

    val currentWeather = Weather(
        temperature = current["temperature_2m"]!!.jsonPrimitive.double,
        condition = weatherCodeToDescription(current["weather_code"]!!.jsonPrimitive.int),
        maxTemperature = daily["temperature_2m_max"]!!.jsonArray[0].jsonPrimitive.double,
        minTemperature = daily["temperature_2m_min"]!!.jsonArray[0].jsonPrimitive.double,
        precipitationProbability = daily["precipitation_probability_max"]!!.jsonArray[0].jsonPrimitive.int,
        date = current["time"]!!.jsonPrimitive.content,
    )

    val forecast = daily["temperature_2m_max"]!!.jsonArray.indices.map { i ->
        Weather(
            temperature = null, //ker za napoved gledas max in min temperaturo !!!!!!!!!!
            condition = weatherCodeToDescription(daily["weather_code"]!!.jsonArray[i].jsonPrimitive.int),
            maxTemperature = daily["temperature_2m_max"]!!.jsonArray[i].jsonPrimitive.double,
            minTemperature = daily["temperature_2m_min"]!!.jsonArray[i].jsonPrimitive.double,
            precipitationProbability = daily["precipitation_probability_max"]!!.jsonArray.getOrNull(i)?.jsonPrimitive?.int
                ?: 0,
            date = daily["time"]!!.jsonArray[i].jsonPrimitive.content
        )
    }

    return WeatherData(
        location = Coordinates(lat, lon),
        currentWeather = currentWeather,
        forecast = WeatherForecast(daily = forecast),
        lastUpdated = Instant.now().toString()
    )
}

fun weatherCodeToDescription(code: Int): String = when (code) {
    0 -> "Clear sky"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing Drizzle"
    61, 63, 65 -> "Rain"
    66, 67 -> "Freezing Rain"
    71, 73, 75 -> "Snow fall"
    77 -> "Snow grains"
    80, 81, 82 -> "Rain showers"
    85, 86 -> "Snow showers"
    95, 96, 99 -> "Thunderstorm"
    else -> "Unknown weather condition"
}

