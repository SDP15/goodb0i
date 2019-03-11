import websocket
import thread
from pynput.keyboard import Key, Listener

components = []
should_wait = False

def key_listener(ws):
    def on_press(key):
        try:
            key_press = key.char
            global should_wait
            if (key_press == '`'):
                should_wait = not should_wait
            if (should_wait):
                return
            elif (key_press == 's'):
                ws.send("Hello")
        except AttributeError:
            pass

    with Listener(on_press=on_press) as listener:
        listener.join()

def on_message(ws, message):
    print("On message " + str(message))   
    ws.send(message)
 

def on_error(ws, error):
    print(error)

def on_close(ws):
    print("closed")

def on_open(ws):
    print("Websocket connected.\ns: Start")
    thread.start_new_thread(key_listener, (ws,))


if __name__ == "__main__":
    ws = websocket.WebSocketApp("ws://127.0.0.1:8080/ping",
                              on_message = on_message,
                              on_error = on_error,
                              on_close = on_close)
    ws.on_open = on_open
    ws.run_forever()