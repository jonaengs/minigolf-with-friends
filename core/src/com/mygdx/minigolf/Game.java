package com.mygdx.minigolf;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.model.levels.CourseLoader;

import java.util.List;

public class Game extends HeadlessGame {

    @Override
    public void create() {
        super.create();

        engine.addSystem(new GraphicsSystem());

        // Test code. Loads a level
        List<Entity> levelContents = levelLoader.loadLevel(CourseLoader.getCourses().get(1));
        factory.createPlayer(9, 12, false);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.4f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(Gdx.graphics.getDeltaTime()); // TODO: Move stuff to GameView
    }

    @Override
    public void dispose() {
    }
}
