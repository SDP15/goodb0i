import threading
import RPi.GPIO as GPIO

from pyzbar import pyzbar
from imutils.video import VideoStream 
import argparse
import datetime
import imutils
import time
import cv2

class WorkerThread(threading.Thread):
    def __init__(self, name, object_instance, work_queue, event_flag=None):
        threading.Thread.__init__(self, name=name)
        self.obj_instance = object_instance
        self.work_queue = work_queue
        self.event_flag = event_flag

    def run(self):
        while True:
            items = self.work_queue.get()
            args = ""
            if type(items) == tuple:
                func = items[0]
                args = items[1:]
            else:
                func = items

            function_to_call = getattr(self.obj_instance, func)

            if len(args) > 0:
                function_to_call(*args)
            else:
                function_to_call()

            if self.event_flag:
                self.event_flag.set()

class ButtonThread(threading.Thread):
    def __init__(self, name, controller_queue, event_flag):
        threading.Thread.__init__(self, name=name)
        self.controller_queue = controller_queue
        self.prev_command = "start"
        self.event_flag = event_flag

        GPIO.setmode(GPIO.BCM)
        GPIO.setup(27, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

    def run(self):
        GPIO.add_event_detect(27, GPIO.RISING, callback=self.button_callback, bouncetime=1000)
        while True:
            pass
    
    def button_callback(self, channel):
        if not self.event_flag.isSet():
            if self.prev_command == "start":
                self.controller_queue.put(("send_message", "stop", "ev3=True"))
                self.prev_command = "stop"
            elif self.prev_command == "stop":
                self.controller_queue.put(("send_message", "start", "ev3=True"))
                self.prev_command = "start"
                

class QRThread(threading.Thread):
    def __init__(self, name, controller_queue):
        print("QR thread starting")
        threading.Thread.__init__(self, name=name)
        self.controller_queue = controller_queue
        self.prev_qr = ""

    def run(self,detectQR = True):
        print("[INFO] starting video stream...")
        try:
            cam = VideoStream(src=0).start()
            print("[INFO] Camera started")
            time.sleep(1.0)

            while detectQR:
                frame = cam.read()
                frame = imutils.resize(frame, width=400)
                barcodeData = "No data"
                barcodes = pyzbar.decode(frame)
                for barcode in barcodes:
                    barcodeData = str(barcode.data.decode("utf-8"))
                    print(barcodeData)
                    if barcodeData != "No data" and self.prev_qr != barcodeData:
                        self.prev_qr = barcodeData
                        self.controller_queue.put(("scanned_qr_code", barcodeData))
        except:
            print("[ERROR] An error occured while scanning QRs from camera")
            cam.stop()





