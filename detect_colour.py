from tcpcom import TCPClient
from time import sleep
import threading
import datetime
import ev3dev.ev3 as ev3

server_ip = "129.215.2.37"
server_port = 5005

class colourTrack:

    def __init__():
        cl = ev3.ColorSensor(ev3.INPUT_1)
        #cl.mode = 'COL_REFLECT'

        cl.connected

        btn = ev3.Button

        readings = ("")
        readings_file = open('results.txt', 'w')

        i = 0

        def zero():
           print("No colour")
        def one():
           print("BLACK")
        def two():
           print("COLOUR")


    def detect(self):
        while i<100:
           val = int(cl.value())
           if val==0:
               zero()
           elif val<5:
               one()
           elif val >=5:
               two()





       #readings = readings = readings + val
       #readings_file.write(readings)
       #readings_file.close()
       ++i
