import socket

#connection 
ip="192.168.105.108"
port=6081
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((ip,port))

while True:
    count = 0
    data = sock.recv(8192)
    if not data: break
    print data
    if data != None:
        count += 1
        valmis = data
        if valmis == None:
            print "[-] no results"
            break
        else:
            print "[+] " + str(count) +  " sending message... "
            sock.send(valmis)
            continue