package com.mygdx.minigolf.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.ComponentMappers;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.InputHandler;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.model.levels.LevelLoader;

import java.util.List;

public class GameView extends HeadlessGame implements Screen {
    GraphicsSystem graphicsSystem;

    @Override
    public void create() {
        super.create();

        graphicsSystem = new GraphicsSystem();

        engine.addSystem(graphicsSystem);
    }

    @Override
    public void show() {
        factory.createControllablePlayer(9,12, graphicsSystem.getCam());
    }

    @Override
    public void render(float delta) {
        render();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.7f, 1);
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

    public GraphicsSystem getGraphicsSystem() {
        return graphicsSystem;
    }

    public void setInput(Entity player) {
        Gdx.input.setInputProcessor(new InputHandler(this.getGraphicsSystem().getCam(), PhysicalMapper.get(player).getBody()));
    }

}
