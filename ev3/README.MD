## EV3 Development tree

Folders:
* ev3dev-lang-cpp – the C++ bindings to ev3, tracks its upstream
* mnt - `sshfs ev3: mnt`
* mtest - tiny test program, with a skeleton Makefile
* util - utilities:
  * ev3sh - put in your PATH, runs Docker shell for compiling EV3 programs
  * setup-ev3docker.sh - run once to download and setup the EV3-jessie docker image
  * sshconfig - SSH configuration to put into ~/.ssh/config to ease EV3 connections
  * util.hpp - utility C++ functions to simplify EV3 API usage with error checking
