## EV3 Development tree

Folders:
* ev3dev-lang-cpp – the C++ bindings to ev3, tracks its upstream
* mnt - `sshfs ev3: mnt`
* mtest - tiny test program, with a skeleton Makefile
* smove - program for manual motor control
* util - utilities:
  * ev3sh - put in your PATH, runs Docker shell for compiling EV3 programs
  * setup-ev3docker.sh - run once to download and setup the EV3-jessie docker image
  * sshconfig - SSH configuration to put into ~/.ssh/config to ease EV3 connections
  * util.hpp - utility C++ functions to simplify EV3 API usage with error checking

## TCP commands

Connect to EV3 through its IP to the port 6081.

To manually control it from the shell, use:
```
nc 192.168.105.100 6081
```

Accepted commands:
* `help` - lists all available commands
* `start` - makes the robot start/resume its line following
* `stop` - makes the robot halt its operations and stay in place until the next `resume` command
* `moving?` - sends `moving = 1/0` depending on the current state of the robot's motors
* `enqueue-stop`, `enqueue-forward`, `enqueue-left`, `enqueue-right` - enqueues actions for the green markers the robot sees
* `dump-queue` - sends: `Queue: forward left right stop OK` or whatever is in the current queue
* `clear-queue` - clears the marker action queue
* `queue-status` - sends `waiting-for-command/empty/in-progress OK` depending on the state of the queue
* `resume-from-stop-marker` - send this when the stop marker disappears from the queue to resume its operation
* `dump` - prints a lot of debug information
* `dump-hsv` - slows down the main loop significantly and prints out the HSV values for the middle sensor to stderr of the process on the EV3
* `disconnect` - shuts down the connection from the server side

All commands that don't explicity output something to the socket echo their name followed by `OK`.
Newlines are also included for use from an interactive terminal.

If no marker action is enqueued and the robot encounters a green marker, the robot will stop and wait for a command from the TCP connection.
