
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

enum class Color { bg, line, turnLeft, turnRight, turnBoth };
static const Color Colors[]{Color::bg, Color::line, Color::turnLeft};

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
  Color currentColor;

public:
  inline ColorSensor(ev3dev::address_type sensor_addr, bool &failFlag) {
    sensor = createConnectedDevice<ev3dev::color_sensor>(sensor_addr, failFlag);
    sensor->set_mode(sensor->mode_rgb_raw);
    update();
  }

  inline void update() {
    auto vals = sensor->raw(false);
    currentRaw.x = std::get<0>(vals);
    currentRaw.y = std::get<1>(vals);
    currentRaw.z = std::get<2>(vals);

    float minDist = 999999999;
    Color minColor = Color::bg;

    glm::vec3 crgb{currentRaw};
    crgb.x /= (float)ColorCalibration::bg.x;
    crgb.y /= (float)ColorCalibration::bg.y;
    crgb.z /= (float)ColorCalibration::bg.z;
    glm::vec3 chsv{hsvColor(crgb)};

    if (chsv.z < 0.2) {
      currentColor = Color::line;
      return;
    } else if (chsv.z > 0.8 && chsv.y < 0.3) {
      currentColor = Color::bg;
      return;
    }

    // Closest color in euclidean rgb space.
    for (auto cmpColor : Colors) {
      float dist{0};
      glm::vec3 xrgb{ColorCalibration::getRGBForColor(cmpColor)};
      xrgb.x /= (float)ColorCalibration::bg.x;
      xrgb.y /= (float)ColorCalibration::bg.y;
      xrgb.z /= (float)ColorCalibration::bg.z;
      glm::vec3 xhsv{hsvColor(xrgb)};
      glm::ivec3 dt = chsv - xhsv;
      dist = dt.x * dt.x + dt.y * dt.y + dt.z * dt.z;
      if (dist < minDist) {
        minDist = dist;
        minColor = cmpColor;
      }
    }
    currentColor = minColor;
  }

  inline glm::ivec3 getRawRGB() { return currentRaw; }

  inline Color getColor() { return currentColor; }
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
