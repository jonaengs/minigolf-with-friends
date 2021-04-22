package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.controller.ScreenController;

import java.io.IOException;
import java.util.Arrays;

public class MainMenuView extends View {
    TextButton newGame;
    TextButton joinGame;
    TextButton settings;
    TextButton tutorial;

    public MainMenuView() {
        super();

        // Creating elements
        newGame = new TextButton("New Game", skin);
        newGame.addListener(new ChangeViewListener(ScreenController.LOBBY_VIEW));

        joinGame = new TextButton("Join Game", skin);
        joinGame.addListener(new ChangeViewListener(ScreenController.JOIN_GAME_VIEW));

        settings = new TextButton("Settings", skin);
        settings.addListener(new ChangeViewListener(ScreenController.SETTINGS_VIEW));

        tutorial = new TextButton("Tutorial", skin);
        tutorial.addListener(new ChangeViewListener(ScreenController.TUTORIAL_VIEW));

        // Transform actors
        for (TextButton btn : Arrays.asList(newGame, joinGame, settings, tutorial)) {
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

        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try { // TODO: Find or create a better way to access the game controller
                    Game.getInstance().gameController.createLobby();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
    }

}
