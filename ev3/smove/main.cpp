#include <cassert>
#include <chrono>
#include <cstdint>
#include <curses.h>
#include <iostream>
#include <memory>
#include <thread>
#include <unistd.h>

#include "ev3dev.cpp"
#include "ev3dev.h"
#include "util.hpp"

using namespace std;
using namespace ev3dev;

char lastch;

class Robot {
private:
  unique_ptr<evutil::Drive> leftDrive, rightDrive, steerDrive;
  unique_ptr<ev3dev::ultrasonic_sensor> sonar;
  bool failed;
  int lspeed, rspeed;
  thread stopper;

public:
  Robot() {
    failed = false;
    leftDrive = make_unique<evutil::Drive>(OUTPUT_A, failed);
    rightDrive = make_unique<evutil::Drive>(OUTPUT_B, failed);
    steerDrive = make_unique<evutil::Drive>(OUTPUT_C, failed);
    sonar = evutil::createConnectedDevice<ev3dev::ultrasonic_sensor>(INPUT_4,
                                                                     failed);
    lspeed = 75;
    rspeed = 75;
    leftDrive->setReversed();
    rightDrive->setReversed();
    calibrateSteering();

    if (failed) {
      throw runtime_error("Error initializing Robot.");
    }

    stopper = thread([&]() {
      while (true) {
        if (sonar->distance_centimeters() < 60) {
          leftDrive->stop();
          rightDrive->stop();
        }
        this_thread::yield();
      }
    });
  }

  void calibrateSteering() {
    cout << "Calibrating steering..." << endl;
    cerr << "** STEERING CALIBRATION **" << endl;
    cerr << "* Phase 1: find left *" << endl;
    cerr << "start pos = " << steerDrive->getPosition() << endl;

    // Go to extreme left
    steerDrive->overrideStopAction(motor::stop_action_coast);
    steerDrive->runForever(-10); // run at low speed not to break anything
    this_thread::sleep_for(chrono::milliseconds{1200});
    steerDrive->stop();
    cerr << "end pos = " << steerDrive->getPosition() << endl;
    steerDrive->setPosition(0);

    cerr << "* Phase 2: find right *" << endl;
    // Get min and max touch positions

    steerDrive->runForever(10);
    this_thread::sleep_for(chrono::milliseconds{2000});
    steerDrive->stop();
    cerr << "r pos" << steerDrive->getPosition();
    int calibratedPos{steerDrive->getPosition() / 2};

    // Set zero point to middle.
    steerDrive->setPosition(steerDrive->getPosition() - calibratedPos);
    steerDrive->resetStopAction();
    // Center wheels
    steerDrive->runToPosition(0);
    steerDrive->waitUntilIdle();
    cerr << "** Wheels calibrated **" << endl;
    cout << "Steering calibrated" << endl;
  }

  void process() {
    if (lastch == 'w') {
      leftDrive->runForever(lspeed);
      rightDrive->runForever(rspeed);
    } else if (lastch == 's') {
      leftDrive->runForever(-lspeed);
      rightDrive->runForever(-rspeed);
    } else if (lastch == 'x') {
      leftDrive->stop();
      rightDrive->stop();
    }

    if (lastch == 'a') {
      steerDrive->runToDegree(-35);
      lspeed = 25;
      rspeed = 75;
    } else if (lastch == 'd') {
      steerDrive->runToDegree(35);
      lspeed = 75;
      rspeed = 25;
    } else if (lastch == 'z') {
      steerDrive->runToDegree(0);
      lspeed = 75;
      rspeed = 75;
    }
  }
};

int main() {
  try {
    initscr();
    noecho();
    cbreak();
    Robot robot{};
    while (!(button::back.pressed() || lastch == 'q')) {
      lastch = getch();
      robot.process();
      this_thread::sleep_for(chrono::milliseconds(90));
    }
    endwin();
    return 0;
  } catch (const exception &err) {
    cerr << "[TERMINATE] Runtime error: " << err.what() << endl;
    endwin();
    return 1;
  } catch (exception *err) {
    cerr << "[TERMINATE] Runtime error: " << err->what() << endl;
    endwin();
    return 1;
  } catch (...) {
    cerr << "[TERMINATE] Unknown error occured" << endl;
    endwin();
    return 2;
  }
}
