package com.mygdx.minigolf.view.screens;

import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;

import java.util.Arrays;

public class MainMenuScreen extends View {

    public MainMenuScreen() {
        super();

        // Creating elements
        TextButton newGame = new TextButton("New Game", skin);
        newGame.addListener(new ScreenController.ChangeViewListener(ScreenController.newGameScreen));

        TextButton joinGame = new TextButton("Join Game", skin);
        joinGame.addListener(new ScreenController.ChangeViewListener(ScreenController.joinGameScreen));

        TextButton settings = new TextButton("Settings", skin);
        settings.addListener(new ScreenController.ChangeViewListener(ScreenController.settingsScreen));

        TextButton tutorial = new TextButton("Tutorial", skin);
        tutorial.addListener(new ScreenController.ChangeViewListener(ScreenController.tutorialScreen));

        // Transform actors
        for(TextButton btn : Arrays.asList(newGame, joinGame, settings, tutorial)) {
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
    }

}
