import pyttsx3 as pyttsx
import json
import os
import sys
import math
import datetime
import subprocess as sp
import requests
import serial
import websocket
from pocketsphinx import LiveSpeech, get_model_path
try:
    import thread
except ImportError:
    import _thread as thread
import time

# from socket_control import on_message, on_error, on_open, on_close, send_message, initialise_socket


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
    kws='kws.list',
)


class SpeechInteractor:
    def __init__(self, controller, state_file='interactor_states.json', list_file='list.json'):
        self.controller = controller
        self.ws = self.controller.get_ws()
        # self.ws = initialise_socket()
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
        print(self.possible_states)
        self.get_shopping_list(list_file)
        self.next_state('connection')
        self.react("n/a")

        # Uncomment the code below to test out NFC tag reading
        # state = input("Please enter shopping0. ")
        # self.next_state(state)

        thread.start_new_thread(self.listen, ())#
        # while True:
        #     pass
        # self.listen()
        # self.listen()

    def next_state(self, state):
        print(state)
        self.state = state
        self.options = self.possible_states[state]

        # These states require other forms of input which is not speech. Wait for the user to input a given text
        if "shopping0" in self.state:
            ser = serial.Serial('/dev/ttyACM0', 9600)

            # Uncomment line below once NFC tags have item strings on them
            #next_item = self.ordered_list[self.list_pointer]

            next_item = "TRACK_NFCTYPE4A"
            new_location = ""

            while(True):
                new_location = ser.readline().decode('ascii')

                # Check if our location has changed and if so update current location
                if new_location not in self.current_location:
                    self.current_location = new_location
                    print("Current location: {:}".format(
                        self.current_location))

                    # Check if we have arrived at the item
                    if next_item in self.current_location:
                        print("You have arrived at {:}".format(next_item))
                        # Need to replace shelf position with that from the JSON
                        self.arrived(next_item, "middle")
                else:
                    print("Location has not changed.")

        if "arrival" in self.state:
            # replace for barcode scanner info.
            action = input("Please enter scanned.  ")
            if "scanned" in action:
                item = self.ordered_list[self.list_pointer]
                self.scanned(item)

    def listen(self, *arg):
        print("listening")
        for sphrase in speech:
            phrase = str(sphrase).lower().split()
            word = self.find_word(phrase)
            print("You said:", word)

            if "repeat" in word:
                print("repeating")
                self.say(self.last_reply)
            elif "options" in word:
                self.list_options()
            elif "multiple" in word:
                print("Multiple keywords detected")
                self.say(
                    "Sorry, I have heard more than one possible option. Can you confirm your option?")
            elif "n/a" in word:
                print("no keyword detected")
                self.list_options()
            else:
                print(word, "detected")
                self.react(word)

            # Logs the word/words that PocketSphinx has detected
            if self.logging is True:
                with open(self.log_filepath, 'a') as f:
                    if "multiple" in word:
                        f.write("## Keyword detection error ##\n")
                        f.write(
                            "Multiple keywords detected: {:}\n".format(phrase))
                    else:
                        f.write("Keyword detected: {:}\n".format(word))

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
        # self.say(self.options[word]['reply'])
        if "cart" in self.state and word == "yes" or word == "no":
            self.cart(word)
        elif "identify" in self.state and word == "yes":
            self.describe_item()
        elif "continue" in self.state and word == "yes":
            self.continue_shopping()
        elif "init" in self.state and word == "start":
            self.start_state(word)
        else:
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
            item + self.options['scanned']['prompt']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['scanned']['nextState'])

    def cart(self, word):
        current_item = self.ordered_list[self.list_pointer]
        quantity = self.shopping_list[current_item]
        self.shopping_list[current_item] = quantity-1

        if "yes" in word:
            self.controller.send_message(self.ws, "PA&")
            if quantity > 1:
                nextState = 'nextState_quantity+'
                response = self.options['yes']['reply_quantity+'] + \
                    str(quantity-1) + self.options['yes']['prompt']
            else:
                nextState = 'nextState_quantity0'
                response = self.options['yes']['reply_quantity0']

        else:
            self.controller.send_message(self.ws, "PR&")
            response = self.options['no']['reply']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options[word][nextState])

    def describe_item(self):
        for tings in self.stuff['products']:
            if tings['product']['name'] == self.ordered_list[self.list_pointer]:
                cost = tings['product']['price']
                # Format the output to tell the user the price in pounds and pence.
                if cost >= 1:
                    pounds = math.floor(cost)
                    pence = math.floor((cost - pounds) * 100)
                    total = str(pounds) + " pounds and " + \
                        str(int(pence)) + " pence"
                else:
                    pence = cost * 100
                    total = str(int(pence)) + " pence"
        response = self.options['yes']['price'] + \
            total + self.options['no']['reply']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['no']['nextState'])

    # Retrieves all the items and quantities on the shopping list.

    def get_shopping_list(self, list_file):
        r = requests.get("http://127.0.0.1:8080/lists/load/1234567")
        json  = r.json()
        # sp.run(['wget', '-O', 'list.json',
        #         'http://127.0.0.1:8080/lists/load/1234567'])
        # print(open('list.json', 'r').read())
        print("JSON is " + str(json))
        self.stuff = json
        self.list_pointer = 0
        self.shopping_list = {}
        self.ordered_list = []
        for tings in self.stuff['products']:
            self.shopping_list.update(
                {tings['product']['name']: tings['quantity']})
            self.ordered_list.append(tings['product']['name'])
        print(self.shopping_list)

    # Sets the current item to the next item on the list and informs the user what item they are
    # going to collect next.

    def continue_shopping(self):
        self.list_pointer = self.list_pointer + 1
        next_product = self.ordered_list[self.list_pointer]
        response = self.options['yes']['reply'] + next_product
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['yes']['nextState'])

    # Sends the server a message that the user is at this cart and ready to start

    def start_state(self, word):
        self.controller.send_message(self.ws, "UR&")
        self.say(self.options[word]['reply'])
        self.last_reply = self.options[word]['reply']
        self.next_state(self.options[word]['nextState'])


