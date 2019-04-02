import threading
import collections
import RPi.GPIO as GPIO

from pyzbar import pyzbar
from imutils.video import VideoStream 
import argparse
import datetime
import imutils
import time
import cv2

from utils.logger import log

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
    def __init__(self, name, controller_queue, event_flag, continue_flag):
        threading.Thread.__init__(self, name=name)
        self.controller_queue = controller_queue
        self.prev_command = "start"
        self.event_flag = event_flag
        self.continue_flag = continue_flag
        self.circular_buffer = collections.deque([0,0,0],maxlen=3)
        self.pin = 24

        GPIO.setmode(GPIO.BCM)
        GPIO.setup(self.pin, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

    def run(self):
        while True:
            # Get input and append it to end of buffer
            input = GPIO.input(self.pin)
            self.circular_buffer.append(input)
            time.sleep(0.025)

            # Check if buffer is full of 1s (indicating button press)
            if self.circular_buffer.count(1) == 3:
                button_press = True
            else:
                button_press = False

            if not self.event_flag.isSet() and button_press:
                if self.prev_command == "start":
                    self.controller_queue.put(("send_message", "stop"))
                    self.prev_command = "stop"
                elif self.prev_command == "stop":
                    self.controller_queue.put(("send_message", "start"))
                    self.prev_command = "start"
                time.sleep(0.25)

            if not self.continue_flag.isSet() and button_press:
                self.controller_queue.put(("send_message","resume-from-stop-marker"))
                self.prev_command = "start"
                self.continue_flag.set()
                time.sleep(0.25)
                

class QRThread(threading.Thread):
    def __init__(self, name, controller_queue):
        log("QR thread starting")
        threading.Thread.__init__(self, name=name)
        self.controller_queue = controller_queue
        self.prev_qr = ""

    def run(self,detectQR = True):
        log("[INFO] starting video stream...")
        try:
            cam = VideoStream(src=0).start()
            log("[INFO] Camera started")
            time.sleep(1.0)

            while detectQR:
                frame = cam.read()
                frame = imutils.resize(frame, width=400)
                barcodeData = "No data"
                barcodes = pyzbar.decode(frame)
                for barcode in barcodes:
                    barcodeData = str(barcode.data.decode("utf-8"))
                    log(barcodeData)
                    if barcodeData != "No data" and self.prev_qr != barcodeData:
                        self.prev_qr = barcodeData
                        self.controller_queue.put(("scanned_qr_code", barcodeData))
        except:
            log("[ERROR] An error occured while scanning QRs from camera")
            cam.stop()





