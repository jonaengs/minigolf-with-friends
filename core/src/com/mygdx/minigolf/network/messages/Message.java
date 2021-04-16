package com.mygdx.minigolf.network.messages;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.HashMap;

public class Message<T extends Enum<T>> implements Serializable {
    public T command;
    public Object data;

    public Message(T command) {
        this(command, null);
    }

    public Message(T command, Object data) {
        this.command = command;
        this.data = data;
        // TODO: Enforce/assert data and its type based on command
    }

    @Override
    public String toString() {
        return "Message{" + command + (data == null ? "" : ", " + data) + '}';
    }

    public enum ServerLobbyCommand {
        NAME, // Give player its name
        LOBBY_ID,
        PLAYER_LIST,
        ENTER_GAME,
        LOBBY_NOT_FOUND
    }

    public enum ClientLobbyCommand {
        CREATE,
        JOIN,
        EXIT,
        START_GAME,
        GAME_READY,
    }

    public enum ServerGameCommand {
        LOAD_LEVEL(String.class),
        LEVEL_COMPLETE(Void.class),
        START_GAME(Void.class),
        PLAYER_EXIT(String.class),
        GAME_DATA(GameState.class),
        GAME_SCORE(HashMap.class), // String playerName -> int score
        GAME_COMPLETE(Void.class);

        private final Class<?> clazz;

        ServerGameCommand(Class<?> clazz) {
            this.clazz = clazz;
        }
    }

    public enum ClientGameCommand {
        INPUT(Vector2.class),
        LEVEL_LOADED(String.class),
        EXIT(Void.class);

        public final Class<?> clazz;

        ClientGameCommand(Class<?> clazz) {
            this.clazz = clazz;
        }
    }
}
