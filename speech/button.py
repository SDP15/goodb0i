import RPi.GPIO as GPIO
import threading

GPIO.setmode(GPIO.BCM)
GPIO.setup(27, GPIO.IN, pull_up_down = GPIO.PUD_DOWN)

class Button:
    

    def __init__(self, button_queue):
        self.button_queue = button_queue
        t1 = threading.Thread(name="ButtonThread", target=self.is_button_pressed)
        t1.start()

    def is_button_pressed(self):
        old_input = 0
        while True:
            input = GPIO.input(27)
            if(old_input != input):
                if input == GPIO.HIGH:
                    self.button_queue.put(("set_button_value", 1))
                else:
                    self.button_queue.put(("set_button_value", 0))
            old_input = input
            
