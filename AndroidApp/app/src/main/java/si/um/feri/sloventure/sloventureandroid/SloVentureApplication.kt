package si.um.feri.sloventure.sloventureandroid

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.serialization.json.Json

import okhttp3.Request
import okhttp3.OkHttpClient

import java.io.File
import java.io.IOException

import timber.log.Timber

import si.um.feri.sloventure.sloventureandroid.core.MQTTClient
import si.um.feri.sloventure.sloventureandroid.location_checker.ProximityService
import si.um.feri.sloventure.sloventureandroid.model.Attraction
import si.um.feri.sloventure.sloventureandroid.model.PhotoPayload

class SloVentureApplication : Application() {
    lateinit var data: MutableList<Attraction>
    val photoData = mutableListOf<PhotoPayload>()
    private lateinit var file: File
    lateinit var mqttClient: MQTTClient
    private lateinit var sharedPref: SharedPreferences
    private val userSettings = "user_settings"
    private val notifsEnabled = "notifs_enabled"
    private val lastPhotoTime = "last_photo_time"
    private var notificationsEnabledRuntime = false
    private var lastPhotoTimeRuntime = 0L

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        notificationsEnabledRuntime = true //FIXXX TODO

        mqttClient = MQTTClient(applicationContext)

        file = File(filesDir, "attractionList.json")

        data = if (file.exists()) {
            loadFromFile()
        } else {
            mutableListOf()
        }
        createNotificationChannels()
        startProximityService()

        sharedPref = getSharedPreferences(userSettings, MODE_PRIVATE)
        applyUserSettings()
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

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabledRuntime = enabled

        sharedPref.edit()
            .putBoolean(notifsEnabled, enabled)
            .apply()

        Timber.i("Notifications enabled set to $enabled")
    }

    fun areNotificationsEnabled(): Boolean = notificationsEnabledRuntime

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

    private fun applyUserSettings() {
        notificationsEnabledRuntime = sharedPref.getBoolean(notifsEnabled, false)
        lastPhotoTimeRuntime = sharedPref.getLong(lastPhotoTime, 0L)

        Timber.i("User settings loaded - notifications = $notificationsEnabledRuntime, lastPhoto = $lastPhotoTimeRuntime")
    }

    private fun startProximityService() {
        val intent = Intent(this, ProximityService::class.java)
        ContextCompat.startForegroundService(this, intent)
        Timber.i("ProximityService started")
    }

}
