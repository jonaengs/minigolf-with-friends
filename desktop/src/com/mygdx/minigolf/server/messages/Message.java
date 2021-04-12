package com.mygdx.minigolf.server.messages;

import java.io.Serializable;

public class Message<T> implements Serializable {
    public T command;
    public Object data;

    public Message(T command) {
        this.command = command;
    }

    public Message(T command, Object data) {
        this.command = command;
        this.data = data;
    }

    public enum ServerLobbyCommand {
        NAME, // Give player its name
        LOBBY_ID,
        PLAYER_LIST,
        ENTER_GAME,
    }

    public enum ClientLobbyCommand {
        CREATE,
        JOIN,
        EXIT,
        START_GAME,
        GAME_READY,
    }
}
