
PROJNAME=g0b-calibrate-color

include ../util/common.make

main: main.cpp ../util/util.hpp Makefile 
	$(CXX) $(CXXFLAGS) -o $@ $<

../mnt/${PROJNAME}: main
	cp main ../mnt/${PROJNAME}
