package com.mygdx.minigolf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.ashley.core.Engine;
import com.mygdx.minigolf.view.screens.MainMenuScreen;

public class Game extends com.badlogic.gdx.Game {
    SpriteBatch batch;
    Engine engine;
    Texture img;
    public static Music music;

    public static com.badlogic.gdx.Game game;

    public Game() {
        game = this;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        engine = new Engine();
        music = Gdx.audio.newMusic(Gdx.files.internal("music/Maxime Abbey - Operation Stealth - The Ballad of J. & J.ogg"));
        music.setLooping(true);
        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void render() {
        super.render();

        /*
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(img, 0, 0);
        batch.end();
        */
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}
