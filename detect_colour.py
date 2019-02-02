from tcpcom import TCPClient
from time import sleep
import threading
import datetime
import ev3dev.ev3 as ev3

server_ip = "129.215.2.37"
server_port = 5005

class colourTrack:

    def __init__(self):
        cl_r = ev3.ColorSensor(ev3.INPUT_1)
        cl_l = ev3.ColorSensor(ev3.INPUT_2)
        #cl.mode = 'COL_REFLECT'

        cl_r.connected
        cl_l.connected

        btn = ev3.Button

        readings = ("")
        readings_file = open('results.txt', 'w')

        i = 0


    def detect(self):
        # returns 1 if the colour detected is black. 0 otherwise

        i = 0
        while i<100:
           val = int(cl_r.value())
           val_1 = int(cl_l.value())
           i = i+1
           if val==0 and val==0:
               print("Both no colour")
               return 0
           elif val<5 and val_1<5:
               print("Both black")
               return 0
           elif val <5:
               print("First black, second colour or none")
               return 1
           elif val_1<5:
               print("Second black, first colour or none")
               return 2
           else:
               print("Other case")
               return 0

    def zero():
       print("No colour")
       return 0
    def one():
       print("BLACK")
       return 1
    def two():
       print("COLOUR")
       return 1




       #readings = readings = readings + val
       #readings_file.write(readings)
       #readings_file.close()
       ++i
