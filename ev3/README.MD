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
* `start` - makes the robot start/resume its line following
* `stop` - makes the robot halt its operations and stay in place until the next `resume` command
