package com.mygdx.minigolf.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.InputHandler;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;

public class GameView extends HeadlessGame implements Screen {
    GraphicsSystem graphicsSystem;

    @Override
    public void create() {
        super.create();

        factory.showGraphics = true;
        graphicsSystem = new GraphicsSystem();

        engine.addSystem(graphicsSystem);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(delta);
    }

    @Override
    public void render() { // Only used by server running with graphics (Game uses screen's render method)
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        super.render();
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

    public GraphicsSystem getGraphicsSystem() {
        return graphicsSystem;
    }

    public void setInput(Entity player) {
        Gdx.input.setInputProcessor(new InputHandler(this.getGraphicsSystem().getCam(), PhysicalMapper.get(player).getBody()));
    }

}
