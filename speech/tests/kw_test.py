import pyttsx3 as pyttsx
import json
import os
import datetime

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
        self.listen()

    def listen(self):
        print("Listening...")
        counter = 1
        for sphrase in speech:
            phrase = str(sphrase).lower().split()
            word = self.find_word(phrase)
            print(counter)
            print("You said:", word)

            if word == "start":
                word_index = 0
            elif word == "yes":
                word_index = 1
            elif word == "no":
                word_index = 2
            elif word == "shopping":
                word_index = 3
            elif word == "tutorial":
                word_index = 4
            elif word == "repeat":
                word_index = 5
            elif word == "options":
                word_index = 6
            else:
                word_index = 7
                print(phrase)
                print("Multiple keywords detected")

            # Logs the word that PocketSphinx has detected
            with open("logs/kw_tests/example.txt", 'a') as f:
                f.write("{:},".format(word_index))
                counter += 1

            # Program stops when 20 words have been detected
            if(counter == 20):
                break

    def find_word(self, phrase):
       if len(phrase)==1:
           # return the single string in the set
           return phrase[0]
       else:
           return "multiple"

if __name__ == '__main__':
    sint = SpeechInteractor()
