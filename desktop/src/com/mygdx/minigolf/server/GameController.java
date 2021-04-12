package com.mygdx.minigolf.server;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.server.messages.GameState;
import com.mygdx.minigolf.server.messages.Message;
import com.mygdx.minigolf.view.GameView;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GameController implements Runnable {
    private GameState gameData;
    private static final int REFRESH_RATE = 1_000 / 30; // in milliseconds
    private final List<GameCommunicationHandler> comms;

    HeadlessGame game;
    boolean showGame = false;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameController(List<CommunicationHandler> comms) {

        if (showGame) {
            game = new GameView();
            new LwjglApplication(game);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            game = new HeadlessGame();
            new HeadlessApplication(game);
            try {
                Thread.sleep(1000); // wait for create() method to be called so engine/factory gets setup
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                .map(comm -> new GameCommunicationHandler(comm.socket, comm.name, this))
                .collect(Collectors.toList());
        this.comms.forEach(comm -> {
            try {
                comm.socket.getOutputStream().write("START GAME\n".getBytes());
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
                            Physical playerPhysics = playerPhysicalComponents.get(comm);
                            System.out.println("V: " + msg.data);
                            playerPhysics.setVelocity((Vector2) msg.data);
                    }
                }
            });
            gameData = new GameState(
                    comms.stream()
                            .collect(Collectors.toMap(
                                    comm -> comm.name,
                                    comm -> {
                                        Physical phys = playerPhysicalComponents.get(comm);
                                        return new GameState.PlayerState(phys.getPosition(), phys.getVelocity());
                                    }
                            ))
            );

            long delta = System.currentTimeMillis() - t0;
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
