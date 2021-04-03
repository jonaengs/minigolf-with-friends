package com.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.server.Utils.isEOF;


class CommunicationHandler implements Runnable {
    public final PriorityQueue<String> sendBuffer = new PriorityQueue<>();
    public final PriorityQueue<String> recvBuffer = new PriorityQueue<>();
    final String name;
    final Socket socket;
    public AtomicBoolean running = new AtomicBoolean(true);

    public CommunicationHandler(Socket s, String n) {
        socket = s;
        name = n;
    }

    public void close() {
        System.out.println(Thread.currentThread().getName() + " told to stop");
        running.set(false);
        try {
            synchronized (recvBuffer) {
                recvBuffer.add("EXIT");
            }
            socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        String tn = Thread.currentThread().getName();
        String sendMsg, recvMsg;
        try {
            BufferedWriter sendStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PushbackInputStream recvStream = new PushbackInputStream(socket.getInputStream());
            while (running.get()) {
                do {
                    synchronized (this.sendBuffer) {
                        sendMsg = this.sendBuffer.poll();
                    }
                    if (sendMsg != null) {
                        System.out.println(tn + " Sending msg: " + sendMsg);
                        sendStream.write(sendMsg + "\n");
                        sendStream.flush();
                    }
                } while (sendMsg != null);

                if (isEOF(recvStream)) {
                    close();
                    break;
                }
                do {
                    recvMsg = Utils.readStream(recvStream);
                    if (!recvMsg.isEmpty()) {
                        System.out.println(tn + " Received msg: " + recvMsg);
                        synchronized (this.recvBuffer) {
                            this.recvBuffer.add(recvMsg);
                        }
                    }
                } while(!recvMsg.isEmpty());
            }
        } catch (IOException ignored) {
        } finally {
            System.out.println(tn + " Exiting");
        }
    }
}