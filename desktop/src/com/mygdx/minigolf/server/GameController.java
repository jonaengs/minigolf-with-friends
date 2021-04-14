package com.mygdx.minigolf.server;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.server.messages.GameState;
import com.mygdx.minigolf.server.messages.GameState.PlayerState;
import com.mygdx.minigolf.server.messages.Message;
import com.mygdx.minigolf.server.messages.Message.ServerGameCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GameController implements Runnable {
    private GameState gameData;
    private static final int REFRESH_RATE = 1_000 / 15; // in milliseconds
    private final List<GameCommunicationHandler> comms;
    public final AtomicInteger stateSeq = new AtomicInteger(0);

    HeadlessGame game;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameController(List<CommunicationHandler> comms) {
        game = new HeadlessGame();
        try {
            Utils.initGame(game);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        comms.forEach(comm -> {
            try {
                comm.running.set(false);
                comm.runningThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        this.comms = comms.stream()
                .map(comm -> new GameCommunicationHandler(comm, this))
                .collect(Collectors.toList());
        this.comms.forEach(comm -> {
            try {
                comm.objOut.writeObject(new Message<>(ServerGameCommand.START_GAME));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.comms.forEach(comm -> new Thread(comm).start());
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        System.out.println("GameController up!");
        Map<GameCommunicationHandler, Entity> players = comms.stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> game.getFactory().createPlayer(10, 10)
                ));
        Map<GameCommunicationHandler, Physical> playerPhysicalComponents = players.keySet().stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> players.get(comm).getComponent(Physical.class)
                ));
        long delta;
        while (true) {
            long t0 = System.currentTimeMillis();

            comms.forEach(comm -> {
                Message<Message.ClientGameCommand> msg;
                synchronized (comm.recvBuffer) {
                    msg = comm.recvBuffer.get();
                }
                if (msg != null) {
                    System.out.println("DATA: " + msg);
                    switch (msg.command) {
                        case EXIT:
                            comms.remove(comm);
                            break;
                        case INPUT:
                            System.out.println("V: " + msg.data);
                            playerPhysicalComponents.get(comm).setVelocity((Vector2) msg.data);
                            System.out.println("Set velocity to: " + playerPhysicalComponents.get(comm).getVelocity());
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
    }

    public GameState getGameData() {
        return gameData;
    }
}
