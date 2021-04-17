package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.controller.ScreenController;

import java.io.IOException;

public class JoinGameView extends View {
    Label title;
    Label status;
    TextField code;
    TextButton joinButton;

    public JoinGameView() {
        super();
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
                try {
                    status.setText("Attempting to join lobby " + code.getText());
                    // TODO: Find better solution
                    while (Game.getInstance().client == null) {
                        Thread.sleep(100);
                    }
                    Game.getInstance().client.joinLobby(Integer.parseInt(code.getText()));
                    Game.getInstance().client.runAsThread();
                    ScreenController.changeScreen(ScreenController.LOBBY_VIEW);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    System.out.println("INVALID LOBBY ID");
                    status.setText("Invalid lobby ID: " + code.getText());
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
        status.setText("");
    }

}
