"""
from ultralytics import YOLO
from matplotlib import pyplot as plt
import cv2

from preprocessing import preprocess_image
from utils import load_images_from_dir

model= YOLO("models\\weights\\best.pt")

image_dir = "dataset\\images\\val"
num_images = 5

images = load_images_from_dir(image_dir, num_images)

for filename, img in images:

    img = preprocess_image(img)

    results = model(img, conf=0.31)

    count = 0
    for r in results:
        if r.boxes is not None:
            for csl in r.boxes.cls:
                if int(csl) == 0:
                    count += 1

    print(f"{filename} → detected people: {count}")

    annotated = results[0].plot()

    plt.imshow(annotated)
    plt.axis("off")
    plt.title(f"{filename} | People: {count}")
    plt.show()
"""

import os
from ultralytics import YOLO
from dotenv import load_dotenv
from preprocessing import preprocess_image

load_dotenv()

MODEL_PATH = os.getenv("MODEL_PATH")

model = YOLO(MODEL_PATH)

def detect_people_from_image(img):
    img = preprocess_image(img)

    results = model(img, conf = 0.31)

    count = 0
    for r in results:
        if r.boxes is not None:
            for csl in r.boxes.cls:
                if int(csl) == 0:
                    count += 1

    return count