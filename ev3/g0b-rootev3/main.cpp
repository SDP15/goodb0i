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
using namespace std::chrono_literals;
using namespace ev3dev;

unique_ptr<evutil::Drive> leftDrive, rightDrive, steerDrive;
unique_ptr<evutil::ColorSensor> leftColor, midColor, rightColor;
unique_ptr<ev3dev::ultrasonic_sensor> sonar;

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
  rightDrive = make_unique<evutil::Drive>(OUTPUT_B, failed);
  leftDrive->setReversed();
  rightDrive->setReversed();

  steerDrive = make_unique<evutil::Drive>(OUTPUT_C, failed);

  leftColor = make_unique<evutil::ColorSensor>(INPUT_1, failed);
  midColor = make_unique<evutil::ColorSensor>(INPUT_2, failed);
  rightColor = make_unique<evutil::ColorSensor>(INPUT_3, failed);

  sonar =
      evutil::createConnectedDevice<ev3dev::ultrasonic_sensor>(INPUT_4, failed);
  sonar->set_mode(sonar->mode_us_dist_cm);

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
  int getForwardSpeed() { return 20; }

  void calibrateSteering() {
    cout << "Calibrating steering..." << endl;
    log() << "** STEERING CALIBRATION **" << endl;
    log() << "* Phase 1: find left *" << endl;
    log() << "start pos = " << steerDrive->getPosition() << endl;

    // Go to extreme left
    steerDrive->overrideStopAction(motor::stop_action_coast);
    steerDrive->runForever(-10); // run at low speed not to break anything
    this_thread::sleep_for(1200ms);
    steerDrive->stop();
    log() << "end pos = " << steerDrive->getPosition() << endl;
    steerDrive->setPosition(0);

    log() << "* Phase 2: find right *" << endl;

    steerDrive->runForever(10);
    this_thread::sleep_for(2000ms);
    steerDrive->stop();
    log() << "r pos" << steerDrive->getPosition() << endl;
    int calibratedPos{steerDrive->getPosition() / 2};

    // Set zero point to middle.
    steerDrive->setPosition(steerDrive->getPosition() - calibratedPos);
    steerDrive->resetStopAction();
    // Center wheels
    steerDrive->runToPosition(0);
    steerDrive->waitUntilIdle();
    log() << "** Wheels calibrated **" << endl;
    cout << "Steering calibrated" << endl;
  }

  enum class Direction { left, right } lastLineDir;
  int avgLineAngle{0};

public:
  Robot() { calibrateSteering(); }

  void process() {
    // simple line follow
    leftColor->update();
    midColor->update();
    rightColor->update();
    evutil::Color lcol{leftColor->getColor()};
    evutil::Color mcol{midColor->getColor()};
    evutil::Color rcol{rightColor->getColor()};
    auto rrcol{rightColor->getRawRGB()};
    cerr << (int)lcol << " " << (int)rcol << " " << rrcol.x << " " << rrcol.y
         << " " << rrcol.z << endl;
    constexpr int STEER_ANGLE{45};
    constexpr int SMALL_STEER_ANGLE{30};
    constexpr int SONAR_STOP_DISTANCE{45};

    if (sonar->distance_centimeters() < SONAR_STOP_DISTANCE) {
      leftDrive->stop();
      rightDrive->stop();
    } else {
      int lineAngle{};
      if (mcol == evutil::Color::line) {
        if (lcol == evutil::Color::line && rcol != evutil::Color::line) {
          // LMr -> adjust slightly left
          lineAngle = -SMALL_STEER_ANGLE;
          lastLineDir = Direction::left;
        } else if (lcol != evutil::Color::line && rcol == evutil::Color::line) {
          // lMR -> adjust slightly right
          lineAngle = SMALL_STEER_ANGLE;
          lastLineDir = Direction::right;
        } else {
          // lMr/LMR -> going straight
          lineAngle = 0;
        }
      } else {
        if (lcol == evutil::Color::line && rcol != evutil::Color::line) {
          // Lmr -> adjust left
          lineAngle = -STEER_ANGLE;
          lastLineDir = Direction::left;
        } else if (lcol != evutil::Color::line && rcol == evutil::Color::line) {
          // lmR -> adjust right
          lineAngle = STEER_ANGLE;
          lastLineDir = Direction::right;
        } else {
          // lmr (LmR would be a bug) -> turn to the predicted line
          lineAngle =
              (lastLineDir == Direction::left) ? -STEER_ANGLE : STEER_ANGLE;
        }
      }
      // Rolling average to smooth out steering
      avgLineAngle = (avgLineAngle + 7 * lineAngle) / 8;
      steerDrive->runToDegree(avgLineAngle);
      leftDrive->runForever(avgLineAngle < 0 ? getForwardSpeed() / 2
                                             : getForwardSpeed());
      rightDrive->runForever(avgLineAngle > 0 ? getForwardSpeed() / 2
                                              : getForwardSpeed());
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
    evutil::ColorCalibration::loadFromFile();
    connectEv3Devices();
    openLogFile();

    Robot robot{};
    while (!(sigintTerminate || button::back.pressed())) {
      robot.process();
      this_thread::sleep_for(15ms);
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
