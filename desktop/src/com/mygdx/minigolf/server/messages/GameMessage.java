package com.mygdx.minigolf.server.messages;

import java.util.HashMap;

public class GameMessage<Command> extends Message<Command> {

    public GameMessage(Command command) {
        super(command);
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
