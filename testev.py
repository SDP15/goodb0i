import ev3dev.ev3 as ev3
import time
import utilities as util

# Connect EV3 color sensor

def waitForMotor(motor):
    time.sleep(0.1)         # Make sure that motor has time to start
    while motor.state==["running"]:
        print('Motor is still running')
        time.sleep(0.1)

def runmotor():
    cl= ev3.ColorSensor(ev3.INPUT_1)
    # Put the color sensor into COL-REFLECT mode
    # to measure reflected light intensity.
    # In this mode the sensor will return a value between 0 and 100
    cl.mode='COL-REFLECT'

    ts = ev3.TouchSensor(ev3.INPUT_2)

    motor=ev3.LargeMotor('outA')
    motor.connected

    while True:

        if ts.value()==1 :
            if motor.state==["running"]:
                motor.stop()
            else:
                motor.run_timed(speed_sp=500, time_sp=5500)
            #stopmotor(motor)

def stopmotor(motor):
    #motor=ev3.LargeMotor(ev3.INPUT_1)
    motor.connected
    if motor.state==["running"]:
        motor.stop()


def recordUltraSonic():
    print("Record readings from ultrasonic")

    sonar = ev3.UltrasonicSensor(ev3.INPUT_4)
    sonar.connected
    sonar.mode = 'US-DIST-CM' # Will return value in mm

    readings = ("")
    readings_file = open('results.txt', 'w')

    while True:
    #readings = readings + str(sonar.value()) + '\n'
        print(str(sonar.value()) + '\n')# Will write to a text file in a column
