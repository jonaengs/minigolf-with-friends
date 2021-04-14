package com.mygdx.minigolf.server;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.server.messages.GameState;
import com.mygdx.minigolf.server.messages.GameState.PlayerState;
import com.mygdx.minigolf.server.messages.Message;
import com.mygdx.minigolf.server.messages.Message.ClientGameCommand;
import com.mygdx.minigolf.server.messages.Message.ServerGameCommand;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GameController implements Runnable {
    private GameState gameData;
    private static final int REFRESH_RATE = 1_000 / 15; // in milliseconds
    private final List<GameCommunicationHandler> comms;
    public final AtomicInteger stateSeq = new AtomicInteger(0);
    private State state = State.INITIALIZING;
    HeadlessGame game;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameController(List<CommunicationHandler> comms) {
        game = new HeadlessGame();
        Utils.initGame(game);

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

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        Map<GameCommunicationHandler, Entity> players = comms.stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> game.getFactory().createPlayer(-1, -1)
                ));
        Map<GameCommunicationHandler, Physical> playerPhysicalComponents = players.keySet().stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> players.get(comm).getComponent(Physical.class)
                ));
        Iterator<String> levelsIterator = Arrays.asList(CourseLoader.getFileNames()).iterator();
        String currentLevel = null;
        Message<ServerGameCommand> msg;
        while (true) {
            switch (state) {
                case INITIALIZING:
                    break;
                case SELECTING_LEVEL:
                    try {
                        currentLevel = levelsIterator.next();
                        msg = new Message<>(ServerGameCommand.LOAD_LEVEL, currentLevel);
                        for (GameCommunicationHandler comm : comms) {
                            Message<ClientGameCommand> data;
                            do { // TODO: Change to not do spin waiting
                                data = comm.recvBuffer.get();
                            } while (data == null || data.command != ClientGameCommand.LEVEL_LOADED);
                        }
                        state = State.LOADING_LEVEL;
                    } catch (NoSuchElementException e) {
                        // all levels complete. Broadcast final scores and exit
                        state = State.EXITING;
                    }
                    break;
                case LOADING_LEVEL:
                    game.loadLevel(currentLevel);
                    playerPhysicalComponents.values().forEach(
                            p -> p.setPosition(2, 2) // TODO: Set position to be somewhere inside level spawn
                    );
                    msg = new Message<>(ServerGameCommand.START_GAME); // TODO: msg can be overwritten before being sent. Change how this is handled
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
                                clientMsg = comm.recvBuffer.get();
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
                        gameData = new GameState(
                                playerPhysicalComponents.entrySet().stream().collect(Collectors.toMap(
                                        entry -> entry.getKey().name,
                                        entry -> new PlayerState(entry.getValue().getPosition(), entry.getValue().getVelocity())
                                )));
                        stateSeq.incrementAndGet();

                        delta = System.currentTimeMillis() - t0;
                        try {
                            Thread.sleep(Math.max(0, REFRESH_RATE - delta));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case LEVEL_COMPLETE:
                    msg = new Message<>(ServerGameCommand.LEVEL_COMPLETE);
                    msg = new Message<>(ServerGameCommand.GAME_SCORE);
                    state = State.SCORE_SCREEN;
                    // msg = new Message<>(ServerGameCommand.GAME_SCORE, gameScore);
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

    public GameState getGameData() {
        return gameData;
    }

    private enum State {
        INITIALIZING, SELECTING_LEVEL, LOADING_LEVEL, IN_GAME, LEVEL_COMPLETE, SCORE_SCREEN, EXITING;
    }
}
