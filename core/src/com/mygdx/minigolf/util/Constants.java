package com.mygdx.minigolf.util;

import java.util.Optional;

public final class Constants {

    // Graphics
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final int FPS = 30;
    public static final float REFRESH_RATE = 1f / FPS;

    // Server
    public static final Integer DEFAULT_NUM_TICKS = 30;
    public static final int NUM_TICKS = Integer.parseInt(Optional.ofNullable(System.getenv("NUM_TICKS")).orElse(DEFAULT_NUM_TICKS.toString()));
    public static final float SERVER_TICK_RATE = 1f / NUM_TICKS;
    public static final long SERVER_TICK_RATE_MS = 1000 / NUM_TICKS;

    // Physics
    public static float PHYSICS_TICK_RATE = REFRESH_RATE;

    // Gameplay
    public static final int MAX_NUM_PLAYERS = 4;

    // Box2D fixture filters
    public static final short BIT_PLAYER = 1;
    public static final short BIT_WALL = 2;
    public static final short BIT_COURSE = 4;
    public static final short BIT_HOLE = 8;
    public static final short BIT_POWERUP = 16;
    public static final short BIT_SPAWN = 32;
    public static final short BIT_OBSTACLE = 64;

    // Other
    public static final float MOVING_MARGIN = 0.1f;

    public static final String TUTORIAL_TEXT = "Singleplayer\n" +
            "To play singleplayer just click 'New Game', followed by 'Start' on the next screen.\n\n" +
            "Multiplayer\n" +
            "To play multiplayer you or a friend need to click 'New Game' while the other clicks 'Join Game'. The latter has to enter the lobby code which is present on the other one's screen.\n\n" +
            "Game Mechanics\n" +
            "When you've started a game, touch and drag to shoot. The small yellow triangle in front of your ball indicates which direction you're shooting, while the expanding white triangle shows how much power you're using.\n" +
            "Your objective is to hit the hole, the black circle.\n" +
            "Other colored circles are power-ups, these give you some special ability such as no collision or an exploding ball\n" +
            "Everything green is considered the playing area, but be aware of obstacles.\n" +
            "If you wish you can enable/disable the music by navigating to settings.";

}
