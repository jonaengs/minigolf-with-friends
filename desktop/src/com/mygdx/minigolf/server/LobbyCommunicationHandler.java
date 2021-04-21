package com.mygdx.minigolf.server;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mygdx.minigolf.network.Utils.readObject;

class LobbyCommunicationHandler extends CommunicationHandler<ServerLobbyCommand, ClientLobbyCommand> {
    AtomicBoolean running = new AtomicBoolean(true);
    Thread runningThread;

    public LobbyCommunicationHandler(Socket socket, ObjectInputStream objIn, ObjectOutputStream objOut) throws IOException {
        super(socket, objIn, objOut);
    }

    public void close() {
        running.set(false);
        recvBuffer.add(new Message<>(ClientLobbyCommand.EXIT));
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    synchronized void send(Message<ServerLobbyCommand> msg) throws IOException {
        objOut.writeObject(msg);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        runningThread = Thread.currentThread();
        try {
            while (running.get()) {
                recvBuffer.add((Message<ClientLobbyCommand>) objIn.readObject());
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            running.set(false);
        } finally {
            System.out.println(playerName + "CH Terminating");
        }
    }
}