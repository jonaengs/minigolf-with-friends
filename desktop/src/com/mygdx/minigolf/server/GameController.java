package com.mygdx.minigolf.server;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.network.messages.GameState;
import com.mygdx.minigolf.network.messages.GameState.PlayerState;
import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientGameCommand;
import com.mygdx.minigolf.network.messages.Message.ServerGameCommand;
import com.mygdx.minigolf.view.GameView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class GameController implements Runnable {
    private static final int REFRESH_RATE = 1_000 / 15; // in milliseconds
    private final List<GameCommunicationHandler> comms;
    private State state = State.INITIALIZING;
    HeadlessGame game;
    Application app;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameController(List<CommunicationHandler> comms) {
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
        this.comms = comms.stream()
                .map(comm -> new GameCommunicationHandler(comm, this))
                .peek(comm -> new Thread(comm).start())
                .collect(Collectors.toList());
    }

    // TODO: Handle IOException by removing comm (and player) that it resulted from.
    private void broadcast(Message<ServerGameCommand> msg) {
        for (GameCommunicationHandler comm : comms) {
            try {
                comm.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForRecv(Message<ClientGameCommand> recv) {
        for (GameCommunicationHandler comm : comms) {
            Message<ClientGameCommand> msg;
            do { // TODO: Change to not do spin waiting
                msg = comm.recvBuffer.poll();
            } while (msg == null || msg.command != recv.command);
        }
    }

    private void synchronize(Message<ServerGameCommand> send, Message<ClientGameCommand> recv) {
        broadcast(send);
        waitForRecv(recv);
    }

    // TODO: Improve parameter or make players an object attribute
    private Map<String, Integer> getScores(Map<GameCommunicationHandler, Entity> players) {
        return players.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().name,
                entry -> 0
        ));
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        // TODO: Consider changing these into attributes
        Map<GameCommunicationHandler, Entity> players = comms.stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> game.getFactory().createPlayer(5, 5)
                ));
        Map<GameCommunicationHandler, Physical> playerPhysicalComponents = players.keySet().stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> players.get(comm).getComponent(Physical.class)
                ));
        GameState gameState = new GameState(comms.stream().collect(Collectors.toMap(
                comm -> comm.name,
                comm -> new GameState.PlayerState(new Vector2(0, 0), new Vector2(0, 0))
        )));

        Iterator<String> levelsIterator = Arrays.asList(CourseLoader.getFileNames()).iterator();
        String currentLevel = null;
        while (true) {
            switch (state) {
                case INITIALIZING:
                    state = State.SELECTING_LEVEL;
                    break;
                case SELECTING_LEVEL:
                    try {
                        currentLevel = levelsIterator.next();
                        synchronize(
                                new Message<>(ServerGameCommand.LOAD_LEVEL, currentLevel),
                                new Message<>(ClientGameCommand.LEVEL_LOADED, currentLevel)
                        );
                        state = State.LOADING_LEVEL;
                    } catch (NoSuchElementException e) {
                        // All levels complete. Broadcast final scores and exit
                        broadcast(new Message<>(ServerGameCommand.GAME_COMPLETE));
                        broadcast(new Message<>(ServerGameCommand.GAME_SCORE, getScores(players)));
                        state = State.EXITING;
                    }
                    break;
                case LOADING_LEVEL:
                    game.loadLevel(currentLevel, app);
                    playerPhysicalComponents.values().forEach(
                            p -> p.setPosition(2, 2) // TODO: Set position to be somewhere inside level spawn
                    );
                    broadcast(new Message<>(ServerGameCommand.START_GAME));
                    state = State.IN_GAME;
                    break;
                case IN_GAME:
                    long delta;
                    while (true) {
                        long t0 = System.currentTimeMillis();

                        // TODO: Set level complete if all players have reached hole
                        // if (players.values().stream().allMatch(p -> ComponentMappers.ObjectiveMapper.get(p).isFinished))
                        if (false) {
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
                                        comms.remove(comm);
                                        if (comms.isEmpty()) state = State.EXITING;
                                        break;
                                    case INPUT:
                                        System.out.println("V: " + clientMsg.data);
                                        playerPhysicalComponents.get(comm).setVelocity((Vector2) clientMsg.data);
                                        System.out.println("Set velocity to: " + playerPhysicalComponents.get(comm).getVelocity());
                                        break;
                                }
                            }
                        });
                        // update gameState and broadcast it
                        playerPhysicalComponents.entrySet().forEach(
                                entry -> {
                                    PlayerState playerState = gameState.stateMap.get(entry.getKey().name);
                                    playerState.position = entry.getValue().getPosition();
                                    playerState.velocity = entry.getValue().getVelocity();
                                }
                        );
                        broadcast(new Message<>(
                                ServerGameCommand.GAME_DATA,
                                gameState
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
                    return;
            }
        }
    }

    private enum State {
        INITIALIZING, SELECTING_LEVEL, LOADING_LEVEL, IN_GAME, LEVEL_COMPLETE, SCORE_SCREEN, EXITING;
    }
}
