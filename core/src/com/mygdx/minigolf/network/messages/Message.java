package com.mygdx.minigolf.network.messages;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

// TODO: Consider changing command types to ControlCommand & GameData, not separating between if the message is meant for a lobby or the game
public class Message<T extends TypedEnum> implements Serializable {
    public T command;
    public Object data;

    public Message(T command) {
        this(command, null);
    }

    public Message(T command, Object data) {
        if (command.getType() != Void.class) {
            if (command.getType() != data.getClass()) {
                new IllegalArgumentException(
                        "Invalid combo: " + command + " [" + command.getType() + "]"
                        + " & " + data + " [" + data.getClass() + "]"
                ).printStackTrace();
            }
        }
        this.command = command;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" + command + (data == null ? "" : ", " + data) + '}';
    }

    public enum ServerLobbyCommand implements TypedEnum {
        NAME(String.class), // Give player its name
        LOBBY_ID(Integer.class),
        PLAYER_LIST(ArrayList.class),
        ENTER_GAME(Void.class),
        LOBBY_NOT_FOUND(Integer.class),
        EXIT(Void.class);

        private final Class<?> clazz;

        ServerLobbyCommand(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getType() {
            return clazz;
        }
    }

    public enum ClientLobbyCommand implements TypedEnum {
        CREATE(Void.class),
        JOIN(Integer.class),
        EXIT(Void.class),
        START_GAME(Void.class),
        GAME_READY(Void.class);

        private final Class<?> clazz;

        ClientLobbyCommand(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getType() {
            return clazz;
        }
    }

    public enum ServerGameCommand implements TypedEnum {
        LOAD_LEVEL(String.class),
        LEVEL_COMPLETE(Void.class),
        START_GAME(Void.class),
        PLAYER_EXIT(String.class),
        GAME_DATA(NetworkedGameState.class),
        GAME_SCORE(HashMap.class), // String playerName -> int score
        GAME_COMPLETE(Void.class);

        private final Class<?> clazz;

        ServerGameCommand(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getType() {
            return clazz;
        }
    }

    public enum ClientGameCommand implements TypedEnum {
        INPUT(Vector2.class),
        LEVEL_LOADED(String.class),
        EXIT(Void.class);

        private final Class<?> clazz;

        ClientGameCommand(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getType() {
            return clazz;
        }
    }
}
