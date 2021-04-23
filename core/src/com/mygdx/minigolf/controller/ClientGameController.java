package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.network.Client;
import com.mygdx.minigolf.view.GameView;

import java.io.IOException;
import java.util.Collections;

public class ClientGameController extends GameController implements GameData.Notifiable {
    Client client;
    GameData gameData;
    GameView game;

    public ClientGameController(GameView game) throws IOException {
        super(game);
        gameData = GameData.get();
        client = new Client();

        GameData.subscribe(this, gameData.levelName, gameData.state);
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
        // TODO: Flesh out
        client = new Client();
        // GameData.reset();
        gameData = GameData.get();
    }

    private void createPlayers() {
        gameData.players.set(super.createPlayers(gameData.playerNames.get()));
    }

    private void setInput(Entity player) {
        Gdx.input.setInputProcessor(
                new InputHandler(game.getGraphicsSystem().getCam(), player, game.factory)
        );
    }

    private void resetPlayers() {
        super.resetPlayers(gameData.players.get().values());
    }

    // Hide player when they complete e level
    private void hidePlayer() {
        // TODO
    }

    // TODO: Make separate versions for client and server or something like that
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
                        game.create();
                        createPlayers();
                        Entity localPlayer = gameData.players.get().get(gameData.localPlayerName.get());
                        setInput(localPlayer);
                        break;
                    case SCORE_SCREEN:
                        System.out.println(Collections.singletonList(gameData.scores.get()));
                        break;
                    case GAME_OVER:
                        try {
                            reset();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
