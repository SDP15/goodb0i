import speech_recognition as sr 
import pyttsx3 as pyttsx
import json
import os
import datetime


universal_phrases = {'repeat', 'options'}
now = datetime.datetime.now()


from pocketsphinx import LiveSpeech, AudioFile, get_model_path


model_path = get_model_path()



# speech = LiveSpeech(
#     verbose=False,
#     sampling_rate=16000,
#     buffer_size=2048,
#     no_search=False,
#     full_utt=False,
#     hmm=os.path.join(model_path, 'en-us'),
#     lm=os.path.join(model_path, 'en-us.lm.bin'),
#     #lm=False,
#     dic=os.path.join(model_path, 'cmudict-en-us.dict'),
#     kws='kws.list',
# )

    # def listen(self):
    #     for sphrase in speech:
    #         phrase = str(sphrase).lower()
    #         print("You said:", phrase)
    #         if phrase == 'repeat':
    #             print("repeating")
    #             say("I'll repeat. " +self.last_reply)
    #         elif phrase in self.options:
    #             print(phrase, "detected")
    #             self.react(phrase)
    #         elif "n/a" in self.options:
    #             print("no keyword detected")
    #             self.react("n/a")
    #         else:
    #             print("no keyword detected")
    #             say(self.all_states['errorMessage'])
    #             self.listOptions()
    #             self.nextState(self.state)

class SpeechInteractor:
    def __init__(self, state_file='interactor_states.json'):
        self.log_filename = "logs/{:}.txt".format(now.strftime("%Y-%m-%d-%H%M%S")) # file name can't contain ':'
        print(self.log_filename)                                                   # (at least on Windows)
        self.phrase_file_name = "phrase.raw"
        self.set_up_recognizer()
        self.set_up_pyttsx()
        self.all_states = json.load(open(state_file,'r'))
        self.last_reply = "I haven't said anything useful yet."
        self.nextState('init')
        print('Interactor initialised.')
        self.record_phrase()        
        # self.listen()

    def set_up_recognizer(self):
        self.recognizer = sr.Recognizer()
        #~~~~~~Comment out the 2 lines below to do dynamic thresholding eg get rid of ambient sound
        self.recognizer.dynamic_energy_threshold = False
        self.recognizer.energy_threshold = 1000 #600

        #~~~~~This could should be used before each phrase recording in order to use the dynamic energy threshold
        #with sr.Microphone(device_index=0, sample_rate=16000) as source:
        #     self.recognizer.adjust_for_ambient_noise(source, duration=1)
        
    def set_up_pyttsx(self):
        self.engine = pyttsx.init()
        self.engine.setProperty('voice', self.engine.getProperty('voices')[3].id)
        self.engine.setProperty('rate', 170)

    def nextState(self, state):
        self.state = state
        self.options = self.all_states[state]
        #print(self.options)


    def listOptions(self):
        self.say("Your options are: %s, options and repeat." 
            % ", ".join(str(o) for o in self.options))

    
    def record_phrase(self, fname="phrase.raw"):
      # We dont need the phrase just was my wee test but i changed it if its annoying i can delete it
        # No just move it
        # Yea, sry
        # obtain audio from the microphone
        # Hey Chris just adding this here to debug
        # ok, Ciaran
        # The code sharing thing still syncs the scrolling for some reason

        # Yeah dunno when i will be turning off the old laptop 
        # That'd be fine as well if you'd prefer that.
        with sr.Microphone(device_index=0, sample_rate=16000) as source:
            # self.recognizer.adjust_for_ambient_noise(source, duration=1)
            print('Energy threshold:', self.recognizer.energy_threshold)
            print('Listening')
            audio = self.recognizer.listen(source) #, phrase_time_limit=5)
            with open("phrase.raw", "wb") as f:
                f.write(audio.get_raw_data())
                print('Audio recorded')
            speech = AudioFile(
                audio_file=fname,
                verbose=False,
                # sampling_rate=16000,
                buffer_size=2048,
                no_search=False,
                full_utt=False,
                hmm=os.path.join(model_path, 'en-us'),
                lm=False,
                dic=os.path.join(model_path, 'cmudict-en-us.dict'),
                kws='kws.list',
            )

            # The recogniser below is way too slow
            # sphrase = self.recognizer.recognize_sphinx(audio, keyword_entries=[
            # ("start", 1e-1),
            # ("yes", 5e-1),
            # ("no", 5e-1),
            # ("shopping", 1e-3),
            # ("tutorial", 1e-3),
            # ("repeat", 1e-2),
            # ("options", 1e-3)])

            words = set()
            for word in speech: #str(sphrase).lower().split(' '):
                words.add(str(word).lower().strip())
        
        # if len(words) > 0:
        # we need to make a decision at this stage even if listen() returns a phrase w/o a keyword
        self.printlog("\nYou said: {}".format(' '.join(list(words))) if words else "\nYou didn't say a keyword.")
        output = self.choose_one_from_list(words)
        self.make_decision(output)
        
        #with open(self.path_prefix + "phrase.raw", "wb") as f:

        # f.write(audio.get_raw_data())
        # print("...")


    def make_decision(self, word):
      if word == 'repeat':
        print("repeating")
        self.say("I'll repeat. " +self.last_reply)
        self.record_phrase()

      elif word == 'options':
        print("options")
        self.listOptions()
        self.record_phrase()

      elif word == "Multiple":
        print("Multiple options detected")
        self.say("Sorry, I have heard more than one possible option. Can you repeat the correct option?")
        self.record_phrase()
      # elif word == "n/a" and "n/a" in self.options:
      #     print("no keyword detected")
      #     self.react("n/a")
      elif word in self.options:
        # Since n/a is in option and is the word then it shouldnt be needed
          print(word, "detected")
          self.react(word)
      else:
          print("no keyword detected")
          self.say(self.all_states['errorMessage'])
          self.listOptions()
          self.nextState(self.state)
          self.record_phrase()



    def react(self, word):
      self.say(self.options[word]['reply'])
      self.last_reply = self.options[word]['reply']
      self.nextState(self.options[word]['nextState'])
      self.record_phrase()


    def choose_one_from_list(self, words, allow_multiple=False):
        detected_options = set(words) & (set(self.options)|universal_phrases)
        if not detected_options:
            # return None
            return "n/a"
        elif len(detected_options)==1:
            # return the single string in the set
            return list(detected_options)[0]
        else:
            return "Multiple"
        # for word in words:
        #     if word in self.options:
        #         detected_options.add(word)
        #         if len(detected_options) == 0:
        #             return None
        #         elif len(detected_options) == 1 or allow_multiple:
        #             return list(detected_options)[0]
        #         else:
        #             return None


    def say(self, string):
        self.printlog("{:}\n".format(string))
        # engine = pyttsx.init()
        self.engine.say(string)
        self.engine.runAndWait()

    # Shorthand function for logging that also prints the logging in the terminal
    def printlog(self, msg):
        with open(self.log_filename, 'a') as f:
          f.write(msg)
        print("LOGGED:", msg)

if __name__ == '__main__':
    speech = SpeechInteractor()