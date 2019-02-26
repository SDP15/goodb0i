# How to use headset with RPi

In order to use the headset as the audio output with the Pi please run the following command. Do not do this if you wish to use the audio jack as the audio output.
```
>> pacmd set-default-sink alsa_output.usb-Logitech_Logitech_USB_Headset_000000000000-00.analog-stereo
```

In ordered to to use the microphone on the headset with the Pi please run the following command.

```
>> pacmd set-default-source alsa_input.usb-Logitech_Logitech_USB_Headset_000000000000-00.analog-mono
```
