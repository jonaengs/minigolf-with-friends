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
    public static final int MAX_LOBBY_SIZE = 4;

    // Box2D filters
    public static final short BIT_PLAYER = 1;
    public static final short BIT_WALL = 2;
    public static final short BIT_COURSE = 4;
    public static final short BIT_HOLE = 8;
    public static final short BIT_POWERUP = 16;
    public static final short BIT_SPAWN = 32;
    public static final short BIT_OBSTACLE = 64;

    // Other
    public static final float MOVING_MARGIN = 0.1f;

}
