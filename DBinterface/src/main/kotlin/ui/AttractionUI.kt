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
import kotlinx.serialization.json.*
import kotlinx.coroutines.launch
import okhttp3.Request
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.window.Dialog
import database.data.WeatherData
import api.fetchWeatherData


@Composable
fun AttractionListScreen() {
    val coroutineScope = rememberCoroutineScope()
    var attractions by remember { mutableStateOf<List<Attraction>>(emptyList()) }
    var filteredAttractions by remember { mutableStateOf<List<Attraction>>(emptyList()) }
    var selectedAttractionData by remember { mutableStateOf<FullAttractionData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    fun fetchAttractions() {
        coroutineScope.launch {
            isLoading = true
            try {
                val request = Request.Builder()
                    .url("http://localhost:3001/attractions/listKotlin")
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                val jsonArray = json.parseToJsonElement(body!!).jsonArray

                val parsed = jsonArray.map {
                    val attraction = it.jsonObject["attraction"]
                    json.decodeFromJsonElement<Attraction>(attraction!!)
                }

                attractions = parsed
                filteredAttractions = parsed
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchAttractions()
    }

    if (selectedAttractionData != null) {
        AttractionDetailScreen(
            data = selectedAttractionData!!,
            onBack = {
                selectedAttractionData = null
                fetchAttractions()
            },
            refresh = {
                fetchFullAttractionData(selectedAttractionData!!.attraction.id)
            },
            onDataRefreshed = { refreshed -> selectedAttractionData = refreshed }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Vse znamenitosti", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { fetchAttractions() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Osveži")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filteredAttractions = attractions.filter { attraction ->
                    attraction.name.contains(it, ignoreCase = true)
                }
            },
            label = { Text("Išči znamenitost...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredAttractions) { attraction ->
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
                                Text(it.city, fontSize = 14.sp)
                            }
                            Text("Lokacija: lat(${attraction.location.lat}), lon(${attraction.location.lon})")
                            Text("Ocena: ${"%.1f".format(attraction.rating)}")
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
fun AttractionDetailScreen(
    data: FullAttractionData,
    onBack: () -> Unit,
    refresh: suspend () -> FullAttractionData,
    onDataRefreshed: (FullAttractionData) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var regions by remember { mutableStateOf<List<Region>>(emptyList()) }
    var isLoadingRegions by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var refreshedData by remember { mutableStateOf(data) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Nazaj")
        }
        Text(data.attraction.name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Section("Opis", data.attraction.description ?: "Ni opisa")
        data.attraction.address?.let {
            Section("Naslov", "${it.street}, ${it.city}, ${it.country}")
        }

        Section("Klasifikacija", data.attraction.classification)
        Section("Tip lokacije", data.attraction.locationType)
        Section("Nadmorska višina", "${data.attraction.elevation} m")
        Section("Dostopnost", data.attraction.accessibilityOptions ?: "Ni podatkov")

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        Text("Ocene", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        RatingItem("• Splošna ocena", data.attraction.rating)
        RatingItem("• Primerno za družine", data.attraction.ratingFamilyFriendly)
        RatingItem("• Primerno za starejše", data.attraction.ratingElderlyFriendly)
        RatingItem("• Dostopno", data.attraction.ratingAccessible)

        data.weatherData?.let { weather ->
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Text("Vremenska napoved", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            weather.forecast.forEach { day ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = formatDate(day.date),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text("• Vreme: ${day.condition}", fontSize = 14.sp)
                    Text("• Temperatura: ${day.minTemperature}°C - ${day.maxTemperature}°C", fontSize = 14.sp)
                    Text("• Možnost padavin: ${day.precipitationProbabilityMax}%", fontSize = 14.sp)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        if (data.nearbyAttractions.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            Text("Bližnje znamenitosti", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))

            val nearbyNames = remember(data.nearbyAttractions) {
                mutableStateListOf<String>()
            }

            LaunchedEffect(data.nearbyAttractions) {
                nearbyNames.clear()
                data.nearbyAttractions.forEach { nearby ->
                    try {
                        val fullData = fetchFullAttractionData(nearby.nearbyAttractionId)
                        nearbyNames.add(fullData.attraction.name)
                    } catch (e: Exception) {
                        nearbyNames.add("Neznana znamenitost")
                    }
                }
            }

            nearbyNames.forEach { name ->
                Text("• $name", fontSize = 14.sp)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { showEditDialog = true }) {
                Text("Uredi")
            }
            Button(
                onClick = { showDeleteConfirmation = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
            ) {
                Text("Izbriši", color = Color.White)
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Napaka: $it", color = MaterialTheme.colors.error)
        }
    }

    LaunchedEffect(showEditDialog) {
        if (showEditDialog && regions.isEmpty() && !isLoadingRegions) {
            isLoadingRegions = true
            regions = getAllRegions()
            isLoadingRegions = false
        }
    }

    if (showEditDialog && regions.isNotEmpty()) {
        EditAttractionDialog(
            attraction = data.attraction,
            regions = regions,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                coroutineScope.launch {
                    val success = updateAttraction(updated)
                    if (success) {
                        val refreshedData = refresh()
                        onDataRefreshed(refreshedData)
                        showEditDialog = false
                    } else {
                        errorMessage = "Neuspešna posodobitev znamenitosti"
                    }
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Izbriši znamenitost") },
            text = { Text("Ali ste prepričani, da želite izbrisati to znamenitost?") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        val success = deleteAttraction(data.attraction.id)
                        if (success) {
                            showDeleteConfirmation = false
                            onBack()
                        } else {
                            errorMessage = "Neuspešno brisanje"
                        }
                    }
                }) {
                    Text("Izbriši")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmation = false }) {
                    Text("Prekliči")
                }
            }
        )
    }
}

@Composable
fun Section(label: String, content: String) {
    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    Text(content, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun RatingItem(label: String, value: Double) {
    Text("$label: ${"%.1f".format(value)}/5", fontSize = 14.sp)
}

fun formatDate(isoDate: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val localDate = LocalDate.parse(isoDate, formatter)
        localDate.format(DateTimeFormatter.ofPattern("EEEE, d. MMM"))
    } catch (e: Exception) {
        isoDate
    }
}

@Composable
fun EditAttractionDialog(
    attraction: Attraction,
    regions: List<Region>,
    onDismiss: () -> Unit,
    onSave: (Attraction) -> Unit
) {
    var updatedAttraction by remember { mutableStateOf(attraction) }

    var name by remember { mutableStateOf(attraction.name) }
    var description by remember { mutableStateOf(attraction.description ?: "") }
    var classification by remember { mutableStateOf(attraction.classification) }
    var locationType by remember { mutableStateOf(attraction.locationType) }
    var elevation by remember { mutableStateOf(attraction.elevation.toString()) }
    var accessibilityOptions by remember { mutableStateOf(attraction.accessibilityOptions ?: "") }

    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isFetchingWeather by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            elevation = 8.dp,
            modifier = Modifier
                .width(600.dp)
                .height(600.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Uredi znamenitost", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ime") })
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis") })
                    OutlinedTextField(
                        value = classification,
                        onValueChange = { classification = it },
                        label = { Text("Klasifikacija") })
                    OutlinedTextField(
                        value = locationType,
                        onValueChange = { locationType = it },
                        label = { Text("Tip lokacije") })
                    OutlinedTextField(
                        value = elevation,
                        onValueChange = { elevation = it },
                        label = { Text("Nadmorska višina (m)") })
                    OutlinedTextField(
                        value = accessibilityOptions,
                        onValueChange = { accessibilityOptions = it },
                        label = { Text("Dostopnost") })

                    Spacer(modifier = Modifier.height(16.dp))

                    RegionDropdown(
                        regions = regions,
                        selectedRegionId = updatedAttraction.regionId,
                        onRegionSelected = { selectedRegion ->
                            updatedAttraction = updatedAttraction.copy(regionId = selectedRegion.id)
                        }
                    )

                    AddressFields(
                        address = updatedAttraction.address,
                        onAddressChange = { updatedAttraction = updatedAttraction.copy(address = it) }
                    )

                    LocationFields(
                        coordinates = updatedAttraction.location,
                        onCoordinatesChange = { updatedAttraction = updatedAttraction.copy(location = it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FetchWeatherButton(
                        onFetchWeather = {
                            val coords = updatedAttraction.location
                            isFetchingWeather = true
                            coroutineScope.launch {
                                try {
                                    val data = fetchWeatherData(coords.lat, coords.lon)
                                    weatherData = data.copy(attractionId = updatedAttraction.id)
                                    postWeatherData(weatherData!!)
                                } catch (e: Exception) {
                                    println("Napaka pri pridobivanju vremena: ${e.message}")
                                } finally {
                                    isFetchingWeather = false
                                }
                            }

                        },
                        enabled = !isFetchingWeather
                    )

                    if (weatherData != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Vremenska napoved:", fontWeight = FontWeight.Bold)
                        weatherData!!.forecast.forEach { day ->
                            Text("${day.date}: ${day.condition}, ${day.minTemperature}°C - ${day.maxTemperature}°C, Padavine: ${day.precipitationProbabilityMax}%")
                        }
                    }

                    if (isFetchingWeather) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pridobivanje podatkov o vremenu...", style = MaterialTheme.typography.body2)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onDismiss) {
                        Text("Prekliči")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        try {
                            onSave(
                                updatedAttraction.copy(
                                    name = name,
                                    description = description.ifBlank { null },
                                    classification = classification,
                                    locationType = locationType,
                                    elevation = elevation.toDoubleOrNull() ?: 0.0,
                                    accessibilityOptions = accessibilityOptions.ifBlank { null }
                                )
                            )
                        } catch (_: Exception) {
                        }
                    }) {
                        Text("Shrani")
                    }
                }
            }
        }
    }
}


@Composable
fun RegionDropdown(
    regions: List<Region>,
    selectedRegionId: String?,
    onRegionSelected: (Region) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedRegionName = regions.find { it.id == selectedRegionId }?.name ?: "Izberi regijo"

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedRegionName)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            regions.forEach { region ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onRegionSelected(region)
                }) {
                    Text(region.name)
                }
            }
        }
    }
}


