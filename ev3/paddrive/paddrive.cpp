#include <arpa/inet.h>
#include <bits/stdc++.h>
#include <cassert>
#include <cstdint>
#include <cstdio>
#include <cstring>
#include <netinet/in.h>
#include <signal.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#include <SDL2/SDL.h>

using namespace std;
using namespace std::chrono_literals;

bool running{true};

void sigintHandler(int sig) {
  if (sig == SIGINT) {
    running = false;
  }
}

int main(int argc, char **argv) {
  if (argc != 2) {
    cerr << "Usage: ./paddrive [EV3-IP]" << endl;
    return 1;
  }
  if (signal(SIGINT, &sigintHandler) == SIG_ERR) {
    cerr << "Could not register SIGINT handler" << endl;
    return 1;
  }

  SDL_Init(SDL_INIT_GAMECONTROLLER | SDL_INIT_EVENTS);

  SDL_GameController *gc{nullptr};
  for (int i = 0; i < SDL_NumJoysticks(); ++i) {
    if (SDL_IsGameController(i)) {
      gc = SDL_GameControllerOpen(i);
      if (gc != nullptr)
        break;
    }
  }
  if (gc == nullptr) {
    cerr << "No joystick found" << endl;
    return 2;
  }

  int socketFd = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
  if (socketFd == -1) {
    perror("Cannot create client socket");
    throw new runtime_error("Cannot create client socket");
  }

  in_addr saddr;
  if (!inet_aton(argv[1], &saddr)) {
    cerr << "Invalid IP address" << endl;
    return 3;
  }
  sockaddr_in addr;
  memset(&addr, 0, sizeof addr);
  addr.sin_family = AF_INET;
  addr.sin_port = htons(6081);
  addr.sin_addr = saddr;

  if (connect(socketFd, reinterpret_cast<sockaddr *>(&addr), sizeof addr) < 0) {
    perror("connect fail");
    throw new runtime_error("Could not connect client socket");
  }

  thread readthread{[socketFd]() {
    char rbuf[128];
    int rlen{0};
    while ((rlen = recv(socketFd, rbuf, sizeof rbuf, 0)) > 0) {
      fwrite(rbuf, rlen, 1, stderr);
    }
  }};

  auto rsend = [socketFd](const char *str) {
    ::send(socketFd, str, strlen(str), 0);
  };

  while (running) {
    this_thread::sleep_for(10ms);
    SDL_Event ev;
    while (SDL_PollEvent(&ev)) {
      switch (ev.type) {
      case SDL_CONTROLLERBUTTONUP:
        switch (ev.cbutton.button) {
        case SDL_CONTROLLER_BUTTON_START:
          rsend("start\n");
          break;
        case SDL_CONTROLLER_BUTTON_BACK:
          rsend("stop\n");
          break;
        case SDL_CONTROLLER_BUTTON_A:
          rsend("resume-from-stop-marker\n");
          break;
        case SDL_CONTROLLER_BUTTON_DPAD_LEFT:
          rsend("clear-queue\n");
          rsend("enqueue-left\n");
          break;
        case SDL_CONTROLLER_BUTTON_DPAD_RIGHT:
          rsend("clear-queue\n");
          rsend("enqueue-right\n");
          break;
        case SDL_CONTROLLER_BUTTON_DPAD_UP:
          rsend("clear-queue\n");
          rsend("enqueue-forward\n");
          break;
        case SDL_CONTROLLER_BUTTON_DPAD_DOWN:
          rsend("clear-queue\n");
          rsend("enqueue-stop\n");
          break;
        }
        break;
      }
    }
  }

  shutdown(socketFd, SHUT_RDWR);
  SDL_Quit();

  return 0;
}
