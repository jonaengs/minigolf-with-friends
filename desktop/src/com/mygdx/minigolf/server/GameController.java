package com.mygdx.minigolf.server;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.HeadlessGame;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameController implements Runnable {
    private static final int REFRESH_RATE = 1_000 / 30; // in milliseconds
    private final List<GameCommunicationHandler> comms;

    HeadlessGame game;
    HeadlessApplication app;
    boolean showGame = true;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameController(List<CommunicationHandler> comms) {

        if (showGame) {
            new LwjglApplication(new Game());
        } else {
            game = new HeadlessGame();
            app = new HeadlessApplication(game);
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
                .map(comm -> new GameCommunicationHandler(comm.socket, comm.name))
                .collect(Collectors.toList());
        this.comms.forEach(gameComm -> new Thread(gameComm).start());
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        System.out.println("GameController up!");
        List<String> inputs;
        String output;
        do {
            long t0 = System.currentTimeMillis();

            inputs = comms.stream().map(comm -> {
                synchronized (comm.recvBuffer) {
                    return comm.recvBuffer[0];
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            long delta = System.currentTimeMillis() - t0;
            try {
                Thread.sleep(Math.max(0, REFRESH_RATE - delta));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
    }
}
