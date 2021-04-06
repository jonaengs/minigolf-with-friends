package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class TutorialView extends View {


    public TutorialView() {
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
