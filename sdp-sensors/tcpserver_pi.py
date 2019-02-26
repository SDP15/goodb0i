import socket
import sys

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# bind the address to the port
server_address = ('192.168.105.144', 8080)
#print("Starting on address: '%s'" % server_address)

sock.bind(server_address)

sock.listen(1)

while True:
    print("waiting for a connection")
    connection, client_address = sock.accept()

    try:
        print("Connection from %s" % str(client_address))
        while True:
            data = connection.recv(32) # max buffer size
            print("received '%s'" % str(data))
            if data:
                print("sending data back to the client")
                connection.sendall(data)
            else:
                print("No more data")
                break
    finally:
        connection.close()





