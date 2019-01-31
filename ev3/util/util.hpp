
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

public:
  inline Drive(ev3dev::address_type motor_addr, bool &failFlag) {
    mtr = createConnectedDevice<ev3dev::motor>(motor_addr, failFlag);
    reversed = false;
    maxSpeed = mtr->max_speed();
    std::cerr << "Motor " << motor_addr << " max speed: " << maxSpeed
              << std::endl;
    mtr->set_stop_action(ev3dev::motor::stop_action_hold);
    mtr->stop();
  }

  inline void setReversed(bool rev = true) { reversed = rev; }

  inline ev3dev::motor *getMotor() { return mtr.get(); }

  inline void stop() { mtr->stop(); }

  inline void runForever(int speedPercent = 100) {
    int sp{speedPercent * maxSpeed / 100};
    mtr->set_speed_sp(reversed ? -sp : sp);
    mtr->run_forever();
  }
};

} // namespace evutil
