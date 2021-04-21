package com.mygdx.minigolf.controller;

import com.badlogic.gdx.Screen;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.model.GameData;

public class ScreenController extends GameData.Subscriber {
    private static ScreenController instance;
    private ScreenController() {
        setupSubscriptions(GameData.get().state);
    }

    public static ScreenController get() {
        if (instance == null) {
            instance = new ScreenController();
        }
        return instance;
    }

    public void changeScreen(Screen screen) {
        Game.getInstance().setScreen(screen);
    }

    @Override
    public void notify(Object change, GameData.Event changeEvent) {
        if (changeEvent == GameData.Event.STATE_SET) {
            switch ((GameData.State) change) {
                case IN_GAME:
                    changeScreen(Screens.GAME_VIEW);
                    break;
            }
        }
    }
}
