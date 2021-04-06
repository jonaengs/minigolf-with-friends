package com.mygdx.minigolf.view.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;

import java.util.ArrayList;
import java.util.List;

public class NewGameScreen extends View {

    private List<Label> players = new ArrayList<>();


    public NewGameScreen() {
        super();

        // Creating actors
        Label label = new Label("New Game", skin);
        Label players_label = new Label("Players", skin);
        TextButton start = new TextButton("Start", skin);

        // Transform actors
        label.setFontScale(3f);
        label.setOrigin(Align.center);
        players_label.setFontScale(2f);
        players_label.setOrigin(Align.center);
        start.setTransform(true);
        start.scaleBy(1f);
        start.setOrigin(Align.center);

        // Add actors to table
        table.add(label).expandX();
        table.row().pad(100f, 0, 40f, 0);
        table.add(players_label).expandX();

        // For testing purposes
        for(int i = 0; i < 4; i++) {
            Label player = new Label("Player [empty]", skin);
            table.row().pad(10f, 0, 10f, 0);
            table.add(player).expandX();
            players.add(player);
        }

        table.row().pad(100f, 0, 0, 0);
        table.add(start).expandX();

        start.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //TODO: Go to game actual game
            }
        });
        start.addListener(new ScreenController.ChangeViewListener(ScreenController.gameView));
    }

    @Override
    public void show() {
        super.show();
        // TODO: some code to update player status, either here or #render
        players.forEach(p -> p.setText("Player " + (Math.random() > 0.5 ? "[joined]" : "[empty]")));
    }
}
