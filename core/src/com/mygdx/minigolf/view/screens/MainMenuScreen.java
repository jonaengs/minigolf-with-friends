package com.mygdx.minigolf.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;

import java.util.ArrayList;

public class MainMenuScreen implements Screen {

    private static Game parent = com.mygdx.minigolf.Game.game;
    private Stage stage;
    private ScreenController controller;


    public MainMenuScreen(Game game) {
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

        // Creating elements
        TextButton newGame = new TextButton("New Game", skin);
        TextButton joinGame = new TextButton("Join Game", skin);
        TextButton settings = new TextButton("Settings", skin);
        TextButton tutorial = new TextButton("Tutorial", skin);

        ArrayList<TextButton> buttons = new ArrayList<>();
        buttons.add(newGame);
        buttons.add(joinGame);
        buttons.add(settings);
        buttons.add(tutorial);

        // Transform actors
        for(TextButton btn : buttons) {
            btn.setTransform(true);
            btn.scaleBy(1f);
            btn.setOrigin(Align.center);
        }

        // Add actors to table
        table.add(newGame).expand();
        table.row().pad(30, 0, 30, 0).expand();
        table.add(joinGame).expand();
        table.row().expand();
        table.add(settings).expand();
        table.row().pad(30, 0, 30, 0).expand();
        table.add(tutorial).expand();

        controller.mainMenu(newGame, settings, tutorial, joinGame, parent);
    }

    @Override
    public void render(float delta) {
        // clear the screen ready for next set of images to be drawn
        Gdx.gl.glClearColor(51f/255f, 153f/255f, 51f/255f, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell our stage to do actions and draw itself
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Change the stage's viewport when teh screen size is changed
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
