import datetime
import json
import math
import os
import queue
import subprocess as sp
import sys
import threading
import time

import pyttsx3 as pyttsx
import requests
import serial
import websocket
from pocketsphinx import LiveSpeech, get_model_path

from utils.custom_threads import WorkerThread
from utils.product import Product

model_path = get_model_path()
now = datetime.datetime.now()
universal_phrases = {'repeat', 'options'}

speech = LiveSpeech(
    verbose=False,
    sampling_rate=16000,
    buffer_size=2048,
    no_search=False,
    full_utt=False,
    hmm=os.path.join(model_path, 'en-us'),
    lm=False,
    dic=os.path.join(model_path, 'cmudict-en-us.dict'),
    kws='resources/kws.list',
)


class SpeechInteractor:
    def __init__(self, websocket_instance, work_queue, state_file='resources/interactor_states.json'):
        self.ws = websocket_instance
        
        log_filename = now.strftime("%Y-%m-%d-%H%M%S")
        self.logging = False

        # Conversation is logged if -log is specified in cmd line
        if len(sys.argv) > 1 and "-log" in sys.argv[1]:
            self.logging = True

        # Log is placed in folder associated with test number
        if len(sys.argv) > 2:
            test_num = sys.argv[2]
            self.log_filepath = "logs/{:}/{:}.txt".format(
                test_num, log_filename)
        else:
            self.log_filepath = "logs/{:}.txt".format(log_filename)

        if self.logging is True:
            print("Conversation is being logged in: {:}".format(
                self.log_filepath))

        self.current_location = ""
        self.possible_states = json.load(open(state_file, 'r'))
        self.next_state('connection')
        self.react("n/a")

        self.scanned_product = None

        # For testing "AppAccepted" message from server
        # self.next_state("identify")
        # self.react("no")

        self.location_event = threading.Event()
        self.listen_event = threading.Event()

        self.work_queue = work_queue
        t1 = WorkerThread("SpeechInteractorThread", self, self.work_queue, self.listen_event)
        t1.start()

        t2 = threading.Thread(name="ListenThread", target=self.listen)
        t2.start()

    def next_state(self, state):
        # print(state)
        self.state = state
        self.options = self.possible_states[state]

        if "shopping0" in self.state:
            next_item = self.ordered_list[0]

            # Start thread to listen for location changes
            t2 = threading.Thread(name='LocationListenerThread', target=self.on_location_change, args=(next_item,))
            t2.start()
            
            # Block current thread while waiting for location change
            self.location_event.wait()
            self.location_event.clear()

            # Need to replace shelf position with that from the JSON
            self.arrived(next_item, "middle")

        if "arrival" in self.state:
            # replace for barcode scanner info.
            print("Waiting for item to be scanned.")
            while self.scanned_product is None:
                pass
            self.scanned(self.scanned_product)




    def listen(self, *arg):
        print("listening")

        for sphrase in speech:
            if self.listen_event.isSet():
                self.listen_event.clear()

            phrase = str(sphrase).lower().split()
            word = self.find_word(phrase)
            print("You said:", word)

            print("Current thread calling listen: {:}".format(threading.current_thread().name))

            # Logs the word/words that PocketSphinx has detected
            if self.logging is True:
                with open(self.log_filepath, 'a') as f:
                    if "multiple" in word:
                        f.write("## Keyword detection error ##\n")
                        f.write(
                            "Multiple keywords detected: {:}\n".format(phrase))
                    else:
                        f.write("Keyword detected: {:}\n".format(word))

            if "repeat" in word:
                print("repeating")
                self.work_queue.put(("say", self.last_reply))
            elif "options" in word:
                self.work_queue.put("list_options")
            elif "multiple" in word:
                print("Multiple keywords detected")
                say_this = "Sorry, I have heard more than one possible option. Can you confirm your option?"
                self.work_queue.put(("say", say_this))
            elif "n/a" in word:
                print("no keyword detected")
                self.work_queue.put("list_options")
            else:
                print(word, "detected")
                self.work_queue.put(("react", word))

            self.listen_event.wait()

    def find_word(self, phrase):
        valid_words = set(phrase) & (set(self.options) | universal_phrases)
        if len(valid_words) == 1:
            return list(valid_words)[0]
        elif not valid_words:
            return "n/a"
        else:
            return "multiple"

    def list_options(self):
        self.say("Your options are: %s, and repeat."
                 % ", ".join(str(o) for o in self.options))

    def react(self, word):
        if "cart" in self.state and (word == "yes" or word == "no"):
            print("React cart")
            self.cart(word)
        elif "identify" in self.state and word == "yes":
            self.describe_item()
        elif "continue" in self.state and word == "yes":
            self.continue_shopping()
        elif "init" in self.state and word == "start":
            self.start_state(word)
        else:
            print("React else")
            print(word)
            self.say(self.options[word]['reply'])
            self.last_reply = self.options[word]['reply']
            self.next_state(self.options[word]['nextState'])

    def say(self, string):
        # Logs the string that is given to the TTS engine
        if self.logging is True:
            with open(self.log_filepath, 'a') as f:
                f.write("{:}\n".format(string))

        engine = pyttsx.init()
        engine.setProperty('voices', 2)
        engine.say(string)
        engine.runAndWait()

    def speak_to_me(self, string):
        # Logs the string that is given to the TTS engine
        if self.logging is True:
            with open(self.log_filepath, 'a') as f:
                f.write("{:}\n".format(string))

        sp.run(["mimic/mimic", "-t", string, "-voice", "awb"])

    def arrived(self, item, shelf):
        response = self.options['arrived']['reply'] + item + \
            self.options['arrived']['second'] + \
            shelf + self.options['arrived']['prompt']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['arrived']['nextState'])

    def scanned(self, item):
        response = self.options['scanned']['reply'] + \
            item.get_name + self.options['scanned']['prompt']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['scanned']['nextState'])
        

    def cart(self, word, app=False):
        if "yes" in word:
            if not app:
                self.ws.send("AcceptedProduct&")

            quantity = self.ordered_list[0].get_quantity

            if self.ordered_list[0] == self.scanned_product:
                quantity-=1
                self.ordered_list[0].set_quantity(quantity)
            if quantity > 1:
                nextState = 'nextState_quantity+'
                response = self.options['yes']['reply_quantity+'] + \
                    str(quantity) + " more " + self.ordered_list[0].get_name() + self.options['yes']['prompt']
            else:
                nextState = 'nextState_quantity0'
                response = self.options['yes']['reply_quantity0']

        else:
            if not app:
                self.ws.send("RejectedProduct&")

        response = self.options['no']['reply']       
        self.say(response)
        self.last_reply = response
        self.next_state(self.options[word][nextState])

    def describe_item(self):
        cost = self.scanned_product.get_price()
        # Format the output to tell the user the price in pounds and pence.
        pounds = math.floor(cost)
        pence = math.floor((cost - pounds) * 100)
        total = (cost >= 1) * (str(pounds) + " pounds and ") + \
                bool(pence) * ( str(int(pence)) + (" penny" if pence == 1 else " pence") )
        response = self.options['yes']['price'] + \
            total + self.options['no']['reply']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['no']['nextState'])


    def set_list(self, ordered_list):
        self.ordered_list = ordered_list


    def set_scanned_item(self, scanned_product):
        self.scanned_product = scanned_product



    # Only called if the user decides to go to the next item
    # Sets the current item to the next item on the list and informs the user what item they are
    # going to collect next. 
    def continue_shopping(self):
        if self.ordered_list[0].get_quantity() > 0:
            self.ws.send("SkippedProduct&")
        del self.ordered_list[0]
        if len(self.ordered_list) == 0:
            response = self.options['yes']['reply_finished']
            nextState = 'finishedState'
        else:
            response = self.options['yes']['reply'] + self.ordered_list[0]
            nextState = 'nextState'
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['yes'][nextState])

    # Sends the server a message that the user is at this cart and ready to start
    def start_state(self, word):
        self.ws.send("UserReady&")
        self.say(self.options[word]['reply'])
        self.last_reply = self.options[word]['reply']
        self.next_state(self.options[word]['nextState'])
    
    def on_location_change(self, next_item):
        ser = serial.Serial('/dev/ttyACM0', 9600)
        new_location = ""

        while not self.location_event.isSet():
            new_location = ser.readline().decode('ascii')

            # Check if our location has changed and if so update current location
            if new_location not in self.current_location:
                self.current_location = new_location
                print("Current location: {:}".format(
                    self.current_location))

                # Check if we have arrived at the item
                if next_item in self.current_location:
                    print("You have arrived at {:}".format(next_item))
                    self.location_event.set()
            else:
                print("Location has not changed.")
