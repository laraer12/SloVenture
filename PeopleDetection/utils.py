import os
import cv2

def load_images_from_dir(directory, max_images): #nalozi doloceno stevilo slik
    images = []

    valid_ext = (".jpg", ".jpeg", ".png")

    for filename in os.listdir(directory):
        if filename.lower().endswith(valid_ext):
            path = os.path.join(directory, filename)

            img = cv2.imread(path)
            if img is None:
                continue

            img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
            images.append((filename, img))

            if len(images) >= max_images:
                break

    return images