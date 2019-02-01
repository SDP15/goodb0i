#include "ev3dev.h"
#include <iostream>
#include <memory>
#include "ev3dev.cpp"
#include "colour_tracking.cpp"
#include "main.cpp"
#include <queue>
#include <string>


using namespace std;
using namespace ev3dev;

class FollowLine {

private:
    queue<String> instructions; // will be populated by messages received
};

control::control() :
     _motor_left(OUTPUT_A),
     _motor_right(OUTPUT_B),
     _sensor_colour(INPUT_1),
     _sensor_ir(INPUT_2)
     _terminate(false) {

}

/*int main() {
    Drive drive;
    drive.run_forever();
    return 0;
}*/

void onIntersectionDetected(string instruction) {
    if (instruction == "FORWARD") {
        leftDrive->runForever();
        rightDrive->runForever();
    } else if (instruction == "BACKWARDS") {
        leftDrive->runForever(-100);
        rightDrive->runForever(-100);
    } else if (instruction == "LEFT") {
        steerDrive->runForever(-5);
    } else {
        steerDrive->runForever(5)
    }
};

void runMotor() {
    leftDrive->runForever();
    rightDrive->runForever();

    int colour = colourTracking::detect();
};