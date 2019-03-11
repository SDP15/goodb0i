import os
import queue
import socket
import sys
import threading
import time

import requests

from speech_interactor import SpeechInteractor
from utils.custom_threads import WorkerThread
from utils.web_socket import WebSocket


class PiController:
    def __init__(self):
        # Data structures for worker threads
        controller_queue = queue.Queue()
        self.speech_interactor_queue = queue.Queue()

        self.ip_port = "127.0.0.1:8080"
        self.ws = WebSocket(controller_queue).get_instance()
        SpeechInteractor(self.ws, self.speech_interactor_queue)
        #self.initialise_ev3_socket()
        
        # Thread runs a given function and it's arguments (if given any) from the work queue
        t1 = WorkerThread("PiControllerThread", self, controller_queue)
        t1.start()

        # To test receiving messages from WebSocket
        # time.sleep(3)
        # self.ws.send("AppAcceptedProduct")

    def on_message(self, message):
        if "AppAcceptedProduct" in message:
            self.speech_interactor_queue.put(("cart", "yes", "app=True"))
        elif "AppRejectedProject" in message:
            self.speech_interactor_queue.put(("cart", "no", "app=True"))
        elif "AppScannedProduct" in message:
            item = message.split("&")
            self.speech_interactor_queue.put(("scanned", item[1]))
        elif "RouteCalculated" in message:
            full_route = message.split("&")
            route_commands = full_route[1].split(",")
            print(route_commands)
            self.ws.send("ReceivedRoute&")
            self.speech_interactor_queue.put(("react", "connected"))
        elif "Assigned" in message:
            list_message = message.split("&")
            list_id = list_message[1]
            self.get_shopping_list(list_id)

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
