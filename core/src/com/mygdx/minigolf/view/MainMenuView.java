package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.util.Constants;

import java.io.IOException;
import java.util.Arrays;

public class MainMenuView extends View {
    TextButton newGame;
    TextButton joinGame;
    TextButton settings;
    TextButton tutorial;
    Label connectionError;

    public MainMenuView() {
        super();
        // Creating elements
        newGame = new TextButton("New Game", skin);
        joinGame = new TextButton("Join Game", skin);

        settings = new TextButton("Settings", skin);
        settings.addListener(new ChangeViewListener(ViewFactory.SettingsView()));

        tutorial = new TextButton("Tutorial", skin);
        tutorial.addListener(new ChangeViewListener(ViewFactory.TutorialView()));

        connectionError = new Label("Unable to connect to server: " + Constants.SERVER_ADDRESS, skin);
        connectionError.setColor(1, 0, 0, 1);

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
                try {
                    Game.getInstance().gameController.resetClient();
                    Game.getInstance().gameController.createLobby();
                } catch (IOException e) {
                    table.addActorAt(0, connectionError);
                }
            }
        });
        joinGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    Game.getInstance().gameController.resetClient();
                    Game.getInstance().screenController.changeScreen(ViewFactory.JoinGameView());
                } catch (IOException e) {
                    table.addActorAt(0, connectionError);
                }
            }
        });
    }

    @Override
    public void show() {
        table.removeActor(connectionError);
        super.show();
    }

}
