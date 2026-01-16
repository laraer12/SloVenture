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
import si.um.feri.sloventure.sloventureandroid.model.CrowdSimulationPayload
import si.um.feri.sloventure.sloventureandroid.model.ExtremeEventPayload
import si.um.feri.sloventure.sloventureandroid.model.SensorReading
import si.um.feri.sloventure.sloventureandroid.model.UserEventPayload
import timber.log.Timber
import java.security.KeyStore
import java.util.UUID
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

class MQTTClient(context: Context) {
    // mqtt topics
    private companion object {
        const val TOPIC_SENSOR_ENVIRONMENT = "sensors/environment"
        const val TOPIC_SENSOR_ENVIRONMENT_SIMULATION = "sensors/environment/simulation"
        const val TOPIC_USER_LOCATION = "sensors/user/location"
        const val TOPIC_EXTREME_EVENT_CROWD = "sensors/events/extreme/crowd"
        const val TOPIC_CROWD_SIMULATION = "analytics/numOfPeople"
        private val TOPIC_USER_EVENT_BASE = "sensors/events/user" //  /warning, /info

    }

    @Volatile
    private var isConnecting = false
    private val brokerUrl = "ssl://sloventure.northeurope-1.ts.eventgrid.azure.net:8883"
    private val clientId = "androidApp-${UUID.randomUUID()}"
    private val authName = "androidApp-authn-ID"
    private val mqttClient = MqttAndroidClient(context, brokerUrl, clientId)
    private var pendingExtremeEvent: ExtremeEventPayload? = null
    private var pendingUserEvent: UserEventPayload? = null
    private var pendingCrowdSimulation: CrowdSimulationPayload? = null
    private val pendingSensorReadings = ArrayDeque<SensorReading>()
    private val pendingSimulatedReadings = ArrayDeque<SensorReading>()
    private val MAX_PENDING_SENSOR_READINGS = 20

    private val options = MqttConnectOptions().apply {
        socketFactory = getSocketFactory(context)
        isCleanSession = true
        isAutomaticReconnect = true
        mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
        userName = authName
    }

    fun connect() {
        if (mqttClient.isConnected || isConnecting) {
            Timber.tag("MQTT").d(
                "Skipping connect | connected=${mqttClient.isConnected} connecting=$isConnecting"
            )
            return
        }

        isConnecting = true

        Timber.tag("MQTT").e(
            "CONNECT attempt | connected=${mqttClient.isConnected} connecting=$isConnecting thread=${Thread.currentThread().name}"
        )

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Timber.tag("MQTT").d("Connected to Azure Event Grid")
                isConnecting = false
                flushPendingExtremeEvent()
                flushPendingSensorReadings()
                flushPendingSimulatedReadings()
                flushPendingCrowdSimulation()
                flushPendingUserEvent()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Timber.tag("MQTT").e(exception, "CONNECT FAILED")
                isConnecting = false
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

    //..........SENSORS..........
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
        val payloadString = Json.Default.encodeToString(reading)
        publish(TOPIC_SENSOR_ENVIRONMENT, payloadString)
    }

    //..........SIMULATION..........
    fun publishSimulatedSensorReading(reading: SensorReading) {
        if (!mqttClient.isConnected) {
            Timber.Forest.tag("MQTT")
                .w("Client not connected, buffering simulated sensor reading")

            if (pendingSimulatedReadings.size >= MAX_PENDING_SENSOR_READINGS) {
                pendingSimulatedReadings.removeFirst()
            }

            pendingSimulatedReadings.addLast(reading)
            connect()
            return
        }

        publishSimulatedSensorReadingInternal(reading)
    }

    private fun flushPendingSimulatedReadings() {
        if (!mqttClient.isConnected) return

        while (pendingSimulatedReadings.isNotEmpty()) {
            val reading = pendingSimulatedReadings.removeFirst()
            publishSimulatedSensorReadingInternal(reading)
        }
    }

    private fun publishSimulatedSensorReadingInternal(reading: SensorReading) {
        val payloadString = Json.Default.encodeToString(reading)
        publish(TOPIC_SENSOR_ENVIRONMENT_SIMULATION, payloadString)
    }

    //..........CROWD SIMULATION........

    fun publishCrowdSimulation(payload: CrowdSimulationPayload) {
        val payloadString = Json.Default.encodeToString(payload)

        if (!mqttClient.isConnected) {
            Timber.tag("MQTT")
                .w("Client not connected, buffering crowd simulation")
            pendingCrowdSimulation = payload
            connect()
            return
        }

        publish(TOPIC_CROWD_SIMULATION, payloadString)
    }

    private fun flushPendingCrowdSimulation() {
        val payload = pendingCrowdSimulation ?: return
        Timber.tag("MQTT").d("Flushing pending crowd simulation")
        publishCrowdSimulation(payload)
        pendingCrowdSimulation = null
    }


    //..........EXTREME EVENT..........
    fun publishExtremeEvent(payload: ExtremeEventPayload) {
        val payloadString = Json.Default.encodeToString(payload)

        if (!mqttClient.isConnected) {
            Timber.Forest.tag("MQTT").w("Client not connected, buffering extreme event")
            pendingExtremeEvent = payload
            connect()
            return
        }

        publish(TOPIC_EXTREME_EVENT_CROWD, payloadString)
    }

    private fun flushPendingExtremeEvent() {
        val payload = pendingExtremeEvent ?: return

        Timber.Forest.tag("MQTT").d("Flushing pending extreme event")
        publishExtremeEvent(payload)
        pendingExtremeEvent = null
    }

    //..........USER EVENT..........
    fun publishUserEvent(payload: UserEventPayload) {
        val topic = "$TOPIC_USER_EVENT_BASE/${payload.eventType}"
        val payloadString = Json.Default.encodeToString(payload)

        if (!mqttClient.isConnected) {
            Timber.Forest.tag("MQTT").w("Client not connected, buffering user event")
            pendingUserEvent = payload
            connect()
            return
        }
        publish(topic, payloadString)
    }

    private fun flushPendingUserEvent() {
        val payload = pendingUserEvent ?: return

        Timber.Forest.tag("MQTT").d("Flushing pending user event")
        publishUserEvent(payload)
        pendingUserEvent = null
    }
}