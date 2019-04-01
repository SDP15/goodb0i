#!/bin/bash

# Setup pulseaudio to use attached headset
pulseaudio --start
pacmd set-default-source alsa_input.usb-Logitech_Logitech_USB_Headset_000000000000-00.analog-mono
pacmd set-default-sink alsa_output.Logitech_Logitech_USB_Headset_000000000000-00.analog-stereo