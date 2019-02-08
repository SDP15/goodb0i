#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <unistd.h>
#define PORT 8080

// part of the code from geeksforgeeks.org 

int main(int argc, char const *argv[]) {
    struct sockaddr_in address; 
    int sock = 0, valread; 
    struct sockaddr_in serv_addr; 
    const char *hello = "Hello from client";
    char buffer[1024] = {0};
    int opt = 1;
    unsigned int milliseconds = 10;

    if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
           printf("\n Socket creation error \n");
          // return -1;
       }

    

    memset(&serv_addr, '0', sizeof(serv_addr));

    serv_addr.sin_family = AF_INET; 
    serv_addr.sin_port = htons(PORT);

       // Convert IPv4 and IPv6 addresses from text to binary form 
    if(inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr)<=0) {
       printf("\nInvalid address/ Address not supported \n");
       usleep(milliseconds);
        //   return -1;
    }

   if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) {
           printf("\nConnection failed \n");
           usleep(milliseconds);
        //   return -1;
    }
    send(sock, hello, strlen(hello), 0);
    printf("Hello message sent\n");
    valread = read(sock, buffer, 1024);
    printf("%s\n", buffer);
    
    return 0;
}