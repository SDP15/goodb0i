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

enum SubsystemId {
  SUBSYS_ROBOT,
  SUBSYS_STEERING,
  SUBSYS_LINEFOLLOW,
  SUBSYS_AVOIDANCE,
};

class Subsystem {
private:
  bool paused{false};

protected:
  virtual void process() {}

public:
  virtual ~Subsystem() {}
  void pause() { paused = true; }
  void resume() { paused = false; }
  void processIfNeeded() {
    if (!paused) {
      process();
    }
  }
  bool isPaused() { return paused; }
  virtual void dump(ostream &os) { os << "Unknown subsystem\n"; }
};

class SteeringSubsystem : public Subsystem {
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

  bool queueEmpty() { return commands.empty(); }

  void pushCommand(Command cmd) {
    if (queueEmpty()) {
      commandWaitStopwatch.restart();
    }
    commands.push_back(cmd);
  }

  void pushCommandToFront(Command cmd) {
    if (queueEmpty()) {
      commandWaitStopwatch.restart();
    }
    commands.push_front(cmd);
  }

private:
  int forwardBaseSpeed{60};
  int currentTurnAngle{0}, currentSpeed{0};

  array<int, 4> currentMotorSpeeds{{0, 0, 0, 0}},
      targetMotorSpeeds{{0, 0, 0, 0}};
  /// Time remaining before the next command is popped off the queue
  int remainingWaitMs{0};
  evutil::Stopwatch commandWaitStopwatch;
  set<SubsystemId> stopCauses;

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
  SteeringSubsystem() { commandWaitStopwatch.restart(); }

  bool isMoving() {
    return targetMotorSpeeds[DRIVE_LEFT_BACK] != 0 ||
           targetMotorSpeeds[DRIVE_RIGHT_BACK] != 0;
  }

  void requestStop(SubsystemId who) {
    if (stopCauses.empty()) {
      commandWaitStopwatch.pause();
    }
    stopCauses.insert(who);
  }

  void cancelStopRequest(SubsystemId who) {
    stopCauses.erase(who);
    if (stopCauses.empty()) {
      commandWaitStopwatch.resume();
    }
  }

  virtual void dump(ostream &os) {
    os << "Steering subsystem:\n";
    os << " * Stop causes: ";
    for (auto &sc : stopCauses) {
      os << sc << " ";
    }
    os << "\n * Turn angle: " << currentTurnAngle;
    os << "\n * Speed: " << currentSpeed;
    os << "\n";
  }

protected:
  void process() {
    // process command(s)
    if (stopCauses.empty() &&
        commandWaitStopwatch.elapsedMilliseconds() >= remainingWaitMs) {
      commandWaitStopwatch.restart();
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
        }

        if (next.milliseconds > 0) {
          remainingWaitMs = next.milliseconds;
          break;
        }
      }
    }
    // push updates to motors
    for (int m = 0; m < drives.size(); ++m) {
      auto targetSpeed{stopCauses.empty() ? targetMotorSpeeds[m] : 0};
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

class AvoidanceSubsystem : public Subsystem {
private:
  int obstacleDistanceCm{45};
  bool seenObstacle{false};
  vector<Subsystem *> systemsToPause;
  SteeringSubsystem *sysSteering;

public:
  AvoidanceSubsystem(SteeringSubsystem *sysSteering) {
    this->sysSteering = sysSteering;
    sonar->distance_centimeters(true);
  }

  void registerSystemToPause(Subsystem *sys) { systemsToPause.push_back(sys); }

  virtual void dump(ostream &os) {
    os << "Avoidance subsystem:";
    os << "\n * Distance cm: " << obstacleDistanceCm;
    os << "\n * Seeing obstacle: " << seenObstacle;
    os << "\n";
  }

protected:
  void process() {
    bool nowSeesObstacle{sonar->distance_centimeters(false) <
                         obstacleDistanceCm};
    if (nowSeesObstacle != seenObstacle) {
      seenObstacle = nowSeesObstacle;
      for_each(systemsToPause.begin(), systemsToPause.end(),
               nowSeesObstacle ? mem_fn(&Subsystem::pause)
                               : mem_fn(&Subsystem::resume));
      if (nowSeesObstacle) {
        sysSteering->requestStop(SUBSYS_AVOIDANCE);
      } else {
        sysSteering->cancelStopRequest(SUBSYS_AVOIDANCE);
      }
    }
  }
};

class LineFollowSubsystem : public Subsystem {
private:
  enum class Direction { left, right } lastLineDir;
  enum class State { following } state{State::following};
  int avgLineAngle{0};
  SteeringSubsystem *sysSteering;

public:
  bool turningOn{true};

  LineFollowSubsystem(SteeringSubsystem *sysSteering) {
    this->sysSteering = sysSteering;
  }

  virtual void dump(ostream &os) {
    os << "Line follow subsystem:";
    os << "\n * Line last seen on the "
       << (lastLineDir == Direction::left ? "left" : "right");
    os << "\n * State: " << (int)state;
    os << "\n * Avg line angle: " << avgLineAngle;
    os << "\n";
  }

protected:
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

    if (sysSteering->queueEmpty()) {
      int lineAngle{};
      if (mcol == evutil::Color::turnRight ||
          lcol == evutil::Color::turnRight ||
          rcol == evutil::Color::turnRight) {
        cerr << "Starting turn" << endl;
        sysSteering->pushCommand(
            {SteeringSubsystem::Command::CommandType::setTurnAngle, 0,
             turningOn ? 2000 : 1000, turningOn ? STEER_ANGLE : 0});
        lastLineDir = Direction::left;
        return;
      }
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
      sysSteering->pushCommand(
          {SteeringSubsystem::Command::CommandType::setTurnAngle, 0, 0,
           avgLineAngle});
      sysSteering->pushCommand(
          {SteeringSubsystem::Command::CommandType::setSpeed, 100, 0, 0});
    }
  }
};

