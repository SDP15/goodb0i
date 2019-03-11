import os
import socket
import sys
import threading
import time
import requests
import serial

import websocket

import speech_interactor
import product


class PiController:
    def __init__(self):
        self.ip_port = "127.0.0.1:8080"
        self.ws = self.initialise_websocket()
        self.sp_interactor = speech_interactor.SpeechInteractor(self)

        #used to get the list and remember the place the robot is in the list.
        self.shopping_list_index = 0

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
            query = "/products/" + item[1]
            item_json = self.query_web_server(ws, query)

            id = item_json['product']['id']
            name = item_json['product']['name']
            price = item_json['product']['price']
            new_product = product.Product(id, 1, name, price)
            self.sp_interactor.set_scanned_item(new_product)

        elif "RouteCalculated" in message:
            #Message format: RouteCalculated&forward,right%something%something,forward
            full_route = message.split("&")
            route_commands = full_route[1].split(",")
            self.marker_list = []
            for commands in route_commands:
                command = commands.split("%")
                #currently server sends center and ev3 receives forward if this doesnt change uncomment
                # if command[0] == "center" or command[0] == "pass":
                #     command[0] = "forward"
                if len(command) > 1:
                    self.marker_list.append(command[1])
                self.send_tcpsocket("enqueue-" + command[0])
            print(route_commands)
            self.send_message(ws, "ReceivedRoute&")
            self.sp_interactor.react("connected")
            
        elif "Assigned" in message:
            list_message = message.split("&")
            list_id = list_message[1]
            self.get_shopping_list(list_id)

        else:
            print(message)

            

    def on_error(self, ws, error):
        print(error)
        # Consider trying to re open a web scoket connection if the error message is a closed websocket.

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
        print("initialise websocket " + self.ip_port + "/trolley")
        websocket.enableTrace(True)
        ws = websocket.WebSocketApp(self.ip_port + "/trolley",
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
