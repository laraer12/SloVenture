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
import api.fetchWeatherData
import api.retrieveAllAttractions
import api.reverseGeocode
import database.data.*
import ui.MainScreen
import kotlinx.coroutines.runBlocking
import webScraper.fetchAttractionDetails
import kotlinx.coroutines.runBlocking


@Preview

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MainScreen()
    }
}
