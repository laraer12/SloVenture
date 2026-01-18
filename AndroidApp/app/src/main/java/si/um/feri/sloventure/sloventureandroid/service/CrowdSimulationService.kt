package si.um.feri.sloventure.sloventureandroid.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.model.Attraction
import si.um.feri.sloventure.sloventureandroid.model.CrowdSimulationPayload
import si.um.feri.sloventure.sloventureandroid.util.MQTTClient
import timber.log.Timber

class CrowdSimulationService : Service() {
    private lateinit var mqttClient: MQTTClient
    private lateinit var app: SloVentureApplication
    private var intervalMs: Long = 60_000L
    private var minCrowdNum: Int = 1
    private var maxCrowdNum: Int = 10
    private var fixedAttractionId: String? = null // null = random
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {

            val attraction = pickAttraction() ?: run {
                Timber.w("No attraction available for crowd simulation")
                handler.postDelayed(this, intervalMs)
                return
            }

            val numPeople = generateRandomCrowdSize()

            val payload = CrowdSimulationPayload(
                attractionId = attraction.id,
                timestamp = System.currentTimeMillis(),
                numOfPeople = numPeople,
                latitude = attraction.location.lat,
                longitude = attraction.location.lon
            )

            app.lastCrowdSimulationReading = payload

            mqttClient.publishCrowdSimulation(payload)

            val intent = Intent(ACTION_CROWD_SIMULATION_UPDATE)
            intent.putExtra("payload", payload)

            LocalBroadcastManager
                .getInstance(this@CrowdSimulationService)
                .sendBroadcast(intent)

            Timber.tag("crowd-simulation").i(
                "Crowd simulated: attraction=${attraction.name}, people=$numPeople"
            )

            handler.postDelayed(this, intervalMs)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_NOT_STICKY
        isRunning = true

        app = application as SloVentureApplication
        mqttClient = app.mqttClient

        intervalMs =
            intent?.getLongExtra("intervalMs", app.crowdSimulationIntervalMs)
                ?: app.crowdSimulationIntervalMs

        minCrowdNum = intent?.getIntExtra("minCrowdNum", 1) ?: 1
        maxCrowdNum = intent?.getIntExtra("maxCrowdNum", 10) ?: 10
        fixedAttractionId = intent?.getStringExtra("attractionId")

        maxCrowdNum = maxCrowdNum.coerceAtMost(20)

        startForeground(3, buildNotification())

        handler.post(runnable)

        Timber.tag("crowd-simulation").i("Crowd simulation started")
        app.crowdSimulationRunning = true

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(runnable)
        isRunning = false
        stopForeground(true)
        app.crowdSimulationRunning = true

        Timber.tag("crowd-simulation").i("Crowd simulation stopped")
    }

    override fun onBind(intent: Intent?) = null

    private fun pickAttraction(): Attraction? {
        val attractions = app.data
        if (attractions.isEmpty()) return null

        fixedAttractionId?.let { id ->
            return attractions.firstOrNull { it.id == id }
        }

        return attractions.random()
    }

    private fun generateRandomCrowdSize(): Int {
        if (minCrowdNum >= maxCrowdNum) return minCrowdNum
        return (minCrowdNum..maxCrowdNum).random()
    }

    private fun buildNotification(): Notification {
        val channelId = "crowd_simulation_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Crowd Simulation",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return Notification.Builder(this, channelId)
            .setContentTitle("Crowd Simulation Running")
            .setContentText("Simulating crowd levels at attractions")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    companion object {
        const val ACTION_CROWD_SIMULATION_UPDATE =
            "si.um.feri.sloventure.ACTION_CROWD_SIMULATION_UPDATE"
    }
}