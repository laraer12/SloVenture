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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
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
    var showAddDialog by remember { mutableStateOf(false) }

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
            .background(AppColors.Beige)
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Znamenitosti",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Navy,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { fetchAttractions() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Osveži", tint = AppColors.Navy)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
                ) {
                    Text("Dodaj znamenitost", color = AppColors.Navy)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filteredAttractions = attractions.filter { attraction ->
                    attraction.name.contains(it, ignoreCase = true)
                }
            },
            label = { Text("Išči znamenitost...") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredAttractions) { attraction ->
                    Card(
                        modifier = Modifier
                            .padding(6.dp)
                            .fillMaxWidth()
                            .border(width = 1.dp, color = AppColors.Navy)
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
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                attraction.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Navy
                            )
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

        if (showAddDialog) {
            AddAttractionDialog(
                onDismiss = { showAddDialog = false },
                onSave = { newAttraction ->
                    showAddDialog = false
                    fetchAttractions()
                }
            )
        }

        errorMessage?.let {
            Text("Error: $it", color = AppColors.Red)
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender),
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            Text("Nazaj")
        }

        Card(
            modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = AppColors.Navy),
            elevation = 4.dp,
            backgroundColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(data.attraction.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.Navy)
                Spacer(modifier = Modifier.height(12.dp))

                Section("Opis", data.attraction.description ?: "Ni opisa")
                data.attraction.address?.let {
                    Section("Naslov", "${it.street}, ${it.city}, ${it.country}")
                }
                Section("Klasifikacija", data.attraction.classification)
                Section("Tip lokacije", data.attraction.locationType)
                Section("Nadmorska višina", "${data.attraction.elevation} m")
                Section("Dostopnost", data.attraction.accessibilityOptions ?: "Ni podatkov")

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = AppColors.Lavender)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Ocene", fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = AppColors.Navy)
                Spacer(modifier = Modifier.height(8.dp))
                RatingItem("Splošna ocena", data.attraction.rating)
                RatingItem("Primerno za družine", data.attraction.ratingFamilyFriendly)
                RatingItem("Primerno za starejše", data.attraction.ratingElderlyFriendly)
                RatingItem("Dostopno", data.attraction.ratingAccessible)

                data.weatherData?.let { weather ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = AppColors.Lavender)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Vremenska napoved", fontWeight = FontWeight.Medium, fontSize = 18.sp, color = AppColors.Navy)
                    Spacer(modifier = Modifier.height(8.dp))

                    weather.forecast.forEach { day ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                formatDate(day.date),
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
                }

                if (data.nearbyAttractions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Bližnje znamenitosti", fontWeight = FontWeight.Medium, fontSize = 18.sp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "•",
                                color = AppColors.Lavender,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text(name, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { showEditDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
            ) {
                Text("Uredi")
            }
            Button(
                onClick = { showDeleteConfirmation = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red)
            ) {
                Text("Izbriši", color = Color.Black)
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Napaka: $it", color = AppColors.Red)
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
                        val refreshed = refresh()
                        onDataRefreshed(refreshed)
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
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = deleteAttraction(data.attraction.id)
                            if (success) {
                                showDeleteConfirmation = false
                                onBack()
                            } else {
                                errorMessage = "Neuspešno brisanje"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red)
                ) {
                    Text("Izbriši")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
                ) {
                    Text("Prekliči")
                }
            }
        )
    }
}


