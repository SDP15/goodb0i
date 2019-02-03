#include <cassert>
#include <chrono>
#include <cstdint>
#include <iostream>
#include <memory>
#include <thread>
#include <unistd.h>

#include "ev3dev.h"
#include "ev3dev.cpp"
#include "util.hpp"

using namespace std;
using namespace ev3dev;

unique_ptr<evutil::Drive> leftDrive, rightDrive, steerDrive;
unique_ptr<ev3dev::color_sensor> leftColor, rightColor;
unique_ptr<ev3dev::ultrasonic_sensor> sonar;
unique_ptr<ev3dev::touch_sensor> wheelCalibrationSensor; 

void connectEv3Devices() {
  bool failed{false};
  leftDrive = make_unique<evutil::Drive>(OUTPUT_A, failed);
  leftDrive->setReversed();
  rightDrive = make_unique<evutil::Drive>(OUTPUT_B, failed);
  rightDrive->setReversed();
  steerDrive = make_unique<evutil::Drive>(OUTPUT_C, failed);

  leftColor = evutil::createConnectedDevice<ev3dev::color_sensor>(INPUT_1, failed);
  rightColor = evutil::createConnectedDevice<ev3dev::color_sensor>(INPUT_2, failed);
  
  sonar = evutil::createConnectedDevice<ev3dev::ultrasonic_sensor>(INPUT_3, failed);
  wheelCalibrationSensor = evutil::createConnectedDevice<ev3dev::touch_sensor>(INPUT_4, failed);

  if (failed) {
    throw runtime_error("Error initializing EV3 connections.");
  }
}

class Robot {
private:

public:
  Robot() {
    
  }

  void process() {
    if (button::up.pressed()) {
      leftDrive->runForever();
      rightDrive->runForever();
    } else if (button::down.pressed()) {
      leftDrive->runForever(-100);
      rightDrive->runForever(-100);
    } else {
      leftDrive->stop();
      rightDrive->stop();
    }

    if (button::left.pressed()) {
      steerDrive->runForever(-5);
    } else if (button::right.pressed()) {
      steerDrive->runForever(5);
    } else {
      steerDrive->stop();
    }
  }
};

int main() {
  try {
    connectEv3Devices();

    Robot robot{};
    while (!button::back.pressed()) {
      robot.process();
      this_thread::sleep_for(chrono::milliseconds(90));
    }
    return 0;
  } catch (const exception &err) {
    cerr << "[TERMINATE] Runtime error: " << err.what() << endl;
    return 1;
  } catch (exception *err) {
    cerr << "[TERMINATE] Runtime error: " << err->what() << endl;
    return 1;
  } catch (...) {
    cerr << "[TERMINATE] Unknown error occured" << endl;
    return 2;
  }
}
