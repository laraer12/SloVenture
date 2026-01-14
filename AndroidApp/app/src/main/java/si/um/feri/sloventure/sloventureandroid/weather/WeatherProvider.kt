package si.um.feri.sloventure.sloventureandroid.weather

import okhttp3.Request
import okhttp3.OkHttpClient
import timber.log.Timber
import org.json.JSONObject
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class WeatherProvider() {
    // klient z 10 sekundnim call timeoutom
    private val client = OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS).build()

    // za asinhrone klice (pa tudi da napaka ne prekine ostalih korutin)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        onResult: (temperature: Double?, description: String?) -> Unit
    ) {
        // zaženem korutino na IO niti, da ne blokiram glavne niti
        scope.launch {
            try {
                val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true"

                // HTTP GET request
                val request = Request.Builder().url(url).build()

                // sinhroni klic API-ja
                val response = client.newCall(request).execute()

                // use, da se response pravilno zapre vire po uporabi
                response.use { res ->
                    if (!res.isSuccessful) {
                        withContext(Dispatchers.Main) { onResult(null, null) }
                        return@launch
                    }
                    val body = res.body?.string()

                    if (body == null) {
                        withContext(Dispatchers.Main) { onResult(null, null) }
                        return@launch
                    }
                    Timber.e("Weather API response: $body")

                    val json = JSONObject(body)
                    val current = json.getJSONObject("current_weather")

                    val temp = current.getDouble("temperature")
                    val weatherCode = current.getInt("weathercode")
                    val description = weatherCodeToDescription(weatherCode)

                    // preklopim na glavno nit, ker callback posodablja UI
                    withContext(Dispatchers.Main) {
                        onResult(temp, description)
                    }
                }
            }
            catch (ex: Exception) {
                ex.printStackTrace()

                withContext(Dispatchers.Main) {
                    onResult(null, null)
                }
            }
        }
    }

    fun cancel() {
        scope.cancel() // s tem prekinem vse korutine znotraj scope
    }

    // tabela vrednosti kod vremena iz https://open-meteo.com/en/docs
    private fun weatherCodeToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3	-> "Partly cloudy"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snow fall"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"

            else -> "Unknown"
        }
    }
}