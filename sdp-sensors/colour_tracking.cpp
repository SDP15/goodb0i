#include "ev3dev.h"
#include <iostream>
#include <memory>
#include "ev3dev.cpp"

class ColourTrack {

private:
    color_sensor   _colour_sensor;


    int detect() {
        remote_control r(_colour_sensor);

        if(!r.connected()) {
            cout << "no infrared sensor found" << endl;
            return 0;
        }

        r.mode("COL-COLOR");
        int val = int(r.read())
        while (int i < 100) { // only allow 100 iterations
            if(val == 0) {
                printf("NO COLOUR");
                return 0;
            } else if (val<5) {
                printf("BLACK");
                return 1;
            } else {
                printf("OTHER COLOURS");
                return 0;
            }

        }
    };


};