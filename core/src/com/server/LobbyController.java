package com.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toList;

class LobbyController implements Runnable {
    final private List<CommunicationHandler> comms;
    private final CommunicationHandler leader;
    final int lobbyID;
    AtomicBoolean update = new AtomicBoolean(true);

    public LobbyController(CommunicationHandler comm, Integer id) {
        this.leader = comm;
        lobbyID = id;
        comms = new ArrayList<>();
        comms.add(leader);
        send(leader, id.toString());
    }

    private void send(CommunicationHandler comm, String msg) {
        synchronized (comm.sendBuffer) {
            comm.sendBuffer.add(msg);
        }
    }

    public void addPlayer(CommunicationHandler comm) {
        synchronized (comms) {
            comms.add(comm);
        }
        update.set(true);
    }

    private void broadCastState() {
        String playerSocks;
        synchronized (comms) {
            playerSocks = comms.stream().map(comm -> comm.name).collect(toList()).toString();
        }
        broadcastMsg(playerSocks);
    }

    private List<CommunicationHandler> copyComms() {
        List<CommunicationHandler> copy;
        synchronized (comms) {
            copy = new ArrayList<>(comms);
        }
        return copy;
    }

    private void broadcastMsg(String msg) {
        for (CommunicationHandler ch : copyComms()) {
            synchronized (ch.sendBuffer) {
                ch.sendBuffer.add(msg);
            }
        }
    }

    private void startGame() {
        broadcastMsg("GAME STARTING");
    }

    private void closeConnections() {
        synchronized (comms) {
            for (CommunicationHandler comm: comms) {
                comm.close();
            }
        }
    }

    @Override
    public void run() {
        String tn = Thread.currentThread().getName();
        while (true) {
            if (update.get()) {
                if (leader.socket.isClosed()) {
                    System.out.println("Shutting down lobby " + tn);
                    closeConnections();
                    return;
                }
                broadCastState();
                update.set(false);
            }
            for (CommunicationHandler comm : copyComms()) {
                String msg;
                do {
                    synchronized (comm.recvBuffer) {
                        msg = comm.recvBuffer.poll();
                    }
                    if (msg != null) {
                        System.out.println(tn + " Read msg: " + msg);
                        if (msg.contentEquals("EXIT")) {
                            System.out.println(tn + " Removing player: " + comm.socket.toString());
                            comms.remove(comm);
                            update.set(true);
                        } else if (msg.contentEquals("START GAME") && comm == leader) {
                            startGame();
                            return;
                        }
                    }
                } while (msg != null);
            }
        }
    }
}
