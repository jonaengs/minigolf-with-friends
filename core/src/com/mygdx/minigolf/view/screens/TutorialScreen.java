package com.mygdx.minigolf.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;

public class TutorialScreen implements Screen {

    private com.badlogic.gdx.Game parent;
    private Stage stage;
    private ScreenController controller;

    public TutorialScreen(Game game) {
        this.parent = game;
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        controller = ScreenController.getInstance();
    }

    @Override
    public void show() {
        // Creating table that fills the screen, everything will go inside this table
        Table table = new Table();
        table.setFillParent(true);
        table.setDebug(true);

        stage.addActor(table);

        Skin skin = new Skin(Gdx.files.internal("skin/vhs-ui.json"));

        Label label = new Label("Tutorial", skin);

        table.add(label);
    }

    @Override
    public void render(float delta) {
        // clear the screen ready for next set of images to be drawn
        Gdx.gl.glClearColor(51f/255f, 153f/255f, 51f/255f, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell our stage to do actions and draw itself
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        controller.catchBackKey(parent);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
    }
}