@Composable
fun AddressFields(
    address: Address?,
    onAddressChange: (Address) -> Unit
) {
    var street by remember { mutableStateOf(address?.street ?: "") }
    var city by remember { mutableStateOf(address?.city ?: "") }
    var postalCode by remember { mutableStateOf(address?.postalCode ?: "") }
    var country by remember { mutableStateOf(address?.country ?: "") }

    Column {
        OutlinedTextField(value = street, onValueChange = {
            street = it
            onAddressChange(Address(street, city, postalCode, country))
        }, label = { Text("Ulica") })

        OutlinedTextField(value = city, onValueChange = {
            city = it
            onAddressChange(Address(street, city, postalCode, country))
        }, label = { Text("Kraj") })

        OutlinedTextField(value = postalCode, onValueChange = {
            postalCode = it
            onAddressChange(Address(street, city, postalCode, country))
        }, label = { Text("Poštna številka") })

        OutlinedTextField(value = country, onValueChange = {
            country = it
            onAddressChange(Address(street, city, postalCode, country))
        }, label = { Text("Država") })
    }
}

@Composable
fun LocationFields(
    coordinates: Coordinates,
    onCoordinatesChange: (Coordinates) -> Unit
) {
    var lat by remember { mutableStateOf(coordinates.lat.toString()) }
    var lon by remember { mutableStateOf(coordinates.lon.toString()) }

    Column {
        OutlinedTextField(value = lat, onValueChange = {
            lat = it
            onCoordinatesChange(Coordinates(lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0))
        }, label = { Text("Latitude") })

        OutlinedTextField(value = lon, onValueChange = {
            lon = it
            onCoordinatesChange(Coordinates(lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0))
        }, label = { Text("Longitude") })
    }
}

@Composable
fun FetchWeatherButton(onFetchWeather: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onFetchWeather, enabled = enabled) {
        Text("Pridobi vremenske podatke")
    }
}



