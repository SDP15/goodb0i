import ev3dev.ev3 as ev3
from time import sleep

def main():
    cl = ev3.ColorSensor(ev3.INPUT_1)
    cl.connected
    cl.mode = 'COL-REFLECT'

    gy = ev3.GyroSensor(ev3.INPUT_2)
    gy.connected
    gy.mode='GYRO-G&A'

    motor_l=ev3.LargeMotor('outA')
    motor_r=ev3.LargeMotor('outB')
    steer=ev3.LargeMotor('outC')

    motor_l.stop()
    motor_r.stop()
    steer.stop()
    btn = ev3.Button()

    readings_left = ("")
    readings_right = ("")
    readings_file = open('results.txt', 'w')

    detect(cl, motor_l, motor_r, steer, btn, gy)


def detect(cl, motor_l, motor_r, steer, btn, gy):
        # returns 1 if the colour detected is black. 0 otherwise
        print("test started")
        i = 0
        instr = "forward" # records the previous instruction
        while True:
            val = float(cl.value())
            print(val)
            print(gy.value())
            verifyAngle(gy, steer)
            sleep(0.05) # let's allow it time to rectify the angle

            if val<5:
                print("turns right")
                instr = "right"
                motor_l.run_forever(speed_sp=50)
                motor_r.run_forever(speed_sp=50)
                #steer.run_timed(time_sp=40, speed_sp=-5)
                steer.run_forever(speed_sp=-3)

            elif val>=14 and instr="left": # turn right
                motor_l.run_forever(speed_sp=50)
                motor_r.run_forever(speed_sp=50)
                steer.run_forever(speed_sp=-3)


            elif val>=14 and inst="right":
                motor_l.run_forever(speed_sp=j)
                motor_l.stop()
                motor_r.stop()
                steer.stop()

            elif val<=10:
                print("turns right")

                motor_l.run_forever(speed_sp=70)
                motor_r.run_forever(speed_sp=70)
                #steer.run_timed(time_sp=40, speed_sp=5)
                steer.run_forever(speed_sp=-3)


            else:
                print("moves forward")
                steer.stop()
                motor_l.run_forever(speed_sp=50)
                motor_r.run_forever(speed_sp=50)

                i = val

def verifyAngle(gy, steer):
    angle = gy.value()[0]

    while(angle>30):
        steer.run_timed(time_sp=1, speed_sp=-3)

    while(angle<-30):
        steer.run_timed(time_sp=1, speed_sp=3)



if __name__ == '__main__':
    main()
