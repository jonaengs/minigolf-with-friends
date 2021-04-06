package com.mygdx.minigolf.view.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class TutorialScreen extends View {


    public TutorialScreen() {
        super();

        Label label = new Label("Tutorial", skin);
        Label text = new Label("Insert tutorial text?", skin);

        // Transform actors
        label.setFontScale(3f);

        // Adding actors to table
        table.add(label);
        table.row().pad(60f, 0, 0, 0);
        table.add(text);
    }

}
