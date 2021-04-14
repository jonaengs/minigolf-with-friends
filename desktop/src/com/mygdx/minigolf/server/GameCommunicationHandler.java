package com.mygdx.minigolf.server;

import com.mygdx.minigolf.server.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mygdx.minigolf.server.messages.Message.ClientGameCommand;
import static com.mygdx.minigolf.server.messages.Message.ServerGameCommand;

// TODO: Generalize and extract logic into a superclass for CommunicationHandler and this class
class GameCommunicationHandler implements Runnable {
    public final ConcurrentLinkedQueue<Message<ClientGameCommand>> recvBuffer = new ConcurrentLinkedQueue<>();
    final String name;
    final Socket socket;
    final GameController gameController;
    final ObjectInputStream objIn;
    final ObjectOutputStream objOut;
    public final AtomicBoolean running = new AtomicBoolean(true);

    public GameCommunicationHandler(CommunicationHandler comm, GameController gameController) {
        this.gameController = gameController;
        this.socket = comm.socket;
        this.name = comm.name;
        this.objIn = comm.objIn;
        this.objOut = comm.objOut;
    }

    public void send(Message<ServerGameCommand> msg) throws IOException {
        objOut.writeObject(msg);
        // Reset cache because gameState object gets cached, so first state is always sent.
        objOut.reset();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        try {
            while (running.get()) {
                recvBuffer.add((Message<ClientGameCommand>) objIn.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            recvBuffer.add(new Message<>(ClientGameCommand.EXIT));
        }
    }
}