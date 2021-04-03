package com.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
                synchronized (ch.sendBuffer) {
                    ch.sendBuffer.add(msg);
                }
            }
        }
    }

    private void receiveFromAll(String msg) {
        synchronized (comms) {
            for (CommunicationHandler comm: comms) {
                synchronized (comm.recvBuffer) {
                    String data;
                    do {
                        data = comm.recvBuffer.poll();
                    } while (data == null || !data.contentEquals(msg));
                }
            }
        }
    }

    private void startGame() {
        synchronized (comms) {
            broadcastMsg("ENTER GAME");
            receiveFromAll("GAME READY");
            new Thread(new GameController(comms)).start();
        }
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
                broadcastPlayerList();
                update.set(false);
            }
            for (CommunicationHandler comm : copyComms()) {
                String msg;
                synchronized (comm.recvBuffer) {
                    msg = comm.recvBuffer.poll();
                }
                if (msg != null) {
                    System.out.println(tn + " Read msg: " + msg);
                    if (msg.contentEquals("EXIT")) {
                        System.out.println(tn + " Removing player: " + comm.socket.toString());
                        comms.remove(comm);
                        update.set(true);
                    } else if (msg.contentEquals("ENTER GAME") && comm == leader) {
                        startGame();
                        return;
                    }
                }
            }
        }
    }
}
