package com.mygdx.minigolf.server.controllers;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;
import com.mygdx.minigolf.server.communicators.LobbyCommunicationHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// TODO: Enforce max player limit
public class LobbyController extends BaseController<LobbyCommunicationHandler, ServerLobbyCommand, ClientLobbyCommand> {
    private static final String[] names = {"Yatzy", "Chess", "Bridge", "Poker", "Jacket", "Shirt", "Pants", "Boots"};
    public final Integer lobbyID;
    private final AtomicInteger nameIndex = new AtomicInteger(new Random().nextInt(names.length));
    private final LobbyCommunicationHandler leader;
    private final AtomicBoolean playerListUpdated = new AtomicBoolean(true);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public LobbyController(LobbyCommunicationHandler leader, Integer lobbyID) throws IOException {
        this.lobbyID = lobbyID;
        this.leader = leader;
        addPlayer(leader);
    }

    public synchronized void addPlayer(LobbyCommunicationHandler comm) throws IOException {
        synchronized (comms) {
            comms.add(comm);
        }
        comm.playerName = names[nameIndex.getAndIncrement() % names.length];
        comm.send(new Message<>(ServerLobbyCommand.LOBBY_ID, lobbyID));
        comm.send(new Message<>(ServerLobbyCommand.NAME, comm.playerName));
        playerListUpdated.set(true);
    }

    public void shutDown() {
        running.set(false);
    }

    ///////////////////////////
    // THREAD FUNCTIONS BELOW
    ///////////////////////////

    private void broadcastPlayerList() {
        List<String> playerNames;
        synchronized (comms) {
            playerNames = comms.stream().map(comm -> comm.playerName).collect(Collectors.toList());
        }
        broadcast(new Message<>(ServerLobbyCommand.PLAYER_LIST, playerNames));
    }

    private void removePlayers(List<LobbyCommunicationHandler> playerList) {
        synchronized (comms) {
            comms.removeAll(playerList);
        }
        playerListUpdated.set(true);
        playerList.clear();
    }

    @Override
    public void run() {
        List<LobbyCommunicationHandler> playersToRemove = new ArrayList<>();
        while (running.get()) {
            if (playerListUpdated.getAndSet(false)) {
                if (comms.isEmpty() || !leader.running.get() || !comms.contains(leader)) {
                    broadcast(new Message<>(ServerLobbyCommand.EXIT));
                    return; // TODO: Should something more be done here?
                }
                broadcastPlayerList();
            }
            synchronized (comms) {
                for (LobbyCommunicationHandler comm : comms) {
                    Message<ClientLobbyCommand> clientMsg = comm.read();
                    if (clientMsg != null) {
                        switch (clientMsg.command) {
                            case EXIT:
                                playersToRemove.add(comm);
                                break;
                            case START_GAME:
                                if (comm == leader) running.set(false);
                                break;
                        }
                    }
                }
            }
            if (!playersToRemove.isEmpty()) {
                removePlayers(playersToRemove);
            }
        }
        synchronized (comms) {
            GameManager gameManager;
            barrier(
                    new Message<>(ServerLobbyCommand.ENTER_GAME),
                    new Message<>(ClientLobbyCommand.GAME_READY)
            );
            try {
                gameManager = new GameManager(comms);
                new Thread(gameManager).start();
            } catch (InterruptedException e) {
                broadcast(new Message<>(ServerLobbyCommand.EXIT));
                e.printStackTrace();
            }
        }
    }
}
