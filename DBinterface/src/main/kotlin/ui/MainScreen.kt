package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import database.data.*

object AppColors {
    val Navy = Color(0xFF07004D)
    val Lavender = Color(0xFFE1B8FC)
    val Beige = Color(0xFFFFFCF4)
    val Red = Color(0xFFFCB3B3)
    val Green = Color(0xFFCEFCB8)
}

@Composable
fun MainScreen() {
    val navItems = listOf(
        "Pregled znamenitosti",
        "Pregled uporabnikov",
        "Uvoz znamenitosti",
        "Uvoz vremena",
        "Generiranje podatkov"
    )

    var selectedScreen by remember { mutableStateOf(navItems.first()) }

    Row(modifier = Modifier.fillMaxSize().background(AppColors.Beige)) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(200.dp)
                .background(AppColors.Beige)
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            navItems.forEach { screen ->
                val isSelected = selectedScreen == screen

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(if (isSelected) AppColors.Beige else Color.Transparent)
                        .clickable { selectedScreen = screen }
                        .then(
                            if (isSelected) {
                                Modifier.border(1.dp, AppColors.Navy)
                            } else {
                                Modifier.border(0.dp, Color.Transparent)
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isSelected) AppColors.Lavender else Color.Transparent)
                            .padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = screen,
                            color = if (isSelected) Color.Black else Color.DarkGray,
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            when (selectedScreen) {
                "Pregled znamenitosti" -> AttractionListScreen()
                "Pregled uporabnikov" -> UserListScreen()
                "Uvoz znamenitosti" -> AttractionImportScreen()
                "Uvoz vremena" -> WeatherImportScreen()
                "Generiranje podatkov" -> FakeDataScreen()
            }
        }
    }
}
