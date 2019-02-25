
#include "ev3dev.h"
#include <fstream>
#define GLM_ENABLE_EXPERIMENTAL
#include <glm/glm.hpp>
#include <glm/gtx/color_space.hpp>
#include <iostream>
#include <memory>

namespace evutil {

template <class T>
std::unique_ptr<T> createConnectedDevice(ev3dev::address_type addr,
                                         bool &failFlag) {
  auto dev = std::make_unique<T>(addr);
  if (!dev->connected()) {
    std::cerr << "[FAIL] Device not connected: " << typeid(T).name() << " at "
              << addr << std::endl;
    failFlag = true;
  }
  return dev;
}

enum class Color { bg, line, turnLeft, turnRight, turnBoth, count };
static const Color Colors[]{Color::bg, Color::line, Color::turnRight};

namespace ColorCalibration {
static constexpr const char *const CAL_FILE = "zcal-color.cfg";
static glm::ivec3 bg, line, turnLeft, turnRight, turnBoth;

inline void loadFromFile() {
  std::ifstream F{CAL_FILE};
  F >> bg.x >> bg.y >> bg.z;
  F >> line.x >> line.y >> line.z;
  F >> turnLeft.x >> turnLeft.y >> turnLeft.z;
  F >> turnRight.x >> turnRight.y >> turnRight.z;
  F >> turnBoth.x >> turnBoth.y >> turnBoth.z;
}

inline void saveToFile() {
  std::ofstream F{CAL_FILE};
  F << bg.x << " " << bg.y << " " << bg.z << std::endl;
  F << line.x << " " << line.y << " " << line.z << std::endl;
  F << turnLeft.x << " " << turnLeft.y << " " << turnLeft.z << std::endl;
  F << turnRight.x << " " << turnRight.y << " " << turnRight.z << std::endl;
  F << turnBoth.x << " " << turnBoth.y << " " << turnBoth.z << std::endl;
  F.flush();
  F.close();
}

inline glm::ivec3 &getRGBForColor(Color c) {
  switch (c) {
  case Color::bg:
    return bg;
  case Color::line:
    return line;
  case Color::turnLeft:
    return turnLeft;
  case Color::turnRight:
    return turnRight;
  case Color::turnBoth:
    return turnBoth;
  }
  return bg;
}
} // namespace ColorCalibration

class ColorSensor {
private:
  std::unique_ptr<ev3dev::color_sensor> sensor;
  glm::ivec3 currentRaw;
  std::array<Color, 4> currentColors;
  int currentColorPtr{0};
  Color stableColor{Color::bg};

public:
  inline ColorSensor(ev3dev::address_type sensor_addr, bool &failFlag) {
    for (auto &c : currentColors) {
      c = Color::bg;
    }
    sensor = createConnectedDevice<ev3dev::color_sensor>(sensor_addr, failFlag);
    sensor->set_mode(sensor->mode_rgb_raw);
    update();
  }

  inline void update() {
    auto vals = sensor->raw(false);
    currentRaw.x = std::get<0>(vals);
    currentRaw.y = std::get<1>(vals);
    currentRaw.z = std::get<2>(vals);

    glm::vec3 crgb{currentRaw};
    crgb.x /= (float)ColorCalibration::bg.x;
    crgb.y /= (float)ColorCalibration::bg.y;
    crgb.z /= (float)ColorCalibration::bg.z;
    glm::vec3 chsv{hsvColor(crgb)};

    Color currentColor{Color::bg};

    if (crgb.x < 0.02 && crgb.y < 0.02 && crgb.z < 0.02) { // in air
      // HSV 240, 1, 0.00689655
      currentColor = Color::bg;
    } else if (chsv.y < 0.4) { // Low saturation = background
      // HSV 70.0459, 0.315693, 0.221719
      currentColor = Color::bg;
    } else if (chsv.x > 20 && chsv.x < 80 && chsv.y > 0.5 &&
               chsv.z > 0.5) { // Yellow line
      // HSV 46.3742, 0.832707, 1.01
      currentColor = Color::line;
    } else if (chsv.x > 90 && chsv.x < 140 && chsv.z < 0.4) { // Green marker
      // HSV 115.071, 0.54898, 0.221719
      currentColor = Color::turnRight;
    }

    currentColors[currentColorPtr] = currentColor;
    currentColorPtr = (currentColorPtr + 1) % currentColors.size();

    int counts[(int)Color::count];
    std::fill(std::begin(counts), std::end(counts), 0);
    std::for_each(currentColors.begin(), currentColors.end(),
                  [&counts](const Color &c) { ++counts[(int)c]; });
    auto mxe = std::max_element(std::begin(counts), std::end(counts));
    stableColor = (Color)(mxe - counts);
  }

  inline glm::ivec3 getRawRGB() { return currentRaw; }

  inline glm::vec3 getRawHSV() {
    glm::vec3 crgb{currentRaw};
    crgb.x /= (float)ColorCalibration::bg.x;
    crgb.y /= (float)ColorCalibration::bg.y;
    crgb.z /= (float)ColorCalibration::bg.z;
    return hsvColor(crgb);
  }

  inline Color getColor() { return stableColor; }
};

class Drive {
private:
  std::unique_ptr<ev3dev::motor> mtr;
  bool reversed;
  int maxSpeed;
  int countsPerDegree;

public:
  inline Drive(ev3dev::address_type motor_addr, bool &failFlag) {
    mtr = createConnectedDevice<ev3dev::motor>(motor_addr, failFlag);
    reversed = false;
    maxSpeed = mtr->max_speed();
    countsPerDegree = mtr->count_per_rot() / 360;
    std::cerr << "Motor " << motor_addr << " max speed: " << maxSpeed
              << " P/deg: " << countsPerDegree << std::endl;
    mtr->set_stop_action(ev3dev::motor::stop_action_hold);
    mtr->stop();
  }

  /// Gets the encoder's position, adjusted for the reversed field.
  inline int getPosition() {
    int p{mtr->position()};
    return reversed ? -p : p;
  }

  /// Changes the zero-point of the encoder for the current position to be pos.
  inline void setPosition(int pos) { mtr->set_position(reversed ? -pos : pos); }

  // converts encoder position to degrees
  inline int getDegrees() { return getPosition() / countsPerDegree; }

  inline void setReversed(bool rev = true) { reversed = rev; }

  inline bool isReversed() { return reversed; }

  inline ev3dev::motor *getMotor() { return mtr.get(); }

  inline void stop() { mtr->stop(); }

  inline void runForever(int speedPercent = 100) {
    int sp{speedPercent * maxSpeed / 100};
    mtr->set_speed_sp(reversed ? -sp : sp);
    mtr->run_forever();
  }

  inline void overrideStopAction(const char *action) {
    mtr->set_stop_action(action);
  }

  inline void resetStopAction() {
    mtr->set_stop_action(ev3dev::motor::stop_action_hold);
  }

  inline void runTimed(int time_ms, int speedPercent = 100) {
    int sp{speedPercent * maxSpeed / 100};
    mtr->set_speed_sp(reversed ? -sp : sp);
    mtr->run_timed();
  }

  inline void waitUntilIdle() {
    while (mtr->state().count(ev3dev::motor::state_running)) {
      std::this_thread::sleep_for(std::chrono::milliseconds{50});
    }
  }

  inline void runToPosition(int pos) {
    mtr->set_position_sp(pos);
    mtr->run_to_abs_pos();
  }

  inline void runToDegree(int deg) { runToPosition(deg * countsPerDegree); }
};

} // namespace evutil
