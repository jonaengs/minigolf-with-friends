package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.network.Client;
import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.util.ConcurrencyUtils;
import com.mygdx.minigolf.view.GameView;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

public class GameController extends GameData.Subscriber {
    HeadlessGame game;
    Client client;
    boolean clientGame = true;

    public GameController(HeadlessGame game) throws IOException {
        setupSubscriptions(GameData.get().levelName, GameData.get().state);
        this.game = game;
        client = new Client();
    }

    public void createLobby() throws IOException {
        client.createLobby();
        GameData.get().state.set(GameData.State.JOINING_LOBBY);
        new Thread(client).start();
    }

    public void joinLobby(Integer lobbyID) throws IOException {
        client.joinLobby(lobbyID);
        GameData.get().state.set(GameData.State.JOINING_LOBBY);
        new Thread(client).start();
    }

    public void startGame() throws IOException {
        client.startGame();
    }

    protected void reset() throws IOException {
        // TODO: new client, ++
        client = new Client();
        GameData.reset();
    }

    private void createPlayers() {
        GameData.get().players.set(
                GameData.get().playerNames.get().stream().collect(Collectors.toMap(
                        name -> name,
                        ____ -> game.factory.createPlayer(-1, -1)
                ))
        );
    }

    private void resetPlayers() {
        GameData.get().players.get().values().forEach(p ->
                ComponentMappers.PhysicalMapper.get(p).setPosition(GameData.get().level.get().getSpawnCenter())
        );
    }

    // Hide player when they complete e level
    private void hidePlayer() {
        // TODO
    }

    public void loadLevel(String levelName) {
        ConcurrencyUtils.waitForPostRunnable(() -> {
                    if (GameData.get().level.get() != null) {
                        // dispose of the previous level before loading the new one
                        GameData.get().level.get().dispose(game.engine);
                    }
                    GameData.get().level.set(game.levelLoader.load(levelName));
                }
        );
    }

    // TODO: Make separate versions for client and server or something like that
    @Override
    public void notify(Object change, GameData.Event event) {
        GameData gameData = GameData.get();
        switch (event) {
            case LEVEL_NAME_SET:
                loadLevel((String) change);
                resetPlayers();
                break;
            case STATE_SET:
                switch ((GameData.State) change) {
                    case INITIALIZING_GAME:
                        game.create();
                        createPlayers();
                        if (clientGame) {
                            Entity localPlayer = gameData.players.get().get(gameData.localPlayerName.get());
                            ((GameView) game).setInput(localPlayer);
                        }
                        break;
                    case SCORE_SCREEN:
                        System.out.println(Collections.singletonList(gameData.scores.get()));
                        // ScreenController.changeScreen(SCORE_SCREEN);
                        break;
                    case GAME_OVER:
                        // TODO: Show "game over" button on score screen that takes player to main menu
                        try {
                            reset();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        ScreenController.changeScreen(ScreenController.MAIN_MENU_VIEW);
                        break;
                }
                break;
            case PLAYER_REMOVED:
                String playerName = (String) change;
                game.engine.removeEntity(gameData.players.get().get(playerName));

                break;
        }
    }
}
