package si.um.feri.sloventure.sloventureandroid.util

import android.content.Context
import android.location.Location
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import si.um.feri.sloventure.sloventureandroid.BuildConfig
import si.um.feri.sloventure.sloventureandroid.R
import si.um.feri.sloventure.sloventureandroid.model.ExtremeEventPayload
import si.um.feri.sloventure.sloventureandroid.model.PhotoPayload
import si.um.feri.sloventure.sloventureandroid.model.SensorReading
import timber.log.Timber
import java.security.KeyStore
import java.util.UUID
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class MQTTClient(context: Context) {
    private val brokerUrl = "ssl://sloventure.northeurope-1.ts.eventgrid.azure.net:8883"
    private val clientId = "androidApp-${UUID.randomUUID()}"
    private val authName = "androidApp-authn-ID"
    private val mqttClient = MqttAndroidClient(context, brokerUrl, clientId)
    private var pendingExtremeEvent: ExtremeEventPayload? = null
    private val pendingSensorReadings = ArrayDeque<SensorReading>()
    private val MAX_PENDING_SENSOR_READINGS = 20

    private val options = MqttConnectOptions().apply {
        socketFactory = getSocketFactory(context)
        isCleanSession = true
        isAutomaticReconnect = true
        mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
        userName = authName
    }

    fun connect() {
        if (mqttClient.isConnected) {
            flushPendingExtremeEvent()
            flushPendingSensorReadings()
            return
        }

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Timber.Forest.tag("MQTT").d("Connected to Azure Event Grid")
                flushPendingExtremeEvent()
                flushPendingSensorReadings()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Timber.Forest.tag("MQTT").e(exception, "Connection failed")
            }
        })
    }

    fun disconnect() {
        if (mqttClient.isConnected) {
            mqttClient.disconnect()
            Timber.Forest.tag("MQTT").d("Mqtt disconnected")
        }
    }

    private fun publish(topic: String, payload: String) {
        if (!mqttClient.isConnected) {
            Timber.Forest.tag("MQTT").w("Client not connected")
            return
        }
        val message = MqttMessage(payload.toByteArray()).apply {
            qos = 1
        }
        mqttClient.publish(topic, message)

        Timber.Forest.tag("MQTT").d("Published message to $topic")
        Timber.Forest.tag("MQTT").d(payload)
    }

    fun publishPhotoPayload(payload: PhotoPayload) {
        val topic = "sensors/camera"
        val payloadString = Json.Default.encodeToString(payload)

        publish(topic, payloadString)
    }

    fun publishLocation(location: Location) {
        val payload = """
        {
          "lat": ${location.latitude},
          "lon": ${location.longitude},
          "timestamp": ${System.currentTimeMillis()}
        }
    """.trimIndent()

        publish("user/location", payload)
    }

    private fun getSocketFactory(context: Context): SSLSocketFactory {
        val p12Pass = BuildConfig.P12_PASSWORD
        //ustvari objekt ki bere .p12 datoteke
        val keyStore = KeyStore.getInstance("PKCS12")
        context.resources.openRawResource(R.raw.android_client).use {
            keyStore.load(it, p12Pass.toCharArray())
        }

        //da client certifikat da je lahko izveden TLS handshake
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, p12Pass.toCharArray())

        //preveri ustreznost strežnika
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(null as KeyStore?)

        //priprava ssl povezave
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(kmf.keyManagers, tmf.trustManagers, null)

        return sslContext.socketFactory
    }
    fun publishSensorReading(reading: SensorReading) {
        if (!mqttClient.isConnected) {
            Timber.Forest.tag("MQTT").w("Client not connected, buffering sensor reading")

            if (pendingSensorReadings.size >= MAX_PENDING_SENSOR_READINGS) {
                pendingSensorReadings.removeFirst() // drop oldest
            }

            pendingSensorReadings.addLast(reading)
            connect()
            return
        }

        publishSensorReadingInternal(reading)
    }

    private fun flushPendingSensorReadings() {
        if (!mqttClient.isConnected) return

        Timber.Forest.tag("MQTT").d(
            "Flushing ${pendingSensorReadings.size} buffered sensor readings"
        )

        while (pendingSensorReadings.isNotEmpty()) {
            val reading = pendingSensorReadings.removeFirst()
            publishSensorReadingInternal(reading)
        }
    }
    private fun publishSensorReadingInternal(reading: SensorReading) {
        val topic = "sensors/environment"
        val payloadString = Json.Default.encodeToString(reading)
        publish(topic, payloadString)
    }


    fun publishExtremeEvent(payload: ExtremeEventPayload) {
        val topic = "events/extreme/crowd"
        val payloadString = Json.Default.encodeToString(payload)

        if (!mqttClient.isConnected) {
            Timber.Forest.tag("MQTT").w("Client not connected, buffering extreme event")
            pendingExtremeEvent = payload
            connect()
            return
        }

        publish(topic, payloadString)
    }

    private fun flushPendingExtremeEvent() {
        val payload = pendingExtremeEvent ?: return

        Timber.Forest.tag("MQTT").d("Flushing pending extreme event")
        publishExtremeEvent(payload)
        pendingExtremeEvent = null
    }


}