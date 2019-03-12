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
unique_ptr<ev3dev::infrared_sensor> sonar;
power_supply battery{"lego-ev3-battery"};

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
      evutil::createConnectedDevice<ev3dev::infrared_sensor>(INPUT_4, failed);
  sonar->set_mode(sonar->mode_ir_prox);

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
  virtual void onPause() {}
  virtual void onResume() {}

public:
  virtual ~Subsystem() {}
  void pause() {
    if (!paused) {
      onPause();
      paused = true;
    }
  }
  void resume() {
    if (paused) {
      onResume();
      paused = false;
    }
  }
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

  int forwardBaseSpeed{30};

private:
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
  int obstacleDistancePct{60};
  bool seenObstacle{false};
  vector<Subsystem *> systemsToPause;
  SteeringSubsystem *sysSteering;

public:
  AvoidanceSubsystem(SteeringSubsystem *sysSteering) {
    this->sysSteering = sysSteering;
  }

  void registerSystemToPause(Subsystem *sys) { systemsToPause.push_back(sys); }

  virtual void dump(ostream &os) {
    os << "Avoidance subsystem:";
    os << "\n * Distance %: " << obstacleDistancePct;
    os << "\n * Seeing obstacle: " << seenObstacle;
    os << "\n * Raw value: " << sonar->proximity(false);
    os << "\n";
  }

