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
using namespace evutil;

int main() {
  try {
    ColorCalibration::loadFromFile();
  } catch (...) {
  }
  bool fail{false};
  ColorSensor cs1{INPUT_1, fail};
  {
    // make clear which sensor is used
    auto cs2{createConnectedDevice<color_sensor>(INPUT_2, fail)};
    cs2->set_mode(color_sensor::mode_ref_raw);
  }
  {
    // make clear which sensor is used
    auto cs3{createConnectedDevice<color_sensor>(INPUT_3, fail)};
    cs3->set_mode(color_sensor::mode_ref_raw);
  }
  if (fail) {
    cerr << "Failed connecting to light sensors." << endl;
    return 1;
  }
  do {
    cout << "Which color to calibrate: bg line turnLeft turnRight turnBoth "
            "save cancel"
         << endl;
    string cmd;
    cin >> cmd;
    auto rgb = cs1.getRawRGB();
    cout << rgb.x << " " << rgb.y << " " << rgb.z << endl;
    cs1.update();
    if (cmd == "save") {
      ColorCalibration::saveToFile();
      break;
    } else if (cmd == "cancel") {
      break;
    } else if (cmd == "bg") {
      ColorCalibration::bg = rgb;
    } else if (cmd == "line") {
      ColorCalibration::line = rgb;
    } else if (cmd == "turnLeft") {
      ColorCalibration::turnLeft = rgb;
    } else if (cmd == "turnRight") {
      ColorCalibration::turnRight = rgb;
    } else if (cmd == "turnBoth") {
      ColorCalibration::turnBoth = rgb;
    } else {
      cout << "Unknown command" << endl;
    }
  } while (1);
  return 0;
}
