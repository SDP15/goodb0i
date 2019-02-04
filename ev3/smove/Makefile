
PROJNAME=smove

include ../util/common.make

main: main.cpp ../util/util.hpp Makefile 
	$(CXX) $(CXXFLAGS) -lcurses -o $@ $<

../mnt/${PROJNAME}: main
	cp main ../mnt/${PROJNAME}
