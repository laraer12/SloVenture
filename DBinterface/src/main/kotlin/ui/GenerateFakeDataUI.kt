package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import database.FakeDataGenerator
import database.FakeDataOptions
import database.data.fetchUsers
import database.data.getAllAttractions
import kotlinx.coroutines.launch
import java.time.LocalDate

/*
@Composable
fun FakeDataScreen() {
    var numUsers by remember { mutableStateOf("10") }
    var numVisits by remember { mutableStateOf("10") }
    var numReviews by remember { mutableStateOf("10") }
    var adminUserPercent by remember { mutableStateOf("0") }

    var minRating by remember { mutableStateOf("1") }
    var maxRating by remember { mutableStateOf("5") }

    var startDate by remember { mutableStateOf(LocalDate.now().minusYears(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    var isGenerating by remember { mutableStateOf(false) }
    var statusMessageUsers by remember { mutableStateOf("") }
    var statusMessageReviews by remember { mutableStateOf("") }
    var statusMessageVisits by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Dodaj znamenitost", style = MaterialTheme.typography.h6, color = AppColors.Navy)
        }

        item {
            SectionCard("Uporabniki") {
                OutlinedTextField(
                    value = numUsers,
                    onValueChange = { numUsers = it },
                    label = { Text("Število uporabnikov") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = AppColors.Navy,
                        focusedBorderColor = AppColors.Lavender,
                        cursorColor = AppColors.Lavender
                    )
                )
                Button(
                    onClick = {
                        isGenerating = true
                        statusMessageUsers = "Generiranje lažnih uporabnikov..."
                        FakeDataGenerator.generateAndPostUsers(numUsers.toIntOrNull() ?: 0)
                        statusMessageUsers = "Lažni uporabniki generirani in shranjeni v bazo!"
                        isGenerating = false
                    },
                    enabled = !isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generiraj uporabnike")
                }
                if (statusMessageUsers.isNotEmpty()) {
                    Text(statusMessageUsers)
                }
            }
        }
        item {
            SectionCard("Obiski") {
                OutlinedTextField(
                    value = numVisits,
                    onValueChange = { numVisits = it },
                    label = { Text("Število obiskov") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = AppColors.Navy,
                        focusedBorderColor = AppColors.Lavender,
                        cursorColor = AppColors.Lavender
                    )
                )
                DateSelector("Začetni datum obiskov", startDate) { startDate = it }
                DateSelector("Končni datum obiskov", endDate) { endDate = it }
                Button(
                    onClick = {
                        isGenerating = true
                        statusMessageVisits = "Generiranje lažnih obiskov..."
                        FakeDataGenerator.generateAndPostVisits(
                            FakeDataOptions(
                                numUsers = numUsers.toIntOrNull() ?: 0,
                                numVisits = numVisits.toIntOrNull() ?: 0,
                                numReviews = numReviews.toIntOrNull() ?: 0,
                                dateStart = startDate,
                                dateEnd = endDate
                            ), users = fetchUsers(), attractions = getAllAttractions()
                        )
                        statusMessageVisits = "Lažni obiski generirani in shranjeni v bazo!"
                        isGenerating = false
                    },
                    enabled = !isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generiraj obiske")
                }
                if (statusMessageVisits.isNotEmpty()) {
                    Text(statusMessageVisits)
                }
            }
        }
        item {
            SectionCard("Ocene") {

                OutlinedTextField(
                    value = numReviews,
                    onValueChange = { numReviews = it },
                    label = { Text("Število ocen") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = AppColors.Navy,
                        focusedBorderColor = AppColors.Lavender,
                        cursorColor = AppColors.Lavender
                    )
                )

                OutlinedTextField(
                    value = minRating,
                    onValueChange = { minRating = it },
                    label = { Text("Najmanjša ocena (1–5)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = AppColors.Navy,
                        focusedBorderColor = AppColors.Lavender,
                        cursorColor = AppColors.Lavender
                    )
                )


                OutlinedTextField(
                    value = maxRating,
                    onValueChange = { maxRating = it },
                    label = { Text("Najvišja ocena (1–5)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = AppColors.Navy,
                        focusedBorderColor = AppColors.Lavender,
                        cursorColor = AppColors.Lavender
                    )
                )
                DateSelector("Začetni datum ocen", startDate) { startDate = it }
                DateSelector("Končni datum ocen", endDate) { endDate = it }

                Button(
                    onClick = {
                        isGenerating = true
                        statusMessageReviews = "Generiranje lažnih ocen..."
                        FakeDataGenerator.generateAndPostReviews(
                            FakeDataOptions(
                                numUsers = numUsers.toIntOrNull() ?: 0,
                                numVisits = numVisits.toIntOrNull() ?: 0,
                                numReviews = numReviews.toIntOrNull() ?: 0,
                                dateStart = startDate,
                                dateEnd = endDate
                            ), users = fetchUsers(), attractions = getAllAttractions()
                        )
                        statusMessageReviews = "Lažne ocene generirane in shranjene v bazo!"
                        isGenerating = false
                    },
                    enabled = !isGenerating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generiraj ocene")
                }
                if (statusMessageReviews.isNotEmpty()) {
                    Text(statusMessageReviews)
                }
            }
        }
    }
}*/
/*
@Composable
fun FakeDataScreen() {
    var numUsers by remember { mutableStateOf("10") }
    var numVisits by remember { mutableStateOf("10") }
    var numReviews by remember { mutableStateOf("10") }
    var minRating by remember { mutableStateOf("1") }
    var maxRating by remember { mutableStateOf("5") }
    var startDate by remember { mutableStateOf(LocalDate.now().minusYears(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var isGenerating by remember { mutableStateOf(false) }

    var statusMessageUsers by remember { mutableStateOf("") }
    var statusMessageVisits by remember { mutableStateOf("") }
    var statusMessageReviews by remember { mutableStateOf("") }

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
                "Generator podatkov",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Navy,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionCard("Uporabniki") {
            OutlinedTextField(
                value = numUsers,
                onValueChange = { numUsers = it },
                label = { Text("Število uporabnikov") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    isGenerating = true
                    statusMessageUsers = "Generiranje lažnih uporabnikov..."
                    FakeDataGenerator.generateAndPostUsers(numUsers.toIntOrNull() ?: 0)
                    statusMessageUsers = "Lažni uporabniki generirani in shranjeni v bazo!"
                    isGenerating = false
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generiraj uporabnike", color = AppColors.Navy)
            }
            if (statusMessageUsers.isNotEmpty()) {
                Text(statusMessageUsers, color = AppColors.Navy, modifier = Modifier.padding(top = 8.dp))
            }
        }

        SectionCard("Obiski") {
            OutlinedTextField(
                value = numVisits,
                onValueChange = { numVisits = it },
                label = { Text("Število obiskov") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            DateSelector("Začetni datum obiskov", startDate) { startDate = it }
            DateSelector("Končni datum obiskov", endDate) { endDate = it }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    isGenerating = true
                    statusMessageVisits = "Generiranje lažnih obiskov..."
                    FakeDataGenerator.generateAndPostVisits(
                        FakeDataOptions(
                            numUsers = numUsers.toIntOrNull() ?: 0,
                            numVisits = numVisits.toIntOrNull() ?: 0,
                            numReviews = numReviews.toIntOrNull() ?: 0,
                            dateStart = startDate,
                            dateEnd = endDate
                        ),
                        users = fetchUsers(),
                        attractions = getAllAttractions()
                    )
                    statusMessageVisits = "Lažni obiski generirani in shranjeni v bazo!"
                    isGenerating = false
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generiraj obiske", color = AppColors.Navy)
            }
            if (statusMessageVisits.isNotEmpty()) {
                Text(statusMessageVisits, color = AppColors.Navy, modifier = Modifier.padding(top = 8.dp))
            }
        }

        SectionCard("Ocene") {
            OutlinedTextField(
                value = numReviews,
                onValueChange = { numReviews = it },
                label = { Text("Število ocen") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = minRating,
                onValueChange = { minRating = it },
                label = { Text("Najmanjša ocena (1–5)") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = maxRating,
                onValueChange = { maxRating = it },
                label = { Text("Najvišja ocena (1–5)") },
                modifier = Modifier.fillMaxWidth(),
                colors = defaultTextFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            DateSelector("Začetni datum ocen", startDate) { startDate = it }
            DateSelector("Končni datum ocen", endDate) { endDate = it }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    isGenerating = true
                    statusMessageReviews = "Generiranje lažnih ocen..."
                    FakeDataGenerator.generateAndPostReviews(
                        FakeDataOptions(
                            numUsers = numUsers.toIntOrNull() ?: 0,
                            numVisits = numVisits.toIntOrNull() ?: 0,
                            numReviews = numReviews.toIntOrNull() ?: 0,
                            dateStart = startDate,
                            dateEnd = endDate
                        ),
                        users = fetchUsers(),
                        attractions = getAllAttractions()
                    )
                    statusMessageReviews = "Lažne ocene generirane in shranjene v bazo!"
                    isGenerating = false
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generiraj ocene", color = AppColors.Navy)
            }
            if (statusMessageReviews.isNotEmpty()) {
                Text(statusMessageReviews, color = AppColors.Navy, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}*/
