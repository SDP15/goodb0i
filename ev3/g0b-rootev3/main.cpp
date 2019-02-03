#include <array>
#include <cassert>
#include <chrono>
#include <cstdint>
#include <fstream>
#include <iostream>
#include <memory>
#include <signal.h>
#include <thread>
#include <unistd.h>

#include "ev3dev.cpp"
#include "ev3dev.h"
#include "util.hpp"

using namespace std;
using namespace ev3dev;

unique_ptr<evutil::Drive> leftDrive, rightDrive, steerDrive;
unique_ptr<ev3dev::color_sensor> leftColor, rightColor;
unique_ptr<ev3dev::ultrasonic_sensor> sonar;
unique_ptr<ev3dev::touch_sensor> wheelCalibrationSensor;

ofstream sensorLog;

/// Helper function to log messages with timestamps.
/// Usage: log() << "My log message" << endl;
ostream &log() {
  auto now{time(nullptr)};
  array<char, 100> timeStr;
  size_t timeStrLen{strftime(timeStr.data(), timeStr.size(), "%T ",
                             localtime(&now))}; // DevSkim: ignore DS154189
  sensorLog.write(timeStr.data(), timeStrLen);
  // return sensorLog;
  return cerr;
}

void connectEv3Devices() {
  bool failed{false};
  leftDrive = make_unique<evutil::Drive>(OUTPUT_A, failed);
  leftDrive->setReversed();
  rightDrive = make_unique<evutil::Drive>(OUTPUT_B, failed);
  rightDrive->setReversed();
  steerDrive = make_unique<evutil::Drive>(OUTPUT_C, failed);

  leftColor =
      evutil::createConnectedDevice<ev3dev::color_sensor>(INPUT_1, failed);
  rightColor =
      evutil::createConnectedDevice<ev3dev::color_sensor>(INPUT_2, failed);

  sonar =
      evutil::createConnectedDevice<ev3dev::ultrasonic_sensor>(INPUT_3, failed);
  wheelCalibrationSensor =
      evutil::createConnectedDevice<ev3dev::touch_sensor>(INPUT_4, failed);

  if (failed) {
    throw runtime_error("Error initializing EV3 connections.");
  }
}

void openLogFile() {
  auto now{time(nullptr)};
  array<char, 100> logFilename;
  size_t logNameLen{strftime(logFilename.data(), logFilename.size(),
                             "zlog-g0b-%F-%H-%M-%S.log",
                             localtime(&now))}; // DevSkim: ignore DS154189
  string logName{logFilename.data(), logNameLen};
  sensorLog = ofstream(logName, ios_base::out | ios_base::app);
  if (!sensorLog.good()) {
    throw runtime_error("Could not open logfile.");
  }
}

class Robot {
private:
public:
  Robot() { calibrateSteering(); }

  void calibrateSteering() {
    cout << "Calibrating steering..." << endl;
    log() << "** STEERING CALIBRATION **" << endl;
    log() << "* Phase 1: find left *" << endl;
    log() << "start pos = " << steerDrive->getPosition() << endl;

    // Go to extreme left
    steerDrive->overrideStopAction(motor::stop_action_coast);
    steerDrive->runForever(-10); // run at low speed not to break anything
    this_thread::sleep_for(chrono::milliseconds{1200});
    steerDrive->stop();
    log() << "end pos = " << steerDrive->getPosition() << endl;
    steerDrive->setPosition(0);

    // Make sure sensor mode is set for short reaction times.
    wheelCalibrationSensor->is_pressed(true);

    log() << "* Phase 2: find minmax *" << endl;
    // Get min and max touch positions
    int minTouchPos, maxTouchPos;
    {
      steerDrive->stop();
      this_thread::yield();
      // Keep this number high, otherwise the motor turns very poorly and leads
      // to very inaccurate results
      steerDrive->runForever(40);
      // wait for it to reach the touch sensor
      while (!wheelCalibrationSensor->is_pressed(false)) {
        this_thread::yield();
      }
      minTouchPos = steerDrive->getPosition();
      // and then for it to not be pressed anymore
      while (wheelCalibrationSensor->is_pressed(false)) {
        this_thread::yield();
      }
      maxTouchPos = steerDrive->getDegrees();
    }
    steerDrive->stop();
    int calibratedPos{(minTouchPos + maxTouchPos) / 2};
    log() << "min pos = " << minTouchPos << endl;
    log() << "max pos = " << maxTouchPos << endl;
    log() << "cal pos = " << calibratedPos << endl;
    // Set zero point to middle.
    steerDrive->setPosition(steerDrive->getPosition() - calibratedPos);
    steerDrive->resetStopAction();
    // Center wheels
    steerDrive->runToPosition(0);
    steerDrive->waitUntilIdle();
    log() << "** Wheels calibrated **" << endl;
    cout << "Steering calibrated" << endl;
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

/// Emergency motor disablement
void disableMotors() {
  array<evutil::Drive *, 3> motors{
      {leftDrive.get(), rightDrive.get(), steerDrive.get()}};
  for (auto &drive : motors) {
    if (drive && drive->getMotor() && drive->getMotor()->connected()) {
      drive->overrideStopAction(motor::stop_action_coast);
      drive->stop();
    }
  }
}

volatile bool sigintTerminate{false};

void sigintHandler(int sig) {
  if (sig == SIGINT) {
    sigintTerminate = true;
  }
}

int main() {
  try {
    if (signal(SIGINT, &sigintHandler) == SIG_ERR) {
      cerr << "Could not register SIGINT handler, continuing..." << endl;
    }
    connectEv3Devices();
    openLogFile();

    Robot robot{};
    while (!(sigintTerminate || button::back.pressed())) {
      robot.process();
      this_thread::yield();
    }
    disableMotors();
    return 0;
  } catch (const exception &err) {
    cerr << "[TERMINATE] Runtime error: " << err.what() << endl;
    disableMotors();
    return 1;
  } catch (exception *err) {
    cerr << "[TERMINATE] Runtime error: " << err->what() << endl;
    disableMotors();
    return 1;
  } catch (...) {
    cerr << "[TERMINATE] Unknown error occured" << endl;
    disableMotors();
    return 2;
  }
}
