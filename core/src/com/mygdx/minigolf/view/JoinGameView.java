package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Null;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.controller.ScreenController;
import com.mygdx.minigolf.network.Client;

import java.io.IOException;

public class JoinGameView extends View {

    public JoinGameView() {
        super();
        Label label = new Label("Multiplayer", skin);
        Label error = new Label("", skin);
        TextField code = new TextField("", skin);
        code.setMaxLength(6);
        TextButton join = new TextButton("Join", skin);

        label.setFontScale(3f);
        code.setScale(2f);
        join.setTransform(true);
        join.scaleBy(1f);
        join.setOrigin(Align.center);

        table.add(label).expandX();
        table.row().pad(200f, 0, 0, 0);
        table.add(error).expandX();
        table.row().pad(10f, 0, 0, 0);
        table.add(code).expandX();
        table.row().pad(100f, 0, 0, 0);
        table.add(join).expandX();

        join.addListener(new InputListener() {
            @Override
            public void enter (InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
                try {
                    if (Game.getInstance().client == null) {
                        Thread.sleep(500);
                    }
                    Game.getInstance().client.joinLobby(Integer.parseInt(code.getText()));
                    Game.getInstance().client.runAsThread();
                    ScreenController.changeScreen(ScreenController.LOBBY_VIEW);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    System.out.println("INVALID LOBBY ID");
                    error.setText("Invalid lobby ID: " + code.getText());
                }
            }
        });
    }

}
