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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
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
    var admins by remember { mutableStateOf<List<AdminUser>>(emptyList()) }
    var selectedAdminId by remember { mutableStateOf("") }

    var showSaveDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val logScrollState = rememberScrollState()
    val logOutput = remember { mutableStateListOf<String>() }
    fun log(message: String, level: String = "INFO") {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logOutput.add("[$timestamp] $level: $message")
    }

    LaunchedEffect(logOutput.size) {
        logScrollState.animateScrollTo(logScrollState.maxValue)
    }

    LaunchedEffect(Unit) {
        try {
            admins = fetchAdminUsers()
        } catch (e: Exception) {
            errorMessage = "Napaka pri nalaganju adminov: ${e.message}"
        }
    }


    fun logInfo(message: String) = log(message, "INFO")
    fun logError(message: String) = log(message, "ERROR")

    fun fetchAttractionsFromAPI() {
        coroutineScope.launch {
            isLoading = true
            logOutput.clear()

            val count = numToFetch.toIntOrNull() ?: 0
            if (count <= 0 || selectedAdminId.isBlank()) {
                logError("Napaka: Neveljaven vnos. Število: $count, Uporabnik: '$selectedAdminId'")
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
                    logInfo("($index) Shranjeno: ${attraction.name} (ID: $savedId)")
                    successCount++

                    for (image in attraction.images) {
                        delay(5)
                        postAttractionImage(
                            image.copy(
                                attractionId = savedId,
                                uploadedBy = selectedAdminId
                            )
                        )
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
        Text("Uvoz znamenitosti", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.Navy)

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = numToFetch,
            onValueChange = { numToFetch = it },
            label = { Text("Število znamenitosti za uvoz") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )

        AdminDropdown(
            admins = admins,
            selectedAdminId = selectedAdminId,
            onAdminSelected = { selectedAdminId = it.id.toString() }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = ::fetchAttractionsFromAPI,
                enabled = !isLoading && !isSaving,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
            ) {
                Text(if (isLoading) "Uvažanje..." else "Uvozi znamenitosti")
            }

            Button(
                onClick = { showSaveDialog = true },
                enabled = filteredAttractions.isNotEmpty() && !isSaving,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
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
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
            ) {
                Text("Prekliči")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Dnevnik dogodkov", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(color = Color.White)
                .border(
                    width = 1.dp,
                    color = AppColors.Navy
                )
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

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filteredAttractions = attractions.filter { a ->
                    a.name.contains(it, ignoreCase = true)
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

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredAttractions) { attraction ->
                Card(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxWidth()
                        .border(width = 1.dp, color = AppColors.Navy)
                        .clickable {
                            selectedAttractionData = FullAttractionData(
                                attraction = attraction,
                                nearbyAttractions = emptyList(),
                                weatherData = null
                            )
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

        errorMessage?.let {
            Text("Napaka: $it", color = AppColors.Red)
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
fun AdminDropdown(
    admins: List<AdminUser>,
    selectedAdminId: String?,
    onAdminSelected: (AdminUser) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAdminName = admins.find { it.id == selectedAdminId }?.username ?: "Izberi admina"

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedAdminName, color = AppColors.Navy)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            admins.forEach { admin ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    onAdminSelected(admin)
                }) {
                    Text(admin.username, color = AppColors.Navy)
                }
            }
        }
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
                        Text("Izbriši", color = Color.White)
                    }
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Napaka: $it", color = MaterialTheme.colors.error)
                }
            }
        }
    }

    LaunchedEffect(showEditDialog) {
        if (showEditDialog && regions.isEmpty() && !isLoadingRegions) {
            isLoadingRegions = true
            try {
                regions = getAllRegions()
            } catch (ex: Exception) {
            }
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
                    }, colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red)
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
                .widthIn(min = 400.dp, max = 600.dp)
                .heightIn(min = 500.dp, max = 700.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                        }, colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Green)) {
                            Text("Shrani")
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
}
