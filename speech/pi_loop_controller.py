import os
import queue
import socket
import sys
import threading
import time
import getopt
import subprocess

import requests
import serial

from simple_speech_interactor import SpeechInteractor
from utils.custom_threads import WorkerThread, ButtonThread, QRThread
from utils.custom_threads import WorkerThread
from utils.sockets import WebSocket, TCPSocket
from utils.logger import log


class PiController:
    def __init__(self):
        self.ev3_ip = "192.168.105.108"
        self.ev3_port = 6081
        self.skip_tut = False

        # Parse command line options/arguments
        self.parse_opts()

        # Data structures for worker threads
        self.controller_queue = queue.Queue()
        self.speech_interactor_queue = queue.Queue()

        self.ev3 = TCPSocket(self.ev3_ip, self.ev3_port, self.controller_queue)
        self.ev3_commands = []

        self.clear_queue_event = threading.Event()
        self.speech_interactor = SpeechInteractor(self.speech_interactor_queue, self.controller_queue,)

        # Thread runs a given function and it's arguments (if given any) from the work queue
        t1 = WorkerThread("PiControllerThread", self, self.controller_queue)
        t1.start()

        # Controls whether user is allowed to press the start/stop button.
        self.button_event = threading.Event()
        self.continue_event = threading.Event()
        self.continue_event.set()

        t2 = ButtonThread("ButtonThread", self.controller_queue, self.button_event, self.continue_event)
        t2.start()

    def on_message(self, message):
        if "detected-marker" in message:
            pass



    def enqueue_ev3_commands(self):
        self.ev3.send("stop")
        self.ev3.send("resume-from-stop-marker")
        self.ev3.send("clear-queue")

        for command in self.ev3_commands:
            self.ev3.send(command)

        self.ev3.send("start")

    def parse_opts(self):
        if len(sys.argv) > 1:
            try:
                long_options = ["skiptut", "mock-ev3", "local-server", "server-address=", "help"]
                opts, _ = getopt.getopt(sys.argv[1:], "", long_options)
            except getopt.GetoptError as err:
                # print help information and exit:
                print(str(err))  # will print something like "option -a not recognized"
                sys.exit()
            for opt, arg in opts:
                if opt in "--skiptut":
                    log("Skipping tutorial...")
                    self.skip_tut = True
                elif opt in "--mock-ev3":
                    log("Mocking EV3")
                    subprocess.Popen(['nc', '-l', '4000'])
                    self.ev3_ip = "localhost"
                    self.ev3_port = 4000
                elif opt in "--local-server":
                    self.server_address = "127.0.0.1:8080"
                elif opt in "--server-address":
                    self.server_address = str(arg)
                elif opt in "--help":
                    print("Options: {:}".format(long_options))
                    print("Options must be prepended with \"--\".")
                    print("Additionally, options with an \"=\" require an argument to be given after the option flag.")
                    sys.exit()

    def clear_continue_event(self):
        self.continue_event.clear()
        log("Continue event cleared")

    def reset(self):
        # Clear speech interactor queue
        self.clear_queue(self.speech_interactor_queue)

        # Reset speech interactor
        self.speech_interactor.reset()

    def clear_queue(self, queue):
        if not queue.empty():
            while queue.qsize != 0:
                queue.get()

        if queue.empty():
            log("Work queue EMPTY")


PiController()