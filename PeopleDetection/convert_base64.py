import cv2
import base64
import numpy as np

def base64_to_cv2_image(base64_string: str):
    image_bytes = base64.b64decode(base64_string)
    np_arr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

    if img is None:
        raise ValueError("Failed to decode image from base64")
    
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    return img

def cv2_to_base64(img):
    img_bgr = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)

    success, buffer = cv2.imencode(".png", img_bgr)
    if not success:
        raise ValueError("Failed to encode image to base64")

    return base64.b64encode(buffer).decode("utf-8")