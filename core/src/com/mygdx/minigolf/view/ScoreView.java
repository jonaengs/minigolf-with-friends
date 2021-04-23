package com.mygdx.minigolf.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.model.GameData;

import java.util.HashMap;
import java.util.Map;


public class ScoreView extends View {
    private Label title;
    private Map<String, Label> playerLabels = new HashMap<>();

    public ScoreView(GameData.Observable... observables) {
        super(observables);
        setupSubscriptions();
        // Creating actors
        title = new Label("Scoreboard", skin);

        // Transform actors
        title.setFontScale(2f);
        title.setOrigin(Align.center);

        // Adding actors to table
        table.add(title).expandX();
        table.row().pad(10f, 0, 10f, 0);
    }

    @Override
    public void show() {
        super.show();

    }

    private void setupScoreBoard(Map<String, Entity> players) {
        for (String player : players.keySet()) {
            Label label = new Label(player + "\t" + 0, skin);
            playerLabels.put(player, label);
            table.add(label).expandX();
            table.row().pad(10f, 0, 10f, 0);
        }
    }

    private void updateScoreBoard(Map<String, Integer> scores) {
        scores.keySet().forEach(player ->
                playerLabels.get(player).setText(player + "\t" + scores.get(player))
        );
    }

    @Override
    public void hide() {
        // Don't unsubscribe when hidden
    }

    @Override
    public void notify(Object change, GameData.Event changeEvent) {
        switch (changeEvent) {
            case PLAYERS_SET:
                setupScoreBoard((Map<String, Entity>) change);
                break;
            case SCORES_SET:
                updateScoreBoard((Map<String, Integer>) change);
                break;
        }
    }
}
