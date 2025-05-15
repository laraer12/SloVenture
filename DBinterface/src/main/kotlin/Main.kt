import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import api.RetrieveResult
import api.fetchWeatherData
import api.retrieveAttractions
import api.searchLocation
import ui.MainScreen
import kotlinx.coroutines.runBlocking
import webScraper.fetchAttractionDetails

//@Preview

/*
fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MainScreen()
    }
}*/
fun main() = runBlocking {

    try {

        println("\nScraping details for Mangart...")
        val details = fetchAttractionDetails("mangart")
        println("Images: ${details.imageLinks.size}")
        println("Descriptions: ${details.descriptionParagraphs.size}")
    } catch (e: Exception) {
        e.printStackTrace()
    }

    try {
        val lat = 46.55472
        val lon = 15.64667

        println("\nFetching weather for lat=$lat, lon=$lon")
        val weatherData = fetchWeatherData(lat, lon)
        println("Current: ${weatherData.currentWeather}")
        println("Forecast entries: ${weatherData.forecast.daily.toString()}")
    } catch (e: Exception) {
        println("Error during test: ${e.message}")
        e.printStackTrace()
    }

    val category = "naravne-lepote"
    val type = "jezero"
    val num = 5
    val page = 1

    when (val result = retrieveAttractions(category, type, num, page)) {
        is RetrieveResult.Success -> {
            println("Retrieved ${result.data.size} attractions:")
            result.data.forEach { enriched ->
                val a = enriched.attraction
                println("- ${a.name} at ${a.location.lat}, ${a.location.lon}")
                if (enriched.images.isNotEmpty()) {
                    println("  Images:")
                    enriched.images.forEach { img ->
                        println("    • ${img.url} (source: ${img.source}, uploadedBy: ${img.uploadedBy})")
                    }
                } else {
                    println("  No images available.")
                }
            }
        }

        is RetrieveResult.Error -> {
            println("Error: ${result.message}")
        }
    }

}
