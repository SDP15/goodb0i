
#include "ev3dev.h"
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
