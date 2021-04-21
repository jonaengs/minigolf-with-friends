package com.mygdx.minigolf.server;

import com.mygdx.minigolf.network.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

abstract class CommunicationHandler<SendCmd extends Message.TypedEnum, RecvCmd extends Message.TypedEnum> implements Runnable {
    final Socket socket;
    final ObjectInputStream objIn;
    final ObjectOutputStream objOut;
    final ConcurrentLinkedQueue<Message<RecvCmd>> recvBuffer = new ConcurrentLinkedQueue<>();
    String playerName;

    public CommunicationHandler(Socket socket, ObjectInputStream objIn, ObjectOutputStream objOut) throws IOException {
        this.socket = socket;
        this.objIn = objIn;
        this.objOut = objOut;
    }

    synchronized void send(Message<SendCmd> msg) throws IOException {
        objOut.writeObject(msg);
    }
}