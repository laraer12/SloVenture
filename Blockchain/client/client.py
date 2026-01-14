import os
import ssl
import time
import json
import uuid
from dotenv import load_dotenv
import paho.mqtt.client as mqtt

load_dotenv()

broker = os.getenv("MQTT_BROKER")
port = int(os.getenv("MQTT_PORT"))

blockchainGet = os.getenv("MQTT_TOPIC_IN_CLIENT")
blockchainSend = os.getenv("MQTT_TOPIC_OUT_CLIENT")

CLIENT_ID = f"blockchain-{uuid.uuid4()}"
auth_name = os.getenv("AUTH_NAME_CLIENT")

cert_key = os.getenv("CERT_KEY")
cert_pem = os.getenv("CERT_PEM")

def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print("Connected successfully", flush=True)
        client.subscribe(blockchainGet)
    else:
        print("Connection failed with code", rc, flush=True)

def on_message(client, userdata, msg):
    try:
        print(f"Received MQTT message on topic:", msg.topic, flush=True)
        payload_str = msg.payload.decode()
        print("Raw payload:", payload_str[:200], "...", flush=True)
        payload = json.loads(payload_str)

    except Exception as e:
        print(f"[ERROR] Failed to process message: {e}")

client = mqtt.Client(client_id=CLIENT_ID, protocol=mqtt.MQTTv311)
client.on_connect = on_connect
client.on_message = on_message

client.username_pw_set(auth_name)

client.tls_set(
    ca_certs=None,
    certfile=cert_pem,
    keyfile=cert_key,
    tls_version=ssl.PROTOCOL_TLSv1_2
)

client.connect(broker, port)
client.loop_forever()