@Composable
fun Section(label: String, content: String) {
    Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = AppColors.Navy)
    Text(content, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun RatingItem(label: String, value: Double) {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = AppColors.Lavender, fontSize = 14.sp)) {
                append("• ")
            }
            withStyle(style = SpanStyle(fontSize = 14.sp, color = LocalContentColor.current)) {
                append("$label: ${"%.1f".format(value)}/5")
            }
        }
    )
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
                .widthIn(min = 400.dp, max = 600.dp)
                .heightIn(min = 500.dp, max = 700.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    "Uredi znamenitost",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ime") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        ),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = classification,
                        onValueChange = { classification = it },
                        label = { Text("Klasifikacija") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )
                    OutlinedTextField(
                        value = locationType,
                        onValueChange = { locationType = it },
                        label = { Text("Tip lokacije") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )
                    OutlinedTextField(
                        value = elevation,
                        onValueChange = { elevation = it },
                        label = { Text("Nadmorska višina (m)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = accessibilityOptions,
                        onValueChange = { accessibilityOptions = it },
                        label = { Text("Dostopnost") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RegionDropdown(
                        regions = regions,
                        selectedRegionId = updatedAttraction.regionId,
                        onRegionSelected = { selectedRegion ->
                            updatedAttraction = updatedAttraction.copy(regionId = selectedRegion.id)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AddressFields(
                        address = updatedAttraction.address,
                        onAddressChange = { updatedAttraction = updatedAttraction.copy(address = it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                        enabled = !isFetchingWeather,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isFetchingWeather) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Pridobivanje podatkov o vremenu...",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    weatherData?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Vremenska napoved:", fontWeight = FontWeight.Bold)
                        it.forecast.forEach { day ->
                            Text(
                                "${formatDate(day.date)}: ${day.condition}, ${day.minTemperature}°C - ${day.maxTemperature}°C, Padavine: ${day.precipitationProbabilityMax}%",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
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
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Green)
                    ) {
                        Text("Shrani")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Prekliči")
                    }
                }
            }
        }
    }
}


@Composable
fun AddAttractionDialog(
    onDismiss: () -> Unit,
    onSave: (Attraction) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var classification by remember { mutableStateOf("") }
    var locationType by remember { mutableStateOf("") }
    var elevation by remember { mutableStateOf("") }
    var accessibilityOptions by remember { mutableStateOf("") }
    var selectedRegionId by remember { mutableStateOf("") }
    var address by remember { mutableStateOf(Address("", "", "", "")) }
    var coordinates by remember { mutableStateOf(Coordinates(0.0, 0.0)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var regions by remember { mutableStateOf<List<Region>>(emptyList()) }
    LaunchedEffect(Unit) {
        try {
            regions = getAllRegions()
        } catch (ex: Exception) {
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            elevation = 8.dp,
            modifier = Modifier
                .width(600.dp)
                .height(750.dp)
                .border(width = 1.dp, color = AppColors.Navy)
                .background(color = AppColors.Beige)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Dodaj znamenitost", style = MaterialTheme.typography.h6, color = AppColors.Navy)

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ime") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )
                    OutlinedTextField(
                        value = classification,
                        onValueChange = { classification = it },
                        label = { Text("Klasifikacija") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )
                    OutlinedTextField(
                        value = locationType,
                        onValueChange = { locationType = it },
                        label = { Text("Tip lokacije") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )
                    OutlinedTextField(
                        value = elevation,
                        onValueChange = { elevation = it },
                        label = { Text("Nadmorska višina (m)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = accessibilityOptions,
                        onValueChange = { accessibilityOptions = it },
                        label = { Text("Dostopnost") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    RegionDropdown(
                        regions = regions,
                        selectedRegionId = selectedRegionId,
                        onRegionSelected = { selectedRegionId = it.id.toString() }
                    )

                    AddressFields(
                        address = address,
                        onAddressChange = { address = it }
                    )

                    LocationFields(
                        coordinates = coordinates,
                        onCoordinatesChange = { coordinates = it }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            try {
                                val newAttraction = Attraction(
                                    id = "",
                                    name = name,
                                    description = description.ifBlank { null },
                                    classification = classification,
                                    locationType = locationType,
                                    elevation = elevation.toDoubleOrNull() ?: 0.0,
                                    accessibilityOptions = accessibilityOptions.ifBlank { null },
                                    regionId = selectedRegionId,
                                    address = address,
                                    location = coordinates,
                                    rating = 0.0,
                                    ratingAccessible = 0.0,
                                    ratingElderlyFriendly = 0.0,
                                    ratingFamilyFriendly = 0.0,
                                    googleMapsLink = "https://maps.google.com/?q=${coordinates.lat},${coordinates.lon}"
                                )
                                val generatedId = postAttractionFromApi(newAttraction)
                                if (generatedId != null) {
                                    val savedAttraction = newAttraction.copy(id = generatedId)
                                    onSave(savedAttraction)
                                } else {
                                    errorMessage = "Znamenitost s tem imenom že obstaja ali je prišlo do napake."
                                }
                            } catch (e: Exception) {
                                errorMessage = "Napaka pri ustvarjanju: ${e.message}"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Green)
                    ) {
                        Text("Ustvari")
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red)
                    ) {
                        Text("Prekliči")
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
            Text(selectedRegionName, color = AppColors.Navy)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            regions.forEach { region ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onRegionSelected(region)
                }) {
                    Text(region.name, color = AppColors.Navy)
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
        OutlinedTextField(
            value = street, onValueChange = {
                street = it
                onAddressChange(Address(street, city, postalCode, country))
            }, label = { Text("Ulica") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )

        OutlinedTextField(
            value = city, onValueChange = {
                city = it
                onAddressChange(Address(street, city, postalCode, country))
            }, label = { Text("Kraj") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )

        OutlinedTextField(
            value = postalCode, onValueChange = {
                postalCode = it
                onAddressChange(Address(street, city, postalCode, country))
            }, label = { Text("Poštna številka") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )

        OutlinedTextField(
            value = country, onValueChange = {
                country = it
                onAddressChange(Address(street, city, postalCode, country))
            }, label = { Text("Država") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )
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
        OutlinedTextField(
            value = lat, onValueChange = {
                lat = it
                onCoordinatesChange(Coordinates(lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0))
            }, label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )

        OutlinedTextField(
            value = lon, onValueChange = {
                lon = it
                onCoordinatesChange(Coordinates(lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0))
            }, label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )
    }
}

@Composable
fun FetchWeatherButton(onFetchWeather: () -> Unit, enabled: Boolean = true, modifier: Modifier) {
    Button(
        modifier = modifier,
        onClick = onFetchWeather, enabled = enabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
    ) {
        Text("Pridobi vremenske podatke")
    }
}





