package com.mygdx.minigolf.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.view.GameView;
import com.mygdx.minigolf.view.JoinGameView;
import com.mygdx.minigolf.view.LobbyView;
import com.mygdx.minigolf.view.MainMenuView;
import com.mygdx.minigolf.view.SettingsView;
import com.mygdx.minigolf.view.TutorialView;

public class ScreenController implements GameData.Notifiable {
    public static final GameView GAME_VIEW = new GameView();
    public static final TutorialView TUTORIAL_VIEW = new TutorialView();
    public static final SettingsView SETTINGS_VIEW = new SettingsView();
    public static final LobbyView LOBBY_VIEW = new LobbyView(GameData.get().lobbyID, GameData.get().playerNames);
    public static final JoinGameView JOIN_GAME_VIEW = new JoinGameView(GameData.get().lobbyID);
    public static final MainMenuView MAIN_MENU_VIEW = new MainMenuView();

    public static void changeScreen(Screen screen) {
        /*
                if (screen == MAIN_MENU_VIEW) {
            GameData.reset();
        }
         */
        Game.getInstance().setScreen(screen);
    }

    public static void catchBackKey() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        if (Gdx.input.isKeyPressed(Input.Keys.BACK) || Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            changeScreen(MAIN_MENU_VIEW);
        }
    }

    @Override
    public void notify(Object change, GameData.Event changeEvent) {
        if (changeEvent == GameData.Event.STATE_SET) {
            switch ((GameData.State) change) {
                case IN_LOBBY:
                    changeScreen(LOBBY_VIEW);
                    break;
                case IN_GAME:
                    changeScreen(GAME_VIEW);
                    break;
            }
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
