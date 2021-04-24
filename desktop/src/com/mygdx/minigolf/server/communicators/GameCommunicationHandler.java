package com.mygdx.minigolf.server.communicators;

import com.mygdx.minigolf.network.messages.Message;

import java.io.IOException;

import static com.mygdx.minigolf.network.messages.Message.ClientGameCommand;
import static com.mygdx.minigolf.network.messages.Message.ServerGameCommand;

public class GameCommunicationHandler extends CommunicationHandler<ServerGameCommand, ClientGameCommand> {
    public GameCommunicationHandler(LobbyCommunicationHandler comm) {
        super(comm.socket, comm.objIn, comm.objOut);
        playerName = comm.playerName;
    }

    @Override
    ClientGameCommand getExitCmd() {
        return Message.ClientGameCommand.EXIT;
    }

    public void send(Message<ServerGameCommand> msg) throws IOException {
        super.send(msg);
        objOut.reset(); // Must reset cache. Otherwise gamestate object mutations won't be sent
    }
}