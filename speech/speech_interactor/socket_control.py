                # requries websocket-client (pip install websocket-client)
import websocket
import os
import subprocess as sp

try:
    import thread
except ImportError:
    import _thread as thread
import time

def on_message(ws, message):
    print("Message: " + str(message))

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


if __name__ == "__main__":
    "Get a life!"
