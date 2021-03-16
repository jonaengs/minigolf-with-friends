package com.mygdx.minigolf;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.ashley.core.Engine;
import com.mygdx.minigolf.view.screens.MainMenu;

public class Game extends com.badlogic.gdx.Game {
    SpriteBatch batch;
    Engine engine;
    Texture img;

    com.badlogic.gdx.Game game;

    public Game() {
        game = this;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        engine = new Engine();
    }

    @Override
    public void render() {
        super.render();
        game.setScreen(new MainMenu(game));

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
