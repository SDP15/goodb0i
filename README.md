# goodb0i

## Sensors

This branch lists all files related to manipulating the robot's sensors: 

* colour sensor for line-tracking and intersection detection 
* ultrasound sensor for obstacle detection 
* NFC tag reading 

and perhaps more if better options are found along the project. 

The robot will interact with the server (that could be any DICE machine) using TCP protocols. The file `client.py` explains how a client machine (Arduino, EV3 or PI) connects to a server and, conversely, the file `server.py` explains how a server machine listens to clients. 

The server can send instructions to the robot by sending string messages to the listening clients. These instructions get added to a FIFO queue. 

The robot moves along its way and consumes an element of the queue (LEFT, RIGHT or FORWARD) everytime it detects an intersection. Perhaps more commands, such as BACKWARDS, could be added after. 

An instruction gets also consumed when the robot detects the end of an aisle, although that's still to be confirmed. Any obstacle detection overrides previous commands. 

Feel free to add any comment if anything needs further explanations. 
