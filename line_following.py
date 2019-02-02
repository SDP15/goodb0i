from tcpcom import TCPClient
from time import sleep
import threading
import datetime
from detect_colour import colourTrack
import ev3dev.ev3 as ev3


server_ip = "172.20.122.166" #TODO: my laptop's IP. To be changed later
server_port = 5006

# The server will send instructions as a series of messages that will be stored in
# a FIFO queue. The elements of the queue only get consumed when the robot gets to
# an intersection


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

instructions = Queue()

isRunning = False


def onStateChanged(state, msg):
    global isConnected
    global isRunning

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
        if(msg=="STOP"):
            stopRobot()
            #main()
        elif(msg=="START"):
            startRobot()
            #main()
        else:
            instructions.enqueue(msg)


def runMotor(cl, motor_l, motor_r):
    # not quite sure yet of what to do with this
    motor_l.connected
    motor_r.connected

    motor_l.stop()
    motor_r.stop()

    while True:

        if motor_l.state==["running"] and motor_r.state==["running"]:
            motor_l.run_forever()
            motor_r.run_forever()

        colourTrack.detect()

def startRobot():
    motor_l=ev3.LargeMotor('outA')
    motor_r=ev3.LargeMotor('outB')
    steer=ev3.LargeMotor('outC')

    trackStart(motor_l, motor_r)

def trackStart(motor_l, motor_r):
    motor_l.connected
    motor_r.connected


    while(True):
        ("start mode triggered")
        motor_l.run_forever(speed_sp=5)
        motor_r.run_forever(speed_sp=5)
        turnAtIntersection(motor_l, motor_r)

def stopRobot():
    motor_l=ev3.LargeMotor('outA')
    motor_r=ev3.LargeMotor('outB')
    steer=ev3.LargeMotor('outC')

    motor_l.stop()
    motor_r.stop()
    steer.stop()


def onIntersectionDetected(motor_l, motor_r):
    motor_l.stop()
    motor_r.stop()

    direction = instructions.dequeue()
    if direction == "left" or direction == "right":
       turnMotor(direction, motor)
    elif direction == "forward":
       runMotor(motor)
    else: # if the instruction is not known
       main()

def turnAtIntersection(motor_l, motor_r):
    colourTracking = colourTrack()
    while(isRunning):
        val = colourTracking.detect()
        if val == 1: # the right-hand sensor detects black
           motor_l.stop()
           motor_l.run_forever(speed_sp=5)
           motor_r.stop()
           motor_r.run_forever(speed_sp=5)
           steer.run_timed(time_sp=10, speed_sp=5)
        elif val == 2: # the left-hand sensor detects black
           motor_l.stop()
           motor_l.run_forever(speed_sp=5)
           motor_r.stop()
           motor_r.run_forever(speed_sp=5)
           steer.run_timed(time_sp=10, speed_sp=-5)


def main():
    cl = ev3.ColorSensor(ev3.INPUT_1)
    #cl.mode = 'COL_REFLECT'
    global client

    client = TCPClient(server_ip, server_port, stateChanged=onStateChanged)
    print("Client starting")
    isRunning = False

    try:
        while True:
            rc = client.connect()
            sleep(0.01)
            if rc:
                isConnected = True

                while isConnected:
                    sleep(0.001)
                    #print("waiting")
            else:
                print("Client:-- Connection failed")

    except KeyboardInterrupt:
        print("Bye.")

    # mission done; close connection
    #

    #client.disconnect()
    #print("Bye")
    #threading.cleanup_stop_thread()  # needed if we want to restart the client



if __name__ == '__main__':
     main()
