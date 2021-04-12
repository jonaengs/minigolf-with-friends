package com.mygdx.minigolf.server.messages;

public class LobbyClientMessage<Command> extends Message<Command> {
    public LobbyClientMessage(Command command) {
        super(command);
    }

    public LobbyClientMessage(Command command, Object data) {
        super(command, data);
    }

    public enum Command {
        CREATE,
        JOIN,
        EXIT,
        START_GAME,
        GAME_READY,
    }
}