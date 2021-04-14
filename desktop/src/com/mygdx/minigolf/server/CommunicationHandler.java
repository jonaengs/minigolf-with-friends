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

class CommunicationHandler implements Runnable {
    public final ConcurrentLinkedQueue<Message<ServerLobbyCommand>> sendBuffer = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<Message<ClientLobbyCommand>> recvBuffer = new ConcurrentLinkedQueue<>();
    final ObjectInputStream objIn;
    final ObjectOutputStream objOut;
    String name;
    final Socket socket;
    public AtomicBoolean running = new AtomicBoolean(true);
    public Thread runningThread;

    public CommunicationHandler(Socket socket, ObjectInputStream objIn) throws IOException {
        this.socket = socket;
        this.objIn = objIn;
        objOut = new ObjectOutputStream(socket.getOutputStream());
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
        Thread.currentThread().setName(this.getClass().getName());
        runningThread = Thread.currentThread();
        Message<ServerLobbyCommand> sendMsg;
        try {
            while (running.get()) {
                while (!sendBuffer.isEmpty()) {
                    sendMsg = sendBuffer.poll();
                    objOut.writeObject(sendMsg);
                }
                Message<ClientLobbyCommand> msg = (Message<ClientLobbyCommand>) readObject(socket, objIn);
                if (msg != null) {
                    recvBuffer.add(msg);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            running.set(false);
        } finally {
            System.out.println(name + "CH Terminating");
        }
    }
}