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

class Robot {
private:
  unique_ptr<evutil::Drive> leftDrive, rightDrive, steerDrive;
  bool failed;

public:
  Robot() {
    failed = false;
    leftDrive = make_unique<evutil::Drive>(OUTPUT_A, failed);
    rightDrive = make_unique<evutil::Drive>(OUTPUT_B, failed);
    steerDrive = make_unique<evutil::Drive>(OUTPUT_C, failed);
    leftDrive->setReversed();
    rightDrive->setReversed();

    if (failed) {
      throw runtime_error("Error initializing Robot.");
    }
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
      steerDrive->runForever(-50);
    } else if (button::right.pressed()) {
      steerDrive->runForever(50);
    } else {
      steerDrive->stop();
    }
  }
};

int main() {
  try {
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
