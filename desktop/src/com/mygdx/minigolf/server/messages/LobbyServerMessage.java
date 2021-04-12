package com.mygdx.minigolf.server.messages;

public class LobbyServerMessage<Command> extends Message<Command> {
    public LobbyServerMessage(Command command) {
        super(command);
    }

    public LobbyServerMessage(Command command, Object data) {
        super(command, data);
    }

    public enum Command {
        NAME, // Give player its name
        LOBBY_ID,
        PLAYER_LIST,
        ENTER_GAME,
    }
}
