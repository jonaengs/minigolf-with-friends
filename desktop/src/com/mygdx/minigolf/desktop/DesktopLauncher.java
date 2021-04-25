package com.mygdx.minigolf.desktop;


import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.util.Constants;
import com.mygdx.minigolf.view.GameView;


public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.x = 100;
        config.y = 100;
        config.width = 1280;
        config.height = 720;
        config.foregroundFPS = Constants.FPS;
        new LwjglApplication(new Game(), config);
    }
}
