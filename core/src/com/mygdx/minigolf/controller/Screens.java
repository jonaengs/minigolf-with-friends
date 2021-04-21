package com.mygdx.minigolf.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.view.GameView;
import com.mygdx.minigolf.view.JoinGameView;
import com.mygdx.minigolf.view.LobbyView;
import com.mygdx.minigolf.view.MainMenuView;
import com.mygdx.minigolf.view.SettingsView;
import com.mygdx.minigolf.view.TutorialView;

public class Screens {
    public static final GameView GAME_VIEW = new GameView();
    public static final TutorialView TUTORIAL_VIEW = new TutorialView();
    public static final SettingsView SETTINGS_VIEW = new SettingsView();
    public static final LobbyView LOBBY_VIEW = new LobbyView(GameData.get().lobbyID, GameData.get().playerNames);
    public static final JoinGameView JOIN_GAME_VIEW = new JoinGameView(GameData.get().lobbyID);
    public static final MainMenuView MAIN_MENU_VIEW = new MainMenuView();

    public static class ChangeViewListener extends ChangeListener {
        Screen view;

        public ChangeViewListener(Screen targetView) {
            this.view = targetView;
        }

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            ScreenController.get().changeScreen(view);
        }
    }
}
