# USAGE
# python barcode_scanner_video.py

# import the necessary packages
#from imutils.video import VideoStream
from pyzbar import pyzbar
from imutils.video import VideoStream 
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
        try:
        #   vs = VideoStream(src=0).start()
           cam = VideoStream(src=0).start()
           print("[INFO] Camera started")
           #vs = VideoStream(usePiCamera=True).start()
           time.sleep(1.0)
       
           previousBarcode = " "
           # loop over the frames from the video stream
           while detectQR:
    	      # grab the frame from the threaded video stream and resize it to
    	      # have a maximum width of 400 pixels
              frame = cam.read()
              frame = imutils.resize(frame, width=400)
   #           print("[INFO] Loop started. Capturing live video now.")
              barcodeData = "No data"
              #cv2.imshow("Live video footage", frame)
    #          print("[INFO] Main frame opened.")
    	      # find the barcodes in the frame and decode each of the barcodes
              barcodes = pyzbar.decode(frame)
     #         print("[INFO] Created scanner.")
     #         print("[INFO] Decoding barcodes now")
    	      # loop over the detected barcodes
              for barcode in barcodes:
                 print("barcode detected")
                 barcodeData = str(barcode.data.decode("utf-8"))
                 barcodeType = barcode.type
                 print(barcodeData)
                 if barcodeData != "No data" and previousBarcode != barcodeData:
                    previousBarcode = barcodeData
                    self.controller_queue.put(("scanned_qr_code", barcodeData))
              print("[INFO] Done running")
        except:
             print("[ERROR] An error occured while scanning QRs from camera")
             cv2.destroyAllWindows()
             vs.stop()
