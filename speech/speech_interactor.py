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
from utils.logger import log

script_path = os.path.dirname(os.path.abspath(__file__))
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
    kws=os.path.join(script_path, 'resources/kws.list'),
)


class SpeechInteractor:
    def __init__(self, work_queue, controller_queue, app_accepted_event, app_skipped_event, clear_queue_event, logging):
        state_file = os.path.join(script_path, 'resources/interactor_states.json')
        self.controller_queue = controller_queue
        
        log_filename = now.strftime("%Y-%m-%d-%H%M%S")
        self.logging = logging

        # Conversation is logged if -log is specified in cmd line
        if len(sys.argv) > 1 and "-log" in sys.argv[1]:
            self.logging = True

        log_string = "logs/{:}.txt".format(log_filename)
        self.log_filepath = os.path.join(script_path, log_string)

        if self.logging is True:
            log("Conversation is being logged in: {:}".format(
                self.log_filepath))

        self.possible_states = json.load(open(state_file, 'r'))
        self.next_state('connection')
        self.begin_shopping = False

        self.location_event = threading.Event()
        self.listen_event = threading.Event()
        self.connected_event = threading.Event()
        self.app_accepted_event = app_accepted_event
        self.app_skipped_event = app_skipped_event
        self.clear_queue_event = clear_queue_event

        # Initialise TTS engine
        self.tts_engine = pyttsx.init()
        self.tts_engine.connect("finished-utterance", self.on_finish_utterance)

        self.work_queue = work_queue
        t1 = WorkerThread("SpeechInteractorThread", self, self.work_queue)
        t1.start()

        t2 = threading.Thread(name="ListenThread", target=self.listen)
        t2.start()

    def reset(self):
        self.next_state('connection')
        self.begin_shopping = False
        
        # Clear event flags
        self.location_event.clear()
        self.listen_event.clear()
        self.connected_event.clear()

    def next_state(self, state):
        log("State: {:}".format(state))
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
                log("You said: {:}".format(word))

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
                    log("repeating")
                    self.work_queue.put(("say", self.last_reply, "True"))
                elif "options" in word:
                    self.work_queue.put("list_options")
                elif "multiple" in word:
                    say_this = "Sorry, I have heard more than one possible option. Can you confirm your option?"
                    self.work_queue.put(("say", say_this, "True"))
                elif "n/a" in word:
                    self.work_queue.put("list_options")
                else:
                    log("{:} detected".format(word))
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
            self.next_state(self.options[word]['nextState'])

        if word == "connected":
            self.react("n/a")
            self.connected_event.set()

    def say(self, string, listen):
        # An ugly way of making sure that we don't say push the button when two items are on same shelf.
        if "Please push the button on the trolley to continue to the next item." in string and self.state != "continue":
            return

        self.finished_utt_callback = False
        
        if string != "":
            self.last_reply = string
            # Logs the string that is given to the TTS engine
            if self.logging is True:
                with open(self.log_filepath, 'a') as f:
                    f.write("{:}\n".format(string))

            log("The robot said: {:}".format(string))

            # Only set listen event when we are asking a question
            if listen == "True":
                self.finished_utt_callback = True
                log("Finished utterance callback = True")
            else:
                log("Finished utterance callback = False")
                log("Utterance doesn't require user response.")

            self.tts_engine.setProperty('voices', 2)
            self.tts_engine.say(string)
            self.tts_engine.runAndWait()

    def on_finish_utterance(self, name, completed):
        if self.finished_utt_callback:
            log("Finishing utterance and setting listen event flag.")
            self.listen_event.set()

    def clear_work_queue(self, queue_items):
        if not self.work_queue.empty():
            while self.work_queue.qsize != 0:
                self.work_queue.get()

        if self.work_queue.empty():
            for item in queue_items:
                self.work_queue.put(item)

        # Clear app_skipped event after we finish clearing the queue
        self.app_skipped_event.clear()

        # Setting clear queue event - new queue created.
        log("Setting clear queue event - new queue created.")
        self.clear_queue_event.set()

    # Used to clear listen event if user responds using app
    def clear_listen_event(self):
        log("Clear listen event.")
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
        listen = "True"
        
        time.sleep(2)

        # Check if the item we have scanned is on our shopping list so we can respond appropriately
        if self.scanned_product.get_id() == self.next_item.get_id():
            if self.app_accepted_event.isSet():
                response = self.options['scanned']['add_to_cart1'] + item.get_name() + self.options['scanned']['add_to_cart2']
                listen = "False"
            else:
                response = self.options['scanned']['reply'] + \
                    item.get_name() + self.options['scanned']['prompt']
        else:
            response = self.options['scanned']['reply'] + item.get_name() + \
                self.options['scanned']['diff_item'] + self.options['scanned']['prompt']
        self.say(response, listen)
        self.last_reply = response
        self.next_state(self.options['scanned']['nextState'])

        self.app_accepted_event.clear()

    def cart(self, word, app=False):
        listen = self.options[word]['listen']
        be_quiet = False

        if self.app_skipped_event.isSet():
            return

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

            # If we have no more of an item left to collect, we want to continue shopping
            if quantity >= 1:
                nextState = 'nextState_quantity+'
                response = self.options['yes']['reply_quantity+'] + \
                    str(quantity) + " more " + self.next_item.get_name() + self.options['yes']['prompt']
            else:
                nextState = 'nextState_quantity0'
                response = self.options['yes']['reply_quantity0']
                listen = "False"
                self.work_queue.put(("say", response, listen))
                be_quiet = True

                # Go to continue shopping before prompting user to push button to continue
                self.next_state(self.options[word][nextState])
                self.continue_shopping()
                return

        else:
            if not app:
                self.controller_queue.put(("send_message", "RejectedProduct&", "websocket=True"))
            response = self.options['no']['reply']

        # Only say something if we aren't going to continue shopping
        if not be_quiet:
            self.say(response, listen)
            self.next_state(self.options[word][nextState])

    # Only called if the user decides to go to the next item
    # Sets the current item to the next item on the list and informs the user what item they are
    # going to collect next. 
    def continue_shopping(self):
        prev_item = self.next_item

        if self.next_item.get_quantity() > 0:
            self.controller_queue.put(("send_message", "SkippedProduct&", "websocket=True"))
 
        # Checks if we have any items left on our shopping list
        if self.ordered_list.qsize() == 0:
            response = self.options['yes']['reply_finished']
            nextState = 'finishedState'
        else:
            self.next_item = self.ordered_list.get()
            log("Get next item from ordered list: {:}".format(self.next_item.get_name()))
            response = self.options['yes']['reply'] + self.next_item.get_name()
            nextState = 'nextState'

            self.next_state(self.options['yes'][nextState])

        # Checks if the prev item and the next item on our list is on the same shelf
        if prev_item.get_shelf_number() == self.next_item.get_shelf_number() and nextState != 'finishedState':
            self.next_state("shopping0")
            self.arrived(self.next_item, same_shelf=True)
        else:
            self.controller_queue.put(("clear_continue_event"))
            self.work_queue.put(("say", response, "False"))

    def skip_product(self):
        self.next_item.set_quantity(0)

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
        self.next_state(self.options['no']['nextState'])

    def set_list(self, ordered_list):
        self.ordered_list = ordered_list

    def on_location_change(self):
        self.arrived(self.next_item)





        
