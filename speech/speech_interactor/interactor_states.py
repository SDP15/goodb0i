import json

errorMessage = "Sorry, I couldn't recognise your command."

unconst = "That is not an option. Thou shalt obey me. Choose one of the other options."

tut1output = "Once you have selected an item, you can scan its barcode using the "\
             "camera on your phone. Don't worry, I will help you with this. "\
             "Once you have scanned the item, I can tell you information about "\
             "the item you have picked up. If you pick up the wrong item, "\
             "you can try again. Does this make sense?"

interactor_states = {
  "init": {
    "start": {
      "reply": "Hello, my name is Iona Trolley. Have you ever used good boy before?",
      "nextState": "beginning0"
    },
    "n/a": {
      "reply": "Hello there, in order to use good boy, please say start.",
      "nextState": "init"
    }
  },
  "waiting1": {
    "start": {
      "reply": "Hello, my name is Iona Trolley. Have you ever used good boy before?",
      "nextState": "beginning1"
    },
    "n/a": {
      "reply": "Hello there, in order to use good boy please say start.",
      "nextState": "waiting1"
    }
  },
  "waiting2": {
    "start": {
      "reply": "Hello, my name is Iona Trolley. Have you ever used good boy before?",
      "nextState": "beginning2"
    },
    "n/a": {
      "reply": "Hello there, in order to use good boy please say start.",
      "nextState": "waiting2"
    }
  },
  "waiting3": {
    "start": {
      "reply": "Hello, my name is Iona Trolley. Have you ever used good boy before?",
      "nextState": "beginning3"
    },
    "n/a": {
      "reply": "Hello there, in order to use good boy, please say start.",
      "nextState": "waiting3"
    }
  },
  "beginning0": {
    "yes": {
      "reply": "Would you like to go shopping?",
      "nextState": "beginning1"
    },
    "no": {
      "reply": "Would you like to do a quick tutorial?",
      "nextState": "beginning2"
    }
  },
  "beginning1": {
    "yes": {
      "reply": "Here we go!",
      "nextState": "shopping1"
    },
    "no": {
      "reply": "Would you like to do a quick tutorial?",
      "nextState": "beginning2"
    }
  },
  "beginning2": {
    "yes": {
      "reply": "Perfect. Today I will be guiding you around the shop. "\
               "You can give me a list of items or simply browse the shop. "\
               "Would you like more information about this service?",
      "nextState": "tutorial0"
    },
    "no": {
      "reply": "Okay. When you are ready to start shopping, say shopping. "\
               "And to begin the tutorial, say tutorial",
      "nextState": "limbo0"
    }
  },
#  "beginning3": {
#  },
  "shopping0": {
  },
  "shopping1": {
  },
#  "shopping2": {
#  },
#  "shopping3": {
#  },
  "tutorial0": {
    "yes": {
      "reply": unconst,
      "nextState": "tutorial0"
    },
    "no": {
      "reply": "I will tell you when we have arrived at an item. "\
               "In this shop, the items will always be on your right. "\
               "I will then tell you what item you have arrived at, "\
               "and what shelf the item is on. Would you like an example?",
      "nextState": "tutorial1"
    }
  },
  "tutorial1": {
    "yes": {
      "reply": "The Garlic Bread is on the middle shelf... " + tut1output,
      "nextState": "tutorial2"
    },
    "no": {
      "reply": tut1output,
      "nextState": "tutorial2"
    }
  },
  "tutorial2": {
    "yes": {
      "reply": "Once we have finished your list, you can continue to browse, "\
               "or you may go to the checkout. Shall we start shopping?",
      "nextState": "tutorial3"
    },
    "no": {
      "reply": unconst,
      "nextState": "tutorial2"
    }
  },
  "tutorial3": {
    "yes": {
      "reply": "Here we go!",
      "nextState": "shopping0"
    },
    "no": {
      "reply": "Okay. When you are ready to start shopping, say shopping."\
      "To begin the tutorial, say tutorial.",
      "nextState": "limbo0"
    }
  },
  "limbo0": {
    "shopping": {
      "reply": "Would you like to go shopping?",
      "nextState": "beginning1"
    },
    "tutorial": {
      "reply": "Would you like to do a quick tutorial?",
      "nextState": "beginning2"
    }
  },
#  "limbo1": {
#  },
#  "limbo2": {
#  },
#  "limbo3": {
#  }
  "errorMessage" : errorMessage
}

if __name__ == '__main__':
  with open("interactor_states.json","w") as f:
    json.dump(interactor_states,f)