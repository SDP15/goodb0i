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
    #lm=os.path.join(model_path, 'en-us.lm.bin'),
    lm=False,
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
        phrase = ""
        for sphrase in speech:
            phrase = str(sphrase).lower()
            break
        print("You said:", phrase)
        if 'repeat' in phrase:
            print("repeating")
            self.say("I'll repeat. " +self.last_reply)
            self.listen()
        elif "options" in phrase:
            print("outlining options")
            self.listOptions()
            self.listen()
        notfound = True
        optionindices = {}
        for o in self.options:
            if o in phrase:
                notfound = False
                print(o, "detected")
                if phrase.index(o) > -1:
                    optionindices[o] = phrase.index(o)
        if optionindices:
            self.react(max(optionindices,key=lambda l: optionindices[l]))
            #print("optionindices")
        if notfound and "n/a" in self.options:
            print("no keyword detected")
            self.react("n/a")

    def react(self, phrase):
        self.say(self.options[phrase]['reply'])
        self.last_reply = self.options[phrase]['reply']
        self.nextState(self.options[phrase]['nextState'])
        self.listen()

    def listOptions(self):
        self.say("Your options are: %s, and repeat." 
            % ", ".join(str(o) for o in self.options))


    def say(self, string):
        engine = pyttsx.init()
        engine.say(string)
        engine.runAndWait()

if __name__ == '__main__':
    sint = SpeechInteractor()
