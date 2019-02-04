
PROJNAME=g0b-rootev3

include ../util/common.make

main: main.cpp ../util/util.hpp Makefile 
	$(CXX) $(CXXFLAGS) -o $@ $<

../mnt/${PROJNAME}: main
	cp main ../mnt/${PROJNAME}
