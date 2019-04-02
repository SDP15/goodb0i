#!/bin/bash

# Setup pulseaudio to use attached headset
pulseaudio --kill
pulseaudio --start
pacmd set-default-source alsa_input.usb-Logitech_Logitech_USB_Headset_000000000000-00.analog-mono
pacmd set-default-sink alsa_output.usb-Logitech_Logitech_USB_Headset_000000000000-00.analog-stereo

# Start pi_controller
/usr/bin/python3 /home/student/goodb0i/speech/pi_controller.py --skiptut