                # requries websocket-client (pip install websocket-client)
import websocket
import os
import subprocess as sp
from pocketsphinx import LiveSpeech, get_model_path

model_path = get_model_path()

lsp = LiveSpeech()

speech = LiveSpeech(
    verbose=False,
    sampling_rate=16000,
    buffer_size=2048,
    no_search=False,
    full_utt=False,
    hmm=os.path.join(model_path, 'en-us'),
    lm=False,
    dic=os.path.join(model_path, 'cmudict-en-us.dict'),
    kws='kws.list',
)

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

def on_open(ws):
    def run(*args):
        sp.run(['wget','-O', 'products.json', 'http://129.215.2.55:8080/products'])
        print(open('products.json','r').read())
        print("listening")
        for phrase in lsp:
            print("You said: "+str(phrase))
            time.sleep(1)
            ws.send(str(phrase))
        time.sleep(1)
        ws.close()
        print("thread terminating...")
    thread.start_new_thread(run, ())


if __name__ == "__main__":
    websocket.enableTrace(True)
    ws = websocket.WebSocketApp("ws://129.215.2.55:8080/trolley",
                              on_message = on_message,
                              on_error = on_error,
                              on_close = on_close)
    ws.on_open = on_open
    ws.run_forever()
