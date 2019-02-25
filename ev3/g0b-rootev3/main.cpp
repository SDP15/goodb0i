#include <arpa/inet.h>
#include <bits/stdc++.h>
#include <cassert>
#include <cstdint>
#include <cstring>
#include <netinet/in.h>
#include <signal.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#include "ev3dev.cpp"
#include "ev3dev.h"
#include "util.hpp"

using namespace std;
using namespace std::chrono_literals;
using namespace ev3dev;

enum DriveNumber : int {
  DRIVE_LEFT_BACK = 0,
  DRIVE_LEFT_FRONT,
  DRIVE_RIGHT_BACK,
  DRIVE_RIGHT_FRONT
};
array<unique_ptr<evutil::Drive>, 4> drives;
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
  return sensorLog;
}

void connectEv3Devices() {
  bool failed{false};
  drives[DRIVE_LEFT_BACK] = make_unique<evutil::Drive>(OUTPUT_A, failed);
  drives[DRIVE_LEFT_FRONT] = make_unique<evutil::Drive>(OUTPUT_B, failed);
  drives[DRIVE_RIGHT_BACK] = make_unique<evutil::Drive>(OUTPUT_C, failed);
  drives[DRIVE_RIGHT_FRONT] = make_unique<evutil::Drive>(OUTPUT_D, failed);
  drives[DRIVE_LEFT_BACK]->setReversed();
  drives[DRIVE_RIGHT_BACK]->setReversed();

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
                             "logs/zlog-g0b-%F-%H-%M-%S.log",
                             localtime(&now))}; // DevSkim: ignore DS154189
  string logName{logFilename.data(), logNameLen};
  sensorLog = ofstream(logName, ios_base::out | ios_base::app);
  if (!sensorLog.good()) {
    throw runtime_error("Could not open logfile.");
  }
}

class SteeringSubsystem {
public:
  struct Command {
    enum class CommandType {
      allStop,
      setSpeed,
      setTurnAngle,
      saveAndHalt,
      resume
    } type;

    int speed{100};
    /// 0 means forever
    int milliseconds{0};
    int newTurnAngle{0};
  };

  deque<Command> commands;

  void pushCommand(Command cmd) { commands.push_back(cmd); }

  void pushCommandToFront(Command cmd) { commands.push_front(cmd); }

  bool queueEmpty() { return commands.empty(); }

private:
  int forwardBaseSpeed{70};
  int currentTurnAngle{0}, currentSpeed{0};

  array<int, 4> currentMotorSpeeds{{0, 0, 0, 0}},
      savedMotorSpeeds{{0, 0, 0, 0}}, targetMotorSpeeds{{0, 0, 0, 0}};
  /// Time remaining before the next command is popped off the queue
  int remainingWaitMs{0};
  chrono::steady_clock::time_point lastWaitUpdate, lastCmdTime;

  /// TurnAngle goes from -100 to 100
  void setTargetSpeedsClassic(int speedPercent, int turnAngle) {
    int bspeed{forwardBaseSpeed * speedPercent / 100};
    int lspeed{bspeed};
    int rspeed{bspeed};
    if (turnAngle < 0) {
      // left turn
      turnAngle = 100 + turnAngle;
      lspeed = lspeed * turnAngle / 100;
    } else if (turnAngle > 0) {
      turnAngle = 100 - turnAngle;
      rspeed = rspeed * turnAngle / 100;
    }

    targetMotorSpeeds[DRIVE_LEFT_BACK] = lspeed;
    targetMotorSpeeds[DRIVE_LEFT_FRONT] = lspeed;
    targetMotorSpeeds[DRIVE_RIGHT_BACK] = rspeed;
    targetMotorSpeeds[DRIVE_RIGHT_FRONT] = rspeed;
  }

  void setTargetSpeedsPivot(int speedPercent, bool turnRight) {
    int bspeed{forwardBaseSpeed * speedPercent / 100};
    targetMotorSpeeds[DRIVE_LEFT_BACK] = turnRight ? bspeed : -bspeed;
    targetMotorSpeeds[DRIVE_LEFT_FRONT] = turnRight ? bspeed : -bspeed;
    targetMotorSpeeds[DRIVE_RIGHT_BACK] = turnRight ? -bspeed : bspeed;
    targetMotorSpeeds[DRIVE_RIGHT_FRONT] = turnRight ? -bspeed : bspeed;
  }

public:
  SteeringSubsystem() {
    lastWaitUpdate = chrono::steady_clock::now();
    lastCmdTime = lastWaitUpdate;
  }

  bool isMoving() {
    return targetMotorSpeeds[DRIVE_LEFT_BACK] != 0 ||
           targetMotorSpeeds[DRIVE_RIGHT_BACK] != 0;
  }

  void process() {
    // process command
    auto newWaitUpdate{chrono::steady_clock::now()};
    auto deltaTime{chrono::duration_cast<chrono::milliseconds>(newWaitUpdate -
                                                               lastCmdTime)};
    lastWaitUpdate = newWaitUpdate;
    if (deltaTime.count() >= remainingWaitMs) {
      // process next command(s)
      remainingWaitMs = 0;
      while (!commands.empty()) {
        Command next{commands.front()};
        commands.pop_front();

        switch (next.type) {
        case Command::CommandType::allStop: {
          for (int &sp : targetMotorSpeeds) {
            sp = 0;
          }
        } break;
        case Command::CommandType::setSpeed: {
          currentSpeed = next.speed;
          setTargetSpeedsClassic(currentSpeed, currentTurnAngle);
        } break;
        case Command::CommandType::setTurnAngle: {
          currentTurnAngle = next.newTurnAngle;
          setTargetSpeedsClassic(currentSpeed, currentTurnAngle);
        } break;
        case Command::CommandType::saveAndHalt: {
          savedMotorSpeeds = targetMotorSpeeds;
          setTargetSpeedsClassic(0, 0);
        } break;
        case Command::CommandType::resume: {
          targetMotorSpeeds = savedMotorSpeeds;
        } break;
        }

        if (next.milliseconds > 0) {
          remainingWaitMs = next.milliseconds;
          lastCmdTime = chrono::steady_clock::now();
          break;
        }
      }
    }
    // push updates to motors
    for (int m = 0; m < drives.size(); ++m) {
      auto targetSpeed{targetMotorSpeeds[m]};
      if (currentMotorSpeeds[m] != targetSpeed) {
        currentMotorSpeeds[m] = targetSpeed;
        if (targetSpeed == 0) {
          drives[m]->stop();
        } else {
          drives[m]->runForever(targetSpeed);
        }
      }
    }
  }
};

