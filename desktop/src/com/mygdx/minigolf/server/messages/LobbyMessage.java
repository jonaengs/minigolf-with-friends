package com.mygdx.minigolf.server.messages;

public class LobbyMessage<Command> extends Message {

    public LobbyMessage(Object command) {
        super(command);
    }

    public LobbyMessage(Object command, Object data) {
        super(command, data);
    }

    public enum Command {
        CREATE,
        ENTER_GAME,
        EXIT,
        GAME_READY,
        JOIN,
        LOBBY_ID,
        NAME,
        PING,
        PLAYER_LIST,
        START_GAME,
    }
}
