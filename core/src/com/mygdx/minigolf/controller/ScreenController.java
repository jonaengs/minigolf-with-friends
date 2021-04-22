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
import com.mygdx.minigolf.view.ViewFactory;

public class ScreenController implements GameData.Notifiable {
    Game game;
    public ScreenController(Game game) {
        this.game = game;
        GameData.subscribe(this, GameData.get().state);
    }

    public void changeScreen(Screen screen) {
        game.setScreen(screen);
    }

    @Override
    public void notify(Object change, GameData.Event changeEvent) {
        System.out.println("change = " + change + ", changeEvent = " + changeEvent);
        if (changeEvent == GameData.Event.STATE_SET) {
            switch ((GameData.State) change) {
                case IN_LOBBY:
                    changeScreen(ViewFactory.LobbyView());
                    break;
                case IN_GAME:
                    changeScreen(ViewFactory.GameView());
                    break;
                case SCORE_SCREEN:
                    // changeScreen(SCORE_VIEW);
                    break;
                case GAME_OVER:
                    // TODO: Show "game over" button on score screen that takes player to main menu
                    changeScreen(ViewFactory.MainMenuView());
                    break;
            }
        }
    }
}