class LineFollowSubsystem {
private:
  enum class Direction { left, right } lastLineDir;
  int avgLineAngle{0};
  bool lastStopped{false}, stopped{false};
  SteeringSubsystem *sysSteering;

public:
  LineFollowSubsystem(SteeringSubsystem *sysSteering) {
    this->sysSteering = sysSteering;
  }

  void process() {
    leftColor->update();
    midColor->update();
    rightColor->update();
    evutil::Color lcol{leftColor->getColor()};
    evutil::Color mcol{midColor->getColor()};
    evutil::Color rcol{rightColor->getColor()};

    auto rrcol{rightColor->getRawRGB()};
    // cerr << (int)lcol << " " << (int)rcol << " " << rrcol.x << " " << rrcol.y
    //     << " " << rrcol.z << endl;
    constexpr int STEER_ANGLE{85};
    constexpr int SMALL_STEER_ANGLE{70};
    constexpr int SONAR_STOP_DISTANCE{45};

    if (sonar->distance_centimeters() < SONAR_STOP_DISTANCE) {
      stopped = true;
    } else {
      stopped = false;
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
      if (lastStopped != stopped) {
        lastStopped = stopped;
        if (stopped) {
          sysSteering->pushCommandToFront(
              {SteeringSubsystem::Command::CommandType::saveAndHalt});
        } else {
          sysSteering->pushCommandToFront(
              {SteeringSubsystem::Command::CommandType::resume});
        }
      } else {
        if (sysSteering->queueEmpty()) {
          sysSteering->pushCommand(
              {SteeringSubsystem::Command::CommandType::setTurnAngle, 0, 0,
               avgLineAngle});
        }
      }
    }
  }
};

class Robot {
private:
  SteeringSubsystem sysSteering;
  LineFollowSubsystem sysLine;

  bool halted{false};
  mutex accessMutex;
  int socketFd{-1};

  void spawnNewConnThread() { new thread(&Robot::socketThreadFn, this); }

  void socketThreadFn() {
    int connectFd{accept(socketFd, nullptr, nullptr)};
    spawnNewConnThread();
    if (connectFd < 0) {
      perror("accept fail");
      return;
    }
    auto rsend = [&](const char *str) {
      ::send(connectFd, str, strlen(str), 0);
    };

    rsend("EV3READY\n");
    char rbuf[64];
    int rlen;
    while ((rlen = recv(connectFd, rbuf, sizeof rbuf, 0)) > 0) {
      if (rbuf[rlen - 1] == '\n') {
        --rlen;
      }
      if (rlen == 0) {
        continue;
      }
      lock_guard<mutex> _l{accessMutex};
      if (strncmp(rbuf, "stop", rlen) == 0) {
        if (!halted) {
          halted = true;
          sysSteering.pushCommandToFront(
              {SteeringSubsystem::Command::CommandType::saveAndHalt});
        }
      }
      if (strncmp(rbuf, "start", rlen) == 0) {
        if (halted) {
          halted = false;
          sysSteering.pushCommandToFront(
              {SteeringSubsystem::Command::CommandType::resume});
        }
      }
    }

    if (shutdown(connectFd, SHUT_RDWR) == -1) {
      perror("shutdown failed");
    }
    close(connectFd);
  }

public:
  Robot() : sysSteering(), sysLine(&sysSteering) {
    sysSteering.pushCommand(
        {SteeringSubsystem::Command::CommandType::setSpeed, 100, 1000, 0});
    sysSteering.pushCommand(
        {SteeringSubsystem::Command::CommandType::setTurnAngle, 0, 3000, 80});
    // Start up server
    {
      socketFd = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
      if (socketFd == -1) {
        perror("Cannot create server socket");
        throw new runtime_error("Cannot create server socket");
      }

      sockaddr_in addr;
      memset(&addr, 0, sizeof addr);
      addr.sin_family = AF_INET;
      addr.sin_port = htons(6081);
      addr.sin_addr.s_addr = htonl(INADDR_ANY);

      if (bind(socketFd, reinterpret_cast<sockaddr *>(&addr), sizeof addr) ==
          -1) {
        perror("bind fail");
        throw new runtime_error("Could not bind server socket");
      }
      if (listen(socketFd, 10) == -1) {
        perror("listen fail");
        throw new runtime_error("Could not listen to socket");
      }
    }
    spawnNewConnThread();
  }

  ~Robot() {
    lock_guard<mutex> _l{accessMutex};
    if (socketFd >= 0) {
      close(socketFd);
    }
  }

  void process() {
    lock_guard<mutex> _l{accessMutex};

    sysSteering.process();
    if (!halted) {
      sysLine.process();
    }
  }
};

/// Emergency motor disablement
void disableMotors() {
  for (auto &drive : drives) {
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
