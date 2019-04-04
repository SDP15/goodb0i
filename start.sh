#!/bin/bash

# Setup pulseaudio to use attached headset
pulseaudio --start
pacmd set-default-source alsa_input.usb-Logitech_Logitech_USB_Headset_000000000000-00.analog-mono

# Start pi_controller
python3 pi_controller.py