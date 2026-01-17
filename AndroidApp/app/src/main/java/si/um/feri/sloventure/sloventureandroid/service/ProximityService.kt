package si.um.feri.sloventure.sloventureandroid.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.sensors.LocationProvider
import si.um.feri.sloventure.sloventureandroid.util.AttractionProximityChecker
import si.um.feri.sloventure.sloventureandroid.util.NotificationHelper
import timber.log.Timber

class ProximityService : Service() {

    private lateinit var locationProvider: LocationProvider
    private lateinit var checker: AttractionProximityChecker
    private lateinit var notifier: NotificationHelper

    private val triggered = mutableMapOf<String, Long>()

    private val locationCheckInterval = 1 * 60 * 1000L // 1 min
    private val photoCooldownIfTaken = 10 * 60 * 1000L // 10 min
    private val photoCooldownIfNotTaken = 5 * 60 * 1000L // 5 min

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            locationProvider.getCurrentLocation { location ->
                location?.let { handleLocation(it) }
            }
            handler.postDelayed(this, locationCheckInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()

        val app = application as SloVentureApplication

        locationProvider = LocationProvider(this)
        notifier = NotificationHelper(this)
        checker = AttractionProximityChecker(app.data)

        Timber.i("ProximityService started")
        startLocationLoop()
    }

    private fun startLocationLoop() {
        triggered.clear()
        handler.post(runnable)
    }

    private fun handleLocation(location: Location) {
        val app = application as SloVentureApplication

        checker.findNearbyAttraction(location)?.let { attraction ->
            val now = System.currentTimeMillis()
            val lastTriggered = triggered[attraction.id] ?: 0L

            val cooldown =
                if (app.wasPhotoTakenRecently(photoCooldownIfTaken))
                    photoCooldownIfTaken
                else
                    photoCooldownIfNotTaken

            if (now - lastTriggered >= cooldown) {
                notifier.showAttractionNotification(attraction)
                triggered[attraction.id] = now
                Timber.i("Notification sent for ${attraction.name}")
            } else {
                Timber.i("Cooldown active for ${attraction.name}")
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Timber.i("App removed from recents → stopping ProximityService")
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        Timber.i("ProximityService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}