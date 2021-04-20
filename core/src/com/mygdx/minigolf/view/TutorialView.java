package com.mygdx.minigolf.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.mygdx.minigolf.util.Constants;

public class TutorialView extends View {

    public TutorialView() {
        super();

        Label label = new Label("Tutorial", skin);
        Label text = new Label(Constants.TUTORIAL_TEXT, skin);
        ScrollPane pane = new ScrollPane(text);
        Texture img = new Texture(Gdx.files.internal("game_input.png"));

        // Transform actors
        label.setFontScale(3f);
        text.setWrap(true);

        // Adding actors to table
        table.add(label);
        table.row().pad(30f, 0, 30f, 0);
        table.add(pane).width(Constants.SCREEN_WIDTH / 2f);
        table.add(new Image(img));
    }

}
