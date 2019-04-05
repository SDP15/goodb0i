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
    def __init__(self, work_queue, controller_queue, clear_queue_event):
        state_file = os.path.join(script_path, 'resources/interactor_states.json')
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
            log("Conversation is being logged in: {:}".format(
                self.log_filepath))


        self.location_event = threading.Event()
        self.clear_queue_event = clear_queue_event

        # Initialise TTS engine
        self.tts_engine = pyttsx.init()
        self.tts_engine.connect("finished-utterance", self.on_finish_utterance)
        log("Callback connected.")

        self.work_queue = work_queue
        t1 = WorkerThread("SpeechInteractorThread", self, self.work_queue)
        t1.start()


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
        if self.app_skipped_event.isSet():
            self.clear_queue_event.set()

        if self.finished_utt_callback:
            log("Finishing utterance and setting listen event flag.")
            self.listen_event.set()

    def reset(self):
        self.begin_shopping = False

        # Clear event flags
        self.location_event.clear()