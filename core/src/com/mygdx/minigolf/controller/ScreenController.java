package com.mygdx.minigolf.controller;

import com.badlogic.gdx.Screen;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.view.GameView;
import com.mygdx.minigolf.view.JoinGameView;
import com.mygdx.minigolf.view.LobbyView;
import com.mygdx.minigolf.view.MainMenuView;
import com.mygdx.minigolf.view.SettingsView;
import com.mygdx.minigolf.view.TutorialView;

public class ScreenController extends GameData.Subscriber {
    private static ScreenController instance;

    public static final GameView GAME_VIEW = new GameView();
    public static final TutorialView TUTORIAL_VIEW = new TutorialView();
    public static final SettingsView SETTINGS_VIEW = new SettingsView();
    public static final LobbyView LOBBY_VIEW = new LobbyView(GameData.get().lobbyID, GameData.get().playerNames);
    public static final JoinGameView JOIN_GAME_VIEW = new JoinGameView(GameData.get().lobbyID);
    public static final MainMenuView MAIN_MENU_VIEW = new MainMenuView();

    private ScreenController() {
        setupSubscriptions(GameData.get().state);
    }

    public static ScreenController get() {
        if (instance == null) {
            instance = new ScreenController();
        }
        return instance;
    }

    public static void changeScreen(Screen screen) {
        Game.getInstance().setScreen(screen);
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
}
