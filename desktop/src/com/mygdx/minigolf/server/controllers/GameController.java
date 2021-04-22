package com.mygdx.minigolf.server.controllers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.ComponentMappers.PlayerMapper;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.network.messages.NetworkedGameState;
import com.mygdx.minigolf.network.messages.NetworkedGameState.PlayerState;
import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientGameCommand;
import com.mygdx.minigolf.network.messages.Message.ServerGameCommand;
import com.mygdx.minigolf.server.ServerUtils;
import com.mygdx.minigolf.server.communicators.GameCommunicationHandler;
import com.mygdx.minigolf.server.communicators.LobbyCommunicationHandler;
import com.mygdx.minigolf.util.ConcurrencyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class GameController extends BaseController<GameCommunicationHandler, ServerGameCommand, ClientGameCommand> {
    private static final int REFRESH_RATE = 1_000 / 15; // in milliseconds
    private State state = State.INITIALIZING;
    HeadlessGame game;

    Map<GameCommunicationHandler, Entity> players;
    Map<GameCommunicationHandler, Physical> playerPhysicals;
    NetworkedGameState networkedGameState;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameController(List<LobbyCommunicationHandler> lobbyComms) throws InterruptedException {
        game = new HeadlessGame();
        ServerUtils.initGame(game);

        // Stop lobby communication handlers
        for (LobbyCommunicationHandler comm : lobbyComms) {
            comm.running.set(false);
            comm.runningThread.join();
        }

        // Setup game communication handlers and start them
        lobbyComms.forEach(comm -> comms.add(new GameCommunicationHandler(comm)));
        comms.forEach(comm -> new Thread(comm).start());

        // TODO: Use GameData and GameController for these
        // Setup game data
        players = comms.stream().collect(Collectors.toMap(
                        comm -> comm,
                        ____ -> game.getFactory().createPlayer(-1, -1)
                ));
        playerPhysicals = players.keySet().stream().collect(Collectors.toMap(
                        comm -> comm,
                        comm -> players.get(comm).getComponent(Physical.class)
                ));
        networkedGameState = new NetworkedGameState(comms.stream().collect(Collectors.toMap(
                comm -> comm.playerName,
                ____ -> new NetworkedGameState.PlayerState(new Vector2(0, 0), new Vector2(0, 0))
        )));
    }

    // TODO: Improve parameter or make players an object attribute
    private Map<String, Integer> getScores(Map<GameCommunicationHandler, Entity> players) {
        return players.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().playerName,
                entry -> PlayerMapper.get(players.get(entry.getKey())).getLevelStrokes()
        ));
    }

    // Clears all comm recvBuffers of input data
    private void clearComms() {
        // TODO (Is this method needed?)
    }

    private void updateGameState() {
        playerPhysicals.entrySet().forEach(
                entry -> {
                    PlayerState playerState = networkedGameState.stateMap.get(entry.getKey().playerName);
                    playerState.position = entry.getValue().getPosition();
                    playerState.velocity = entry.getValue().getVelocity();
                }
        );
    }

    private void broadcastGameState() {
        broadcast(new Message<>(ServerGameCommand.GAME_DATA, networkedGameState));
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());

        List<GameCommunicationHandler> playersToRemove = new ArrayList<>();
        Iterator<String> levelsIterator = Arrays.asList(CourseLoader.getFileNames()).iterator();
        String currentLevel = null;
        State prevState = null;
        while (true) {
            if (prevState != state)
                System.out.println(state);
            prevState = state;

            switch (state) {
                case INITIALIZING: // TODO: Remove
                    state = State.SELECTING_LEVEL;
                    break;
                case SELECTING_LEVEL:
                    try {
                        currentLevel = levelsIterator.next();
                        barrier(
                                new Message<>(ServerGameCommand.LOAD_LEVEL, currentLevel),
                                new Message<>(ClientGameCommand.LEVEL_LOADED, currentLevel)
                        );
                        state = State.LOADING_LEVEL;
                    } catch (NoSuchElementException e) {
                        broadcast(new Message<>(ServerGameCommand.GAME_COMPLETE));
                        state = State.EXITING;
                    }
                    break;
                case LOADING_LEVEL:
                    game.loadLevel(currentLevel, Gdx.app);
                    // TODO: Use GameController
                    ConcurrencyUtils.waitForPostRunnable(() -> {
                        playerPhysicals.values().forEach(
                                p -> p.setPosition(game.currentLevel.getSpawnCenter())
                        );
                        players.values().forEach(p -> PlayerMapper.get(p).completed = false);
                    });
                    broadcast(new Message<>(ServerGameCommand.START_GAME));
                    state = State.IN_GAME;
                    break;
                case IN_GAME:
                    long delta;
                    while (true) {
                        long t0 = System.currentTimeMillis();

                        if (players.values().stream().allMatch(p -> PlayerMapper.get(p).isCompleted())) {
                            state = State.LEVEL_COMPLETE;
                            break;
                        }

                        comms.forEach(comm -> {
                            Message<ClientGameCommand> msg = comm.read();
                            if (msg != null) {
                                System.out.println("DATA: " + msg);
                                switch (msg.command) {
                                    case EXIT:
                                        playersToRemove.add(comm);
                                        break;
                                    case INPUT:
                                        System.out.println("V: " + msg.data);
                                        playerPhysicals.get(comm).setVelocity((Vector2) msg.data);
                                        PlayerMapper.get(players.get(comm)).incrementStrokes();
                                        System.out.println("Set velocity to: " + playerPhysicals.get(comm).getVelocity());
                                        break;
                                }
                            }
                        });
                        // TODO: Notify players explicitly in some way?
                        comms.removeAll(playersToRemove);
                        playersToRemove.clear();
                        if (comms.isEmpty()) state = State.EXITING;
                        updateGameState();
                        broadcastGameState();

                        // TODO: Where is engine.update()??
                        delta = System.currentTimeMillis() - t0;
                        try {
                            Thread.sleep(Math.max(0, REFRESH_RATE - delta));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case LEVEL_COMPLETE:
                    broadcast(new Message<>(ServerGameCommand.LEVEL_COMPLETE));
                    state = State.SCORE_SCREEN;
                    break;
                case SCORE_SCREEN:
                    broadcast(new Message<>(ServerGameCommand.GAME_SCORE, getScores(players)));
                    try {
                        Thread.sleep(5_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    state = State.SELECTING_LEVEL;
                    break;
                case EXITING:
                    comms.forEach(c -> c.running.set(false));
                    game.dispose();
                    Gdx.app.exit();
                    return;
            }
        }
    }

    private enum State {
        INITIALIZING, SELECTING_LEVEL, LOADING_LEVEL, IN_GAME, LEVEL_COMPLETE, SCORE_SCREEN, EXITING;
    }
}
