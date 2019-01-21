#include <cassert>
#include <chrono>
#include <cstdint>
#include <iostream>
#include <memory>
#include <thread>
#include <unistd.h>

#include "ev3dev.cpp"
#include "ev3dev.h"
#include "util.hpp"

using namespace std;
using namespace ev3dev;

class Robot {
private:
  unique_ptr<motor> drive;
  unique_ptr<touch_sensor> touch;
  bool failed;

public:
  Robot() {
    failed = false;
    drive = evutil::createConnectedDevice<medium_motor>(OUTPUT_A, failed);
    touch = evutil::createConnectedDevice<touch_sensor>(INPUT_1, failed);
    if (failed) {
      throw runtime_error("Error initializing Robot.");
    }
  }

  void process() {
    if (touch->is_pressed()) {
      drive->set_speed_sp(900).run_forever();
    } else {
      drive->set_stop_action("brake").stop();
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
