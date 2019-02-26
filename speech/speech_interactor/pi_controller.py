from speech_interactor import SpeechInteractor
import websocket
import subprocess
import pexpect

try:
    import thread
except ImportError:
    import _thread as thread
import time

class PiController:
    def __init__(self):
        self.speech_interactor = SpeechInteractor(self)
        self.ws = initialise_socket(self.speech_interactor)
        self.speech_interactor.listen()
 
    def message_server(self,message):
        print("Sent " + message)
        self.ws.send(str(message))
    
    def message_ev3(self,msg):
        pexpect.run(['nc','192.168.105.100','6081'])
    


def on_error(ws, error):
    print(error)

def on_close(ws):
    print("### closed ###")

# def send_message(ws, message):
#     print("Sent " + message)
#     ws.send(str(message))


def on_open(ws):
    def run(*args):
        print("listening")
        # for phrase in lsp:
        #     print("You said: "+str(phrase))
        #     time.sleep(1)
        #     ws.send(str(phrase))
        time.sleep(1)
        ws.close()
        print("thread terminating...")
    thread.start_new_thread(run, ())

def initialise_socket(spint):
    websocket.enableTrace(True)

    def on_message(ws, message):
        print("Message: " + str(message))
        if "AA" in message:
            spint.cart(word = "yes")
        elif "AR" in message:
            spint.cart("no")
        # elif "AS" in message:
        #     speech_interactor.scanned(item)

    ws = websocket.WebSocketApp("ws://129.215.2.55:8080/trolley",
                            on_message = on_message,
                            on_error = on_error,
                            on_close = on_close)
    ws.on_open = on_open
    ws.run_forever()
    return ws