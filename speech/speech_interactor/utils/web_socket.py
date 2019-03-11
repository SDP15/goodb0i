import websocket
import threading
import time

class WebSocket:
    def __init__(self, controller_queue):
        self.ip_port = "127.0.0.1:8080"
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

        ws = websocket.WebSocketApp("ws://127.0.0.1:8080/trolley",
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