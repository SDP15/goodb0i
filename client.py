#!/usr/bin/env python3

'''
The client, such as the Raspberry PI connects to a server (e.g. any DICE machine) and they can both communicate by sending
messages back-and-forth.
'''

from tcpcom import TCPClient
from time import sleep
import threading
import datetime


server_ip = "129.215.2.37"
server_port = 5005

reading = "Sensor: 128 ms"


def onStateChanged(state, msg):
    global isConnected

    if state == "LISTENING":
        print("DEBUG: Client:-- Listening...")
        client.sendMessage("Sending you a message.")

    elif state == "CONNECTED":
        isConnected = True
        print("DEBUG: Client:-- Connected to ", msg)
        client.sendMessage("Sending you a message.")

    elif state == "DISCONNECTED":
        isConnected = False
        print("DEBUG: Client:-- Connection lost.")
        main()

    elif state == "MESSAGE":
        print("DEBUG: Client:-- Message received: ", msg)
        client.sendMessage("Message acknowledged.")



def main():
    global client

    client = TCPClient(server_ip, server_port, stateChanged=onStateChanged)
    print("Client starting")

    try:
        while True:
            rc = client.connect()
            sleep(0.01)
            if rc:
                isConnected = True
                client.sendMessage("Message acknowledged.")
                while isConnected:
                    sleep(0.001)
            else:
                print("Client:-- Connection failed")

    except KeyboardInterrupt:
        print("Bye.")

    # mission done; close connection
    client.disconnect()
    threading.cleanup_stop_thread()  # needed if we want to restart the client


if __name__ == '__main__':
   main()