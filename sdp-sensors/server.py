from tcpcom import TCPServer
import sys, getopt
import socket

'''
The client, such as the Raspberry PI connects to a server (e.g. any DICE machine) and they can both communicate by sending
messages back-and-forth.
'''

# connection configuration settings
print(socket.gethostbyname(socket.gethostname()))
tcp_port = 5006
tcp_reply = "Server message"


def onStateChanged(state, msg):
    print state, msg
    global isConnected

    if state == "LISTENING":
        print("Server:-- Listening...")
    elif state == "CONNECTED":
        isConnected = True

        start = raw_input("Enter START if you wish to start the robot\n")
        server.sendMessage(start)
        stop()

    elif state == "MESSAGE":
        print("Server:-- Message received:", msg)

def stop():
    stop = raw_input("Enter STOP if you with to stop the robot\n")
    server.sendMessage(stop)
    print("message sent")

def main():
    global server
    server = TCPServer(tcp_port, stateChanged=onStateChanged)



if __name__ == '__main__':
     main()