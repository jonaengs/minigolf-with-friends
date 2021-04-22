package com.mygdx.minigolf.server.communicators;

import com.mygdx.minigolf.network.Utils;
import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.TypedEnum;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

// TODO: Encapsulation
public abstract class CommunicationHandler<SendCmd extends TypedEnum, RecvCmd extends TypedEnum> implements Runnable {
    public final AtomicBoolean running = new AtomicBoolean(true);
    public final Socket socket;
    public final ObjectInputStream objIn;
    public final ObjectOutputStream objOut;
    private final BlockingQueue<Message<RecvCmd>> recvBuffer = new LinkedBlockingQueue<>();
    public String playerName;

    abstract RecvCmd getExitCmd();

    public CommunicationHandler(Socket socket, ObjectInputStream objIn, ObjectOutputStream objOut) {
        this.socket = socket;
        this.objIn = objIn;
        this.objOut = objOut;
    }

    public synchronized void send(Message<SendCmd> msg) throws IOException {
        objOut.writeObject(msg);
    }

    public Message<RecvCmd> read() {
        Message<RecvCmd> msg = recvBuffer.poll();
        if (msg != null) System.out.println(msg);
        return msg;
    }

    public Message<RecvCmd> blockingRead() {
        try {
            Message<RecvCmd> msg = recvBuffer.take();
            System.out.println(msg);
            return msg;
        } catch (InterruptedException e) {
            return blockingRead(); // TODO: As with MessageBuffer, this may be problematic
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        try {
            Message<RecvCmd> msg;
            while (running.get()) {
                msg = Utils.readObject(socket, objIn);
                if (msg != null) {
                    recvBuffer.add(msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            recvBuffer.add(new Message<>(getExitCmd()));
        }
    }
}