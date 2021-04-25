package com.mygdx.minigolf.util;

public final class Constants {

    // Graphics
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final int FPS = 60;
    public static final float REFRESH_RATE = 1f / FPS;

    // Client
    public static final String SERVER_IP = or(System.getenv("SERVER_IP"), "golf.intveld.no");

    // Server
    public static final Integer DEFAULT_NUM_TICKS = FPS;
    public static final int NUM_TICKS = or(tryParse(System.getenv("NUM_TICKS")), DEFAULT_NUM_TICKS);
    public static final float SERVER_TICK_RATE = 1f / NUM_TICKS;
    public static final long SERVER_TICK_RATE_MS = 1000 / NUM_TICKS;

    // Physics
    public static float PHYSICS_TICK_RATE = SERVER_TICK_RATE;

    // Gameplay
    public static final int MAX_NUM_PLAYERS = 5;

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

    /**
     * Returns first non-null value
     */
    private static <T> T or(T... values) {
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }

    private static Integer tryParse(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
