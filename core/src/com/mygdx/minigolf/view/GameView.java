package com.mygdx.minigolf.view;


import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.controller.systems.Physics;
import com.mygdx.minigolf.controller.systems.PowerUpSystem;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.model.levels.LevelLoader;

import java.util.List;

public class GameView extends HeadlessGame implements Screen {

    GraphicsSystem graphicsSystem;

    public GameView() {
        super.create();

        this.graphicsSystem = new GraphicsSystem();
        engine.addSystem(graphicsSystem);


        LevelLoader levelLoader = new LevelLoader(factory);
        List<Entity> levelContents = levelLoader.loadLevel(CourseLoader.getCourses().get(1));
    }

    @Override
    public void show() {
        // --- Scenario 1: Exploding power up ---
        factory.createPlayer(9, 12);
        factory.createControllablePlayer(9,5, graphicsSystem.getCam());

        // --- Scenario 2: No collision power-up ----
        //factory.createControllablePlayer(12,5, graphicsSystem.getCam());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1f, 0f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
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
