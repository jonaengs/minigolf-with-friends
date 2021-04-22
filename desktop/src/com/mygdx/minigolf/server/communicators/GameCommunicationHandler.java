package com.mygdx.minigolf.server.communicators;

import com.mygdx.minigolf.network.messages.Message;

import java.io.IOException;

import static com.mygdx.minigolf.network.messages.Message.ClientGameCommand;
import static com.mygdx.minigolf.network.messages.Message.ServerGameCommand;

public class GameCommunicationHandler extends CommunicationHandler<ServerGameCommand, ClientGameCommand> {
    final ClientGameCommand exitCmd = ClientGameCommand.EXIT;

    public GameCommunicationHandler(LobbyCommunicationHandler comm) throws IOException {
        super(comm.socket, comm.objIn, comm.objOut);
    }

    public void send(Message<ServerGameCommand> msg) throws IOException {
        super.send(msg);
        objOut.reset(); // Must reset cache. Otherwise gamestate object mutations won't be sent
    }
}