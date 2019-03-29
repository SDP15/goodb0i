import os
import queue
import socket
import sys
import threading
import time

import requests
import serial

from button import Button
from speech_interactor import SpeechInteractor
from utils.custom_threads import WorkerThread
from utils.product import Product
from utils.sockets import WebSocket, TCPSocket
# from qr_scanner_live import QRDetection


class PiController:
    def __init__(self):
        # Data structures for worker threads
        controller_queue = queue.Queue()
        self.speech_interactor_queue = queue.Queue()

        self.ip_port = "127.0.0.1:8080"
        # self.ev3_ip = "192.168.105.108"
        # self.ev3_port = 6081
        self.ev3_ip = "localhost"
        self.ev3_port = 4000
        self.ws = WebSocket(self.ip_port, controller_queue).get_instance()
        self.ev3 = TCPSocket(self.ev3_ip, self.ev3_port, controller_queue)
        self.ev3_commands = []

        SpeechInteractor(self.speech_interactor_queue, controller_queue)

        self.button_pressed = 0
        Button(controller_queue)

        # Thread runs a given function and it's arguments (if given any) from the work queue
        t1 = WorkerThread("PiControllerThread", self, controller_queue)
        t1.start()

        # t2 = threading.Thread(name="CheckMovementThread", target=self.poll_ev3)
        # t2.start()

        # # To test receiving messages from WebSocket/to EV3
        # time.sleep(3)
        # self.ws.send("RouteCalculated&start,stop%3%0,pass%11")

        # time.sleep(2)
        # self.ws.send("ConfirmMessage&UserReady")

    def on_message(self, message):
        if "AppAcceptedProduct" in message:
            self.speech_interactor_queue.put(("next_state", "cart"))
            self.speech_interactor_queue.put(("cart", "yes", "app=True"))
        elif "AppRejectedProject" in message:
            self.speech_interactor_queue.put(("cart", "no", "app=True"))
        elif "AppScannedProduct" in message:
            item = message.split("&")
            query = "/products/" + item[1]
            item_json = self.query_web_server(query)

            id = item_json['id']
            name = item_json['name']
            price = item_json['price']
            new_product = Product(id, 1, name, price)
            self.speech_interactor_queue.put(("scanned", new_product))
        elif "RouteCalculated" in message:
            self.ordered_list = queue.Queue()
            self.marker_list = queue.Queue()
            self.ordered_shelves = []

            while not self.list_downloaded:
                pass

            route_trace = self.calculate_route_trace(message)
            
            # Calculates queue of commands with their marker numbers/shelf numbers
            self.calculate_route_queue(route_trace)

            for commands in route_trace:
                command = commands.split("%")
                self.ev3_commands.append("enqueue-" + command[0])

                if len(command) > 1:
                    self.marker_list.put(command[1])
                
                # The numbers after marker number are indices into the shopping list generated by the user 
                # this can be used to order the shopping list.
                if len(command) > 2:
                    for index in command[2:]:
                        self.ordered_list.put(self.unordered_list[int(index)])
                        # shelf = shelf.Shelf(command[1], command[2:])
                        # self.ordered_shelves.append(shelf)
                
            self.speech_interactor_queue.put(("set_list", self.ordered_list))
            self.ws.send("ReceivedRoute&")
            self.speech_interactor_queue.put(("react", "connected"))
        elif "Assigned" in message:
            self.list_downloaded = False
            list_message = message.split("&")
            list_id = list_message[1]
            self.get_shopping_list(list_id)
        elif "ConfirmMessage&UserReady" in message:
            # Server & pi ready to go => queue commands on EV3
            self.ev3.send("stop")
            self.ev3.send("resume-from-stop-marker")
            self.ev3.send("clear-queue")

            for command in self.ev3_commands:
                self.ev3.send(command)

            self.ev3.send("start")
        elif "detected-marker" in message:
            command = self.route_queue.get()

            if "%" in command:
                marker_num = self.marker_list.get()
                self.ws.send("ReachedPoint&" + marker_num)

            if "stop" in message:
                self.speech_interactor_queue.put("on_location_change")
        # elif "moving =" in message:
        #     self.is_cart_moving(message)

    def send_message(self, msg, websocket=False, ev3=False):
        if websocket:
            self.ws.send(msg)
        elif ev3:
            self.ev3.send(msg)

    #the request parameter has to be in the correct format e.g. /lists/load/7654321
    def query_web_server(self, request):
        r = requests.get("http://"+self.ip_port + request)
        list_json = r.json()
        return list_json
     
    # Retrieves all the items and quantities on the shopping list.
    def get_shopping_list(self, list_file):
        r = requests.get("http://" + self.ip_port + "/lists/load/" + list_file)
        json  = r.json()
        self.unordered_list = []
        for products in json['products']:
            id = products['product']['id']
            quantity = products['quantity']
            name = products['product']['name']
            price = products['product']['price']
            new_product = Product(id, quantity, name, price)
            self.unordered_list.append(new_product)
        self.list_downloaded = True

    # Calculates queue of commands with their marker numbers/shelf numbers
    def calculate_route_queue(self, route_trace):
        self.route_queue = queue.Queue()
            
        for command in route_trace:
            self.route_queue.put(command)

    # Calculates desired route trace from the given message (eg. "RouteCalculated&start,forward%12..")
    def calculate_route_trace(self, message):
        message = message.split("&")
        route_trace = message[1].split(",")

        print("Original route trace: {:}".format(route_trace))

        # We can ignore the "start" command
        route_trace.remove("start")

        # Replace "end" command with "stop"
        end_command = route_trace[-1]
        end_command = end_command.split("%")
        end_command[0] = "stop"
        end_command = "%".join(end_command)
        route_trace[-1] = end_command

        # Replace "pass" commands with "forward"
        for command in route_trace:
            if "pass" in command:
                split_command = command.split("%")
                split_command[0] = "forward"
                command = "%".join(split_command)

        print("New route trace: {:}".format(route_trace))
        return route_trace

    # def scanned_qr_code(self, qr_code):
    #     print(qr_code)

    def is_button_pressed(self, pressed):
        self.button_pressed = pressed
        if not pressed:
            # self.ev3.send("moving?")
            self.ev3.send("stop")
        else:
            self.ev3.send("start")
        
    # def is_cart_moving(self, message):
    #     if "moving = 1" in message:
    #         self.ev3.send("stop")



PiController()
