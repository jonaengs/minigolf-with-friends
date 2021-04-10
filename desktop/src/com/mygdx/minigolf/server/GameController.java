package com.mygdx.minigolf.server;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.model.components.Physical;

import java.io.IOException;
import java.util.Arrays;
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
            game = new Game();
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
            game.getFactory().setUseGraphics(false);
        }

        comms.forEach(comm -> {
            try {
                comm.running.set(false);
                comm.t.join();
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
                        comm -> game.getFactory().createPlayer(10, 10, false)
                ));
        Map<GameCommunicationHandler, Physical> playerPhysicalComponents = players.keySet().stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> players.get(comm).getComponent(Physical.class)
                ));
        while (true) {
            long t0 = System.currentTimeMillis();

            comms.forEach(comm -> {
                String data;
                synchronized (comm.recvBuffer) {
                    data = comm.recvBuffer[0];
                    comm.recvBuffer[0] = null;
                }
                if (data != null) {
                    System.out.println("DATA: " + data);
                    if (data.contentEquals("EXIT")) {
                        comms.remove(comm);
                    } else {
                        Physical playerPhysics = playerPhysicalComponents.get(comm);
                        // if (playerPhysics.getVelocity().isZero()) {
                        if (true) {
                            String[] split = data.split(", "); // format: "vel_x, vel_y"
                            Vector2 v = new Vector2(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
                            System.out.println("V: " + v.toString());
                            playerPhysics.setVelocity(v);
                        }
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

            // System.out.println(gameData);

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
