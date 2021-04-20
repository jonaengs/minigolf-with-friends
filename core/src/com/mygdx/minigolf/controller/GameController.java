package com.mygdx.minigolf.controller;

import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.GameState;
import com.mygdx.minigolf.model.levels.LevelLoader;
import com.mygdx.minigolf.util.ConcurrencyUtils;
import com.mygdx.minigolf.view.GameView;

import java.util.stream.Collectors;

import static com.mygdx.minigolf.model.GameState.Event.LEVEL_NAME_CHANGE;
import static com.mygdx.minigolf.model.GameState.Event.STATE_CHANGE;

public class GameController implements GameState.Observer {
    LevelLoader levelLoader;
    HeadlessGame game;
    GameState gameState;
    boolean server = false;

    public GameController(HeadlessGame game) {
        this.game = game;
        levelLoader = new LevelLoader(game.factory);
        gameState.addObserver(this, LEVEL_NAME_CHANGE, STATE_CHANGE);
    }

    public void connectToServer() {

    }

    public void setupClient() {

    }

    public void createPlayers() {
        gameState.setPlayers(
                gameState.getPlayerNames().stream().collect(Collectors.toMap(
                        name -> name,
                        ____ -> game.getFactory().createPlayer(-1, -1)
                ))
        );
    }

    public void resetPlayers() {

    }

    // Hide player when they complete e level
    public void hidePlayer() {

    }

    private void loadLevel(String levelName) {
        ConcurrencyUtils.waitForPostRunnable(() -> {
                    if (GameState.getLevel() != null) {
                        // dispose of the previous level before loading the new one
                        GameState.getLevel().dispose(game.engine);
                    }
                    GameState.setLevel(levelLoader.load(levelName));
                }
        );
    }

    @Override
    public void update(Object change, GameState.Event event) {
        switch (event) {
            case LEVEL_NAME_CHANGE:
                resetPlayers();
                loadLevel((String) change);
                break;
            case STATE_CHANGE:
                switch ((GameState.State) change) {
                    case INITIALISING_GAME:
                        createPlayers();
                        if (!server) {
                            ((GameView) game).setInput(GameState.getPlayers().get(GameState.getLocalPlayerName()));
                            ScreenController.LOBBY_VIEW.enterGame();
                        }
                        break;
                    case SCORE_SCREEN:
                        // ScreenController.changeScreen(SCORE_SCREEN);
                        break;
                }
        }
    }
}
