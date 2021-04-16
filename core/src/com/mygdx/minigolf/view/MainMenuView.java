package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.controller.ScreenController;
import com.mygdx.minigolf.network.Client;

import java.io.IOException;
import java.util.Arrays;

public class MainMenuView extends View {

    public MainMenuView() {
        super();

        // Creating elements
        TextButton newGame = new TextButton("New Game", skin);
        newGame.addListener(new ScreenController.ChangeViewListener(ScreenController.LOBBY_VIEW));

        TextButton joinGame = new TextButton("Join Game", skin);
        joinGame.addListener(new ScreenController.ChangeViewListener(ScreenController.JOIN_GAME_VIEW));

        TextButton settings = new TextButton("Settings", skin);
        settings.addListener(new ScreenController.ChangeViewListener(ScreenController.SETTINGS_VIEW));

        TextButton tutorial = new TextButton("Tutorial", skin);
        tutorial.addListener(new ScreenController.ChangeViewListener(ScreenController.TUTORIAL_VIEW));

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

        ChangeListener toNetworkedScreen = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    Game.getInstance().client = new Client();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        joinGame.addListener(toNetworkedScreen);
        newGame.addListener(toNetworkedScreen);
        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    ScreenController.LOBBY_VIEW.lobbyID = Game.getInstance().client.createLobby();
                    Game.getInstance().client.runAsThread();
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
