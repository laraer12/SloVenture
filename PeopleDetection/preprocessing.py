import cv2
import numpy as np

def preprocess_image(img):
    #zmanjsanje suma
    img = cv2.GaussianBlur(img, (5, 5), 0)

    #boljsi kontrast
    lab = cv2.cvtColor(img, cv2.COLOR_RGB2LAB)
    l, a, b = cv2.split(lab)

    #Contrast Limited Adaptive Histogram Equalization
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    l = clahe.apply(l)

    #merganje kanalov nazaj
    lab = cv2.merge((l, a, b))
    img = cv2.cvtColor(lab, cv2.COLOR_LAB2RGB)

    return img
