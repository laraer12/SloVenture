package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import database.data.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import api.fetchWeatherData
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeatherImportScreen() {
    val coroutineScope = rememberCoroutineScope()
    var isFetching by remember { mutableStateOf(false) }
    val logOutput = remember { mutableStateListOf<String>() }
    var weatherMap by remember { mutableStateOf<Map<String, WeatherData>>(emptyMap()) }
    val attractions by remember { mutableStateOf(getAllAttractions()) }
    val expandedItems = remember { mutableStateMapOf<String, Boolean>() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    fun log(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logOutput.add("[$timestamp] $message")
    }

    fun fetchAllWeather() {
        coroutineScope.launch {
            isFetching = true
            logOutput.clear()
            log("Najdenih ${attractions.size} znamenitosti v bazi.")

            val resultMap = mutableMapOf<String, WeatherData>()

            for ((index, a) in attractions.withIndex()) {
                try {
                    log("[$index] Pridobivam vreme za: ${a.name}")
                    if (a.location == null) {
                        log("Lokacija ni znana - ne dodajam vremena.")
                    } else {
                        val data = fetchWeatherData(a.location.lat, a.location.lon)
                        val addedAttractionId = data.copy(attractionId = a.id)
                        resultMap[a.id] = addedAttractionId
                        log("[$index] Vreme uspešno pridobljeno: ${a.name}")
                        delay(10)
                    }
                } catch (e: Exception) {
                    log("[$index] Napaka pri vremenu za ${a.name}: ${e.message}")
                }
            }

            weatherMap = resultMap
            log("Vse vremenske podatke uspešno pridobljeni.")
            isFetching = false
        }
    }

    fun saveAllToDatabase() {
        coroutineScope.launch {
            var saved = 0
            weatherMap.values.forEach {
                val result = postWeatherData(it)
                if (result) saved++
            }
            log("Shranjeni $saved vremenski podatki v bazo.")
        }
    }

    fun deleteLocal(attractionId: String) {
        weatherMap = weatherMap.toMutableMap().apply {
            remove(attractionId)
        }
        log("Odstranjeni podatki za $attractionId iz lokalnega pomnilnika.")
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Vremenski podatki", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = ::fetchAllWeather, enabled = !isFetching) {
                Text(if (isFetching) "Pridobivam..." else "Pridobi vremenske podatke")
            }

            Button(
                onClick = { if (weatherMap.isNotEmpty()) showConfirmDialog = true },
                enabled = weatherMap.isNotEmpty()
            ) {
                Text("Shrani vse v bazo")
            }

            Button(onClick = {
                weatherMap = emptyMap()
                log("Počistil vremenske podatke.")
            }, enabled = weatherMap.isNotEmpty()) {
                Text("Prekliči")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Log:")
        LazyColumn(modifier = Modifier.height(150.dp)) {
            items(logOutput) { line ->
                val color = if (line.contains("Napaka")) Color.Red else Color.Blue
                Text(line, color = color)
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn {
            items(weatherMap.entries.toList()) { (attractionId, weatherData) ->
                val attractionName = attractions.find { it.id == attractionId }?.name ?: "Neznana znamenitost"
                val isExpanded = expandedItems[attractionId] == true

                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            expandedItems[attractionId] = !isExpanded
                        },
                    shape = RoundedCornerShape(8.dp),
                    elevation = 4.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Znamenitost: $attractionName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Zadnjič posodobljeno: ${weatherData.lastUpdated}"
                        )

                        if (isExpanded) {
                            Spacer(Modifier.height(8.dp))
                            Divider()
                            Spacer(Modifier.height(8.dp))

                            weatherData.forecast.forEach { day ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = formatDate(day.date),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Text("• Vreme: ${day.condition}", fontSize = 14.sp)
                                    Text(
                                        "• Temperatura: ${day.minTemperature}°C - ${day.maxTemperature}°C",
                                        fontSize = 14.sp
                                    )
                                    Text("• Možnost padavin: ${day.precipitationProbabilityMax}%", fontSize = 14.sp)
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }

                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { deleteLocal(attractionId) },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
                            ) {
                                Text("Izbriši", color = Color.White)
                            }
                        } else {
                            Text("Klikni za več podrobnosti...", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDialog) {
        Dialog(onDismissRequest = { showConfirmDialog = false }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                elevation = 8.dp,
                color = Color.White
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Shrani vse vremenske podatke v bazo?", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            saveAllToDatabase()
                            showConfirmDialog = false
                        }) {
                            Text("Da, shrani")
                        }
                        Button(onClick = { showConfirmDialog = false }) {
                            Text("Prekliči")
                        }
                    }
                }
            }
        }
    }
}


