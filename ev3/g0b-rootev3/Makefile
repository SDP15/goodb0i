
PROJNAME=g0b-rootev3

CC=arm-linux-gnueabi-gcc
CXX=arm-linux-gnueabi-g++
CXXCFLAGS=-DEV3DEV_PLATFORM_EV3 -I../util -isystem ../ev3dev-lang-cpp -pthread
CFLAGS=-std=c99 ${CXXCFLAGS}
CXXFLAGS=-std=gnu++17 ${CXXCFLAGS}

.PHONY: all

all: ../mnt/${PROJNAME}

main: main.cpp ../util/util.hpp Makefile 
	$(CXX) $(CXXFLAGS) -o $@ $<

../mnt/${PROJNAME}: main
	cp main ../mnt/${PROJNAME}
