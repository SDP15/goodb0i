
CC=arm-linux-gnueabi-gcc
CXX=arm-linux-gnueabi-g++
CXXCFLAGS=-DEV3DEV_PLATFORM_EV3 -I../util -isystem ../ev3dev-lang-cpp -isystem ../glm -pthread
CFLAGS=-std=c99 -O3 ${CXXCFLAGS}
CXXFLAGS=-std=gnu++17 -O3 ${CXXCFLAGS}

.PHONY: all

all: ../mnt/${PROJNAME}
