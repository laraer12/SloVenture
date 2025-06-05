package ui

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import database.FakeDataGenerator
import database.FakeDataOptions
import java.time.LocalDate

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
    var statusMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Generiraj podatke", style = MaterialTheme.typography.h5)
        }

        item {
            SectionCard("Uporabniki") {
                LabeledTextField("Število uporabnikov", numUsers) { numUsers = it }
                LabeledTextField("Procent admin-ov", adminUserPercent) { adminUserPercent = it }
            }
        }

        item {
            SectionCard("Obiski") {
                LabeledTextField("Število obiskov", numVisits) { numVisits = it }
            }
        }

        item {
            SectionCard("Ocene") {
                LabeledTextField("Število ocen", numReviews) { numReviews = it }
                LabeledTextField("Najmanjša ocena (1–5)", minRating) { minRating = it }
                LabeledTextField("Najvišja ocena (1–5)", maxRating) { maxRating = it }
            }
        }

        item {
            SectionCard("Časovno obdobje obiskov in ocen") {
                DateSelector("Začetni datum", startDate) { startDate = it }
                DateSelector("Končni datum", endDate) { endDate = it }
            }
        }

        item {
            Button(
                onClick = {
                    isGenerating = true
                    statusMessage = "Generiranje lažnih podatkov..."
                    FakeDataGenerator.generateAndPostAll(
                        FakeDataOptions(
                            numUsers = numUsers.toIntOrNull() ?: 0,
                            numVisits = numVisits.toIntOrNull() ?: 0,
                            numReviews = numReviews.toIntOrNull() ?: 0,
                            dateStart = startDate,
                            dateEnd = endDate
                        )
                    )
                    statusMessage = "Lažni podatki generirani in shranjeni v bazo!"
                    isGenerating = false
                },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generiraj")
            }
        }

        if (statusMessage.isNotEmpty()) {
            item {
                Text(statusMessage)
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), elevation = 8.dp) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun LabeledTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

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
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = month,
                onValueChange = {
                    month = it
                    updateDate(year, month, day, onChange)
                },
                label = { Text("Month") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = day,
                onValueChange = {
                    day = it
                    updateDate(year, month, day, onChange)
                },
                label = { Text("Day") },
                modifier = Modifier.weight(1f)
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
