package com.mygdx.minigolf.controller;

import com.badlogic.gdx.Gdx;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.model.levels.LevelLoader;
import com.mygdx.minigolf.network.Client;
import com.mygdx.minigolf.util.ConcurrencyUtils;
import com.mygdx.minigolf.view.GameView;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

// Singleton
public class GameController implements GameData.Observer {
    LevelLoader levelLoader;
    HeadlessGame game;
    GameData gameData;
    Client client;
    boolean server = false;

    public GameController(HeadlessGame game) throws IOException {
        this.game = game;
        levelLoader = new LevelLoader(game.factory);
        gameData = new GameData();
        client = new Client(gameData);

        gameData.subscribe(this, gameData.levelName, gameData.state);
    }

    public void createLobby() throws IOException, ClassNotFoundException {
        client.createLobby();
    }

    public void createPlayers() {
        gameData.players.set(
                gameData.playerNames.get().stream().collect(Collectors.toMap(
                        name -> name,
                        ____ -> game.getFactory().createPlayer(-1, -1)
                ))
        );
    }

    public void resetPlayers() {
        Gdx.app.postRunnable(() ->
                gameData.players.get().values().forEach(p ->
                        ComponentMappers.PhysicalMapper.get(p).setPosition(gameData.level.get().getSpawnCenter())
                )
        );
    }

    // Hide player when they complete e level
    public void hidePlayer() {

    }

    private void disposeGameData() {
        gameData = new GameData();
    }

    private void loadLevel(String levelName) {
        ConcurrencyUtils.waitForPostRunnable(() -> {
                    if (gameData.level.get() != null) {
                        // dispose of the previous level before loading the new one
                        gameData.level.get().dispose(game.engine);
                    }
                    gameData.level.set(levelLoader.load(levelName));
                }
        );
    }

    @Override
    public void notify(Object change, GameData.Event event) {
        switch (event) {
            case LEVEL_NAME_SET:
                loadLevel((String) change);
                resetPlayers();
                break;
            case STATE_SET:
                switch ((GameData.State) change) {
                    case INITIALIZING_GAME:
                        createPlayers();
                        if (!server) {
                            ((GameView) game).setInput(gameData.players.get().get(gameData.localPlayerName.get()));
                            ScreenController.LOBBY_VIEW.enterGame();
                        }
                        break;
                    case SCORE_SCREEN:
                        System.out.println(Collections.singletonList(gameData.scores.get()));
                        // ScreenController.changeScreen(SCORE_SCREEN);
                        break;
                }
            case PLAYER_REMOVED:
                String playerName = (String) change;
                game.engine.removeEntity(gameData.players.get().get(playerName));
                break;
        }
    }
}
