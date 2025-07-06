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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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

    val logScrollState = rememberScrollState()
    LaunchedEffect(logOutput.size) {
        logScrollState.animateScrollTo(logScrollState.maxValue)
    }

    fun log(message: String, level: String = "INFO") {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logOutput.add("[$timestamp] $level: $message")
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
                    log("[$index] Napaka pri vremenu za ${a.name}: ${e.message}", "ERROR")
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
        weatherMap = weatherMap.toMutableMap().apply { remove(attractionId) }
        log("Odstranjeni podatki za $attractionId iz lokalnega pomnilnika.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Beige)
            .padding(16.dp)
    ) {
        Text("Uvoz vremenskih podatkov", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.Navy)

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = ::fetchAllWeather,
                enabled = !isFetching,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
            ) {
                Text(if (isFetching) "Pridobivam..." else "Pridobi vremenske podatke", color = AppColors.Navy)
            }

            Button(
                onClick = { if (weatherMap.isNotEmpty()) showConfirmDialog = true },
                enabled = weatherMap.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
            ) {
                Text("Shrani vse v bazo", color = Color.White)
            }

            Button(
                onClick = {
                    weatherMap = emptyMap()
                    log("Počistil vremenske podatke.")
                },
                enabled = weatherMap.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red)
            ) {
                Text("Prekliči", color = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Dnevnik dogodkov", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.White)
                .border(1.dp, AppColors.Navy)
                .padding(12.dp)
                .verticalScroll(logScrollState)
        ) {
            Column {
                logOutput.forEach { entry ->
                    val parts = entry.split(" ", limit = 3)
                    val time = parts.getOrNull(0)?.removeSurrounding("[", "]") ?: ""
                    val level = parts.getOrNull(1)?.removeSuffix(":") ?: "INFO"
                    val message = parts.getOrNull(2) ?: ""

                    val levelColor = when (level) {
                        "ERROR" -> AppColors.Red
                        "INFO" -> AppColors.Navy
                        else -> Color.Gray
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = time,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(60.dp)
                        )
                        Text(
                            text = level,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(50.dp)
                        )
                        Text(
                            text = message,
                            fontSize = 13.sp,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(weatherMap.entries.toList()) { (attractionId, weatherData) ->
                val attractionName = attractions.find { it.id == attractionId }?.name ?: "Neznana znamenitost"
                val isExpanded = expandedItems[attractionId] == true

                Card(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .border(width = 1.dp, color = AppColors.Navy)
                        .clickable { expandedItems[attractionId] = !isExpanded },
                    elevation = 4.dp,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Znamenitost: $attractionName",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = AppColors.Navy
                        )
                        Text("Zadnjič posodobljeno: ${weatherData.lastUpdated}", fontSize = 14.sp)

                        if (isExpanded) {
                            Spacer(Modifier.height(8.dp))
                            Divider()
                            Spacer(Modifier.height(8.dp))

                            weatherData.forecast.forEach { day ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(
                                        text = formatDate(day.date),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = AppColors.Navy
                                    )
                                    Row {
                                        Text(
                                            "•",
                                            color = AppColors.Lavender,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        Text("Vreme: ${day.condition}", fontSize = 14.sp)
                                    }
                                    Row {
                                        Text(
                                            "•",
                                            color = AppColors.Lavender,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        Text("Temperatura: ${day.minTemperature}°C - ${day.maxTemperature}°C", fontSize = 14.sp)

                                    }
                                    Row {
                                        Text(
                                            "•",
                                            color = AppColors.Lavender,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        Text("Možnost padavin: ${day.precipitationProbabilityMax}%", fontSize = 14.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = AppColors.Lavender)
                            }

                            Button(
                                onClick = { deleteLocal(attractionId) },
                                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red),
                                modifier = Modifier.align(Alignment.End)
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

        if (showConfirmDialog) {
            Dialog(onDismissRequest = { showConfirmDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    elevation = 8.dp,
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
}



