package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;


public class ScoreView extends View {

    public ScoreView() {
        super();

        // Creating actors
        Label label = new Label("Scoreboard", skin);

        // Transform actors
        label.setFontScale(2f);
        label.setOrigin(Align.center);

        // Adding actors to table
        table.add(label).expandX();
        table.row().pad(10f, 0, 10f, 0);
    }

    @Override
    public void show() {
        super.show();

        // TODO: Use client playerlist instead
        for (int i = 0; i < 4; i++) {
            Label player = new Label("Player: " + (int)(Math.random() * 10) + " strokes", skin);
            table.row().pad(10f, 0, 10f, 0);
            table.add(player).expandX();
        }
    }

}
