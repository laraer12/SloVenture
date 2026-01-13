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