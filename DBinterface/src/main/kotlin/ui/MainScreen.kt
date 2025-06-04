package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import database.data.*
import kotlinx.serialization.json.*
import java.util.*

@Composable
fun MainScreen() {

    var selectedScreen by remember { mutableStateOf("Pregled znamenitosti") }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { selectedScreen = "Pregled znamenitosti" },
                enabled = selectedScreen != "Pregled znamenitosti"
            ) {
                Text("Pregled znamenitosti")
            }

            Button(
                onClick = { selectedScreen = "Pregled uporabnikov" },
                enabled = selectedScreen != "Pregled uporabnikov"
            ) {
                Text("Pregled uporabnikov")
            }

            Button(
                onClick = { selectedScreen = "Uvoz znamenitosti" },
                enabled = selectedScreen != "Uvoz znamenitosti"
            ) {
                Text("Uvoz znamenitosti")
            }
            Button(
                onClick = { selectedScreen = "Uvoz vremena" },
                enabled = selectedScreen != "Uvoz vremena"
            ) {
                Text("Uvoz vremena")
            }
            Button(
                onClick = { selectedScreen = "Generiranje podatkov" },
                enabled = selectedScreen != "Generiranje podatkov"
            ) {
                Text("Generiranje podatkov")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedScreen) {
            "Pregled znamenitosti" -> AttractionListScreen()
            "Pregled uporabnikov" -> UserListScreen()
            "Uvoz znamenitosti" -> AttractionImportScreen()
            "Uvoz vremena" -> WeatherImportScreen()
            "Generiranje podatkov" -> FakeDataScreen()
        }
    }
}

