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
import si.um.feri.sloventure.sloventureandroid.util.MQTTClient
import si.um.feri.sloventure.sloventureandroid.sensors.LocationProvider
import si.um.feri.sloventure.sloventureandroid.sensors.SensorReadingCollector
import si.um.feri.sloventure.sloventureandroid.sensors.WeatherProvider
import timber.log.Timber

class AutoCaptureService : Service() {
    private lateinit var mqttClient: MQTTClient
    private var captureIntervalMs: Long = 60000

    private var isRunning = false
    private lateinit var collector: SensorReadingCollector

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {

            collector.collect { reading ->
                if (reading != null) {
                    (application as SloVentureApplication)
                        .lastSensorReading = reading
                    mqttClient.publishSensorReading(reading)
                    val intent = Intent(ACTION_SENSOR_UPDATE)
                    intent.putExtra("reading", reading)
                    LocalBroadcastManager
                        .getInstance(this@AutoCaptureService)
                        .sendBroadcast(intent)
                }
            }
            handler.postDelayed(this, captureIntervalMs)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isRunning) {
            Timber.tag("service").w("Service already running, ignoring start")
            return START_NOT_STICKY
        }
        isRunning = true
        captureIntervalMs =
            intent?.getLongExtra("intervalMs", 60000L) ?: 60000L

        mqttClient = (application as SloVentureApplication).mqttClient

        collector = SensorReadingCollector(
            LocationProvider(this),
            WeatherProvider()
        )

        startForeground(1, buildNotification())
        handler.post(runnable)

        Timber.tag("service").i("Auto capture started")

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Timber.tag("service").i("Auto capture stopped")

        handler.removeCallbacks(runnable)
        isRunning = false

        stopForeground(true)
    }

    override fun onBind(intent: Intent?) = null

    private fun buildNotification(): Notification {
        val channelId = "sensor_auto_capture_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sensor Auto Capture",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("Sensor Auto Capture Running")
            .setContentText("Collecting sensor data in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    companion object {
        const val ACTION_SENSOR_UPDATE = "si.um.feri.sloventure.ACTION_SENSOR_UPDATE"
    }
}
