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
    var selectedScreen by remember { mutableStateOf("Attractions") }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { selectedScreen = "Attractions" },
                enabled = selectedScreen != "Attractions"
            ) {
                Text("Attractions")
            }

            Button(
                onClick = { selectedScreen = "Users" },
                enabled = selectedScreen != "Users"
            ) {
                Text("Users")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedScreen) {
            "Attractions" -> AttractionListScreen()
            "Users" -> UserListScreen()
        }
    }
}

