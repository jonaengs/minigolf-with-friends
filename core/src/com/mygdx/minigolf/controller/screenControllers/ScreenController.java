package com.mygdx.minigolf.controller.screenControllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.view.GameView;
import com.mygdx.minigolf.view.JoinGameView;
import com.mygdx.minigolf.view.MainMenuView;
import com.mygdx.minigolf.view.NewGameView;
import com.mygdx.minigolf.view.SettingsView;
import com.mygdx.minigolf.view.TutorialView;

public class ScreenController {

    public static final GameView gameView = new GameView();
    public static final TutorialView TUTORIAL_VIEW = new TutorialView();
    public static final SettingsView SETTINGS_VIEW = new SettingsView();
    public static final NewGameView NEW_GAME_VIEW = new NewGameView();
    public static final JoinGameView JOIN_GAME_VIEW = new JoinGameView();
    public static final MainMenuView MAIN_MENU_VIEW = new MainMenuView();

    public static void changeScreen(Screen screen) {
    }

    public static void catchBackKey() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        if (Gdx.input.isKeyPressed(Input.Keys.BACK) || Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            changeScreen(MAIN_MENU_VIEW);
        }
    }

    public static class ChangeViewListener extends ChangeListener {

        Screen view;

        public ChangeViewListener(Screen targetView) {
            this.view = targetView;
        }


        @Override
        public void changed(ChangeEvent event, Actor actor) {
            ScreenController.changeScreen(view);
        }
    }
}
