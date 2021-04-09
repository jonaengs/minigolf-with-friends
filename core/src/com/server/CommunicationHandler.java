package com.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.server.Utils.isEOF;


class CommunicationHandler implements Runnable {
    public final ConcurrentLinkedQueue<String> sendBuffer = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<String> recvBuffer = new ConcurrentLinkedQueue<>();
    final String name;
    final Socket socket;
    public AtomicBoolean running = new AtomicBoolean(true);
    Thread t;

    public CommunicationHandler(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public void close() {
        System.out.println(name + "CH CLOSING!");
        running.set(false);
        try {
            recvBuffer.add(Msg.EXIT);
            socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        t = Thread.currentThread();
        t.setName(this.getClass().getName());
        String sendMsg, recvMsg;
        try {
            BufferedWriter sendStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PushbackInputStream recvStream = new PushbackInputStream(socket.getInputStream());
            while (running.get()) {
                while (!sendBuffer.isEmpty()) {
                    sendMsg = this.sendBuffer.poll();
                    if (sendMsg != null) {
                        System.out.println(name + "CH Sending msg: " + sendMsg);
                        sendStream.write(sendMsg + "\n");
                        sendStream.flush();
                    }
                }
                if (isEOF(socket, recvStream)) {
                    close();
                    break;
                }
                do {
                    recvMsg = Utils.readLine(recvStream);
                    if (!recvMsg.isEmpty()) {
                        this.recvBuffer.add(recvMsg);
                    }
                } while(!recvMsg.isEmpty());
            }
        } catch (IOException ignored) {
        } finally {
            System.out.println(name + "CH Exiting");
        }
    }
}