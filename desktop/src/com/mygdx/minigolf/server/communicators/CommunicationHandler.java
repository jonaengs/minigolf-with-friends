package com.mygdx.minigolf.server.communicators;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.TypedEnum;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: Encapsulation
public abstract class CommunicationHandler<SendCmd extends TypedEnum, RecvCmd extends TypedEnum> implements Runnable {
    public final AtomicBoolean running = new AtomicBoolean(true);
    public final Socket socket;
    public final ObjectInputStream objIn;
    public final ObjectOutputStream objOut;
    public final ConcurrentLinkedQueue<Message<RecvCmd>> recvBuffer = new ConcurrentLinkedQueue<>();
    public String playerName;
    RecvCmd exitCmd;

    private RecvCmd getExitCmd() {
        return exitCmd;
    }

    public CommunicationHandler(Socket socket, ObjectInputStream objIn, ObjectOutputStream objOut) throws IOException {
        this.socket = socket;
        this.objIn = objIn;
        this.objOut = objOut;
    }

    public synchronized void send(Message<SendCmd> msg) throws IOException {
        objOut.writeObject(msg);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        try {
            while (running.get()) {
                recvBuffer.add((Message<RecvCmd>) objIn.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            recvBuffer.add(new Message<RecvCmd>(getExitCmd()));
        }
    }
}