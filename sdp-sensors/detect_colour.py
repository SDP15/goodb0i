from tcpcom import TCPClient
from time import sleep
import threading
import datetime
import ev3dev.ev3 as ev3

server_ip = "129.215.2.37"
server_port = 5005

class colourTrack:

    def __init__(self):
        cl_1 = ev3.ColorSensor(ev3.INPUT_1)
        cl_2 = ev3.ColorSensor(ev3.INPUT_2)
        #cl.mode = 'COL_REFLECT'

        cl_1.connected
        cl_2.connected

        btn = ev3.Button

        readings = ("")
        readings_file = open('results.txt', 'w')

        i = 0




    def detect(self):
        # returns 1 if the colour detected is black. 0 otherwise
        while i<100:
           i+=1
           val = int(cl_1.value())
           val_1 = int(cl_2.value())
           if val==0 and val_1 == 0:
               print("BOTH NO COLOUR")
           elif val<5 and val_1<5:
               print("BOTH BLACK")
           elif val >=5 and val_1>=5:
               print("BOTH COLOUR")
           else:
               print("DIFFERENT COLOURS")

    def zero(self):
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
