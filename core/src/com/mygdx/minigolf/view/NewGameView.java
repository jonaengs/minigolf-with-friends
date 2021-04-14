package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;
import com.mygdx.minigolf.network.Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewGameView extends View {

    public static final int MAX_NUM_PLAYERS = 4;
    private List<Label> players = new ArrayList<>();
    Label lobbyID;
    Client client;


    public NewGameView() {
        super();

        // Creating actors
        Label title = new Label("New Game", skin);
        lobbyID = new Label("LOBBY ID", skin);
        Label players = new Label("Players", skin);
        TextButton start = new TextButton("Start", skin);

        // Transform actors
        title.setFontScale(3f);
        title.setOrigin(Align.center);
        lobbyID.setFontScale(2f);
        lobbyID.setOrigin(Align.center);
        players.setFontScale(2f);
        players.setOrigin(Align.center);
        start.setTransform(true);
        start.scaleBy(1f);
        start.setOrigin(Align.center);

        // Add actors to table
        table.add(title).expandX();
        table.row().pad(20f, 0, 40f, 0);
        table.add(lobbyID).expandX();
        table.row().pad(10f, 0, 40f, 0);
        table.add(players).expandX();

        // For testing purposes
        for (int i = 0; i < MAX_NUM_PLAYERS; i++) {
            Label player = new Label("Player [empty]", skin);
            table.row().pad(10f, 0, 10f, 0);
            table.add(player).expandX();
            this.players.add(player);
        }

        table.row().pad(50f, 0, 0, 0);
        table.add(start).expandX();

        start.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    ScreenController.gameView.create();
                    client.startGame();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //TODO: Go to game actual game
            }
        });
        start.addListener(new ScreenController.ChangeViewListener(ScreenController.gameView));
    }

    @Override
    public void show() {
        super.show();
        try {
            client = new Client();
            lobbyID.setText(client.createLobby().toString());
            client.runAsThread();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        // TODO: some code to update player status, either here or #render
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (client != null) {
            for (int i = 0; i < client.playerList.size(); i++) {
                players.get(i).setText(client.playerList.get(i));
            }
        }
    }
}
