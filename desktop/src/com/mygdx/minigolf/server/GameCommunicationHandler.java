package com.mygdx.minigolf.server;

import com.mygdx.minigolf.network.messages.Message;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mygdx.minigolf.network.messages.Message.ClientGameCommand;
import static com.mygdx.minigolf.network.messages.Message.ServerGameCommand;

// TODO: Generalize and extract logic into a superclass for CommunicationHandler and this class
class GameCommunicationHandler extends CommunicationHandler<ServerGameCommand, ClientGameCommand> {
    final GameController gameController;
    public final AtomicBoolean running = new AtomicBoolean(true);

    public GameCommunicationHandler(LobbyCommunicationHandler comm, GameController gameController) throws IOException {
        super(comm.socket, comm.objIn, comm.objOut);
        this.gameController = gameController;
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