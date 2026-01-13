package si.um.feri.sloventure.sloventureandroid.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import si.um.feri.sloventure.sloventureandroid.SloVentureApplication
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.core.MQTTClient
import si.um.feri.sloventure.sloventureandroid.core.SensorDataManager
import si.um.feri.sloventure.sloventureandroid.location.LocationProvider
import si.um.feri.sloventure.sloventureandroid.weather.WeatherProvider

class SensorAutoCaptureService : Service() {

    private lateinit var sensorDataManager: SensorDataManager
    private lateinit var mqttClient: MQTTClient
    private var captureIntervalMs: Long = 60000

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            sensorDataManager.collectSensorDataOnly { payload ->
                if (payload != null) {

                    Log.i("service","Auto-capture payload: lat=${payload.latitude}, lon=${payload.longitude}, temp=${payload.temperature}")


                    mqttClient.publishPhotoPayload(payload)
                    val intent = Intent(ACTION_SENSOR_UPDATE)
                    intent.putExtra("payload", payload)
                    LocalBroadcastManager.getInstance(this@SensorAutoCaptureService).sendBroadcast(intent)
                }
                handler.postDelayed(this, captureIntervalMs)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.i("service","SensorAutoCaptureService started, intervalMs=$captureIntervalMs")


        captureIntervalMs = intent?.getLongExtra("intervalMs", 60000L) ?: 60000L

        sensorDataManager = SensorDataManager(
            LocationProvider(this),
            OrientationProvider(this),
            WeatherProvider()
        )
        mqttClient = (application as SloVentureApplication).mqttClient

        startForeground(1, buildNotification())
        handler.post(runnable)

        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
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
