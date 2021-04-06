package com.mygdx.minigolf.controller.screenControllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.view.GameView;
import com.mygdx.minigolf.view.screens.JoinGameScreen;
import com.mygdx.minigolf.view.screens.MainMenuScreen;
import com.mygdx.minigolf.view.screens.NewGameScreen;
import com.mygdx.minigolf.view.screens.SettingsScreen;
import com.mygdx.minigolf.view.screens.TutorialScreen;

public class ScreenController {

    public static final GameView gameView = new GameView();
    public static final TutorialScreen tutorialScreen = new TutorialScreen();
    public static final SettingsScreen settingsScreen = new SettingsScreen();
    public static final NewGameScreen newGameScreen = new NewGameScreen();
    public static final JoinGameScreen joinGameScreen = new JoinGameScreen();
    public static final MainMenuScreen mainMenuScreen = new MainMenuScreen();

    public static void changeScreen(Screen screen) {
        Game.getInstance().setScreen(screen);
    }

    public static void catchBackKey() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        if(Gdx.input.isKeyPressed(Input.Keys.BACK) ||Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            changeScreen(mainMenuScreen);
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
