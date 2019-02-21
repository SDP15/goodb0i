# NFC reading

The `src` folder contains all the necessary source files to read and write on NFC tags. They should therefore not be altered unless it's necessary. The `examples` folder contains two files: `X_NUCLEO_...HelloWorld.ino` and `X_NUCLEO_...write.ino`. The first one was provided by the original documentation from STMicroelectronics (the maker of the NFC reader and tags). What it does is detect the tags and send a message through USB (Serial) to the Raspberry PI. There's a file on the PI called `test_serial.py` that should be run simultaneously to the Arduino code, which receives messages from the Arduino and prints them on the console. 

## How to use the files

Right now our tags are of type 4A, I therefore commented out the code for all the other types of tags (and I did so in every file in the `src` folder) but this part should not be removed in case we change (and we should) the type of tags we use. Just plugging the Arduino in the PI is enough for the code to run. Otherwise the code needs to be loaded from the Arduino IDE. To use the IDE on DICE run the command: 
```
>> arduino
```
on the terminal. Don't forget to update the libraries in the menu `Sketch > Include Libraries'. 

## What needs to be done

* Modify the file `X_NUCLEO_...write.ino` so that it can properly write on NFC tags. This is optional if we choose to use an Android phone to write on the tags directly. Some error messages appear when compiling the file, so read them carefully and make the appropriate changes in the files in the `src` folder to fix them but make sure not to screw everything up! 

* Modify the file `test_serial.py` on the PI (or create a new one) so that it can be included in the route planning thingy. Specifically when receiving messages from the Arduino, the PI should convert them into instructions to be sent to the EV3 (in case these are directional messages) or into other messages to the attention of the phone (like product details). This should be straight-forward if using my `tcpserver.py` and `tcpclient.py` files that are available in the folder above. 

## Documentation

You can find the original source files from STMicroelectronics at: https://github.com/stm32duino/X-NUCLEO-NFC03A1

