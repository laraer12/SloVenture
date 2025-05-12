package database

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.bson.Document
import java.io.File
import java.sql.Connection
import java.sql.DriverManager


object MongoDBClient {
    private const val CONFIG_PATH = "src/main/kotlin/database/config.json"

    private var username: String = ""
    private var password: String = ""
    var connectionString: String=" "

    private val client: MongoClient
    val database: MongoDatabase

    init {
        loadConfig()
        connectionString = buildConnectionString()
        client = MongoClients.create(connectionString)
        database = client.getDatabase("SloVentureDB")
    }

    private fun loadConfig() {
        try {
            val fileContent = File(CONFIG_PATH).readText()
            username = extractJsonValue(fileContent, "username")
            password = extractJsonValue(fileContent, "password")
        } catch (e: Exception) {
            println("Error loading DB config: ${e.message}")
        }
    }

    private fun extractJsonValue(json: String, key: String): String {
        val regex = Regex(""""$key"\s*:\s*"([^"]+)"""")
        val matchResult = regex.find(json)
        return matchResult?.groups?.get(1)?.value ?: ""
    }

    private fun buildConnectionString(): String {
        return "mongodb+srv://$username:$password@sloventure.4djf5rv.mongodb.net/?retryWrites=true&w=majority&appName=SloVenture"
    }

    fun getCollection(collectionName: String): MongoCollection<Document> {
        return database.getCollection(collectionName)
    }
}
