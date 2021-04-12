package com.mygdx.minigolf.server;

import com.mygdx.minigolf.server.messages.Message;
import com.mygdx.minigolf.server.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.server.messages.Message.ServerLobbyCommand;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


class CommunicationHandler implements Runnable {
    public final ConcurrentLinkedQueue<Message<ServerLobbyCommand>> sendBuffer = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<Message<ClientLobbyCommand>> recvBuffer = new ConcurrentLinkedQueue<>();
    String name;
    final Socket socket;
    public AtomicBoolean running = new AtomicBoolean(true);

    public CommunicationHandler(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public void close() {
        running.set(false);
        recvBuffer.add(new Message<>(ClientLobbyCommand.EXIT));
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        Message<ServerLobbyCommand> sendMsg;
        try {
            ObjectOutputStream objSender = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objRecv = new ObjectInputStream(socket.getInputStream());
            while (running.get()) {
                while (!sendBuffer.isEmpty()) {
                    sendMsg = sendBuffer.poll();
                    objSender.writeObject(sendMsg);
                }
                while (objRecv.available() > 0) {
                    recvBuffer.add((Message<ClientLobbyCommand>) objRecv.readObject());
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println(name + "CH Terminating");
        }
    }
}