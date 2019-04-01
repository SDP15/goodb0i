import socket
import errno
from subprocess import STDOUT, check_output
from socket import error as socket_error

# this function scans all IP addresses present in the network and pings each of them individually at port 8080/products.
# if it can download some data, it returns this IP address, assumed to be the one of the server. It returns 0 otherwise

class IPPortScanner():

    def __init__(self):
        print("[INFO] looking for a server...")

    def get_server(self):
        hostname = socket.gethostname()
        pi_ip = socket.gethostbyname(hostname)
       # pi_ip = "192.168.105.144"
        sub = check_output(['nmap', '-sL', '-n', pi_ip + '/24']).decode('utf8').splitlines()
        ip_list = list()
        for elem in sub:
            ip_list.append(elem[21:len(elem)])

        print(len(ip_list))
        for ip in ip_list[1:len(ip_list) - 2]:
            #   print(ip)
           # print("ip address is %s" % ip)
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(0.05)
            try:
                result = sock.connect((ip, 8080))
                #print("port open at address %s" % ip)
                try:
                    get = check_output(['curl', ip + ":8080/products"], timeout=0.05)
                    if get != 0:
                        print("hurray")
                        return ip
                except Exception:
                    continue
            except socket_error as serr:
                if serr.errno == 13:
                  #  print("Oh my...!")
                    sock.close()
                    continue
                elif serr.errno != errno.ECONNREFUSED:
                  #  print("An exception occured")
                    sock.close()
                    continue
                else:
                  #  print("No connection at this address: %s" % ip)
                    sock.close()
                    continue

        return 0
