package com.mygdx.minigolf.server.controllers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
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

import java.io.IOException;
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
    Application app;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameController(List<LobbyCommunicationHandler> comms) throws InterruptedException {
        game = new HeadlessGame();
        app = ServerUtils.initGame(game);

        // Stop lobby communication handlers
        comms.forEach(comm -> {
            try {
                comm.running.set(false);
                comm.runningThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // Setup game communication handlers and start them
        comms.forEach(comm -> {
            try {
                com.mygdx.minigolf.server.communicators.GameCommunicationHandler gameComm = new com.mygdx.minigolf.server.communicators.GameCommunicationHandler(comm);
                this.comms.add(gameComm);
                new Thread(gameComm).start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    // TODO: Improve parameter or make players an object attribute
    private Map<String, Integer> getScores(Map<com.mygdx.minigolf.server.communicators.GameCommunicationHandler, Entity> players) {
        return players.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().playerName,
                entry -> PlayerMapper.get(players.get(entry.getKey())).getLevelStrokes()
        ));
    }

    // Clears all comm recvBuffers of input data
    private void clearComms() {
        comms.forEach(comm -> {
            synchronized (comm.recvBuffer) {
                comm.recvBuffer.forEach(msg -> {
                    if (msg.command == ClientGameCommand.INPUT) {
                        comm.recvBuffer.remove(msg);
                    }
                });
            }
        });
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        // TODO: Consider changing these into attributes
        Map<com.mygdx.minigolf.server.communicators.GameCommunicationHandler, Entity> players = comms.stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> game.getFactory().createPlayer(-1, -1)
                ));
        Map<GameCommunicationHandler, Physical> playerPhysicalComponents = players.keySet().stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> players.get(comm).getComponent(Physical.class)
                ));
        NetworkedGameState networkedGameState = new NetworkedGameState(comms.stream().collect(Collectors.toMap(
                comm -> comm.playerName,
                comm -> new NetworkedGameState.PlayerState(new Vector2(0, 0), new Vector2(0, 0))
        )));

        Iterator<String> levelsIterator = Arrays.asList(CourseLoader.getFileNames()).iterator();
        String currentLevel = null;
        State prevState = state;
        while (true) {
            if (prevState != state)
                System.out.println(state);
            prevState = state;

            switch (state) {
                case INITIALIZING:
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
                        // All levels complete. Broadcast final scores and exit
                        broadcast(new Message<>(ServerGameCommand.GAME_COMPLETE));
                        // broadcast(new Message<>(ServerGameCommand.GAME_SCORE, getScores(players)));
                        state = State.EXITING;
                    }
                    break;
                case LOADING_LEVEL:
                    game.loadLevel(currentLevel, app);
                    Object lock = new Object();
                    synchronized (lock) {
                        Gdx.app.postRunnable(() -> {
                                    playerPhysicalComponents.values().forEach(
                                            p -> p.setPosition(game.currentLevel.getSpawnCenter())
                                    );
                                    players.values().forEach(p -> PlayerMapper.get(p).completed = false);
                                    synchronized (lock) {
                                        lock.notify();
                                    }
                                }
                        );
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
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
                            Message<ClientGameCommand> clientMsg;
                            synchronized (comm.recvBuffer) {
                                clientMsg = comm.recvBuffer.poll();
                            }
                            if (clientMsg != null) {
                                System.out.println("DATA: " + clientMsg);
                                switch (clientMsg.command) {
                                    case EXIT:
                                        comms.remove(comm); // TODO: Don't remove inside loop. Causes ConcurrentModificationException (?)
                                        if (comms.isEmpty()) state = State.EXITING;
                                        break;
                                    case INPUT:
                                        System.out.println("V: " + clientMsg.data);
                                        playerPhysicalComponents.get(comm).setVelocity((Vector2) clientMsg.data);
                                        PlayerMapper.get(players.get(comm)).incrementStrokes();
                                        System.out.println("Set velocity to: " + playerPhysicalComponents.get(comm).getVelocity());
                                        break;
                                }
                            }
                        });
                        // update gameState and broadcast it
                        playerPhysicalComponents.entrySet().forEach(
                                entry -> {
                                    PlayerState playerState = networkedGameState.stateMap.get(entry.getKey().playerName);
                                    playerState.position = entry.getValue().getPosition();
                                    playerState.velocity = entry.getValue().getVelocity();
                                }
                        );
                        broadcast(new Message<>(
                                ServerGameCommand.GAME_DATA,
                                networkedGameState
                        ));

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