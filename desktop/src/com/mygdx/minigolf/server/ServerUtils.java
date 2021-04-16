package com.mygdx.minigolf.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.network.Utils;
import com.mygdx.minigolf.view.GameView;

public class ServerUtils {
    public static Application initGameView(GameView game) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.x = 100; config.y = 100;
        config.width = 1280; config.height = 720;
        config.useHDPI = true;
        return new LwjglApplication(game, config);
    }

    public static Application initGame(HeadlessGame game) throws InterruptedException {
        Application app = game instanceof GameView ? initGameView((GameView) game) : Utils.initHeadlessGame(game);
        final Object lock = new Object();
        synchronized (lock) {
            app.postRunnable(() -> {
                synchronized (lock) {
                    lock.notify();
                }
            });
            lock.wait();
        }
        return app;
    }
}
