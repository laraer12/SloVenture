package si.um.feri.sloventure.sloventureandroid

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import si.um.feri.sloventure.sloventureandroid.model.Attraction
import si.um.feri.sloventure.sloventureandroid.model.CrowdSimulationPayload
import si.um.feri.sloventure.sloventureandroid.model.SensorReading
import si.um.feri.sloventure.sloventureandroid.util.MQTTClient
import timber.log.Timber
import java.io.File
import java.io.IOException

class SloVentureApplication : Application() {
    lateinit var data: MutableList<Attraction>
    private lateinit var file: File
    lateinit var mqttClient: MQTTClient
    private lateinit var sharedPref: SharedPreferences
    private val userSettings = "user_settings"
    private val notifsEnabled = "notifs_enabled"
    private val lastPhotoTime = "last_photo_time"
    private var lastPhotoTimeRuntime = 0L

    @Volatile
    var lastSensorReading: SensorReading? = null
    @Volatile
    var lastSimulationReading: SensorReading? = null
    @Volatile
    var lastCrowdSimulationReading: CrowdSimulationPayload? = null
    @Volatile
    var captureIntervalMs: Long = 60_000L
    @Volatile
    var simulationIntervalMs: Long = 60_000L
    @Volatile
    var crowdSimulationIntervalMs: Long = 60_000L
    var simulationMinTemp: Double = 0.0
    var simulationMaxTemp: Double = 10.0
    var simulationWeatherIndex: Int = 0
    var simulationLat: Double = 46.55472
    var simulationLon: Double = 15.64667
    @Volatile var crowdSimulationRunning = false
    @Volatile var crowdMinPeople = 1
    @Volatile var crowdMaxPeople = 10
    @Volatile var crowdAttractionId: String? = null

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        mqttClient = MQTTClient(this)
        mqttClient.connect()

        file = File(filesDir, "attractionList.json")

        data = if (file.exists()) {
            loadFromFile()
        } else {
            mutableListOf()
        }
        createNotificationChannels()

        sharedPref = getSharedPreferences(userSettings, MODE_PRIVATE)
        lastPhotoTimeRuntime = sharedPref.getLong(lastPhotoTime, 0L)
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

    suspend fun getAllAttractions() = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        /*
                val request = Request.Builder()
                    //za povezavo s telefonom je potrebno zamenjati 10.0.2.2 z ip-jem računalnika
                    .url("http://10.0.2.2:3001/attractions/getAllAttractionsKotlin") //za emulator
                    .get()
                    .build()*/

        val request = Request.Builder()
            .url("http://192.168.1.101:3001/attractions/getAllAttractionsKotlin")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.tag("HTTP").e("Failed to fetch attractions: HTTP ${response.code}")
                    return@withContext
                }

                val responseBody = response.body?.string() ?: return@withContext

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
        } catch (e: Exception) {
            Timber.tag("HTTP").e(e, "Network error while fetching attractions")
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

// za spremljanje lokacije v ozadju
        val serviceChannel = NotificationChannel(
            "SERVICE",
            "Background Service",
            NotificationManager.IMPORTANCE_LOW
        )

// obvestila za ko si blizu znamenitosti
        val eventChannel = NotificationChannel(
            "EVENTS",
            "Attraction Events",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(eventChannel)
    }
    fun markPhotoTaken() {
        lastPhotoTimeRuntime = System.currentTimeMillis()

        sharedPref.edit()
            .putLong(lastPhotoTime, lastPhotoTimeRuntime)
            .apply()

        Timber.i("Photo marked at $lastPhotoTimeRuntime")
    }
    fun wasPhotoTakenRecently(cooldownMs: Long): Boolean {
        return System.currentTimeMillis() - lastPhotoTimeRuntime < cooldownMs
    }
}