protected:
  void process() {
    bool nowSeesObstacle{sonar->proximity(false) < obstacleDistancePct};
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
public:
  enum class QueuedAction { stop, turnLeft, turnRight, goStraight };

private:
  enum class Direction { left, right, middle };
  /// On which side of the robot was the line last seen.
  Direction lastLineDir{Direction::left};
  /// If seeing diverging lines AND in the turning state, which line to follow.
  Direction turnBias{Direction::middle};
  enum class State {
    following,
    stoppedAtMarker,
    turningAtMarker,
    waitingForCommand
  } state{State::following};
  int avgLineAngle{0};
  SteeringSubsystem *sysSteering;
  deque<QueuedAction> queuedActions;

  bool ignoreMarker{false};
  evutil::Stopwatch markerIgnoreStopwatch;
  int markerIgnoreMs{0};
  static constexpr int MARKER_IGNORE_MS{2000};

public:
  bool turningOn{true};
  int slightTurnRatio{110}, maxTurnRatio{110};

  LineFollowSubsystem(SteeringSubsystem *sysSteering) {
    this->sysSteering = sysSteering;
  }

  /// Add marker action to the queue
  void enqueueAction(QueuedAction a) { queuedActions.push_back(a); }

  bool isWaitingOnQueue() { return state == State::waitingForCommand; }

  bool queueEmpty() { return queuedActions.empty(); }

  /// Iterate over queued actions
  deque<QueuedAction>::const_iterator begin() { return queuedActions.begin(); }

  /// Iterate over queued actions
  deque<QueuedAction>::const_iterator end() { return queuedActions.end(); }

  /// Clears action queue
  void forceClearQueue() { queuedActions.clear(); }

  virtual void dump(ostream &os) {
    os << "Line follow subsystem:";
    os << "\n * Line last seen on the "
       << (lastLineDir == Direction::left ? "left" : "right");
    os << "\n * State: " << (int)state;
    os << "\n * Avg line angle: " << avgLineAngle;
    os << "\n";
  }

  void resumeFromStopMarker() {
    if (state == State::stoppedAtMarker) {
      ignoreMarker = true;
      markerIgnoreMs = MARKER_IGNORE_MS;
      markerIgnoreStopwatch.restart();
      state = State::following;
    }
  }

protected:
  virtual void onPause() { markerIgnoreStopwatch.pause(); }

  virtual void onResume() { markerIgnoreStopwatch.resume(); }

  void process() {

    if (state == State::stoppedAtMarker) {
      return;
    }

    leftColor->update();
    midColor->update();
    rightColor->update();
    evutil::Color lcol{leftColor->getColor()};
    evutil::Color mcol{midColor->getColor()};
    evutil::Color rcol{rightColor->getColor()};

    auto rrcol{rightColor->getRawRGB()};

    bool seeingMarker{lcol == evutil::Color::turnRight ||
                      mcol == evutil::Color::turnRight ||
                      rcol == evutil::Color::turnRight};
    if (ignoreMarker) {
      if (markerIgnoreStopwatch.elapsedMilliseconds() >= markerIgnoreMs) {
        ignoreMarker = false;
        markerIgnoreStopwatch.pause();
      } else if (seeingMarker) {
        seeingMarker = false;
        // make the robot blind to the marker
        lcol = evutil::Color::line;
        mcol = evutil::Color::line;
        rcol = evutil::Color::line;
      }
    }

    if (seeingMarker || state == State::waitingForCommand) {
      ignoreMarker = true;
      markerIgnoreMs = MARKER_IGNORE_MS;
      markerIgnoreStopwatch.restart();
      log() << "Seen marker: ";
      if (queuedActions.empty()) {
        if (state != State::waitingForCommand) {
          log() << "MISSING QUEUE ACTION, HALTING" << endl;
          state = State::waitingForCommand;
        }
      } else {
        QueuedAction A{queuedActions.front()};
        queuedActions.pop_front();
        switch (A) {
        case QueuedAction::stop:
          log() << "Stop marker" << endl;
          state = State::stoppedAtMarker;
          break;
        case QueuedAction::goStraight:
          log() << "Straight marker" << endl;
          state = State::turningAtMarker;
          turnBias = Direction::middle;
          break;
        case QueuedAction::turnLeft:
          log() << "Left marker" << endl;
          state = State::turningAtMarker;
          turnBias = Direction::left;
          break;
        case QueuedAction::turnRight:
          log() << "Right marker" << endl;
          state = State::turningAtMarker;
          turnBias = Direction::right;
          break;
        }
      }
    }

    // Do not move if stopped.
    if (state == State::waitingForCommand || state == State::stoppedAtMarker) {
      sysSteering->requestStop(SUBSYS_LINEFOLLOW);
      return;
    } else {
      sysSteering->cancelStopRequest(SUBSYS_LINEFOLLOW);
    }

    if (state == State::turningAtMarker && !ignoreMarker) {
      // out of time for the turn - resume normally
      state = State::following;
    }

    int lineAngle{0};
    // Track line position
    if (mcol == evutil::Color::line) {
      // Handle turn bias - mask out unwanted line parts
      if (state == State::turningAtMarker) {
        switch (turnBias) {
        case Direction::left:
          // take only leftmost reading
          if (lcol == evutil::Color::line) {
            mcol = evutil::Color::bg;
          } else if (rcol == evutil::Color::line) {
            rcol = evutil::Color::bg;
          }
          break;
        case Direction::middle:
          // take only middle reading (TODO: This might be buggy, might be
          // good to know intersection layout)
          lcol = evutil::Color::bg;
          rcol = evutil::Color::bg;
          break;
        case Direction::right:
          // take only rightmost reading
          if (lcol == evutil::Color::line) {
            lcol = evutil::Color::bg;
          } else if (rcol == evutil::Color::line) {
            mcol = evutil::Color::bg;
          }
          break;
        }
      }

      if (lcol == evutil::Color::line && rcol != evutil::Color::line) {
        // LMr -> adjust slightly left
        lineAngle = -slightTurnRatio;
        lastLineDir = Direction::left;
      } else if (lcol != evutil::Color::line && rcol == evutil::Color::line) {
        // lMR -> adjust slightly right
        lineAngle = slightTurnRatio;
        lastLineDir = Direction::right;
      } else {
        // lMr/LMR -> going straight
        lineAngle = 0;
      }
    } else {
      if (lcol == evutil::Color::line && rcol != evutil::Color::line) {
        // Lmr -> adjust left
        lineAngle = -maxTurnRatio;
        lastLineDir = Direction::left;
      } else if (lcol != evutil::Color::line && rcol == evutil::Color::line) {
        // lmR -> adjust right
        lineAngle = maxTurnRatio;
        lastLineDir = Direction::right;
      } else if (state != State::turningAtMarker &&
                 lcol != evutil::Color::line) {
        // lmr -> turn to the predicted line
        lineAngle =
            (lastLineDir == Direction::left) ? -maxTurnRatio : maxTurnRatio;
      } else {
        // LmR -> turn to bias line if turning
        lineAngle =
            (turnBias == Direction::right) ? maxTurnRatio : -maxTurnRatio;
        lastLineDir =
            (turnBias == Direction::right) ? Direction::right : Direction::left;
      }
    }
    avgLineAngle = lineAngle;

    sysSteering->pushCommand(
        {SteeringSubsystem::Command::CommandType::setTurnAngle, 0, 0,
         avgLineAngle});
    sysSteering->pushCommand(
        {SteeringSubsystem::Command::CommandType::setSpeed, 100, 0, 0});
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
    led::set_color(led::left, led::amber);
    char rbuf[64];
    char wbuf[64];
    string cmdbuf;
    int rlen;
    while ((rlen = recv(connectFd, rbuf, sizeof rbuf, 0)) > 0) {
      cmdbuf.append(rbuf, rlen);
      int splitPoint = cmdbuf.npos;
      while ((splitPoint = cmdbuf.find('\n')) != cmdbuf.npos) {
        // found a command
        string cmd = cmdbuf.substr(0, splitPoint);
        cmdbuf = cmdbuf.substr(splitPoint + 1);

        lock_guard<mutex> _l{accessMutex};
        // Implemented TCP commands
        if (cmd == "help") {
          rsend("Supported commands: help stop start moving? enqueue-stop "
                "enqueue-forward enqueue-left enqueue-right queue-status "
                "dump-queue clear-queue resume-from-stop-marker dump dump-hsv "
                "battery `set-speed 100` get-speed `set-max-turn-ratio 110` "
                "`set-slight-turn-ratio 90` get-turn-ratios disconnect\n");
        } else if (cmd == "stop") {
          if (!halted) {
            sysSteering.requestStop(SUBSYS_ROBOT);
            halted = true;
          }
          rsend("stop OK\n");
        } else if (cmd == "start") {
          if (halted) {
            sysSteering.cancelStopRequest(SUBSYS_ROBOT);
            halted = false;
          }
          rsend("start OK\n");
        } else if (cmd == "moving?") {
          snprintf(wbuf, sizeof wbuf, "moving = %d\n",
                   sysSteering.isMoving() ? 1 : 0);
          rsend(wbuf);
        } else if (cmd == "dump") {
          rsend("EV3DUMP START\n");
          stringstream ss;
          sysSteering.dump(ss);
          sysAvoid.dump(ss);
          sysLine.dump(ss);
          rsend(ss.str().c_str());
          rsend("\nEV3DUMP END\n");
        } else if (cmd == "enqueue-stop") {
          sysLine.enqueueAction(LineFollowSubsystem::QueuedAction::stop);
          rsend("enqueue-stop OK\n");
        } else if (cmd == "enqueue-forward") {
          sysLine.enqueueAction(LineFollowSubsystem::QueuedAction::goStraight);
          rsend("enqueue-forward OK\n");
        } else if (cmd == "enqueue-left") {
          sysLine.enqueueAction(LineFollowSubsystem::QueuedAction::turnLeft);
          rsend("enqueue-left OK\n");
        } else if (cmd == "enqueue-right") {
          sysLine.enqueueAction(LineFollowSubsystem::QueuedAction::turnRight);
          rsend("enqueue-right OK\n");
        } else if (cmd == "resume-from-stop-marker") {
          sysLine.resumeFromStopMarker();
          rsend("resume-from-stop-marker OK\n");
        } else if (cmd == "dump-queue") {
          stringstream ss;
          ss << "Queue: ";
          for (auto action : sysLine) {
            switch (action) {
            case LineFollowSubsystem::QueuedAction::stop:
              ss << "stop ";
              break;
            case LineFollowSubsystem::QueuedAction::turnLeft:
              ss << "left ";
              break;
            case LineFollowSubsystem::QueuedAction::turnRight:
              ss << "right ";
              break;
            case LineFollowSubsystem::QueuedAction::goStraight:
              ss << "forward ";
              break;
            }
          }
          ss << "OK\n";
          rsend(ss.str().c_str());
        } else if (cmd == "clear-queue") {
          sysLine.forceClearQueue();
          rsend("clear-queue OK\n");
        } else if (cmd == "queue-status") {
          if (sysLine.isWaitingOnQueue()) {
            rsend("waiting-for-command OK\n");
          } else if (sysLine.queueEmpty()) {
            rsend("empty OK\n");
          } else {
            rsend("in-progress OK\n");
          }
        } else if (cmd == "dump-hsv") {
          hsvDump = !hsvDump;
          rsend("dump-hsv OK\n");
        } else if (cmd == "disconnect") {
          rsend("disconnect OK\n");
          break;
        } else if (cmd == "battery") {
          float V{battery.measured_volts()};
          float A{battery.measured_amps()};
          snprintf(wbuf, sizeof wbuf, "battery volt %.3f max 8.4 amp %.6f OK\n",
                   V, A);
          rsend(wbuf);
        } else if (cmd == "get-speed") {
          snprintf(wbuf, sizeof wbuf, "speed %d OK\n",
                   sysSteering.forwardBaseSpeed);
          rsend(wbuf);
        } else if (cmd.find("set-speed") == 0) {
          int spd{30};
          sscanf(cmd.c_str(), "set-speed %d", &spd);
          if (spd < 5) {
            spd = 5;
          }
          if (spd > 100) {
            spd = 100;
          }
          snprintf(wbuf, sizeof wbuf, "set-speed %d (old %d ) OK\n", spd,
                   sysSteering.forwardBaseSpeed);
          sysSteering.forwardBaseSpeed = spd;
          rsend(wbuf);
        } else if (cmd == "get-turn-ratios") {
          snprintf(wbuf, sizeof wbuf,
                   "max-turn-ratio %d slight-turn-ratio %d OK\n",
                   sysLine.maxTurnRatio, sysLine.slightTurnRatio);
          rsend(wbuf);
        } else if (cmd.find("set-slight-turn-ratio") == 0) {
          int tr{110};
          sscanf(cmd.c_str(), "set-slight-turn-ratio %d", &tr);
          if (tr < 5) {
            tr = 5;
          }
          if (tr > 300) {
            tr = 300;
          }
          snprintf(wbuf, sizeof wbuf, "set-slight-turn-ratio %d (old %d ) OK\n",
                   tr, sysLine.slightTurnRatio);
          sysLine.slightTurnRatio = tr;
          rsend(wbuf);
        } else if (cmd.find("set-max-turn-ratio") == 0) {
          int tr{110};
          sscanf(cmd.c_str(), "set-max-turn-ratio %d", &tr);
          if (tr < 5) {
            tr = 5;
          }
          if (tr > 300) {
            tr = 300;
          }
          snprintf(wbuf, sizeof wbuf, "set-max-turn-ratio %d (old %d ) OK\n",
                   tr, sysLine.maxTurnRatio);
          sysLine.maxTurnRatio = tr;
          rsend(wbuf);
        } else {
          rsend("unknown command FAIL\n");
        }
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
    for (int cfd : connectionFds) {
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
  led::all_off();
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

    led::all_off();
    led::set_color(led::left, led::red);

    while (!(sigintTerminate || button::back.pressed())) {
      robot.process();
      this_thread::yield();
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
