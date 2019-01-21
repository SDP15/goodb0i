
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

} // namespace evutil
