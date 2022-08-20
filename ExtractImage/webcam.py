import cv2
import os,os.path

import imutils as imutils

version = '_4'
_, _, files = next(os.walk("image"))
file_count = len(files)

webCam = cv2.VideoCapture(0)
currentframe = file_count+1
delay = 0


while (True):
    success, frame = webCam.read()
    dim = (860, 1020)
    frame = cv2.resize(frame, dim)
    # frame = imutils.resize(frame, width=820, height=800)
    frame = frame[0:1020, 0:620]
    # Save Frame by Frame into disk using imwrite method
    cv2.imshow("Output", frame)
    if delay == 10:
        path = './image/Frame' + str(currentframe) + version + '.jpg'
        cv2.imwrite(path, frame)
        delay = 0
        currentframe += 1

    delay += 1
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break



webCam.release()
cv2.destroyAllWindows()