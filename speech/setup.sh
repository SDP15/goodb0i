#!/bin/bash
pulseaudio --start
pacmd set-default-sink alsa_output.usb-Logitech_Logitech_USB_Headset_000000000000-00.analog-stereo
pacmd set-default-source alsa_input.usb-Logitech_Logitech_USB_Headset_000000000000-00.analog-mono