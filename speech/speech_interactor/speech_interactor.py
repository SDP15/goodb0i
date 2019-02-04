import pyttsx3 as pyttsx
import json
import os
import datetime

from pocketsphinx import LiveSpeech, get_model_path

model_path = get_model_path()
now = datetime.datetime.now()

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
        self.log_filename = "logs/{:}.txt".format(now.strftime("%Y-%m-%d-%H%M%S")) # file name can't contain ':'
        print(self.log_filename)                                                   # (at least on Windows)
        self.possible_states = json.load(open(state_file,'r'))
        self.last_reply = "I haven't said anything useful yet."
        self.nextState('init')
        self.react("n/a")
        self.listen()

    def nextState(self, state):
        self.state = state
        self.options = self.possible_states[state]
    
    def listen(self):
        for sphrase in speech:
            phrase = str(sphrase).lower()
            print("You said:", phrase)

            # Logs the phrase that PocketSphinx has detected
            with open(self.log_filename, 'a') as f:
                f.write("Phrase detected: {:}\n".format(phrase))

            notfound = True
            for o in self.options:
                if o in phrase:
                    notfound = False
                    print(o, "detected")
                    self.react(o)

            if "repeat" in phrase:
                print("repeating")
                self.say("I'll repeat. " + self.last_reply)
            elif "options" in phrase:
                self.listOptions()
            elif notfound:
                print("no keyword detected")
                self.listOptions()



    def react(self, phrase):
        self.say(self.options[phrase]['reply'])
        self.last_reply = self.options[phrase]['reply']
        self.nextState(self.options[phrase]['nextState'])

    def listOptions(self):
        self.say("Your options are: %s, and repeat." 
            % ", ".join(str(o) for o in self.options))


    def say(self, string):
        # Logs the string that is given to the TTS engine
        with open(self.log_filename, 'a') as f:
            f.write("{:}\n".format(string))
    
        engine = pyttsx.init()
        engine.say(string)
        engine.runAndWait()

if __name__ == '__main__':
    sint = SpeechInteractor()
