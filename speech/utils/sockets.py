import threading
import time

import websocket
import socket


class WebSocket:
    def __init__(self, ip_port, controller_queue):
        self.ip_port = ip_port
        self.ws = self.initialise_websocket()
        self.controller_queue = controller_queue

    def initialise_websocket(self):
        print("initialise websocket")
        websocket.enableTrace(True)
        def run(*args):
            args[0].run_forever()

        def on_message(ws, message):
            print("Message: " + str(message))
            self.controller_queue.put(("on_message", message))

        def on_error(ws, error):
            print(error)

        def on_close(ws):
            print("### closed ###")
            # ws.on_open = self.on_open
            # ws.run_forever()
        
        def on_open(ws):
            pass

        ws = websocket.WebSocketApp("ws://" + self.ip_port + "/trolley",
                                    on_message=on_message,
                                    on_error=on_error,
                                    on_close=on_close)
        ws.on_open = on_open    
        print("Websocket connection")    
        t1 = threading.Thread(name='WebSocketThread', target=run, args=(ws,))
        t1.start()
        return ws
    
    def get_instance(self):
        return self.ws


class TCPSocket:
    def __init__(self, ip_addr, port, controller_queue):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((ip_addr,port))
        self.controller_queue = controller_queue
        t2 = threading.Thread(name='TCPSocketReceivingThread', target=self.receive)
        t2.start()

    def receive(self):
        while True:
            data = self.sock.recv(8192)
            if not data: break
            msg = data.decode('utf-8')
            self.controller_queue.put(("on_message", msg))

    def send(self, msg):
        msg += "\n"
        msg = msg.encode()
        self.sock.send(msg)
