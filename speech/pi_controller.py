import os
import queue
import socket
import sys
import threading
import time

import requests
import serial

from speech_interactor import SpeechInteractor
# from utils.custom_threads import WorkerThread, ButtonThread, QRThread
from utils.custom_threads import WorkerThread
from utils.product import Product
from utils.sockets import WebSocket, TCPSocket


class PiController:
    def __init__(self):
        # Data structures for worker threads
        self.controller_queue = queue.Queue()
        self.speech_interactor_queue = queue.Queue()

        self.ip_port = "127.0.0.1:8080"
        # self.ev3_ip = "192.168.105.108"
        # self.ev3_port = 6081
        self.ev3_ip = "localhost"
        self.ev3_port = 4000
        self.ws = WebSocket(self.ip_port, self.controller_queue).get_instance()
        self.ev3 = TCPSocket(self.ev3_ip, self.ev3_port, self.controller_queue)
        self.ev3_commands = []

        SpeechInteractor(self.speech_interactor_queue, self.controller_queue)
        
        # Thread runs a given function and it's arguments (if given any) from the work queue
        t1 = WorkerThread("PiControllerThread", self, self.controller_queue)
        t1.start()

        # Controls whether user is allowed to press the start/stop button.
        self.button_event = threading.Event()
        self.qr_event = threading.Event()

    def on_message(self, message):
        if "AppAcceptedProduct" in message:
            self.speech_interactor_queue.put("clear_listen_event")
            self.speech_interactor_queue.put(("next_state", "cart"))
            self.speech_interactor_queue.put(("cart", "yes", "app=True"))
        elif "AppRejectedProject" in message:
            self.speech_interactor_queue.put("clear_listen_event")
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
            self.marker_list = []
            self.ordered_shelves = []
            self.stop_markers = []

            while not self.list_downloaded:
                pass

            route_trace = self.calculate_route_trace(message)
            
            # Calculates queue of commands with their marker numbers/shelf numbers
            self.calculate_route_queue(route_trace)

            for commands in route_trace:
                command = commands.split("%")
                self.ev3_commands.append("enqueue-" + command[0])

                if len(command) > 1:
                    self.marker_list.append(command[1])
                
                # The numbers after marker number are indices into the shopping list generated by the user 
                # this can be used to order the shopping list.
                if len(command) > 2:

                    self.stop_markers.append(command[1])
                    self.set_shelf_attrs(command, route_trace)
                    for index in command[2:]:
                        self.ordered_list.append(self.unordered_list[int(index)])
                
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

            Start ButtonThread to listen for button presses to start/stop trolley
            t2 = ButtonThread("ButtonThread", self.controller_queue, self.button_event)
            t2.start()
            t3 = QRDetection("QRDetectionThread", self.controller_queue, self.qr_event)
            t3.start()
            
        elif "detected-marker" in message:
            command = self.route_queue.get()

            if "%" in command:
                marker_num = self.marker_list[0]
                del(marker_list[0])
                self.ws.send("ReachedPoint&" + marker_num)

            if "stop" in message:
                # Prevents button from being pressed when robot stops at a marker.
                self.button_event.set()
                self.speech_interactor_queue.put("on_location_change")

        elif "ReplanCalculated&" in message:
            self.ev3.send("dump-queue")
            self.replanned_route = calculate_route_trace(message)
        elif "Queue:" in message:
            # delete all the commands before the next stop and add the new route recieved
            self.ev3.send("clear-queue")
            stop_index = -1
            new_ev3_route = message.split(" ")
            for index, command in enumerate(new_ev3_route):
                if "stop" in command:
                    stop_index = index
                    break 
            
            # if there is a stop marker then add the new replanned route to the next stop then continue
            # from there
            if stop_index >= 0:
                new_route_plan = replan_queue + new_ev3_route[stop_index+1:]
                print("New replanned route trace: {:}".format(new_route_plan))
                for marker in new_route_plan:
                    self.ev3.send("enqueue-" + marker)
        elif "SessionComplete&" in message:
            #Clear everything that we store
            self.marker_list = []
            self.ordered_shelves = []
            self.stop_markers = []


    def send_message(self, msg, websocket=False, ev3=False):
        if websocket:
            self.ws.send(msg)
        elif ev3:
            if "resume-from-stop-marker" in msg:
                self.button_event.clear()
            self.ev3.send(msg)

    # The request parameter has to be in the correct format e.g. /lists/load/7654321
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
        for index,command in enumerate(route_trace):
            if "pass" in command:
                split_command = command.split("%")
                split_command[0] = "forward"
                route_trace[index] = "%".join(split_command)

        print("New route trace: {:}".format(route_trace))
        return route_trace

    def replan_route():

    
    def scanned_qr_code(self, qr_code):
        self.qr_detected = qr_code
        print("[QR CODE detected]: " + qr_code)

        if qr_code in self.marker_list:
            #TODO: get rid of stop markers as we go.
            next_stop_marker = self.stop_markers[0]
            index_next = marker_list.index(next_stop_marker)
            index_qr = marker_list.index(qr_code)

            if qr_code == marker_list[0]:
                print("correct location")
            elif index_qr < index_next:
                #get rid of everything before the 
                for i in range(index_qr):
                    del(marker_list[i])
            elif index_qr > index_next:
                print("Passed a stop marker")
                #replan
                self.ws.send("RequestReplan&" + index_qr + "%" + next_stop_marker)
                
        else:
            print("QR not in the list")
            self.ws.send("RequestReplan&" + index_qr + "%" + next_stop_marker)


    def set_shelf_attrs(self, command, route_trace):
        # Disregard stop as it is no longer relevant
        command = command[1:]
        shelf_num = command[0]

        # Get the indices corresponding to our position in unordered list
        product_indices = range(1,len(command)-1,2)
        for i in product_indices:
            index = command[i]
            product = self.unordered_list[int(index)]
            product.set_shelf_number(shelf_num)
            shelf_pos = command[i+1]
            if shelf_pos == "0":
                shelf_pos = "bottom"
            elif shelf_pos == "1":
                shelf_pos = "middle"
            elif shelf_pos == "2":
                shelf_pos = "top"
            else:
                print("ERROR: Invalid shelf position given in route trace")
            product.set_shelf_position(shelf_pos)




PiController()