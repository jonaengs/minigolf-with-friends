package com.mygdx.minigolf.server.controllers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.GameController;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientGameCommand;
import com.mygdx.minigolf.network.messages.Message.ServerGameCommand;
import com.mygdx.minigolf.network.messages.NetworkedGameState;
import com.mygdx.minigolf.network.messages.NetworkedGameState.PlayerState;
import com.mygdx.minigolf.server.ServerUtils;
import com.mygdx.minigolf.server.communicators.GameCommunicationHandler;
import com.mygdx.minigolf.server.communicators.LobbyCommunicationHandler;
import com.mygdx.minigolf.util.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.util.ComponentMappers.PlayerMapper;
import com.mygdx.minigolf.util.ConcurrencyUtils;
import com.mygdx.minigolf.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class GameManager extends BaseController<GameCommunicationHandler, ServerGameCommand, ClientGameCommand> {
    HeadlessGame game;
    Application app;

    GameController gameController;
    Map<String, Entity> players;
    Map<String, Physical> playerPhysicals;
    NetworkedGameState networkedGameState;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameManager(List<LobbyCommunicationHandler> lobbyComms) throws InterruptedException {
        game = new HeadlessGame();
        gameController = new GameController(game);
        app = ServerUtils.initGame(game);

        // Stop lobby communication handlers
        for (LobbyCommunicationHandler comm : lobbyComms) {
            comm.running.set(false);
            comm.runningThread.join();
        }

        // Setup game communication handlers and start them
        lobbyComms.forEach(comm -> comms.add(new GameCommunicationHandler(comm)));
        comms.forEach(comm -> new Thread(comm).start());

        // Setup game data
        ConcurrencyUtils.skipWaitPostRunnable(() -> {
            List<String> playerNames = lobbyComms.stream().map(c -> c.playerName).collect(Collectors.toList());
            players = gameController.createPlayers(playerNames);
            playerPhysicals = players.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> PhysicalMapper.get(entry.getValue())
            ));
        });
        networkedGameState = new NetworkedGameState(comms.stream().collect(Collectors.toMap(
                comm -> comm.playerName,
                ____ -> new NetworkedGameState.PlayerState(new Vector2(), new Vector2())
        )));
    }

    private Map<String, Integer> getScores() {
        return players.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> PlayerMapper.get(players.get(entry.getKey())).getLevelStrokes()
        ));
    }

    // Clears all comm recvBuffers of input data
    private void clearComms() {
        // TODO (Is this method needed?)
    }

    private void updateGameState() {
        ConcurrencyUtils.skipWaitPostRunnable(() ->
                playerPhysicals.entrySet().forEach(
                        entry -> {
                            PlayerState playerState = networkedGameState.stateMap.get(entry.getKey());
                            playerState.position = entry.getValue().getPosition();
                            playerState.velocity = entry.getValue().getVelocity();
                        }
                )
        );
    }

    private void broadcastGameState() {
        broadcast(new Message<>(ServerGameCommand.GAME_DATA, networkedGameState));
    }

    private void shutDown() {
        comms.forEach(c -> c.running.set(false));
        game.dispose();
        ConcurrencyUtils.postRunnable(() -> {throw new RuntimeException();});
        // ConcurrencyUtils.postRunnable(() -> Thread.currentThread().interrupt());
        // app.exit(); // This appears to exit the entire program, not only the libGdx app
    }

    private void removePlayers(List<String> playersToRemove) {
        playersToRemove.forEach(name -> {
            players.remove(name);
            playerPhysicals.remove(name);
            comms.removeIf(comm -> comm.playerName.contentEquals(name));
        });
        playersToRemove.clear();
    }

    private boolean nextLevel(Iterator<String> levelsIterator) {
        try {
            String level = levelsIterator.next();
            barrier(new Message<>(ServerGameCommand.LOAD_LEVEL, level),
                    new Message<>(ClientGameCommand.LEVEL_LOADED, level));
            gameController.loadLevel(level);
            ConcurrencyUtils.skipWaitPostRunnable(() -> gameController.resetPlayers(players.values()));
        } catch (NoSuchElementException e) {
            return false;
        }
        return true;
    }

    private void gameLoop() {
        List<String> exitingPlayers = new ArrayList<>();
        long delta, t0;

        while (!players.values().stream().allMatch(p -> PlayerMapper.get(p).isCompleted())) {
            t0 = System.currentTimeMillis();

            comms.forEach(comm -> {
                Message<ClientGameCommand> msg = comm.read();
                if (msg != null) {
                    System.out.println("DATA: " + msg);
                    switch (msg.command) {
                        case EXIT: // TODO: Handling exit doesn't have the same priority as input. Maybe have two separate buffers or smth
                            exitingPlayers.add(comm.playerName);
                            break;
                        case INPUT:
                            playerPhysicals.get(comm.playerName).setVelocity((Vector2) msg.data);
                            PlayerMapper.get(players.get(comm.playerName)).incrementStrokes();
                            break;
                    }
                }
            });
            removePlayers(exitingPlayers); // TODO: Notify players explicitly in some way?
            updateGameState();
            broadcastGameState();

            // TODO: Where is engine.update()??
            delta = System.currentTimeMillis() - t0;
            ServerUtils.sleep(Math.max(0, Constants.SERVER_TICK_RATE_MS - delta));
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        Iterator<String> levelsIterator = Arrays.asList(CourseLoader.getFileNames()).iterator();

        while (nextLevel(levelsIterator)) {
            broadcast(new Message<>(ServerGameCommand.START_GAME));
            gameLoop();
            broadcast(new Message<>(ServerGameCommand.LEVEL_COMPLETE));
            broadcast(new Message<>(ServerGameCommand.GAME_SCORE, getScores()));
            ServerUtils.sleep(5000);
        }
        broadcast(new Message<>(ServerGameCommand.GAME_COMPLETE));
        shutDown();
    }

}
