package com.mygdx.minigolf.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.net.Socket;

import static com.mygdx.minigolf.server.Utils.isEOF;


class GameCommunicationHandler implements Runnable {
    final public String[] recvBuffer = new String[1];
    final String name;
    final Socket socket;
    final GameController gameController;

    public GameCommunicationHandler(Socket socket, String name, GameController gameController) {
        this.gameController = gameController;
        this.socket = socket;
        this.name = name;
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
                sendMsg = gameController.getGameData();
                sendStream.write(sendMsg);
                sendStream.flush();

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