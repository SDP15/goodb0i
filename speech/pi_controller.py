import os
import queue
import socket
import sys
import threading
import time

import requests
import serial

from speech_interactor import SpeechInteractor
from utils.custom_threads import WorkerThread
from utils.product import Product
from utils.sockets import WebSocket, TCPSocket


class PiController:
    def __init__(self):
        # Data structures for worker threads
        controller_queue = queue.Queue()
        self.speech_interactor_queue = queue.Queue()

        self.ip_port = "127.0.0.1:8080"
        self.ws = WebSocket(self.ip_port, controller_queue).get_instance()
        SpeechInteractor(self.ws, self.speech_interactor_queue)
        self.ev3 = TCPSocket("192.168.105.108", 6081)
        # self.ev3 = TCPSocket("localhost",4000)
        self.ev3_commands = []
        
        # Thread runs a given function and it's arguments (if given any) from the work queue
        t1 = WorkerThread("PiControllerThread", self, controller_queue)
        t1.start()

        # # To test receiving messages from WebSocket/to EV3
        # time.sleep(3)
        # self.ws.send("RouteCalculated&start,stop%3%0,pass%11")

        # time.sleep(2)
        # self.ws.send("ConfirmMessage&UserReady")

    def on_message(self, message):
        if "AppAcceptedProduct" in message:
            self.speech_interactor_queue.put(("cart", "yes", "app=True"))
        elif "AppRejectedProject" in message:
            self.speech_interactor_queue.put(("cart", "no", "app=True"))
        elif "AppScannedProduct" in message:
            item = message.split("&")
            query = "/products/" + item[1]
            item_json = self.query_web_server(query)

            id = item_json['product']['id']
            name = item_json['product']['name']
            price = item_json['product']['price']
            new_product = Product(id, 1, name, price)
            self.speech_interactor_queue.put(("scanned", new_product))
        elif "RouteCalculated" in message:
            full_route = message.split("&")
            route_commands = full_route[1].split(",")
            self.marker_list = []
            self.shelf_count = {}
            for commands in route_commands:
                command = commands.split("%")
                if command[0] == "pass":
                    command[0] = "forward"
                if len(command) > 1:
                    self.marker_list.append(command[1])
                    self.shelf_count[command[1]] = len(command) - 1
                if command[0] != "start":
                    self.ev3_commands.append("enqueue-" + command[0])
            self.ws.send("ReceivedRoute&")
            self.speech_interactor_queue.put(("react", "connected"))
        elif "Assigned" in message:
            list_message = message.split("&")
            list_id = list_message[1]
            self.get_shopping_list(list_id)
        elif "ConfirmMessage&UserReady" in message:
            # Server & pi ready to go => queue commands on EV3
            for command in self.ev3_commands:
                self.ev3.send(command)

            # Start following route
            self.ev3.send("start")

    #the request parameter has to be in the correct format e.g. /lists/load/7654321
    def query_web_server(self, request):
        r = requests.get("http://"+self.ip_port + request)
        list_json = r.json()
        return list_json
     
    # Retrieves all the items and quantities on the shopping list.
    def get_shopping_list(self, list_file):
        r = requests.get("http://" + self.ip_port + "/lists/load/" + list_file)
        json  = r.json()
        self.shopping_list = {}
        self.ordered_list = []
        for products in json['products']:
            id = products['product']['id']
            quantity = products['quantity']
            name = products['product']['name']
            price = products['product']['price']
            new_product = Product(id, quantity, name, price)
            self.ordered_list.append(new_product)
        print(self.ordered_list)
        self.speech_interactor_queue.put(("set_list", self.ordered_list))

    def readline_tcpsocket(self, port):
        message = ""
        byte = ""
        while True:
            byte = port.read()
            if byte == "\n":
                break
            message += byte
        # TODO: NEED TO CHANGE THIS! 
        if message == self.marker_list[0]:
            self.sp_interactor.on_location_change()
            self.send_message(self.ws, "ReachPoint&" + message)
        else:
            print(message)
        return message

PiController()
