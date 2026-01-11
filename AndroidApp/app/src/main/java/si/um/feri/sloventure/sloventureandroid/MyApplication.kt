package si.um.feri.sloventure.sloventureandroid

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences

import kotlinx.serialization.json.Json

import okhttp3.Request
import okhttp3.OkHttpClient

import java.io.File
import java.io.IOException

import timber.log.Timber

import si.um.feri.sloventure.sloventureandroid.core.MQTTClient
import si.um.feri.sloventure.sloventureandroid.model.Attraction

class MyApplication : Application() {
    lateinit var data: MutableList<Attraction>
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

        mqttClient = MQTTClient(applicationContext)
        mqttClient.connect()

        file = File(filesDir, "attractionList.json")

        data = if (file.exists()) {
            loadFromFile()
        } else {
            mutableListOf()
        }
        createNotificationChannels()

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
}