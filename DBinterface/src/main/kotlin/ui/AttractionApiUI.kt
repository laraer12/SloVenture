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
import api.retrieveAllAttractions
import database.data.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun AttractionImportScreen() {
    val coroutineScope = rememberCoroutineScope()

    var attractions by remember { mutableStateOf<List<Attraction>>(emptyList()) }
    var filteredAttractions by remember { mutableStateOf<List<Attraction>>(emptyList()) }
    var selectedAttractionData by remember { mutableStateOf<FullAttractionData?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var numToFetch by remember { mutableStateOf("10") }
    var userUsername by remember { mutableStateOf("Uporaniško ime") }
    var userId by remember { mutableStateOf<String?>(null) }

    var showSaveDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val logOutput = remember { mutableStateListOf<String>() }

    fun log(message: String, level: String = "INFO") {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logOutput.add("[$timestamp] $level: $message")
    }

    fun logInfo(message: String) = log(message, "INFO")
    fun logError(message: String) = log(message, "ERROR")

    fun fetchAttractionsFromAPI() {
        coroutineScope.launch {
            isLoading = true
            logOutput.clear()

            val count = numToFetch.toIntOrNull() ?: 0
            if (count <= 0 || userUsername.isBlank()) {
                logError("Napaka: Neveljaven vnos. Število: $count, Uporabnik: '$userUsername'")
                isLoading = false
                return@launch
            }

            userId = getUserIdByUsername(userUsername)
            if (userId == null) {
                logError("Napaka: Uporabnik '$userUsername' ne obstaja.")
                isLoading = false
                return@launch
            }

            val results = mutableListOf<Attraction>()

            try {
                logInfo("Začenjam uvoz $count znamenitosti...")

                val fetched = retrieveAllAttractions(count) { attraction ->
                    results.add(attraction)
                    attractions = results.toList()
                    filteredAttractions = results.toList()
                    logInfo("Uvoženo: ${attraction.name}")
                    delay(5)
                }
                attractions = fetched
                filteredAttractions = fetched
                logInfo("Uvoženih ${fetched.size} znamenitosti.")
            } catch (e: Exception) {
                logError("Napaka pri uvozu: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }


    fun saveFetchedAttractions() {
        coroutineScope.launch {
            isSaving = true
            logInfo("Shranjevanje ${filteredAttractions.size} znamenitosti...")

            var successCount = 0
            var failCount = 0

            for ((index, attraction) in filteredAttractions.withIndex()) {
                delay(10)

                val savedId = postAttractionFromApi(attraction)
                if (savedId != null) {
                    logInfo("($index) Shrani: ${attraction.name} (ID: $savedId)")
                    successCount++

                    for (image in attraction.images) {
                        delay(5)
                        val saved = postAttractionImage(
                            image.copy(
                                attractionId = savedId,
                                uploadedBy = userId
                            )
                        )
                        if (saved) {
                            logInfo("Slika shranjena: ${image.url}")
                        } else {
                            logError("Napaka pri sliki: ${image.url}")
                        }
                    }
                } else {
                    logError("($index) Napaka pri shranjevanju: ${attraction.name}")
                    failCount++
                }
            }

            logInfo("Shranjevanje končano: $successCount uspešnih, $failCount neuspešnih.")
            isSaving = false
        }
    }


    if (selectedAttractionData != null) {
        AttractionAPIDetailScreen(
            data = selectedAttractionData!!,
            onBack = { selectedAttractionData = null },
            onDataRefreshed = { updated ->
                attractions = attractions.map {
                    if (it.id == updated.attraction.id) updated.attraction else it
                }
                filteredAttractions = filteredAttractions.map {
                    if (it.id == updated.attraction.id) updated.attraction else it
                }
                selectedAttractionData = null
            },
            onDelete = { deletedId ->
                attractions = attractions.filterNot { it.id == deletedId }
                filteredAttractions = filteredAttractions.filterNot { it.id == deletedId }
                selectedAttractionData = null
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Uvoz znamenitosti", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = numToFetch,
            onValueChange = { numToFetch = it },
            label = { Text("Število znamenitosti za uvoz") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = userUsername,
            onValueChange = { userUsername = it },
            label = { Text("Uporabniško ime") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = ::fetchAttractionsFromAPI, enabled = !isLoading && !isSaving) {
                Text(if (isLoading) "Uvažanje..." else "Uvozi znamenitosti")
            }

            Button(
                onClick = { showSaveDialog = true },
                enabled = filteredAttractions.isNotEmpty() && !isSaving,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
            ) {
                Text("Shrani vse v bazo")
            }

            Button(
                onClick = {
                    attractions = emptyList()
                    filteredAttractions = emptyList()
                    searchQuery = ""
                    log("Uvoz preklican.")
                },
                enabled = filteredAttractions.isNotEmpty() && !isSaving,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336))
            ) {
                Text("Prekliči")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Log:")
        LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
            items(logOutput) { line ->
                val color = when {
                    line.contains("ERROR") -> Color.Red
                    else -> Color.Blue
                }
                Text(line, color = color)
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filteredAttractions = attractions.filter { a ->
                    a.name.contains(it, ignoreCase = true)
                }
            },
            label = { Text("Išči znamenitost...") },
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredAttractions) { attraction ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clickable {
                            selectedAttractionData = FullAttractionData(
                                attraction = attraction,
                                nearbyAttractions = emptyList(),
                                weatherData = null
                            )
                        },
                    elevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(attraction.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        attraction.address?.let { Text(it.city, fontSize = 14.sp) }
                        Text("Lokacija: lat(${attraction.location.lat}), lon(${attraction.location.lon})")
                    }
                }
            }
        }

        errorMessage?.let {
            Text("Napaka: $it", color = MaterialTheme.colors.error)
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Potrditev shranjevanja") },
            text = { Text("Ali želiš shraniti ${filteredAttractions.size} znamenitosti v bazo?") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveDialog = false
                    coroutineScope.launch {
                        // da se zapre popup
                        delay(100)
                        saveFetchedAttractions()
                    }
                }) {
                    Text("Shrani")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Prekliči")
                }
            }
        )
    }
}

@Composable
fun AttractionAPIDetailScreen(
    data: FullAttractionData,
    onBack: () -> Unit,
    onDataRefreshed: (FullAttractionData) -> Unit,
    onDelete: (String) -> Unit
) {
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
        EditAttractionAPIDialog(
            attraction = data.attraction,
            regions = regions,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                val refreshed = data.copy(attraction = updated)
                onDataRefreshed(refreshed)
                showEditDialog = false
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
                        showDeleteConfirmation = false
                        onDelete(data.attraction.id)
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
fun EditAttractionAPIDialog(
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
}
