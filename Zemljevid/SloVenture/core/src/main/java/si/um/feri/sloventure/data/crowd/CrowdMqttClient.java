package si.um.feri.sloventure.data.crowd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.UUID;
import java.util.function.Consumer;

import javax.net.ssl.SSLSocketFactory;

public class CrowdMqttClient {

    private static final String BROKER =
        "ssl://sloventure.northeurope-1.ts.eventgrid.azure.net:8883";
    private static final String TOPIC = "analytics/numOfPeople";

    private final MqttClient client;
    private final Json json = new Json();

    public CrowdMqttClient(SSLSocketFactory sslSocketFactory,
                           Consumer<CrowdData> onMessage) throws Exception {

        String clientId = "desktop-map-" + UUID.randomUUID();
        client = new MqttClient(BROKER, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setSocketFactory(sslSocketFactory);
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setUserName("androidApp-authn-ID");
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Gdx.app.error("MQTT", "Connection lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String jsonPayload = new String(message.getPayload());
                Gdx.app.log("MQTT", "message recieved: " + jsonPayload);
                try {
                    CrowdData crowdData =
                        new com.badlogic.gdx.utils.Json()
                            .fromJson(CrowdData.class, jsonPayload);

                    Gdx.app.postRunnable(() -> onMessage.accept(crowdData));
                    Gdx.app.log("CROWD", String.valueOf(crowdData.numOfPeople));

                } catch (Exception e) {
                    Gdx.app.error("MQTT", "Invalid crowd payload: " + jsonPayload, e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        client.connect(options);
        client.subscribe(TOPIC, 1);
        if (client.isConnected()) {
            Gdx.app.log("MQTT", "Mqtt connected to: " + TOPIC);
        }
    }

    public void disconnect() throws MqttException {
        if (client.isConnected()) {
            client.disconnect();
            client.close();
        }
        ;
    }
}
