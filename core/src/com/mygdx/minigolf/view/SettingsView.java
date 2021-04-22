package com.mygdx.minigolf.view;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.minigolf.Game;

public class SettingsView extends View {

    public SettingsView() {
        super();

        // Creating actors
        Label label = new Label("Settings", skin);
        Label volume_text = new Label("Volume: ", skin);
        CheckBox volume = new CheckBox("Music ON/OFF", skin);

        // Transform actors
        label.setFontScale(3f);
        volume_text.setFontScale(2f);
        volume.setScale(1f);

        // Adding actors to table
        table.add(label);
        table.row().pad(30, 0, 60, 0);
        table.add(volume_text);
        table.add(volume);

        volume.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Music music = Game.getInstance().music;
                music.setVolume(music.getVolume() > 0 ? 0 : 1);
            }
        });
    }

}
