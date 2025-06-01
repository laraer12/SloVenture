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
                ",precipitation_probability_max&timezone=Europe%2FBerlin"

    val response: String= HttpClientProvider.client.get(url).body()
    val json = Json.parseToJsonElement(response).jsonObject
    val daily = json["daily"]!!.jsonObject
    val forecast = daily["temperature_2m_max"]!!.jsonArray.indices.map { i ->
        Weather(
            condition = weatherCodeToDescription(daily["weather_code"]!!.jsonArray[i].jsonPrimitive.int),
            maxTemperature = daily["temperature_2m_max"]!!.jsonArray[i].jsonPrimitive.double,
            minTemperature = daily["temperature_2m_min"]!!.jsonArray[i].jsonPrimitive.double,
            precipitationProbabilityMax = daily["precipitation_probability_max"]!!.jsonArray.getOrNull(i)?.jsonPrimitive?.int
                ?: 0,
            date = daily["time"]!!.jsonArray[i].jsonPrimitive.content
        )
    }

    return WeatherData(
        location = Coordinates(lat, lon),
        forecast = forecast,
        lastUpdated = Instant.now().toString()
    )
}

fun weatherCodeToDescription(code: Int): String = when (code) {
    0 -> "Jasno nebo"
    1 -> "Pretežno jasno"
    2 -> "Delno oblačno"
    3 -> "Oblačno"
    45, 48 -> "Megla"
    51, 53, 55 -> "Rosenje"
    56, 57 -> "Zmrznjeno rosenje"
    61, 63, 65 -> "Dež"
    66, 67 -> "Zmrznjen dež"
    71, 73, 75 -> "Sneženje"
    77 -> "Snežni kristali"
    80, 81, 82 -> "Plohe"
    85, 86 -> "Snežne plohe"
    95 -> "Nevihta"
    96, 99 -> "Nevihta s točo"
    else -> "Neznane vremenske razmere"
}

