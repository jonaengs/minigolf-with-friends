package com.mygdx.minigolf.server;

import java.io.Serializable;

public class LobbyMessage implements Serializable {
    Command command;
    Object data;

    public LobbyMessage(Command command) {
        this.command = command;
    }

    public LobbyMessage(String name) {
        this.data = name;
        this.command = Command.NAME;
    }

    public enum Command {
        CREATE,
        JOIN,
        EXIT,
        NAME,
        PLAYER_LIST,
        PING,
        LOBBY_ID,
        ENTER_GAME,
        START_GAME,
        GAME_READY,
    }
}
