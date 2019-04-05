import os
import queue
import socket
import sys
import threading
import time
import getopt
import subprocess

import requests
import serial

from speech_interactor import SpeechInteractor
from utils.custom_threads import WorkerThread, ButtonThread, QRThread
from utils.custom_threads import WorkerThread
from utils.product import Product
from utils.sockets import WebSocket, TCPSocket
from utils.logger import log


class PiController:
    def __init__(self):
        self.server_address = "192.168.105.125:8080"
        self.ev3_ip = "192.168.105.108"
        self.ev3_port = 6081
        self.skip_tut = False
        self.logging = False

        # Parse command line options/arguments
        self.parse_opts()

        # Data structures for worker threads
        self.controller_queue = queue.Queue()
        self.speech_interactor_queue = queue.Queue()

        self.ws = WebSocket(self.server_address, self.controller_queue).get_instance()
        self.ev3 = TCPSocket(self.ev3_ip, self.ev3_port, self.controller_queue)
        self.ev3_commands = []

        self.app_accepted_event = threading.Event()
        self.app_skipped_event = threading.Event()
        self.clear_queue_event = threading.Event()
        self.speech_interactor = SpeechInteractor(self.speech_interactor_queue, self.controller_queue, self.app_accepted_event, self.app_skipped_event, self.clear_queue_event, self.logging)
        
        # Thread runs a given function and it's arguments (if given any) from the work queue
        t1 = WorkerThread("PiControllerThread", self, self.controller_queue)
        t1.start()

        # Controls whether user is allowed to press the start/stop button.
        self.button_event = threading.Event()
        self.qr_event = threading.Event()
        self.continue_event = threading.Event()
        self.continue_event.set()
        self.route_replan_event = threading.Event()

    def on_message(self, message):
        if "AppAcceptedProduct" in message:
            self.app_accepted_event.set()
            self.speech_interactor_queue.put("clear_listen_event")
            self.speech_interactor_queue.put(("next_state", "cart"))
            self.speech_interactor_queue.put(("cart", "yes", "app=True"))
        elif "AppRejectedProject" in message:
            self.speech_interactor_queue.put("clear_listen_event")
            self.speech_interactor_queue.put(("cart", "no", "app=True"))
        elif "AppSkippedProduct" in message:
            self.app_skipped_event.set()
            new_queue_items = ["clear_listen_event", "skip_product", ("next_state", "continue"), "continue_shopping"]
            self.speech_interactor_queue.put(("clear_work_queue", new_queue_items))
            self.clear_queue_event.clear()
            self.clear_queue_event.wait()
            self.clear_queue_event.clear()
        elif "AppScannedProduct" in message:
            item = message.split("&")
            query = "/products/" + item[1]
            item_json = self.query_web_server(query)

            id = item_json['id']
            name = item_json['shortName']
            price = item_json['price']
            new_product = Product(id, 1, name, price)

            # Hopefully this should allow user to use app v quickly
            self.speech_interactor_queue.put(("next_state", "arrival"))

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
                    self.set_shelf_attrs(command, route_trace)
                    product_indices = range(2,len(command)-1,2)
                    for i in product_indices:
                        product_idx = command[i]
                        self.ordered_list.put(self.unordered_list[int(product_idx)])

                if command[0] == "stop":
                    self.stop_markers.append(command[1])
                      
            self.speech_interactor_queue.put(("set_list", self.ordered_list))
            self.ws.send("ReceivedRoute&")


            # Check if option has been given to skip tutorial
            if self.skip_tut:
                self.speech_interactor_queue.put(("react", "connected_skip_tut"))
            else:
                self.speech_interactor_queue.put(("react", "connected"))
        elif "Assigned" in message:
            self.list_downloaded = False
            list_message = message.split("&")
            list_id = list_message[1]
            self.get_shopping_list(list_id)
        elif "ConfirmMessage&UserReady" in message:
            # Server & pi ready to go => queue commands on EV3
            self.enqueue_ev3_commands()

            # Start ButtonThread to listen for button presses to start/stop trolley
            t2 = ButtonThread("ButtonThread", self.controller_queue, self.button_event, self.continue_event, self.route_replan_event)
            t2.start()
            t3 = QRThread("QRDetectionThread", self.controller_queue)
            t3.start()
        elif "detected-marker" in message:
            command = self.route_queue.get()

            if len(command.split("%")) > 1:
                node_num = command.split("%")[1]

            if "%" in command:
                marker_num = self.marker_list[0]
                del(self.marker_list[0])
                self.ws.send("ReachedPoint&" + marker_num)

            if "stop" in message:
                # Check if the node number is the end point
                if node_num == "9":
                    self.speech_interactor_queue.put("clear_listen_event")
                    log("Session completed")
                else:
                     self.speech_interactor_queue.put("on_location_change")

                log("Stop marker to delete: {:}".format(self.stop_markers[0]))
                del(self.stop_markers[0])

                # Prevents button from being pressed when robot stops at a marker.
                self.button_event.set()
                log("Set button event - stops button from being pressed")
        elif "ReplanCalculated&" in message:
            self.replanned_route = self.calculate_route_trace(message)
            self.ev3.send("dump-queue")
        elif "Queue: " in message:
            # We want to replace all commands up to and including next "stop" command
            # Disregard strings that aren't commands
            log("EV3 Command Queue: {:}".format(message))
            ev3_command_queue = message
            ev3_command_queue = ev3_command_queue.split(" ")
            ev3_command_queue.remove("Queue:")
            log("ev3_command_queue: {:}".format(ev3_command_queue))
            ev3_command_queue.remove("OK\n")

            # Clear current command queue on EV3
            self.ev3.send("clear-queue")

            # Find the index of the next stop command
            for index, command in enumerate(ev3_command_queue):
                if "stop" in command:
                    stop_index = index
                    break

            route_queue_list = []
            while self.route_queue.qsize() != 0:
                route_queue_list.append(self.route_queue.get())

            log("route_queue_list: {:}".format(route_queue_list))

            route_queue_list = route_queue_list[stop_index+1:]
            log("route_queue_list: {:}".format(route_queue_list))

            
            # if there is a stop marker then add the new replanned route to the next stop then continue
            # from there
            if stop_index >= 0:
                new_route_trace = self.replanned_route + route_queue_list
                log("New replanned route trace: {:}".format(new_route_trace))

                # Update data structures that rely upon the route trace
                self.update_route_data_structs(new_route_trace)

                log("EV3 commands to queue: {:}".format(self.ev3_commands))
                # Update command queue on EV3
                self.enqueue_ev3_commands()
                self.route_replan_event.set()
        elif "SessionComplete&" in message:
            #Clear everything that we store
            self.marker_list = []
            self.ordered_shelves = []
            self.stop_markers = []

    def send_message(self, msg, websocket=False):
        if websocket:
            self.ws.send(msg)
        else:
            if "resume-from-stop-marker" in msg:
                self.button_event.clear()
                log("Clear button event so user can push button.")
            self.ev3.send(msg)

    # The request parameter has to be in the correct format e.g. /lists/load/7654321
    def query_web_server(self, request):
        r = requests.get("http://"+self.server_address + request)
        list_json = r.json()
        return list_json
     
    # Retrieves all the items and quantities on the shopping list.
    def get_shopping_list(self, list_file):
        r = requests.get("http://" + self.server_address + "/lists/load/" + list_file)
        json  = r.json()
        self.unordered_list = []
        for products in json['products']:
            id = products['product']['id']
            quantity = products['quantity']
            name = products['product']['shortName']
            price = products['product']['price']
            new_product = Product(id, quantity, name, price)
            self.unordered_list.append(new_product)
        self.list_downloaded = True

    # Calculates queue of commands with their marker numbers/shelf numbers
    def calculate_route_queue(self, route_trace):
        self.route_queue = queue.Queue()
            
        for command in route_trace:
            self.route_queue.put(command)

    # Calculates desired route trace from the given message (eg. "RouteCalculated&start%10,forward%12..")
    def calculate_route_trace(self, message):
        message = message.split("&")
        route_trace = message[1].split(",")

        # Replace "start" command with "forward"
        start_command = route_trace[0]
        start_command = start_command.split("%")
        start_command[0] = "forward"
        start_command = "%".join(start_command)
        route_trace[0] = start_command

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

        log("Route trace: {:}".format(route_trace))
        return route_trace

    # This has to be done after a route has been recalculated
    def update_route_data_structs(self, route_trace):
        # Update route_queue
        self.route_queue = ""
        self.calculate_route_queue(route_trace)

        log("Route trace to update data structs with: {:}".format(route_trace))
        self.ev3_commands = []
        self.stop_markers = []
        self.marker_list = []

        for commands in route_trace:
            command = commands.split("%")
            self.ev3_commands.append("enqueue-" + command[0])
            
            # Update marker list
            if len(command) > 1:
                self.marker_list.append(command[1])

            # Update stop_markers
            if command[0] == "stop":
                self.stop_markers.append(command[1])

    def scanned_qr_code(self, qr_code):
        self.qr_detected = qr_code
        log("[QR CODE detected]: " + qr_code)
        next_stop_marker = self.stop_markers[0]

        if qr_code in self.marker_list:
            #TODO: get rid of stop markers as we go.
            index_next = self.marker_list.index(next_stop_marker)
            index_qr = self.marker_list.index(qr_code)

            if qr_code == self.marker_list[0]:
                log("correct location")
            elif index_qr < index_next:
                #get rid of everything before the 
                for i in range(index_qr):
                    del(self.marker_list[i])
            elif index_qr > index_next:
                log("Passed a stop marker")
                #replan
                self.ws.send("RequestReplan&" + index_qr + "%" + next_stop_marker)
                
        else:
            log("QR not in the list")
            self.ws.send("RequestReplan&" + qr_code + "%" + next_stop_marker)


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
                log("ERROR: Invalid shelf position given in route trace")
            product.set_shelf_position(shelf_pos)

    def enqueue_ev3_commands(self):
        self.ev3.send("stop")
        self.ev3.send("resume-from-stop-marker")
        self.ev3.send("clear-queue")

        for command in self.ev3_commands:
            self.ev3.send(command)
            
        self.ev3.send("start")

    def parse_opts(self):
        if len(sys.argv) > 1:
            try:
                long_options = ["skiptut", "mock-ev3", "local-server","server-address=","help","log"]
                opts, _ = getopt.getopt(sys.argv[1:], "", long_options)
            except getopt.GetoptError as err:
                # print help information and exit:
                print(str(err))  # will print something like "option -a not recognized"
                sys.exit()
            for opt, arg in opts:
                if opt in "--skiptut":
                    log("Skipping tutorial...")
                    self.skip_tut = True
                elif opt in "--mock-ev3":
                    log("Mocking EV3")
                    subprocess.Popen(['nc','-l','4000'])
                    self.ev3_ip = "localhost"
                    self.ev3_port = 4000
                elif opt in "--local-server":
                    self.server_address = "127.0.0.1:8080"
                elif opt in "--server-address":
                    self.server_address = str(arg)
                elif opt in "--log":
                    self.logging = True
                elif opt in "--help":
                    print("Options: {:}".format(long_options))
                    print("Options must be prepended with \"--\".")
                    print("Additionally, options with an \"=\" require an argument to be given after the option flag.")
                    sys.exit()

    def clear_continue_event(self):
        self.continue_event.clear()
        log("Continue event cleared")

    def reset(self):
        # Clear speech interactor queue
        self.clear_queue(self.speech_interactor_queue)
        
        # Reset speech interactor
        self.speech_interactor.reset()
        
    def clear_queue(self, queue):
        if not queue.empty():
            while queue.qsize != 0:
                queue.get()

        if queue.empty():
            log("Work queue EMPTY")            


PiController()