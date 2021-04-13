package com.mygdx.minigolf.server;

import com.mygdx.minigolf.server.messages.GameState;
import com.mygdx.minigolf.server.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import static com.mygdx.minigolf.server.messages.Message.*;


class GameCommunicationHandler implements Runnable {
    final public Container<Message<ClientGameCommand>> recvBuffer = new Container<>();
    final String name;
    final Socket socket;
    final GameController gameController;
    final ObjectInputStream objIn;
    final ObjectOutputStream objOut;

    public GameCommunicationHandler(CommunicationHandler comm, GameController gameController) {
        this.gameController = gameController;
        this.socket = comm.socket;
        this.name = comm.name;
        this.objIn = comm.objIn;
        this.objOut = comm.objOut;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        GameState sendState;
        int stateID = -1;
        Message<ClientGameCommand> msg;
        try {
            while (socket.isConnected()) {
                // TODO: Change how data is sent. Current solution may send duplicate data. Maybe send from GameController instead
                if (stateID < gameController.stateID.getAndSet(stateID)) {
                    sendState = gameController.getGameData();
                    objOut.writeObject(new Message<>(ServerGameCommand.GAME_DATA, sendState));
                }

                while (objIn.available() > 0) {
                    msg = (Message<ClientGameCommand>) objIn.readObject();
                    synchronized (recvBuffer) {
                        recvBuffer.set(msg);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            synchronized (recvBuffer) {
                recvBuffer.set(new Message<>(ClientGameCommand.EXIT));
            }
        }
    }

    public static class Container<T> {
        private T data;
        public void set(T data) {
            this.data = data;
        }
        public T get() {
            T temp = data;
            this.data = null;
            return temp;
        }
    }
}