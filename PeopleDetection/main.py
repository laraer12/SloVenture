from ultralytics import YOLO
from matplotlib import pyplot as plt
import cv2

model= YOLO("models\\weights\\best.pt")

img = cv2.imread("dataset\\images\\val\\d0c4d6e2-Messenger_creation_CE1B4DD1-8AC8-440D-BB89-D0AA2961BF36.jpeg")
img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

results = model(img, conf=0.31) #nastavitev confidence praga na 0.31

count=0
for r in results:
    if r.boxes is not None:
        for csl in r.boxes.cls:
            if int(csl)==0: #če je zaznana oseba
                count+=1


print("Number of detected people:", count)

annotated = results[0].plot()

plt.imshow(annotated)
plt.axis("off")
plt.title(f"Number of people: {count}")
plt.show()
