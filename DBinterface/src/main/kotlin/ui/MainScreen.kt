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
import database.testMongoConnection

@Composable
fun MainScreen(){
    Column(modifier=Modifier.padding(16.dp)){
        Text("Glavna stran!", modifier=Modifier.padding(bottom = 8.dp))
        //AttractionListScreen()
        UserListScreen()
    }
}