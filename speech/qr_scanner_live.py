# USAGE
# python barcode_scanner_video.py

# import the necessary packages
from imutils.video import VideoStream
from pyzbar import pyzbar
import argparse
import datetime
import imutils
import time
import cv2
import serial
from threading import Thread
import socket
import threading


class QRDetection:
    def __init__(self, controller_queue):
        self.controller_queue = controller_queue
        
        t1 = threading.Thread(name="ScanningThread", target=self.track_codes)
        t1.start()

    def track_codes(self,detectQR = True):

        # initialize the video stream and allow the camera sensor to warm up
        print("[INFO] starting video stream...")
        vs = VideoStream(src=0).start()
        #vs = VideoStream(usePiCamera=True).start()
        time.sleep(2.0)

        previousBarcode = " "
        # loop over the frames from the video stream
        while detectQR:
    	   # grab the frame from the threaded video stream and resize it to
    	   # have a maximum width of 400 pixels
           frame = vs.read()
           frame = imutils.resize(frame, width=400)

           barcodeData = "No data"

    	   # find the barcodes in the frame and decode each of the barcodes
           barcodes = pyzbar.decode(frame)

    	   # loop over the detected barcodes
           for barcode in barcodes:
                print("barcode detected")
                barcodeData = str(barcode.data.decode("utf-8"))
                barcodeType = barcode.type

                if barcodeData != "No data" and previousBarcode != barcodeData:
                    previousBarcode = barcodeData
                    self.controller_queue.put(("scanned_qr_code", barcodeData))
