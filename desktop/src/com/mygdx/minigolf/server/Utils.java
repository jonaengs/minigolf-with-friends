package com.mygdx.minigolf.server;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.view.GameView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Utils {

    // Read object stream. Returns null if timeout is reached.
    public static Object readObject(Socket socket, ObjectInputStream objIn) throws IOException, ClassNotFoundException {
        socket.setSoTimeout(10);
        try {
            return objIn.readObject();
        } catch (SocketTimeoutException e) {
            return null;
        } finally {
            socket.setSoTimeout(0);
        }
    }

    public static Application initHeadlessGame(HeadlessGame game) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.renderInterval = 1 / 30f;
        return new HeadlessApplication(game, config);
    }

    public static Application initGameView(GameView game) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.x = 100; config.y = 100;
        config.width = 1280; config.height = 720;
        config.useHDPI = true;
        return new LwjglApplication(game, config);
    }

    public static Application initGame(HeadlessGame game) {
        Application app = game instanceof GameView ? initGameView((GameView) game) : initHeadlessGame(game);
        try {
            Thread.sleep(1_000); // Sleep to allow create method to run
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return app;
    }
}
