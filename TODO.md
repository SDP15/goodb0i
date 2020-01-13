# Set up

- Connect laptop to SDPRobots 
- Open the config.conf file in the root SDP/ directory, and change the resource addresses to match your account
- Run Main (from IntelliJ toolbar, or sidebar in Main.kt)
- Run hostname -I to find the IP of the server

- Connect a phone to SDPRobots
- Press volume down, select server address and change it (Open and close the app after doing this)
- Create a shopping list

- Start up the PI 
- SSH to student@caterpie
- Navigate to goodb0i/speech and edit the server address in pi_controller.py
- Navigate back to the goodb0i root directory and run start.sh

Starting navigation on the phone should now start the trolley. (start.sh skips the tutorial)
