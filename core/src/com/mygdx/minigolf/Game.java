package com.mygdx.minigolf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;


public class Game extends com.badlogic.gdx.Game {
    private static Game instance;
    public Music music;

    @Override
    public void create() {
        instance = this;

        music = Gdx.audio.newMusic(Gdx.files.internal("music/Maxime Abbey - Operation Stealth - The Ballad of J. & J.ogg"));
        music.setLooping(true);
        ScreenController.changeScreen(ScreenController.MAIN_MENU_VIEW);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.0f, 0.4f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public static Game getInstance() {
        return instance;
    }
}
