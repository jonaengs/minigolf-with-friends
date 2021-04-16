package com.mygdx.minigolf.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.controller.ScreenController;
import com.mygdx.minigolf.network.Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LobbyView extends View {

    public static final int MAX_NUM_PLAYERS = 4;
    private List<Label> players = new ArrayList<>();
    Integer lobbyID = 0;
    Label lobbyIDLabel;
    Client client;


    public LobbyView() {
        super();

        // Creating actors
        Label title = new Label("New Game", skin);
        lobbyIDLabel = new Label("LOBBY ID", skin);
        Label players = new Label("Players", skin);
        TextButton start = new TextButton("Start", skin);

        // Transform actors
        title.setFontScale(3f);
        title.setOrigin(Align.center);
        lobbyIDLabel.setFontScale(2f);
        lobbyIDLabel.setOrigin(Align.center);
        players.setFontScale(2f);
        players.setOrigin(Align.center);
        start.setTransform(true);
        start.scaleBy(1f);
        start.setOrigin(Align.center);

        // Add actors to table
        table.add(title).expandX();
        table.row().pad(20f, 0, 40f, 0);
        table.add(lobbyIDLabel).expandX();
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
                    client.startGame();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void enterGame() throws InterruptedException {
        final Object lock = new Object();
        synchronized (lock) {
            Gdx.app.postRunnable(() -> {
                ScreenController.gameView.create();
                ScreenController.changeScreen(ScreenController.gameView);
                synchronized (lock) {
                    lock.notify();
                }
            });
            lock.wait();
        }
    }

    @Override
    public void render(float delta) {
        if (client == null) {
            client = Game.getInstance().client;
            if (client != null) {
                lobbyIDLabel.setText(lobbyID.toString());
            }
        }
        super.render(delta);
        if (client != null) {
            for (int i = 0; i < client.playerList.size(); i++) {
                players.get(i).setText(client.playerList.get(i));
            }
        }
    }
}
