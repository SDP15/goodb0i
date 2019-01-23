from tcpcom import TCPServer
import datetime

'''
The client, such as the Raspberry PI connects to a server (e.g. any DICE machine) and they can both communicate by sending
messages back-and-forth.
'''

# connection configuration settings
tcp_ip = "129.215.2.37"
tcp_port = 5005
tcp_reply = "Server message"


def onStateChanged(state, msg):
    print state, msg
    global isConnected

    if state == "LISTENING":
        print("Server:-- Listening...")
    elif state == "CONNECTED":
        isConnected = True
        print("Server:-- Connected to" + msg)
        server.sendMessage("This is a new message.")
    elif state == "MESSAGE":
        print("new messa")
        print("Server:-- Message received:", msg)
        server.sendMessage("Message acknowledged.")



def main():
    global server
    server = TCPServer(tcp_port, stateChanged=onStateChanged)


if __name__ == '__main__':
     main()