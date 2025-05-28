package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import database.data.fetchUsersFromApi
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import database.data.User


val json = Json { ignoreUnknownKeys = true }
val client = OkHttpClient()

@Composable
fun UserListScreen() {
    val coroutineScope = rememberCoroutineScope()

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            try {
                users = fetchUsersFromApi()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    if (selectedUser != null) {
        UserDetailScreen(user = selectedUser!!, onBack = { selectedUser = null })
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Users", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(users) { user ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable {
                                // Fetch details on click (if needed you can add detail fetching)
                                selectedUser = user
                            },
                        elevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(user.username, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Text(user.email, fontSize = 14.sp)
                            Text(if (user.isAdmin) "Admin" else "User")
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
fun UserDetailScreen(user: User, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = onBack, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Back")
        }

        Text(user.username, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Email: ${user.email}", modifier = Modifier.padding(vertical = 8.dp))
        Text("Profile Picture: ${user.profilePicture}")
        Text("Role: ${if (user.isAdmin) "Administrator" else "User"}")
        Text("Created At: ${user.createdAt ?: "N/A"}")
    }
}
