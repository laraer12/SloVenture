import os
import ssl
import json
import uuid
from dotenv import load_dotenv
import paho.mqtt.client as mqtt
from main import detect_people_from_image
from convert_base64 import base64_to_cv2_image, cv2_to_base64

"""
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import pkcs12
"""

load_dotenv()

BROKER = os.getenv("MQTT_BROKER")
PORT = int(os.getenv("MQTT_PORT"))
TOPIC_IN = os.getenv("MQTT_TOPIC_IN")
TOPIC_OUT = os.getenv("MQTT_TOPIC_OUT")

CLIENT_ID = f"python-client-{uuid.uuid4()}"
AUTHNAME = "peopleDetection-authn-ID"

"""
P12_PATH = os.getenv("P12_PATH")
P12_PASSWORD = os.getenv("P12_PASSWORD")
"""

CERT_DIR = "certificates"
CERT_FILE = os.path.join(CERT_DIR, "client_cert.pem")
KEY_FILE = os.path.join(CERT_DIR, "client_key.pem")

latest_result_ref = {"data": None} # te podatke pokažem na dashboardu
analytics_result = {"data": None} # tej pa se pošljejo naprej, brez slike

"""
# funkcija s pomočjo katere pridobim ključ ter certifikat iz certifikata .p12, ker knjižnica paho-mqtt ne zna direkt brati .p12
with open(P12_PATH, "rb") as f:
    p12_data = f.read()

private_key, certificate, additional_certs = pkcs12.load_key_and_certificates(
    p12_data,
    P12_PASSWORD.encode()
)

# Shrani certificate in private key
with open("client_cert.pem", "wb") as f:
    f.write(certificate.public_bytes(serialization.Encoding.PEM))

with open("client_key.pem", "wb") as f:
    f.write(private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.TraditionalOpenSSL,
        encryption_algorithm=serialization.NoEncryption()
    ))
"""

# to se pokliče, ko se MQTT klient poveže z brokerjem
def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        print("Connected successfully", flush=True)
        client.subscribe(TOPIC_IN)
    else:
        print("Connection failed with code", rc, flush=True)

# to se pokliče, ko prejme sporočilo iz MQTT topica
def on_message(client, userdata, msg):
    try:
        print("Received MQTT message on topic:", msg.topic, flush=True)
        payload_str = msg.payload.decode()
        print("Raw payload:", payload_str[:200], "...", flush=True)
        payload = json.loads(payload_str)
        base64_img = payload.get("imageBase64")

        if not base64_img:
            print("No imageBase64 in payload", flush=True)
            return

        img = base64_to_cv2_image(base64_img)
        num_people, annotated_img = detect_people_from_image(img)

        annotated_b64 = cv2_to_base64(annotated_img)

        latest_result_ref["data"] = {
            "timestamp": payload["timestamp"],
            "latitude": payload["latitude"],
            "longitude": payload["longitude"],
            "numOfPeople": num_people,
            "imageBase64": annotated_b64
        }
        
        analytics_result["data"] = {
            "timestamp": payload["timestamp"],
            "latitude": payload["latitude"],
            "longitude": payload["longitude"],
            "numOfPeople": num_people
        }

        client.publish(
            TOPIC_OUT,
            json.dumps(analytics_result["data"])
        )
        print("Published analytics:", analytics_result, flush=True)

    except Exception as e:
        print("Error processing message:", e, flush=True)

def start_mqtt():
    client = mqtt.Client(client_id=CLIENT_ID, protocol=mqtt.MQTTv311)
    client.on_connect = on_connect
    client.on_message = on_message

    client.username_pw_set(AUTHNAME)

    client.tls_set(
        ca_certs=None,
        certfile=CERT_FILE,
        keyfile=KEY_FILE,
        tls_version=ssl.PROTOCOL_TLSv1_2
    )

    client.connect(BROKER, PORT)
    client.loop_forever()