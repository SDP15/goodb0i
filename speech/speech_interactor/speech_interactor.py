import pyttsx3 as pyttsx
import json
import os
import sys
import datetime
import subprocess as goodboi
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
    def __init__(self, state_file='interactor_states.json'):
        log_filename = now.strftime("%Y-%m-%d-%H%M%S")

        # Log is place in folder associated with test number
        if len(sys.argv) > 1:
            test_num = sys.argv[1]
            self.log_filepath = "logs/{:}/{:}.txt".format(test_num, log_filename)
        else:
            self.log_filepath = "logs/{:}.txt".format(log_filename)

        print(self.log_filepath)
        self.possible_states = json.load(open(state_file,'r'))
        self.next_state('init')
        self.react("n/a")
        self.listen()

    def next_state(self, state):
        self.state = state
        self.options = self.possible_states[state]
    
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
            with open(self.log_filepath, 'a') as f:
                if "multiple" in word:
                    f.write("## Keyword detection error ##\n")
                    f.write("Multiple keywords detected: {:}\n".format(phrase))
                else:
                    f.write("Keyword detected: {:}\n".format(word))

    def react(self, word):
        # self.say(self.options[word]['reply'])
        self.speak_to_me(self.options[word]['reply'])
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
        with open(self.log_filepath, 'a') as f:
            f.write("{:}\n".format(string))
    
        engine = pyttsx.init()
        engine.setProperty('voices', 2)
        engine.say(string)
        engine.runAndWait()
    
    def speak_to_me(self, string):
        goodboi.run(["mimic/mimic", "-t", string, "-voice", "awb"])

if __name__ == '__main__':
    sint = SpeechInteractor()
