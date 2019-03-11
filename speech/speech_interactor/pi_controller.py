import os
import queue
import socket
import sys
import threading
import time
import serial

from speech_interactor import SpeechInteractor
from utils.custom_threads import WorkerThread
from utils.web_socket import WebSocket
import product


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
            query = "/products/" + item[1]
            item_json = self.query_web_server(ws, query)

            id = item_json['product']['id']
            name = item_json['product']['name']
            price = item_json['product']['price']
            new_product = product.Product(id, 1, name, price)
            self.speech_interactor_queue.put(("scanned", item[1]))
        elif "RouteCalculated" in message:
            #Message format: RouteCalculated&forward,right%something%something,forward
            full_route = message.split("&")
            route_commands = full_route[1].split(",")
            self.marker_list = []
            self.shelf_count = {}
            for commands in route_commands:
                command = commands.split("%")
                #currently server sends center and ev3 receives forward if this doesnt change uncomment
                # if command[0] == "center" or command[0] == "pass":
                #     command[0] = "forward"
                if len(command) > 1:
                    self.marker_list.append(command[1])
                    self.shelf_count[command[1]] = len(command) - 1
                    print("We are collecting " + (len(command) -1) + " at shelf " + command[1])
                self.send_tcpsocket("enqueue-" + command[0])
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
        self.shopping_list = {}
        self.ordered_list = []
        for products in json['products']:
            id = products['product']['id']
            quantity = products['quantity']
            name = products['product']['name']
            price = products['product']['price']
            new_product = product.Product(id, quantity, name, price)
            self.ordered_list.append(new_product)
        print(self.ordered_list)
        self.sp_interactor.set_list(self.ordered_list)


    def get_next_item(self):
        self.shopping_list_index = self.shopping_list_index + 1
        self.ordered_list[self.shopping_list_index]
    

    def initialise_ev3_socket(self):
        self.ser = serial.Serial(port = "/dev/ttyACM0", baudrate = 9600, timeout = 2)
        t2 = threading.Thread(name='ev3SocketThread', target=self.readline_tcpsocket, args=(self.ser, ))
        t2.start()

    def readline_tcpsocket(self, port):
        message = ""
        byte = ""
        while True:
            byte = port.read()
            if byte == "\n":
                break
            message += byte
        # NEED TO CHANGE THIS! 
        if message == stop_list[0]:
            self.sp_interactor.on_location_change()
            self.send_message(self.ws, "ReachPoint&" + message)
        else:
            print(message)
        return message


    def send_tcpsocket(self, message):
        print("sending \"" + message + "\" to the client")
        self.ser.write(message.encode('utf-8'))
        self.ser.flush()
        

    def close_tcpsocket(self):       
        if self.ser.is_open:
            self.ser.close()

PiController()
