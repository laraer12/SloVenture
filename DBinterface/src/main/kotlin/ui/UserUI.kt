package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import database.data.*
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient


val json = Json { ignoreUnknownKeys = true }
val client = OkHttpClient()


@Composable
fun UserListScreen() {
    val coroutineScope = rememberCoroutineScope()

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var filteredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    fun fetchUsersAndUpdateList() {
        coroutineScope.launch {
            isLoading = true
            try {
                val fetched = fetchUsers()
                users = fetched
                filteredUsers = if (searchQuery.isBlank()) fetched
                else fetched.filter { it.username.contains(searchQuery, ignoreCase = true) }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchUsersAndUpdateList()
    }

    if (selectedUser != null) {
        UserDetailScreen(
            user = selectedUser!!,
            onBack = {
                selectedUser = null
                fetchUsersAndUpdateList()
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(AppColors.Beige)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Uporabniki",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Navy
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { fetchUsersAndUpdateList() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Osveži", tint = AppColors.Navy)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)
                ) {
                    Text("Dodaj uporabnika", color = AppColors.Navy)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                filteredUsers = users.filter { user ->
                    user.username.contains(it, ignoreCase = true)
                }
            },
            label = { Text("Išči uporabnika...") },
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
                items(filteredUsers) { user ->
                    Card(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .fillMaxWidth()
                            .border(width = 1.dp, color = AppColors.Navy)
                            .clickable { selectedUser = user },
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                user.username,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Navy
                            )
                            Text(user.email, fontSize = 14.sp)
                            Text(if (user.isAdmin) "Admin" else "Uporabnik", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddUserDialog(
                onDismiss = { showAddDialog = false },
                onSave = { _ ->
                    showAddDialog = false
                    fetchUsersAndUpdateList()
                })
        }

        errorMessage?.let {
            Text("Error: $it", color = AppColors.Red)
        }
    }
}


@Composable
fun UserDetailScreen(user: User, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }
    var password by remember { mutableStateOf(user.password) }
    var isAdmin by remember { mutableStateOf(user.isAdmin) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Uredi uporabnika", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Uporabniško ime") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Elektronska pošta") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Geslo") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = AppColors.Navy,
                focusedBorderColor = AppColors.Lavender,
                cursorColor = AppColors.Lavender
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isAdmin, onCheckedChange = { isAdmin = it },
                colors = CheckboxDefaults.colors(
                checkedColor = AppColors.Lavender,
                uncheckedColor = AppColors.Lavender,
                checkmarkColor = Color.White
            ))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Je admin", color = AppColors.Navy)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Lavender)) {
                Text("Nazaj")
            }

            Row {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = updateUser(
                                user.copy(
                                    username = username,
                                    email = email,
                                    password = password,
                                    isAdmin = isAdmin
                                )
                            )
                            if (success) {
                                onBack()
                            } else {
                                message = "Urejanje neuspešno."
                            }
                        }
                    }, colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Green)
                ) {
                    Text("Shrani")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = user.id?.let { deleteUser(it) } ?: false
                            if (success) onBack() else message = "Brisanje neuspešno."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red)
                ) {
                    Text("Izbriši", color = Color.Black)
                }
            }
        }
        message?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = AppColors.Red)
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            elevation = 8.dp,
            modifier = Modifier
                .width(600.dp)
                .height(375.dp)
                .border(width = 1.dp, color = AppColors.Navy)
                .background(color = AppColors.Beige)
        ) {

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Dodaj uporabnika", style = MaterialTheme.typography.h6, color = AppColors.Navy)

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Uporabniško ime") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Elektronska pošta") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Geslo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            unfocusedBorderColor = AppColors.Navy,
                            focusedBorderColor = AppColors.Lavender,
                            cursorColor = AppColors.Lavender
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isAdmin, onCheckedChange = { isAdmin = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AppColors.Lavender,
                                uncheckedColor = AppColors.Lavender,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Je admin")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val newUser = User(
                                        username = username,
                                        email = email,
                                        password = password,
                                        isAdmin = isAdmin,
                                        isFakeData = false
                                    )
                                    val success = postUser(newUser)
                                    if (success) {
                                        onSave(newUser)
                                    } else {
                                        message = "Napaka pri shranjevanju uporabnika."
                                    }
                                } catch (e: Exception) {
                                    message = "Napaka: ${e.message}"
                                }
                            }, colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Green)
                        ) {
                            Text("Ustvari")
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Red)
                        ) {
                            Text("Nazaj")
                        }
                    }
                    message?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = AppColors.Red)
                    }
                }
            }
        }
    }
}




