import os
import ssl
import time
import json
import uuid
from dotenv import load_dotenv
import paho.mqtt.client as mqtt
from blockchain import Blockchain
from blockdata import BlockData
from block import Block
from utils import block_to_json, block_from_json

load_dotenv()

broker = os.getenv("MQTT_BROKER")
port = int(os.getenv("MQTT_PORT"))
blockchain = Blockchain()

blockchainServerGet = os.getenv("MQTT_TOPIC_IN_SERVER")
blockchainServerSend = os.getenv("MQTT_TOPIC_OUT_SERVER")

CLIENT_ID = f"blockchainServer-{uuid.uuid4()}"
auth_name = os.getenv("AUTH_NAME")

cert_key = os.getenv("CERT_KEY")
cert_pem = os.getenv("CERT_PEM")

def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print("Connected successfully", flush=True)
        client.subscribe(blockchainServerGet)
    else:
        print("Connection failed with code", rc, flush=True)

def on_message(client, userdata, msg):
    try:
        print(f"Received MQTT message on topic:", msg.topic, flush=True)
        payload_str = msg.payload.decode()
        print("Raw payload:", payload_str[:200], "...", flush=True)
        payload = json.loads(payload_str)

        if "data" in payload:
            data_payload = payload["data"]
        else:
            data_payload = payload
            
        data = BlockData.from_dict(data_payload)

        new_block = blockchain.create_block(data)
        client.publish(blockchainServerSend, block_to_json(new_block))
        print(f"[INFO] Created new block with index {new_block.index}")

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