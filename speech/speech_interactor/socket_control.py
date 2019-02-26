                # requries websocket-client (pip install websocket-client)
import websocket
import os
import subprocess as sp
from speech_interactor import SpeechInteractor

try:
    import thread
except ImportError:
    import _thread as thread
import time

def on_message(ws, message):
    print("Message: " + str(message))
    if "AA" in message:
        SpeechInteractor.cart(word = "yes")
    elif "AR" in message:
        SpeechInteractor.cart("no")
    # elif "AS" in message:
    #     speech_interactor.scanned(item)


def on_error(ws, error):
    print(error)

def on_close(ws):
    print("### closed ###")

def send_message(ws, message):
    print("Sent " + message)
    ws.send(str(message))


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

def initialise_socket():
    websocket.enableTrace(True)
    ws = websocket.WebSocketApp("ws://129.215.2.55:8080/trolley",
                            on_message = on_message,
                            on_error = on_error,
                            on_close = on_close)
    ws.on_open = on_open
    ws.run_forever()
    return ws

if __name__ == "__main__":
    "Get a life!"
