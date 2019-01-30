import pyttsx3 as pyttsx
import json
import os

from pocketsphinx import LiveSpeech, get_model_path

model_path = get_model_path()

speech = LiveSpeech(
    verbose=False,
    sampling_rate=16000,
    buffer_size=2048,
    no_search=False,
    full_utt=False,
    hmm=os.path.join(model_path, 'en-us'),
    lm=os.path.join(model_path, 'en-us.lm.bin'),
    #lm=False,
    dic=os.path.join(model_path, 'cmudict-en-us.dict'),
    kws='kws.list',
)

class SpeechInteractor:
    def __init__(self, state_file='interactor_states.json'):
        self.possible_states = json.load(open(state_file,'r'))
        self.last_reply = "I haven't said anything useful yet."
        self.nextState('init')
        self.listen()

    def nextState(self, state):
        self.state = state
        self.options = self.possible_states[state]
        #print(self.options)
    
    def listen(self):
        for sphrase in speech:
            phrase = str(sphrase).lower()
            print("You said:", phrase)
            if phrase == 'repeat':
                print("repeating")
                say("I'll repeat. " +self.last_reply)
            elif phrase in self.options:
                print(phrase, "detected")
                self.react(phrase)
            elif "n/a" in self.options:
                print("no keyword detected")
                self.react("n/a")
            else:
                print("no keyword detected")
                say(self.possible_states['errorMessage'])
                self.listOptions()
                self.nextState(self.state)

    def react(self, phrase):
        say(self.options[phrase]['reply'])
        self.last_reply = self.options[phrase]['reply']
        self.nextState(self.options[phrase]['nextState'])

    def listOptions(self):
        say("Your options are: %s, and repeat." 
            % ", ".join(str(o) for o in self.options))


def say(string):
    engine = pyttsx.init()
    engine.say(string)
    engine.runAndWait()

if __name__ == '__main__':
    sint = SpeechInteractor()
