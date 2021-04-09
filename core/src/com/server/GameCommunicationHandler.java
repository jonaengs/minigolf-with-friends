package com.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.net.Socket;

import static com.server.Utils.isEOF;


class GameCommunicationHandler implements Runnable {
    final public String[] sendBuffer = new String[1];
    final public String[] recvBuffer = new String[1];
    final String name;
    final Socket socket;

    public GameCommunicationHandler(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
        sendBuffer[0] = "START GAME";
        recvBuffer[0] = null;
    }

    public void close() {
        System.out.println(Thread.currentThread().getName() + " told to stop");
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        String sendMsg, recvMsg;
        try {
            BufferedWriter sendStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            PushbackInputStream recvStream = new PushbackInputStream(socket.getInputStream());
            while (socket.isConnected()) {
                synchronized (sendBuffer) {
                    sendMsg = sendBuffer[0];
                    sendBuffer[0] = null;
                }
                if (sendMsg != null) {
                    sendStream.write(sendMsg + "\n");
                    sendStream.flush();
                }

                if (isEOF(socket, recvStream)) {
                    socket.close();
                    break;
                }
                recvMsg = Utils.readLine(recvStream);
                if (!recvMsg.isEmpty()) {
                    synchronized (recvBuffer) {
                       recvBuffer[0] = recvMsg;
                    }
                }
            }
        } catch (IOException ignored) {
        } finally {
            synchronized (recvBuffer) {
                recvBuffer[0] = "EXIT";
            }
        }
    }
}