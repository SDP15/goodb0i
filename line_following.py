from tcpcom import TCPClient
from time import sleep
import threading
import datetime
from detect_colour import colourTrack


server_ip = "172.20.113.221" #TODO: my laptop's IP. To be changed later
server_port = 5005

# The server will send instructions as a series of messages that will be stored in
# a FIFO queue. The elements of the queue only get consumed when the robot gets to
# an intersection

instructions = Queue()

class Queue:
    def __init__(self):
        self.items = []

    def isEmpty(self):
        return self.items == []

    def enqueue(self, item):
        self.items.insert(0,item)

    def dequeue(self):
        return self.items.pop()

    def size(self):
        return len(self.items)


def onStateChanged(state, msg):
    global isConnected

    if state == "LISTENING":
        print("DEBUG: Client:-- Listening...")
        client.sendMessage("Client is listening.")

    elif state == "CONNECTED":
        isConnected = True
        print("DEBUG: Client:-- Connected to ", msg)
        client.sendMessage("Client is connected.")

    elif state == "DISCONNECTED":
        isConnected = False
        print("DEBUG: Client:-- Connection lost.")
        main()

    elif state == "MESSAGE":
        print("DEBUG: Client:-- Instruction received: ", msg)
        instructions.enqueue(msg)

def runMotor(cl, motor_1, motor_2):

    motor_1.connected
    motor_2.connected

    while True:

        if motor_1.state==["running"] && motor_2.state==["running"]:
            motor.run_forever()

        detectLines()


def turnMotor(direction, motor):
    #TODO: implement this function to make the rear wheels of the robot turn
    colourTrack.detect()  

def onIntersectionDetected(motor):
    motor.stop()
    direction = instructions.dequeue()
    if direction == "left" || direction == "right":
       turnMotor(direction, motor)
    elif direction == "forward":
       runMotor(motor)
    else: # if the instruction is not known
       main()


def main():
    cl = ev3.ColorSensor(ev3.INPUT_1)
    cl.mode = 'COL_REFLECT'

    motor_1=ev3.LargeMotor('outA')
    motor_2=ev3.LargeMotor('outB')

    runMotor(cl, motor_1, motor_2)
