import os
import ssl
import time
import json
import uuid
from dotenv import load_dotenv
import paho.mqtt.client as mqtt
from Blockchain.server.blockchain import Blockchain
from Blockchain.common.block import Block
from Blockchain.common.blockdata import BlockData
from Blockchain.common.serialization import block_from_json, block_to_json

json_dir = os.path.join("Blockchain", "json")
os.makedirs(json_dir, exist_ok=True)

load_dotenv()

broker = os.getenv("MQTT_BROKER")
port = int(os.getenv("MQTT_PORT"))

blockchainServerGet = os.getenv("MQTT_TOPIC_IN_SERVER")
blockchainServerSend = os.getenv("MQTT_TOPIC_OUT_SERVER")
blockchainServerGetFromClient = os.getenv("MQTT_TOPIC_IN_SERVER_CLIENT")
blockchainChain = os.getenv("MQTT_TOPIC_CHAIN")

CLIENT_ID = f"blockchainServer-{uuid.uuid4()}"
auth_name = os.getenv("AUTH_NAME_SERVER")
cert_key = os.getenv("CERT_KEY")
cert_pem = os.getenv("CERT_PEM")

blockchain = Blockchain()

block_in_path = os.path.join(json_dir, "server_block_in.json")
block_out_path = os.path.join(json_dir, "server_block_out.json")

def save_chain_to_file():
    with open(block_in_path, "w") as f:
        json.dump([b.to_dict() for b in blockchain.chain], f, indent=4)

def broadcast_chain(client):
    payload = {
        "type": "CHAIN",
        "chain": [b.to_dict() for b in blockchain.chain]
    }
    client.publish(blockchainChain, json.dumps(payload))
    print("[INFO] Broadcasted full blockchain")

def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print("[INFO] Connected successfully")
        client.subscribe(blockchainServerGet)
        client.subscribe(blockchainServerGetFromClient)
        client.subscribe(blockchainChain)

    else:
        print(f"[ERROR] Connection failed with code {rc}")

def on_message(client, userdata, msg):
    try:
        print(f"[INFO] Received MQTT message on topic: {msg.topic}")
        payload_str = msg.payload.decode()
        payload = json.loads(payload_str)

        if payload.get("type") == "CHAIN":
            remote = Blockchain()
            remote.chain = [Block.from_dict(b) for b in payload["chain"]]

            local_blockchain = Blockchain.choose_chain(local_blockchain, remote)
            print("[INFO] Local blockchain synchronized")
            return

        if msg.topic == blockchainServerGetFromClient:
            new_block = block_from_json(payload_str)

            if new_block.index == 0 and not blockchain.chain:
                blockchain.chain = [new_block]
                save_chain_to_file()
                broadcast_chain(client)
                print("[INFO] Genesis block set")
                return

            if blockchain.validate_block(new_block):
                blockchain.add_block(new_block)
                save_chain_to_file()
                broadcast_chain(client)
                print(f"[INFO] Block {new_block.index} added")
            else:
                print("[WARN] Invalid block rejected")

        elif msg.topic == blockchainServerGet:
            if "data" in payload:
                data_payload = payload["data"]
            else:
                data_payload = payload

            data = BlockData.from_dict(data_payload)
            new_block = blockchain.create_block(data)
            new_block.nonce = 0
            new_block.hash = ""

            client.publish(blockchainServerSend, block_to_json(new_block))
            print(f"[INFO] Created new block from analytics with index {new_block.index}")

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
print("[INFO] Server MQTT client connected, waiting for messages...")
client.loop_forever()