import pyttsx3 as pyttsx
import json
import os
import sys
import datetime
import subprocess as goodboi
import serial
from pocketsphinx import LiveSpeech, get_model_path


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
    def __init__(self, state_file='interactor_states.json', list_file = 'example_list.json'):
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
            print("Conversation is being logged in: {:}".format(self.log_filepath))

        self.current_location = ""
        self.possible_states = json.load(open(state_file,'r'))
        self.get_shopping_list(list_file)
        self.next_state('init')
        self.react("n/a")

        # Uncomment the code below to test out NFC tag reading
        # state = input("Please enter shopping0. ")
        # self.next_state(state)

        self.listen()


    def next_state(self, state):
        print(state)
        self.state = state
        self.options = self.possible_states[state]

        #These states require other forms of input which is not speech. Wait for the user to input a given text
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
                    print("Current location: {:}".format(self.current_location))

                    # Check if we have arrived at the item
                    if next_item in self.current_location:
                        print("You have arrived at {:}".format(next_item))
                        self.arrived(next_item, "middle") # Need to replace shelf position with that from the JSON
                else:
                    print("Location has not changed.")

        if "arrival" in self.state:
            #replace for barcode scanner info.
            action = input("Please enter scanned.  ")
            if "scanned" in action: 
                item = self.ordered_list[self.list_pointer]
                self.scanned(item)
        
    
    def listen(self):
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
                self.say("Sorry, I have heard more than one possible option. Can you confirm your option?")
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
                        f.write("Multiple keywords detected: {:}\n".format(phrase))
                    else:
                        f.write("Keyword detected: {:}\n".format(word))
                        

    def react(self, word):
        # self.say(self.options[word]['reply'])
        if "cart" in self.state and word == "yes":
            self.cart()
        elif "identify" in self.state and word == "yes":
            self.describe_item()
        else:
            self.say(self.options[word]['reply'])
            self.last_reply = self.options[word]['reply']
            self.next_state(self.options[word]['nextState'])

    def list_options(self):
        self.say("Your options are: %s, and repeat." 
            % ", ".join(str(o) for o in self.options))

    def find_word(self, phrase):
       valid_words = set(phrase) & (set(self.options)|universal_phrases)
       if len(valid_words)==1:
           return list(valid_words)[0]
       elif not valid_words:
           return "n/a"
       else:
           return "multiple"

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
                    
        goodboi.run(["mimic/mimic", "-t", string, "-voice", "awb"])

    def arrived(self, item, shelf):
        response = self.options['arrived']['reply'] + item + self.options['arrived']['second'] + shelf + self.options['arrived']['prompt']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['arrived']['nextState'])

    def scanned(self, item):
        response = self.options['scanned']['reply'] + item + self.options['scanned']['prompt']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['scanned']['nextState'])

    def cart(self):
        currentItem = self.ordered_list[self.list_pointer]
        quantity = self.shopping_list[currentItem]
        self.shopping_list[currentItem] = quantity-1
        if quantity > 1:
            nextState = 'nextState_quantity+'
            response = self.options['yes']['reply_quantity+'] + str(quantity) + self.options['yes']['prompt']
        else:
            nextState = 'nextState_quantity0'
            response = self.options['yes']['reply_quantity0']
            self.list_pointer = self.list_pointer + 1
        
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['yes'][nextState])

    def describe_item(self):
        for tings in self.stuff['products']:
            if tings['product']['name'] == self.ordered_list[self.list_pointer]:
                response = self.options['yes']['price'] + str(tings['product']['price']) + self.options['no']['reply']
        self.say(response)
        self.last_reply = response
        self.next_state(self.options['no']['nextState'])
    
    #Retrieves all the items and quantities on the shopping list.
    def get_shopping_list(self, list_file):
        self.stuff = json.load(open(list_file, 'r'))
        self.list_pointer = 0
        self.shopping_list = {}
        self.ordered_list = []
        for tings in self.stuff['products']:
            self.shopping_list.update({tings['product']['name']:tings['quantity']})
            self.ordered_list.append(tings['product']['name'])
        print(self.shopping_list)


if __name__ == '__main__':
    sint = SpeechInteractor()
