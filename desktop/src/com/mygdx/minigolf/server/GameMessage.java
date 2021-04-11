package com.mygdx.minigolf.server;

import java.util.HashMap;

public class GameMessage {
    Command command;
    Object data;

    public GameMessage(Command command) {
        this.command = command;
    }

    public GameMessage(Command command, Object data) {
        this.command = command;
        this.data = data;
    }

    public enum Command {
        LOAD_LEVEL(String.class),
        LEVEL_COMPLETE(null),
        START_GAME(null),
        PLAYER_EXIT(String.class),
        GAME_SCORE(HashMap.class), // String playerName -> int score
        GAME_DATA(GameState.class);

        private final Class<?> clazz;

        Command(Class<?> clazz) {
            this.clazz = clazz;
        }
    }
}
