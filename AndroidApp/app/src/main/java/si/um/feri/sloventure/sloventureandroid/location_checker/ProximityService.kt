package si.um.feri.sloventure.sloventureandroid.location_checker

import android.os.Looper
import android.os.Handler
import android.os.IBinder
import android.app.Service
import android.content.Intent
import android.app.Notification
import android.location.Location
import androidx.core.app.NotificationCompat

import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.core.MQTTClient
import si.um.feri.sloventure.sloventureandroid.location.LocationProvider

import timber.log.Timber

class ProximityService : Service() {
    private lateinit var locationProvider: LocationProvider
    private lateinit var checker: AttractionProximityChecker
    private lateinit var notifier: NotificationHelper
    private lateinit var mqttClient: MQTTClient
    private val triggered = mutableMapOf<String, Long>()
    private val locationCheckInterval = 1 * 30 * 1000L // 1 minuta // 10 * 500L // 5 sekund za test
    private val photoCooldownIfTaken = 1 * 60 * 1000L // 1 ura // 10 * 2000L // 20 sekund za test
    private val photoCooldownIfNotTaken = 1 * 60 * 1000L // pol ure // 10 * 1000L // 10 sekund za test

    override fun onCreate() {
        super.onCreate()

        val app = application as SloVentureApplication

        startForeground(1, createServiceNotification())

        locationProvider = LocationProvider(this)
        mqttClient = app.mqttClient
        notifier = NotificationHelper(this)
        checker = AttractionProximityChecker(app.data)

        startLocationLoop()
    }

    private fun startLocationLoop() {
        triggered.clear()

        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                /* // testne koordinate
                val testLocation = Location("test").apply {
                    latitude = 46.49132
                    longitude = 14.05411
                    accuracy = 5f
                }
                handleLocation(testLocation)
                */

                // uporaba dejanskih koordinat
                locationProvider.getCurrentLocation { location ->

                   // Log.i("ProximityService", "is running")

                    location?.let { handleLocation(it) }
                }

                Handler(Looper.getMainLooper()).postDelayed(this, locationCheckInterval)
            }
        }, 0)
    }

    private fun handleLocation(location: Location) {
        val app = application as SloVentureApplication

        if (!app.areNotificationsEnabled()) {
            Timber.i("Notifications disabled by user")
            return
        }
        //mqttClient.publishLocation(location)

        checker.findNearbyAttraction(location)?.let { attraction ->
            val now = System.currentTimeMillis()
            val lastTriggered = triggered[attraction.id] ?: 0L

            // določim cooldown glede na to, ali je uporabnik že slikal
            val cooldown = if (app.wasPhotoTakenRecently(photoCooldownIfTaken))
                photoCooldownIfTaken

            else
                photoCooldownIfNotTaken

            if (now - lastTriggered >= cooldown) {
                notifier.showAttractionNotification(attraction)
                triggered[attraction.id] = now
                Timber.i("Notification sent for ${attraction.name}")
            }
            else
                Timber.i("Notification skipped for ${attraction.name}, cooldown not finished")
        }
    }

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, "SERVICE")
            .setContentTitle(getString(R.string.active_service))
            .setContentText(getString(R.string.location_tracking))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}