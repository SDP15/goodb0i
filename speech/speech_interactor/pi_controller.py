import os
import socket
import sys
import threading
import time
import requests

import websocket

import speech_interactor


class PiController:
    def __init__(self):
        self.ip_port = "127.0.0.1:8080"
        self.ws = self.initialise_websocket()
        self.sp_interactor = speech_interactor.SpeechInteractor(self)
        print("\n\n\n\nReturned\n\n\n\n")
        #self.initialise_ev3_socket()
        while True:
            pass
        
    def get_ws(self):
        return self.ws 

    def on_message(self, ws, message):
        print("Message: " + str(message))
        if "AppAcceptedProduct" in message:
            self.sp_interactor.cart(word="yes")
        elif "AppRejectedProject" in message:
            self.sp_interactor.cart("no")
        elif "AppScannedProduct" in message:
            item = message.split("&")
            self.sp_interactor.scanned(item[1])
        elif "RouteCalculated" in message:
            full_route = message.split("&")
            route_commands = full_route[1].split(",")
            print(route_commands)
            self.send_message(ws, "ReceivedRoute&")
            self.sp_interactor.react("connected")
        elif "Assigned" in message:
            list_message = message.split("&")
            list_id = list_message[1]
            self.get_shopping_list(list_id)

            

    def on_error(self, ws, error):
        print(error)

    def on_close(self, ws):
        print("### closed ###")

    def send_message(self, ws, message):
        print("Sent " + message)
        ws.send(str(message))

    def run(self, *args):
        # print("listening")
        # for phrase in lsp:
        #     print("You said: "+str(phrase))
        #     time.sleep(1)
        #     ws.send(str(phrase))
        args[0].run_forever()

    def on_open(self, ws):
        pass

    def initialise_websocket(self):
        print("initialise websocket")
        websocket.enableTrace(True)
        ws = websocket.WebSocketApp("ws://127.0.0.1:8080/trolley",
                                    on_message=self.on_message,
                                    on_error=self.on_error,
                                    on_close=self.on_close)
        ws.on_open = self.on_open
        # ws.run_forever()
        t1 = threading.Thread(name='WebSocketThread', target=self.run, args=(ws, ))
        t1.start()
        # threading.start_new_thread(self.run, (ws, ))
        print("Websocket connection")
        return ws

    #the request parameter has to be in the correct format e.g. /lists/load/7654321
    def query_web_server(self, ws, request):
        r = requests.get("http://"+self.ip_port + request)
        list_json = r.json()
        return list_json
     

    # Retrieves all the items and quantities on the shopping list.
    def get_shopping_list(self, list_file):
        r = requests.get("http://" + self.ip_port + "/lists/load/" + list_file)
        json  = r.json()
        print("JSON is " + str(json))
        self.stuff = json
        self.list_pointer = 0
        self.shopping_list = {}
        self.ordered_list = []
        for tings in self.stuff['products']:
            self.shopping_list.update(
                {tings['product']['name']: tings['quantity']})
            self.ordered_list.append(tings['product']['name'])
        print(self.shopping_list)
        return (self.shopping_list, self.ordered_list)

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

    def receive_tcpsocket(self):
        global connection
        while True:
            data = connection.recv(32)  # max buffer size
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
        self.send_tcpsocket(message="Hello")


PiController()
