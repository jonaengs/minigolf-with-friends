package com.mygdx.minigolf.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.util.Constants;


abstract class View extends GameData.Subscriber implements Screen {
    protected Stage stage;
    protected Table table;
    protected Skin skin;
    protected Color backgroundColor = new Color(51f / 255f, 153f / 255f, 51f / 255f, 0);

    protected View(GameData.Observable... observables) {
        super(observables);
        stage = new Stage(new ScreenViewport());

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        skin = new Skin(Gdx.files.internal("skin/vhs-ui.json"));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        setupSubscriptions();
    }

    @Override
    public void render(float delta) {
        // clear the screen ready for next set of images to be drawn
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // tell our stage to do actions and draw itself
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), Constants.REFRESH_RATE));
        stage.draw();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        if (Gdx.input.isKeyPressed(Input.Keys.BACK) || Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Game.getInstance().screenController.changeScreen(ViewFactory.MainMenuView());
        }
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
        removeSubscriptions();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void notify(Object change, GameData.Event changeEvent) {
    }

    public static class ChangeViewListener extends ChangeListener {
        Screen view;

        public ChangeViewListener(Screen targetView) {
            this.view = targetView;
        }

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            Game.getInstance().screenController.changeScreen(view);
        }
    }
}
