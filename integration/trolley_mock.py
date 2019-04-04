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
            print("PRESSED", key_press)
            if (key_press == 'q'):
                exit()
            elif (key_press == 'p'):
                current = components.pop(0)
                print("Reached point " + str(current))
                if (current[0] == "pass"):
                    ws.send("ReachedPoint&" + str(current[1]))
                else:
                    ws.send("ReachedPoint&" + str(current[1].split("%")[0]))
            elif (key_press == 'a'):
                ws.send("AcceptedProduct&")
            elif (key_press == 'r'):
                ws.send("RejectedProduct&")
            elif (key_press == 'u'):
                ws.send("UserReady&")
            elif (key_press == 's'):
                ws.send("SkippedProduct&")
        except AttributeError:
            pass

    with Listener(on_press=on_press) as listener:
        listener.join()

def on_message(ws, message):
    print("On message " + str(message))   
    global components
    if (str(message).startswith("RouteCalculated")):
        route = message.split("&", 1)[1]
        parts = route.split(",")
        print("Route parts " + str(parts))
        print("Components are " + str(components))

        for part in parts:
            print("Part: " + str(part))
            if(str(part).startswith("pass")):
                components.append(("pass", part.split("%")[1]))
            elif(str(part).startswith("stop")):
                components.append(("stop", part.split("%")[1]))
            elif(str(part).startswith("end")):
                components.append(("stop", part.split("%")[1]))
        print("Computed route " + str(components))
        ws.send("RouteReceived&")
    elif (str(message).startswith("SessionComplete")):
        print("Clearing route")
        components = []
 

def on_error(ws, error):
    print(error)

def on_close(ws):
    print("closed")

def on_open(ws):
    print("Websocket connected.\nq: Quit\n`: pause\np: Reach next point\na: Accept\ns: Skip\nr: Reject\nu: User ready ")
    thread.start_new_thread(key_listener, (ws,))


if __name__ == "__main__":
    ws = websocket.WebSocketApp("ws://127.0.0.1:8080/trolley",
                              on_message = on_message,
                              on_error = on_error,
                              on_close = on_close)
    ws.on_open = on_open
    ws.run_forever()