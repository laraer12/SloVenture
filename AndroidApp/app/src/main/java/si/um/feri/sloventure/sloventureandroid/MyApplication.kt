package si.um.feri.sloventure.sloventureandroid

import android.app.Application
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import si.um.feri.sloventure.sloventureandroid.model.Attraction
import timber.log.Timber
import java.io.File
import java.io.IOException

class MyApplication : Application() {
    lateinit var data: MutableList<Attraction>
    private lateinit var file: File

    override fun onCreate() {
        super.onCreate()
        file = File(filesDir, "attractionList.json")

        data = if (file.exists()) {
            loadFromFile()
        } else {
            mutableListOf()
        }
    }

    fun loadFromFile(): MutableList<Attraction> {
        return try {
            val jsonString = file.readText()
            Json.decodeFromString(jsonString)
        } catch (e: IOException) {
            mutableListOf()
        }
    }

    fun saveToFile() {
        val jsonString = Json.encodeToString(data)
        file.writeText(jsonString)
    }

    fun getAllAttractions() {
        val client = OkHttpClient()

        val request = Request.Builder()
            //za povezavo s telefonom je potrebno zamenjati 10.0.2.2 z ip-jem računalnika
            .url("http://10.0.2.2:3001/attractions/getAllAttractionsKotlin") //za emulator
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Timber.tag("HTTP").e("Failed to fetch attractions: HTTP ${response.code}")
                return
            }

            val responseBody = response.body?.string() ?: return

            Timber.tag("HTTP").i(responseBody)

            val json = Json { ignoreUnknownKeys = true }
            val attractions = try {
                json.decodeFromString<List<Attraction>>(responseBody)
            } catch (e: Exception) {
                Timber.tag("HTTP").e("Failed to parse attractions: ${e.message}")
                emptyList()
            }

            data.clear()
            data.addAll(attractions)
            saveToFile()
        }
    }
}