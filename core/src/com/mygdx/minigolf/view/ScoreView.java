package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.model.GameData;

import java.util.HashMap;
import java.util.Map;


public class ScoreView extends View {
    private final Map<String, Label> playerLabels = new HashMap<>();

    public ScoreView(GameData.Observable... observables) {
        super(observables);
        setupSubscriptions();
        // Creating actors
        Label title = new Label("Scores", skin);

        // Transform actors
        title.setFontScale(2f);
        title.setOrigin(Align.center);

        // Adding actors to table
        table.add(title).expandX();
        table.row().pad(10f, 0, 10f, 0);

        // Requires ScoreView to be lazily created.
        createScoreBoard();
    }

    @Override
    public void show() { // Don't change user input
    }

    @Override
    public void hide() { // Don't unsubscribe from notifications
    }

    // TODO: Sort scoreboard. Easy solution is to delete and recreate labels with each score update
    private void createScoreBoard() {
        for (String player : GameData.get().playerNames.get()) {
            Label label = new Label(player + "\t" + 0, skin);
            playerLabels.put(player, label);
            table.add(label).expandX();
            table.row().pad(10f, 0, 100f, 0);
        }
    }

    private void updateScoreBoard(Map<String, Integer> scores) {
        scores.keySet().forEach(player ->
                playerLabels.get(player).setText(player + "\t" + scores.get(player))
        );
    }

    @Override
    public void notify(Object change, GameData.Event changeEvent) {
        switch (changeEvent) {
            case PLAYERS_SET:
                // setupScoreBoard((Map<String, Entity>) change); // Requires scoreboard to not be lazily created
                break;
            case SCORES_SET:
                if (playerLabels.isEmpty()) createScoreBoard();
                updateScoreBoard((Map<String, Integer>) change);
                break;
            case STATE_SET:
                switch ((GameData.State) change) {
                    case GAME_OVER:
                        playerLabels.values().forEach(label -> table.removeActor(label));
                        playerLabels.clear();
                        break;
                }
        }
    }
}
