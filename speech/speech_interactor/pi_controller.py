from speech_interactor import SpeechInteractor
import websocket
import os
import socket
import sys

try:
    import thread
except ImportError:
    import _thread as thread
import time

class PiController:



    def __init__(self):
        self.sp_interactor = SpeechInteractor()
        self.ws = self.initialise_websocket()
        self.initialise_ev3_socket()


    def on_message(self, ws, message):
        print("Message: " + str(message))
        if "AP" in message:
            self.sp_interactor.cart(word = "yes")
        elif "AR" in message:
            self.sp_interactor.cart("no")
        #
        # elif "AS" in message:
        #     sp_interactor.scanned(item)
        elif "RC" in message:
            full_route = message.split("&")
            route_commands = full_route[1].split(",")
            print(route_commands)
            self.send_message(ws, "RR&")
            self.sp_interactor.react("connected")

        


    def on_error(self, ws, error):
        print(error)

    def on_close(self, ws):
        print("### closed ###")

    def send_message(self, ws, message):
        print("Sent " + message)
        ws.send(str(message))


    def on_open(self, ws):
        def run(*args):
            print("listening")
            # for phrase in lsp:
            #     print("You said: "+str(phrase))
            #     time.sleep(1)
            #     ws.send(str(phrase))
            time.sleep(1)
            ws.close()
            print("thread terminating...")
        thread.start_new_thread(run, ())

    def initialise_websocket(self):
        websocket.enableTrace(True)
        ws = websocket.WebSocketApp("ws://129.215.2.55:8080/trolley",
                                on_message = self.on_message,
                                on_error = self.on_error,
                                on_close = self.on_close)
        ws.on_open = self.on_open
        ws.run_forever()
        return ws

    
    def initialise_ev3_socket(self):
        global connection
        # Create a TCP/IP socket
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # bind the address to the port
        server_address = ('192.168.105.144', 8080)
        #print("Starting on address: '%s'" % server_address)

        sock.bind(server_address)

        sock.listen(1)

        while True:
            print("waiting for a connection")
            connection, client_address = sock.accept()
            try:
                print("Connection from %s" % str(client_address))
            finally:
                self.receive_tcpsocket()
                self.send_tcpsocket(message = "Hello")
                break
            


    def receive_tcpsocket(self):
        global connection
        while True:
            data = connection.recv(32) # max buffer size
            print("received '%s'" % str(data))
            if data:
                print("sending data back to the client")
            else:
                print("No more data")
                break


    def send_tcpsocket(self, message):
        global connection
        print("sending data back to the client")
        connection.sendall(message) 
        self.close_tcpsocket()


    def close_tcpsocket(self):
        global connection
        connection.close()

PiController()


    

