package com.mygdx.minigolf.server;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.systems.Physics;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;

import org.lwjgl.Sys;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameController implements Runnable {
    private String gameData = "START GAME";
    private static final int REFRESH_RATE = 1_000 / 30; // in milliseconds
    private final List<GameCommunicationHandler> comms;

    HeadlessGame game;
    boolean showGame = true;

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
        String output;
        Map<GameCommunicationHandler, Entity> players = comms.stream()
                .collect(Collectors.toMap(
                        comm -> comm,
                        comm -> game.getFactory().createPlayer(0, 0, true)
                ));
        Map<Entity, Physical> playerPhysicalComponents = players.values().stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> p.getComponent(Physical.class)
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
                    Physical playerPhysics = playerPhysicalComponents.get(players.get(comm));
                    if (playerPhysics.getVelocity().isZero()) {
                        String[] split = data.split(", "); // format: "vel_x, vel_y"
                        Vector2 v = new Vector2(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
                        playerPhysics.setVelocity(v);
                    }
                }
            });

            gameData = playerPhysicalComponents.values().stream()
                    .map(phys -> Arrays.toString(new Vector2[]{phys.getPosition(), phys.getVelocity()}))
                    .collect(Collectors.joining("|"));

            long delta = System.currentTimeMillis() - t0;
            try {
                Thread.sleep(Math.max(0, REFRESH_RATE - delta));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getGameData() {
        return gameData;
    }
}
