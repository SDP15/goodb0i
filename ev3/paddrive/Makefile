
CC=clang
CXX=clang++
CXXCFLAGS=-D_REENTRANT -pthread -lSDL2
CFLAGS=-std=c99 -O2 -g ${CXXCFLAGS}
CXXFLAGS=-std=gnu++17 -O2 -g ${CXXCFLAGS}

.PHONY: all

all: paddrive

paddrive: paddrive.cpp Makefile 
	$(CXX) $(CXXFLAGS) -o $@ $<
