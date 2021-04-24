package com.mygdx.minigolf.server.controllers;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;
import com.mygdx.minigolf.server.ConnectionDelegator;
import com.mygdx.minigolf.server.communicators.LobbyCommunicationHandler;
import com.mygdx.minigolf.util.Constants;

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

    public LobbyController(LobbyCommunicationHandler leader, Integer lobbyID) throws IOException {
        this.lobbyID = lobbyID;
        this.leader = leader;
        addPlayer(leader);
    }

    public synchronized void addPlayer(LobbyCommunicationHandler comm) throws IOException, IllegalArgumentException {
        if (comms.size() >= Constants.MAX_NUM_PLAYERS)
            throw new IllegalArgumentException("Lobby is full");
        synchronized (comms) {
            comms.add(comm);
        }
        comm.playerName = names[nameIndex.getAndIncrement() % names.length];
        comm.send(new Message<>(ServerLobbyCommand.LOBBY_ID, lobbyID));
        comm.send(new Message<>(ServerLobbyCommand.NAME, comm.playerName));
        playerListUpdated.set(true);
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

    private void startGame() throws InterruptedException {
        synchronized (comms) {
            GameManager gameManager;
            barrier(
                    new Message<>(ServerLobbyCommand.ENTER_GAME),
                    new Message<>(ClientLobbyCommand.GAME_READY)
            );
            gameManager = new GameManager(comms);
            new Thread(gameManager).start();
        }
    }

    @Override
    public void run() {
        List<LobbyCommunicationHandler> playersToRemove = new ArrayList<>();
        boolean startRequested = false;
        try {
            while (!startRequested) {
                if (playerListUpdated.getAndSet(false)) {
                    if (!leader.running.get() || !comms.contains(leader)) {
                        broadcast(new Message<>(ServerLobbyCommand.EXIT));
                        break;
                    }
                    broadcastPlayerList();
                }
                synchronized (comms) {
                    for (LobbyCommunicationHandler comm : comms) {
                        Message<ClientLobbyCommand> clientMsg = comm.read();
                        if (clientMsg != null) {
                            if (clientMsg.command == ClientLobbyCommand.EXIT) {
                                playersToRemove.add(comm);
                            } else if (clientMsg.command == ClientLobbyCommand.START_GAME) {
                                startRequested = comm == leader;
                            }
                        }
                    }
                }
                if (!playersToRemove.isEmpty()) {
                    removePlayers(playersToRemove);
                }
                Thread.sleep(100);
            }
            if (startRequested) startGame();
        } catch (InterruptedException e) {
            broadcast(new Message<>(ServerLobbyCommand.EXIT));
            e.printStackTrace();
        } finally {
            ConnectionDelegator.lobbies.remove(this.lobbyID);
        }
    }
}
