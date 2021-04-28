package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.model.GameData;

import java.io.IOException;

public class JoinGameView extends View {
    Label title;
    Label status;
    TextField code;
    TextButton joinButton;

    public JoinGameView(GameData.Observable... observables) {
        super(observables);
        title = new Label("Multiplayer", skin);
        status = new Label("", skin);
        code = new TextField("", skin);
        joinButton = new TextButton("Join", skin);

        code.setMaxLength(6);
        title.setFontScale(3f);
        code.setScale(2f);
        joinButton.setTransform(true);
        joinButton.scaleBy(1f);
        joinButton.setOrigin(Align.center);

        table.add(title).expandX();
        table.row().pad(200f, 0, 0, 0);
        table.add(status).expandX();
        table.row().pad(10f, 0, 0, 0);
        table.add(code).expandX();
        table.row().pad(100f, 0, 0, 0);
        table.add(joinButton).expandX();

        joinButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                status.setText("Attempting to join lobby " + code.getText());
                Integer lobbyID = Integer.parseInt(code.getText());
                try {
                    Game.getInstance().gameController.joinLobby(lobbyID);
                } catch (IOException e) {
                    status.setText("A network error has occurred. Try restarting the game");
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
        status.setText("");
        code.setText("");
    }

    @Override
    public void notify(Object change, GameData.Event changeEvent) {
        if (changeEvent == GameData.Event.LOBBY_ID_SET) {
            Integer lobbyID = (Integer) change;
            if (lobbyID == -1) {
                status.setText("Could not find lobby: " + code.getText());
            } else if (lobbyID == -2) {
                status.setText("Lobby full: " + code.getText());
            } else {
                code.setText(lobbyID.toString());
            }
        }
    }
}
