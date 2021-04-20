package com.mygdx.minigolf.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.model.levels.LevelLoader;

import java.util.List;

public class GameView extends HeadlessGame implements Screen {

    GraphicsSystem graphicsSystem;

    public GameView() {
        super.create();

        this.graphicsSystem = new GraphicsSystem();

        engine.addSystem(graphicsSystem);
        // engine.addSystem(new PhysicsDebugSystem(world, graphicsSystem.getCam()));

        factory = new EntityFactory(engine, world);

        // --- Start dummy demo code ---
        // Test code. Loads a level
        LevelLoader levelLoader = new LevelLoader(factory);
        LevelLoader.Level level = levelLoader.loadLevel(CourseLoader.getFileNames()[0]);
    }

    @Override
    public void show() {
        factory.createControllablePlayer(12,15, graphicsSystem.getCam());
        factory.createPlayer(11,13);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        graphicsSystem.getViewport().update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