class Robot {
private:
  SteeringSubsystem sysSteering;
  AvoidanceSubsystem sysAvoid;
  LineFollowSubsystem sysLine;

  bool halted{true};
  bool hsvDump{false};

  mutex accessMutex;
  int socketFd{-1};
  set<int> connectionFds;

  void spawnNewConnThread() { new thread(&Robot::socketThreadFn, this); }

  void socketThreadFn() {
    int connectFd{accept(socketFd, nullptr, nullptr)};
    spawnNewConnThread();
    if (connectFd < 0) {
      perror("accept fail");
      return;
    }
    {
      lock_guard<mutex> _l{accessMutex};
      connectionFds.insert(connectFd);
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
      // Implemented TCP commands
      if (strncmp(rbuf, "stop", rlen) == 0) {
        if (!halted) {
          sysSteering.requestStop(SUBSYS_ROBOT);
          halted = true;
        }
      } else if (strncmp(rbuf, "start", rlen) == 0) {
        if (halted) {
          sysSteering.cancelStopRequest(SUBSYS_ROBOT);
          halted = false;
        }
      } else if (strncmp(rbuf, "dump", rlen) == 0) {
        rsend("EV3DUMP START\n");
        stringstream ss;
        sysSteering.dump(ss);
        sysAvoid.dump(ss);
        sysLine.dump(ss);
        rsend(ss.str().c_str());
        rsend("\nEV3DUMP END\n");
      } else if (strncmp(rbuf, "right", rlen) == 0) {
        sysLine.turningOn = true;
      } else if (strncmp(rbuf, "straight", rlen) == 0) {
        sysLine.turningOn = false;
      }
    }

    {
      lock_guard<mutex> _l{accessMutex};
      connectionFds.erase(connectFd);
    }
    if (shutdown(connectFd, SHUT_RDWR) == -1) {
      perror("socket shutdown failed");
    }
    close(connectFd);
  }

public:
  Robot() : sysSteering(), sysAvoid(&sysSteering), sysLine(&sysSteering) {
    sysAvoid.registerSystemToPause(&sysLine);
    sysSteering.requestStop(SUBSYS_ROBOT);

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
    for (auto &cfd : connectionFds) {
      shutdown(cfd, SHUT_RDWR);
      close(cfd);
    }
  }

  void process() {
    lock_guard<mutex> _l{accessMutex};

    sysSteering.processIfNeeded();
    if (!halted) {
      sysAvoid.processIfNeeded();
      sysLine.processIfNeeded();
    }

    if (hsvDump) {
      midColor->update();
      cerr << midColor->getRawHSV().x << ", " << midColor->getRawHSV().y << ", "
           << midColor->getRawHSV().z << endl;
      this_thread::sleep_for(70ms);
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
      this_thread::sleep_for(8ms);
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
