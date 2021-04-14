package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.controller.screenControllers.ScreenController;

public class JoinGameView extends View {

    public JoinGameView() {
        super();
        Label label = new Label("Multiplayer", skin);
        TextField code = new TextField("", skin);
        code.setMaxLength(6);
        TextButton join = new TextButton("Join", skin);

        label.setFontScale(3f);
        code.setScale(2f);
        join.setTransform(true);
        join.scaleBy(1f);
        join.setOrigin(Align.center);

        table.add(label).expandX();
        table.row().pad(250f, 0, 0, 0);
        table.add(code).expandX();
        table.row().pad(100f, 0, 0, 0);
        table.add(join).expandX();

        join.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Join lobby, access text in textfield
                // code.getText();
            }
        });
        join.addListener(new ScreenController.ChangeViewListener(ScreenController.gameView));
    }

}
