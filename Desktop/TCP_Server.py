# -*- coding: UTF-8 -*-
from socket import AF_INET, socket, SOCK_STREAM, error
from threading import Thread
import time


def accept_incoming_connections():
    """Sets up handling for incoming clients."""
    while True:
        client, client_address = SERVER.accept()
        print("> New connection: %s:%s" % client_address)
        client.send(bytes("Welcome! Now type your name and press enter!".encode("utf-8")))
        addresses[client] = client_address
        Thread(target=handle_client, args=(client,)).start()


def handle_client(client):  # Takes client socket as argument.
    """Handles a single client connection."""

    name = client.recv(BUFSIZ).decode("utf-8")
    welcome = 'Welcome %s ! Type {quit} to exit.' % name
    try:
        client.send(bytes(welcome.encode("utf-8")))
    except TypeError:
        pass
    msg = "%s has joined the chat!" % str(name)
    broadcast(bytes(msg.encode("utf-8")))
    clients[client] = name

    while True:
        msg = client.recv(BUFSIZ)
        if msg != bytes("{quit}", "utf8"):
            if len(msg):
                print("MSG: " + name + ": " + msg.decode("utf-8"))
                broadcast(msg, name + ": ")
        else:
            client.send(bytes("{quit}".encode("utf-8")))
            client.close()
            del clients[client]
            broadcast(bytes("%s has left the chat." % name, "utf-8"))
            break
        time.sleep(1)


def broadcast(msg, prefix=""):  # prefix is for name identification.
    """Broadcasts a message to all the clients."""

    for sock in clients:
        try:
            # sock.send(msg)
            sock.send(bytes(prefix.encode("utf-8")) + msg)
        except error:
            print("> A socket error occurred")



clients = {}
addresses = {}

HOST = ''
PORT = 65525
BUFSIZ = 1024
ADDR = (HOST, PORT)

SERVER = socket(AF_INET, SOCK_STREAM)
SERVER.bind(ADDR)

if __name__ == "__main__":
    SERVER.listen(20)
    print("Waiting for connection...")
    ACCEPT_THREAD = Thread(target=accept_incoming_connections)
    ACCEPT_THREAD.start()
    ACCEPT_THREAD.join()
    SERVER.close()
