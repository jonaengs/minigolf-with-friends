package com.mygdx.minigolf.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

class LobbyController implements Runnable {
    private final List<CommunicationHandler> comms;
    private final CommunicationHandler leader;
    private final AtomicBoolean playerListUpdated = new AtomicBoolean(true);
    private final Integer lobbyID;

    public LobbyController(CommunicationHandler comm, Integer lobbyID) {
        this.lobbyID = lobbyID;
        this.leader = comm;
        comms = new ArrayList<>();
        comms.add(leader);
        leader.sendBuffer.add(lobbyID.toString());
    }

    public void addPlayer(CommunicationHandler comm) {
        synchronized (comms) {
            comms.add(comm);
        }
        playerListUpdated.set(true);
    }

    ///////////////////////////
    // THREAD FUNCTIONS BELOW
    ///////////////////////////

    private void broadcastPlayerList() {
        String playerNames;
        synchronized (comms) {
            playerNames = comms.stream().map(comm -> comm.name).collect(Collectors.joining(", "));
        }
        broadcastMsg(playerNames);
    }

    private List<CommunicationHandler> copyComms() {
        List<CommunicationHandler> copy;
        synchronized (comms) {
            copy = new ArrayList<>(comms);
        }
        return copy;
    }

    private void broadcastMsg(String msg) {
        synchronized (comms) {
            for (CommunicationHandler ch : comms) {
                ch.sendBuffer.add(msg);
            }
        }
    }

    private void receiveFromAll(String msg) {
        synchronized (comms) {
            for (CommunicationHandler comm : comms) {
                String data;
                // TODO: Change to not do spin waiting
                do {
                    data = comm.recvBuffer.poll();
                } while (data == null || !data.contentEquals(msg));
            }
        }
    }

    private void startGame() {
        GameController gameController;
        synchronized (comms) {
            broadcastMsg("ENTER GAME");
            receiveFromAll("GAME READY");
            gameController = new GameController(comms);
        }
        new Thread(gameController).start();
    }

    private void closeConnections() {
        synchronized (comms) {
            for (CommunicationHandler comm : comms) {
                comm.close();
            }
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        boolean hasPlayers = true;
        while (hasPlayers) {
            // If the player list (comms) has been updated, broadcast it.
            if (playerListUpdated.getAndSet(false)) { // set to false immediately to capture any later update
                broadcastPlayerList();
                if (!leader.running.get()) { // If leader no longer running, shut down lobby.
                    closeConnections();
                    return;
                }
            }

            // Receive and react to messages
            for (CommunicationHandler comm : copyComms()) {
                String msg;
                msg = comm.recvBuffer.poll();
                if (msg != null) {
                    if (msg.contentEquals("EXIT")) {
                        comms.remove(comm);
                        playerListUpdated.set(true);
                    } else if (msg.contentEquals("ENTER GAME") && comm == leader) {
                        startGame();
                        return;
                    }
                }
            }
            // unnecessary?
            synchronized (comms) {
                hasPlayers = !comms.isEmpty();
            }
        }
    }
}
