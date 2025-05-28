package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import database.data.Attraction
import database.data.fetchFullAttractionData
import database.data.FullAttractionData
import kotlinx.serialization.json.*
import kotlinx.coroutines.launch
import okhttp3.Request

@Composable
fun AttractionListScreen() {
    val coroutineScope = rememberCoroutineScope()

    var attractions by remember { mutableStateOf<List<Attraction>>(emptyList()) }
    var selectedAttractionData by remember { mutableStateOf<FullAttractionData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            try {
                val request = Request.Builder()
                    .url("http://localhost:3001/attractions")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                val jsonArray = json.parseToJsonElement(body!!).jsonArray

                val parsed = jsonArray.map {
                    val attraction = it.jsonObject["attraction"]
                    json.decodeFromJsonElement<Attraction>(attraction!!)
                }

                attractions = parsed
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    if (selectedAttractionData != null) {
        AttractionDetailScreen(
            data = selectedAttractionData!!,
            onBack = { selectedAttractionData = null }
        )
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("All Attractions", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(attractions) { attraction ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    isLoading = true
                                    try {
                                        selectedAttractionData = fetchFullAttractionData(attraction.id)
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                        elevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(attraction.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            attraction.address?.let {
                                Text("${it.city}, ${it.country}", fontSize = 14.sp)
                            }
                            Text("Rating: ${"%.1f".format(attraction.rating)}")
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Text("Error: $it", color = MaterialTheme.colors.error)
        }
    }
}

@Composable
fun AttractionDetailScreen(data: FullAttractionData, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Back")
        }

        Text(data.attraction.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(
            "Description: ${data.attraction.description ?: "No description"}",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        data.attraction.address?.let {
            Text("Address: ${it.street}, ${it.city}, ${it.country}")
        }

        Text("Classification: ${data.attraction.classification}")
        Text("Location Type: ${data.attraction.locationType}")
        Text("Elevation: ${data.attraction.elevation}m")
        Text("Accessibility: ${data.attraction.accessibilityOptions ?: "N/A"}")

        Text("Ratings:")
        Text(" - Family Friendly: ${data.attraction.ratingFamilyFriendly}")
        Text(" - Elderly Friendly: ${data.attraction.ratingElderlyFriendly}")
        Text(" - Accessible: ${data.attraction.ratingAccessible}")

        Spacer(modifier = Modifier.height(8.dp))

        data.weatherData?.let { weather ->
            Text("Weather: ${weather.forecast}")
        }

        if (data.nearbyAttractions.isNotEmpty()) {
            Text("Nearby Attractions:", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            data.nearbyAttractions.forEach {
                data.nearbyAttractions.forEach {
                    Text("- Attraction ID: ${it.nearbyAttractionId}") //TODO
                }
            }
        }
    }
}
