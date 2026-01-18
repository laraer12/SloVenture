import time
import json
from threading import Thread
from flask import Flask, jsonify, render_template, Response
from mqtt_client import start_mqtt, latest_result_ref, analytics_result

app = Flask(__name__)

@app.route("/")
def home():
    return render_template("index.html")

@app.route("/latest")
def latest():
    if not latest_result_ref["data"]:
        return jsonify({"status": "no data yet"})
    return jsonify(latest_result_ref["data"])

@app.route("/analytics")
def analytics():
    if not analytics_result["data"]:
        return jsonify({"status": "no data yet"})
    return jsonify(analytics_result["data"])

# za sse (server sent events), stran se mi zaradi tega osveži ko dobim nove podatke
@app.route("/stream")
def stream():
    def event_stream(): # generator, sproti pošilja podatke
        last_data = None
        while True:
            data = latest_result_ref["data"]
            if data != last_data:
                last_data = data
                yield f"data: {json.dumps(data)}\n\n"
            time.sleep(0.1)
    return Response(event_stream(), mimetype="text/event-stream")

if __name__ == "__main__":
    Thread(target=start_mqtt, daemon=True).start()
    app.run(host="0.0.0.0", port=5000, debug=True, use_reloader=False)