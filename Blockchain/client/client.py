import os
import ssl
import time
import json
import uuid
import subprocess
from dotenv import load_dotenv
import paho.mqtt.client as mqtt
from Blockchain.common.serialization import block_from_json, block_to_json, blockdata_to_json, blockdata_from_json
from Blockchain.common import Block, BlockData
from Blockchain.server.blockchain import Blockchain

json_dir = os.path.join("Blockchain", "json")
os.makedirs(json_dir, exist_ok=True)

load_dotenv()

local_blockchain = Blockchain()

broker = os.getenv("MQTT_BROKER")
port = int(os.getenv("MQTT_PORT"))

blockchainGet = os.getenv("MQTT_TOPIC_IN_CLIENT")
blockchainSend = os.getenv("MQTT_TOPIC_OUT_CLIENT")
blockchainChain = os.getenv("MQTT_TOPIC_CHAIN")

CLIENT_ID = f"blockchain-{uuid.uuid4()}"
auth_name = os.getenv("AUTH_NAME_CLIENT")

cert_key = os.getenv("CERT_KEY") 
cert_pem = os.getenv("CERT_PEM")

block_in_path = os.path.abspath(os.path.join(json_dir, "server_block_out.json"))
block_out_path = os.path.abspath(os.path.join(json_dir, "client_block_out.json"))

print("[DEBUG] block_in_path:", block_in_path)
print("[DEBUG] block_out_path:", block_out_path)

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

        last_block = local_blockchain.get_last_block()
        if "index" in payload:
            candidate_block = block_from_json(payload_str)

        else:
            data_payload = payload.get("data", payload)
            candidate_block = Block(
                index=0,
                previous_hash="0",
                timestamp=int(time.time()),
                difficulty=int(os.getenv("BLOCK_DIFF", 1)),
                data=BlockData.from_dict(data_payload),
                nonce=0,
                hash=""
            )

        with open(block_in_path, "w") as f:
            json.dump(candidate_block.to_dict(), f, indent=4)

        cmd = [
            os.getenv("MPI_EXEC"),
            "-n", os.getenv("MPI_NODES", "2"),
            os.getenv("MPI_PROG"),
            "-N", os.getenv("MPI_THREADS", "4"),
            "-diff", os.getenv("BLOCK_DIFF", "2"),
            block_in_path,
            block_out_path
        ]

        print("[INFO] Starting MPI mining...")
        result = subprocess.run(cmd)
        if result.returncode != 0:
            print("[ERROR] MPI mining failed:", result.stderr)
            return
        
        with open(block_out_path, "r") as f:
            mined_block_json = f.read()

        mined_block = block_from_json(mined_block_json)

        local_blockchain.add_block(mined_block)

        client.publish(blockchainSend, block_to_json(mined_block))
        print("[INFO] Mined block sent to server")

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