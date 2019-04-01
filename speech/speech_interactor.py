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
    def __init__(self, work_queue, controller_queue, state_file='resources/interactor_states.json'):
        self.controller_queue = controller_queue
        
        log_filename = now.strftime("%Y-%m-%d-%H%M%S")
        self.logging = False

        # Conversation is logged if -log is specified in cmd line
        if len(sys.argv) > 1 and "-log" in sys.argv[1]:
            self.logging = True

        # Log is placed in folder associated with test number
        if len(sys.argv) > 2:
            test_num = sys.argv[2]
            self.log_filepath = "logs/{:}/{:}.txt".format(test_num, log_filename)
        else:
            self.log_filepath = "logs/{:}.txt".format(log_filename)

        if self.logging is True:
            print("Conversation is being logged in: {:}".format(
                self.log_filepath))

        self.current_location = ""
        self.possible_states = json.load(open(state_file, 'r'))
        self.next_state('connection')
        self.begin_shopping = False

        self.location_event = threading.Event()
        self.listen_event = threading.Event()
        self.connected_event = threading.Event()

        self.work_queue = work_queue
        t1 = WorkerThread("SpeechInteractorThread", self, self.work_queue)
        t1.start()

        t2 = threading.Thread(name="ListenThread", target=self.listen)
        t2.start()

    def next_state(self, state):
        self.state = state
        self.options = self.possible_states[state]

        if "shopping0" in self.state:
            if not self.begin_shopping:
                self.controller_queue.put(("send_message", "UserReady&", "websocket=True"))
                self.begin_shopping = True
                self.next_item = self.ordered_list.get()

    def listen(self, *arg):
        for sphrase in speech:
            # Only perform SR logic if listen event is set.
            if self.listen_event.isSet():
                self.listen_event.clear()

                phrase = str(sphrase).lower().split()
                word = self.find_word(phrase)
                print("You said:", word)

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
                    self.work_queue.put(("say", self.last_reply, "True"))
                elif "options" in word:
                    self.work_queue.put("list_options")
                elif "multiple" in word:
                    print("Multiple keywords detected")
                    say_this = "Sorry, I have heard more than one possible option. Can you confirm your option?"
                    self.work_queue.put(("say", say_this, "True"))
                elif "n/a" in word:
                    print("no keyword detected")
                    self.work_queue.put("list_options")
                else:
                    print(word, "detected")
                    self.work_queue.put(("react", word))

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
                 % ", ".join(str(o) for o in self.options), "True")

    def react(self, word):
        if "cart" in self.state and (word == "yes" or word == "no"):
            self.cart(word)
        elif "identify" in self.state and word == "yes":
            self.describe_item()
        elif "continue" in self.state and word == "yes":
            self.continue_shopping()
        else:
            self.say(self.options[word]['reply'], self.options[word]['listen'])
            self.last_reply = self.options[word]['reply']
            self.next_state(self.options[word]['nextState'])

        if "connected" in word:
            self.react("n/a")
            self.connected_event.set()

    def say(self, string, listen):
        if string != "":
            # Logs the string that is given to the TTS engine
            if self.logging is True:
                with open(self.log_filepath, 'a') as f:
                    f.write("{:}\n".format(string))

            engine = pyttsx.init()

            # Only set listen event when we are asking a question
            if listen == "True":
                engine.connect("finished-utterance", self.on_finish_utterance)
            else:
                print("Utterance doesn't require user response.")

            engine.setProperty('voices', 2)
            engine.say(string)
            engine.runAndWait()

    def on_finish_utterance(self, name, completed):
        print("Finishing utterance and setting listen event flag.")
        self.listen_event.set()

    # Used to clear listen event if user responds using app
    def clear_listen_event(self):
        if self.listen_event.isSet():
            self.listen_event.clear()

    def arrived(self, item, same_shelf=False):
        if same_shelf:
            response = self.options['arrived']['reply_ss'] + item.get_name() + \
                self.options['arrived']['reply_ss2'] + self.options['arrived']['second'] + \
                item.get_shelf_position() + self.options['arrived']['prompt']
        else:
            response = self.options['arrived']['reply'] + item.get_name() + \
                self.options['arrived']['second'] + \
                item.get_shelf_position() + self.options['arrived']['prompt']
        self.say(response, self.options['arrived']['listen'])
        self.last_reply = response
        self.next_state(self.options['arrived']['nextState'])

    def scanned(self, item):
        self.scanned_product = item

        # Check if the item we have scanned is on our shopping list so we can respond appropriately
        if self.scanned_product.get_id() == self.next_item.get_id():
            response = self.options['scanned']['reply'] + \
                item.get_name() + self.options['scanned']['prompt']
        else:
            response = self.options['scanned']['reply'] + item.get_name() + \
                self.options['scanned']['diff_item'] + self.options['scanned']['prompt']
        self.say(response, self.options['scanned']['listen'])
        self.last_reply = response
        self.next_state(self.options['scanned']['nextState'])

    def cart(self, word, app=False):
        listen = self.options[word]['listen']

        if "yes" in word:
            if not app:
                self.controller_queue.put(("send_message", "AcceptedProduct&", "websocket=True"))

            quantity = self.next_item.get_quantity()

            if self.next_item.get_id() == self.scanned_product.get_id():
                quantity-=1
                self.next_item.set_quantity(quantity)
            else:
                # Reminds user of where the item they are looking for is on the shelf next to them
                nextState = 'nextState_diff_item'
                response = self.options['yes']['reply_diff_item'] + self.next_item.get_shelf_position() + \
                    self.options['yes']['reply_diff_item2']
                listen = "False"

            if quantity >= 1:
                nextState = 'nextState_quantity+'
                response = self.options['yes']['reply_quantity+'] + \
                    str(quantity) + " more " + self.next_item.get_name() + self.options['yes']['prompt']
            else:
                nextState = 'nextState_quantity0'
                response = self.options['yes']['reply_quantity0']

        else:
            if not app:
                self.controller_queue.put(("send_message", "RejectedProduct&", "websocket=True"))
            response = self.options['no']['reply']       
        self.say(response, listen)
        self.last_reply = response
        self.next_state(self.options[word][nextState])

    def describe_item(self):
        cost = self.scanned_product.get_price()
        # Format the output to tell the user the price in pounds and pence.
        pounds = int(cost)
        pence = round((cost - pounds) * 100)
        total = bool(pounds) * (str(pounds) + " pound" + (pounds >= 2)*"s") + bool(pounds)*bool(pence) * (" and ") + \
                bool(pence) * ( str(int(pence)) + (" penny" if pence == 1 else " pence") ) + (pounds+pence == 0) * "nothing"
        response = self.options['yes']['price'] + \
            total + self.options['no']['reply']
        self.say(response, self.options['no']['listen'])
        self.last_reply = response
        self.next_state(self.options['no']['nextState'])

    def set_list(self, ordered_list):
        self.ordered_list = ordered_list

    # Only called if the user decides to go to the next item
    # Sets the current item to the next item on the list and informs the user what item they are
    # going to collect next. 
    def continue_shopping(self):
        prev_item = self.next_item
        print("Prev item: {:}".format(prev_item.get_name()))

        if self.next_item.get_quantity() > 0:
            self.controller_queue.put(("send_message", "SkippedProduct&", "websocket=True"))

        # Checks if we have any items left on our shopping list
        if self.ordered_list.qsize() == 0:
            response = self.options['yes']['reply_finished']
            nextState = 'finishedState'
        else:
            self.next_item = self.ordered_list.get()
            print("Get next item from ordered list: {:}".format(self.next_item.get_name()))
            response = self.options['yes']['reply'] + self.next_item.get_name()
            nextState = 'nextState'

        self.next_state(self.options['yes'][nextState])

        # Checks if the prev item and the next item on our list is on the same shelf
        if prev_item.get_shelf_number() == self.next_item.get_shelf_number() and nextState != 'finishedState':
            self.arrived(self.next_item, same_shelf=True)
        else:
            self.say(response, "False")
            self.last_reply = response
    
    def on_location_change(self):
        self.arrived(self.next_item)
        
