# requries websocket-client (pip install websocket-client)
import websocket
try:
    import thread
except ImportError:
    import _thread as thread
import time

def on_message(ws, message):
    if (message.startswith("RC")):
        ws.send("RR&")
        time.sleep(3)
        ws.send("UR&")
        time.sleep(3)
        ws.send("RP&3")
    print("Message: " + str(message))

def on_error(ws, error):
    print(error)

def on_close(ws):
    print("### closed ###")

def on_open(ws):
    def run(*args):
        while True:
            pass
        print("thread terminating...")
    thread.start_new_thread(run, ())


if __name__ == "__main__":
    ws = websocket.WebSocketApp("ws://127.0.0.1:8080/trolley",
                              on_message = on_message,
                              on_error = on_error,
                              on_close = on_close)
    ws.on_open = on_open
    ws.run_forever()