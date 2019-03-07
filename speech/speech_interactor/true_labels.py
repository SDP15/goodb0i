import os

label = 0

while label is not 7:
    counter = 0
    while counter is not 20:
        with open("logs/kw_tests/true_labels.txt", 'a') as f:
                        f.write("{:},".format(label))
                        counter += 1
    label += 1