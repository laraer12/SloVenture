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
import si.um.feri.sloventure.sloventureandroid.model.SensorReading
import si.um.feri.sloventure.sloventureandroid.util.MQTTClient
import timber.log.Timber

class SimulationService : Service() {
    private lateinit var mqttClient: MQTTClient
    private var intervalMs: Long = 60000
    private var minTemp = 0.0
    private var maxTemp = 0.0
    private lateinit var weather: String
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var isRunning = false
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {

            val reading = SensorReading(
                timestamp = System.currentTimeMillis(),
                latitude = latitude,
                longitude = longitude,
                temperature = generateRandomTemperature(),
                weather = weather
            )

            (application as SloVentureApplication)
                .lastSimulationReading = reading

            mqttClient.publishSimulatedSensorReading(reading)

            val intent = Intent(ACTION_SIMULATION_UPDATE)
            intent.putExtra("reading", reading)

            LocalBroadcastManager
                .getInstance(this@SimulationService)
                .sendBroadcast(intent)

            Timber.tag("simulation").i(
                "Publishing simulated reading: lat=$latitude lon=$longitude temp=${reading.temperature} weather=$weather"
            )

            handler.postDelayed(this, intervalMs)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) return START_NOT_STICKY
        isRunning = true

        val app = application as SloVentureApplication

        intervalMs = intent!!.getLongExtra("intervalMs", app.simulationIntervalMs)
        minTemp = intent.getDoubleExtra("minTemp", 0.0)
        maxTemp = intent.getDoubleExtra("maxTemp", 0.0)
        weather = intent.getStringExtra("weather") ?: "Unknown"
        latitude = intent.getDoubleExtra("lat", 0.0)
        longitude = intent.getDoubleExtra("lon", 0.0)

        mqttClient = (application as SloVentureApplication).mqttClient

        startForeground(2, buildNotification())

        handler.post {
            runnable.run()
        }

        Timber.tag("simulation").i("Simulation started")

        return START_NOT_STICKY
    }
    private fun generateRandomTemperature(): Double {
        val raw = minTemp + Math.random() * (maxTemp - minTemp)
        return kotlin.math.round(raw * 10) / 10
    }


    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(runnable)
        isRunning = false
        stopForeground(true)

        Timber.tag("simulation").i("Simulation stopped")
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification(): Notification {
        val channelId = "simulation_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Simulation Mode",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return Notification.Builder(this, channelId)
            .setContentTitle("Simulation Running")
            .setContentText("Sending simulated sensor data")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    companion object {
        const val ACTION_SIMULATION_UPDATE =
            "si.um.feri.sloventure.ACTION_SIMULATION_UPDATE"
    }
}
