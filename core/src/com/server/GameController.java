package com.server;

import com.mygdx.minigolf.HeadlessGame;

import java.util.List;
import java.util.stream.Collectors;

public class GameController implements Runnable {
    HeadlessGame game;
    List<GameCommunicationHandler> comms;

    // Receive LobbyComms. Shut them down and transfer sockets to GameComms
    GameController(List<CommunicationHandler> comms) {
        this.game = new HeadlessGame();
        comms.forEach(comm -> {
            try {
                comm.t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            comm.running.set(false);
        });
        this.comms = comms.stream()
                .map(comm -> new GameCommunicationHandler(comm.socket, comm.name))
                .collect(Collectors.toList());
        this.comms.forEach(gc -> new Thread(gc).start());
    }

    @Override
    public void run() {
        System.out.println("GameController up!");
    }
}
