import threading
import RPi.GPIO as GPIO

class WorkerThread(threading.Thread):
    def __init__(self, name, object_instance, work_queue, event_flag=None):
        threading.Thread.__init__(self, name=name)
        self.obj_instance = object_instance
        self.work_queue = work_queue
        self.event_flag = event_flag

    def run(self):
        while True:
            items = self.work_queue.get()
            args = ""
            if type(items) == tuple:
                func = items[0]
                args = items[1:]
            else:
                func = items

            function_to_call = getattr(self.obj_instance, func)

            if len(args) > 0:
                function_to_call(*args)
            else:
                function_to_call()

            if self.event_flag:
                self.event_flag.set()

class ButtonThread(threading.Thread):
    def __init__(self, name, object_instance, controller_queue):
        threading.Thread.__init__(self, name=name)
        self.controller_queue = controller_queue
        self.prev_command = "start"

        GPIO.setmode(GPIO.BCM)
        GPIO.setup(27, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

    def run(self):
        GPIO.add_event_callback(27, GPIO.RISING, callback=self.button_callback, bouncetime=200)
        while True:
            pass
    
    def button_callback(self, channel):
        if self.prev_command == "start":
            self.controller_queue.put(("send_message", "stop", "ev3=True"))
            self.prev_command = "stop"
        elif self.prev_command == "stop":
            self.controller_queue.put(("send_message", "start", "ev3=True"))
            self.prev_command = "stop"