@Composable
fun FakeDataScreen() {
    var numUsers by remember { mutableStateOf("10") }
    var numVisits by remember { mutableStateOf("10") }
    var numReviews by remember { mutableStateOf("10") }
    var minRating by remember { mutableStateOf("1") }
    var maxRating by remember { mutableStateOf("5") }
    var startDate by remember { mutableStateOf(LocalDate.now().minusYears(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var isGenerating by remember { mutableStateOf(false) }

    var statusMessageUsers by remember { mutableStateOf("") }
    var statusMessageVisits by remember { mutableStateOf("") }
    var statusMessageReviews by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Beige)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Generator podatkov",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Navy,
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionCard("Uporabniki") {
                    OutlinedTextField(
                        value = numUsers,
                        onValueChange = { numUsers = it },
                        label = { Text("Število uporabnikov") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            isGenerating = true
                            statusMessageUsers = "Generiranje lažnih uporabnikov..."
                            FakeDataGenerator.generateAndPostUsers(numUsers.toIntOrNull() ?: 0)
                            statusMessageUsers = "Lažni uporabniki generirani in shranjeni v bazo!"
                            isGenerating = false
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generiraj uporabnike", color = AppColors.Navy)
                    }
                    if (statusMessageUsers.isNotEmpty()) {
                        Text(statusMessageUsers, color = AppColors.Navy, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            item {
                SectionCard("Obiski") {
                    OutlinedTextField(
                        value = numVisits,
                        onValueChange = { numVisits = it },
                        label = { Text("Število obiskov") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    DateSelector("Začetni datum obiskov", startDate) { startDate = it }
                    DateSelector("Končni datum obiskov", endDate) { endDate = it }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            isGenerating = true
                            statusMessageVisits = "Generiranje lažnih obiskov..."
                            FakeDataGenerator.generateAndPostVisits(
                                FakeDataOptions(
                                    numUsers = numUsers.toIntOrNull() ?: 0,
                                    numVisits = numVisits.toIntOrNull() ?: 0,
                                    numReviews = numReviews.toIntOrNull() ?: 0,
                                    dateStart = startDate,
                                    dateEnd = endDate
                                ),
                                users = fetchUsers(),
                                attractions = getAllAttractions()
                            )
                            statusMessageVisits = "Lažni obiski generirani in shranjeni v bazo!"
                            isGenerating = false
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generiraj obiske", color = AppColors.Navy)
                    }
                    if (statusMessageVisits.isNotEmpty()) {
                        Text(statusMessageVisits, color = AppColors.Navy, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            item {
                SectionCard("Ocene") {
                    OutlinedTextField(
                        value = numReviews,
                        onValueChange = { numReviews = it },
                        label = { Text("Število ocen") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = minRating,
                        onValueChange = { minRating = it },
                        label = { Text("Najmanjša ocena (1–5)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxRating,
                        onValueChange = { maxRating = it },
                        label = { Text("Najvišja ocena (1–5)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    DateSelector("Začetni datum ocen", startDate) { startDate = it }
                    DateSelector("Končni datum ocen", endDate) { endDate = it }

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            isGenerating = true
                            statusMessageReviews = "Generiranje lažnih ocen..."
                            FakeDataGenerator.generateAndPostReviews(
                                FakeDataOptions(
                                    numUsers = numUsers.toIntOrNull() ?: 0,
                                    numVisits = numVisits.toIntOrNull() ?: 0,
                                    numReviews = numReviews.toIntOrNull() ?: 0,
                                    dateStart = startDate,
                                    dateEnd = endDate
                                ),
                                users = fetchUsers(),
                                attractions = getAllAttractions()
                            )
                            statusMessageReviews = "Lažne ocene generirane in shranjene v bazo!"
                            isGenerating = false
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generiraj ocene", color = AppColors.Navy)
                    }
                    if (statusMessageReviews.isNotEmpty()) {
                        Text(statusMessageReviews, color = AppColors.Navy, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(width = 1.dp, color = AppColors.Navy),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Navy)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun defaultTextFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    unfocusedBorderColor = AppColors.Navy,
    focusedBorderColor = AppColors.Lavender,
    cursorColor = AppColors.Lavender
)


/*
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = 8.dp) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}*/

@Composable
fun DateSelector(label: String, date: LocalDate, onChange: (LocalDate) -> Unit) {
    var year by remember { mutableStateOf(date.year.toString()) }
    var month by remember { mutableStateOf(date.monthValue.toString()) }
    var day by remember { mutableStateOf(date.dayOfMonth.toString()) }

    Column(Modifier.padding(vertical = 8.dp)) {
        Text("$label", style = MaterialTheme.typography.subtitle1)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = year,
                onValueChange = {
                    year = it
                    updateDate(year, month, day, onChange)
                },
                label = { Text("Year") },
                modifier = Modifier.weight(1f),
                colors = defaultTextFieldColors()
            )
            OutlinedTextField(
                value = month,
                onValueChange = {
                    month = it
                    updateDate(year, month, day, onChange)
                },
                label = { Text("Month") },
                modifier = Modifier.weight(1f),
                colors = defaultTextFieldColors()
            )
            OutlinedTextField(
                value = day,
                onValueChange = {
                    day = it
                    updateDate(year, month, day, onChange)
                },
                label = { Text("Day") },
                modifier = Modifier.weight(1f),
                colors = defaultTextFieldColors()
            )
        }
    }
}

private fun updateDate(year: String, month: String, day: String, onChange: (LocalDate) -> Unit) {
    val y = year.toIntOrNull()
    val m = month.toIntOrNull()
    val d = day.toIntOrNull()

    if (y != null && m in 1..12 && d in 1..31) {
        try {
            val newDate = LocalDate.of(y, m!!, d!!)
            onChange(newDate)
        } catch (_: Exception) {
        }
    }
}
