import json

errorMessage = "Sorry, I couldn't recognise your command."

unconst = "That is not an option. Please try another option. To hear your options, say: options."

tut1output = "Once you have selected an item, you can scan its barcode using the "\
             "camera on your phone."\
             "Once you have scanned the item, I can tell you information about "\
             "the item you have picked up. Does this make sense?"

tut0no = "I will tell you when we have arrived at an item. "\
               "In this shop, the items will always be on your right. "\
               "I will then tell you what item you have arrived at, "\
               "and what shelf the item is on. Would you like an example?"

interactor_states = {
  "init": {
    "start": {
      "reply": "Hello, my name is Iona Trolley. Have you ever used good boy before?",
      "nextState": "beginning0"
    },
    "n/a": {
      "reply": "Hello there, in order to use good boy, please say: start.",
      "nextState": "init"
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
      "reply": "Okay. When you are ready to do something, say that thing.", #start shopping, say: shopping. "\
              # "And to begin the tutorial, say: tutorial. ",
      "nextState": "limbo0"
    }
  },
  "shopping0": {
  },
  "shopping1": {
  },

  "tutorial0": {
    "yes": {
      "reply": "I am a shopping trolley that follows a route around the shop to help you pick up items. To give me a list simply attach your "\
                        "smartphone and send me the list... " + tut0no,
      "nextState": "tutorial1"
    },
    "no": {
      "reply": tut0no,
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
      "reply": "Okay. When you are ready to start shopping, say: shopping."\
      "To begin the tutorial, say: tutorial.",
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
  "errorMessage" : errorMessage
}

if __name__ == '__main__':
  with open("interactor_states.json","w") as f:
    json.dump(interactor_states,f)