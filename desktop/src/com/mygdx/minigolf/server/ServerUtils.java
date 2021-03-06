package com.mygdx.minigolf.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.util.ConcurrencyUtils;
import com.mygdx.minigolf.util.Constants;
import com.mygdx.minigolf.view.GameView;

public class ServerUtils {
    public static Application initGameView(GameView game) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.x = 100; config.y = 100;
        config.width = 1280; config.height = 720;
        config.useHDPI = true;
        config.foregroundFPS = Constants.FPS;
        return new LwjglApplication(game, config);
    }

    public static HeadlessApplication initHeadlessGame(HeadlessGame game) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.renderInterval = Constants.SERVER_TICK_RATE; // TODO: Maybe use different send/receive rate and game update rate
        return new HeadlessApplication(game, config);
    }

    public static Application initGame(HeadlessGame game) {
        Application app = game instanceof GameView ? initGameView((GameView) game) : initHeadlessGame(game);
        ConcurrencyUtils.skipWaitPostRunnable(() -> {});
        return app;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
