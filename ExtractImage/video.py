import cv2
import os,os.path

version = '_5'
_, _, files = next(os.walk("image"))
file_count = len(files)

filename = 'เปลี่ยนเลขทะเบียนรถยนต์ ต้องทำอย่างไง แล้วเลขจะสวยถูกใจเพื่อนๆ หรือเปล่า มาดูกัน  รถซิ่งไทยแลนด์.mp4'
fullpath = 'video/'+filename
vid = cv2.VideoCapture(fullpath)
delay = 0
try:
    if not os.path.exists('data'):
        os.makedirs('data')
except OSError:
    print('Error: Creating directory of data')

currentframe = file_count+1

while (True):

    success, frame = vid.read()

    if success:

        if delay == 150:
            path = './image/Frame' + str(currentframe) + version + '.jpg'
            cv2.imwrite(path, frame)
            delay = 0
            currentframe += 1

    else:
        break
    delay+=1
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# Release all space and windows once done
vid.release()
cv2.destroyAllWindows()