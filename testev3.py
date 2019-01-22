import time
import ev3dev.ev3 as ev3

# Local Imports
import testev as testfile

print ('Welcome to ev3')

#ev3.Sound.speak('Welcome to e v 3').wait()

# Step A: Basic open driving
testfile.runmotor()
#testfile.recordUltraSonic()

print ("wait 10sec, then end")
time.sleep(10)
