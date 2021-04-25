package com.mygdx.minigolf.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.mygdx.minigolf.model.GameData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ScoreView extends View {

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
        title.setFontScale(1f);
    }

    @Override
    public void show() { // Don't change user input
    }

    @Override
    public void hide() { // Don't unsubscribe from notifications
    }

    private List<Label> generatePlayerLabels(List<String> players) {
        String localPlayer = GameData.get().localPlayerName.get();
        return players.stream().map(p ->
                new Label(localPlayer.contentEquals(p) ? "(You) " + p : p, skin)
        ).collect(Collectors.toList());
    }

    private List<Label> generatePlayerLabels(Map<String, Integer> players) {
        String localPlayer = GameData.get().localPlayerName.get();
        return players.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> {
                    String player = entry.getKey();
                    String suffix = localPlayer.contentEquals(player) ? "\t<-- (YOU)" : "";
                    return new Label(player + "\t" + entry.getValue() + suffix, skin);
                })
                .collect(Collectors.toList());

    }

    private void createScoreBoard(Map<String, Integer> scores) {
        for (Label label : generatePlayerLabels(scores)) {
            table.add(label).expandX();
            table.row().pad(10f, 0, 10f, 0);
        }
    }

    @Override
    public void notify(Object change, GameData.Event changeEvent) {
        switch (changeEvent) {
            case PLAYERS_SET:
                // setupScoreBoard((Map<String, Entity>) change); // Requires scoreboard to not be lazily created
                break;
            case SCORES_SET:
                createScoreBoard((Map<String, Integer>) change);
                break;
        }
    }
}
