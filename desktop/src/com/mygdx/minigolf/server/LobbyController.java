package com.mygdx.minigolf.server;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// TODO: Enforce max player limit
class LobbyController extends ClientsController<LobbyCommunicationHandler> {
    private static final String[] names = {"Leader", "Yatzy", "Chess", "Bridge", "Poker", "Jacket", "Shirt", "Pants"};
    public final Integer lobbyID;
    private final AtomicInteger nameIndex = new AtomicInteger(0);
    private final LobbyCommunicationHandler leader;
    private final AtomicBoolean playerListUpdated = new AtomicBoolean(true);
    private final AtomicBoolean running = new AtomicBoolean(true);

    public LobbyController(LobbyCommunicationHandler leader, Integer lobbyID) throws IOException {
        this.lobbyID = lobbyID;
        this.leader = leader;
        addPlayer(leader);
    }

    public void addPlayer(LobbyCommunicationHandler comm) throws IOException {
        synchronized (comms) {
            comms.add(comm);
        }
        comm.playerName = names[nameIndex.getAndIncrement()];
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

    private List<LobbyCommunicationHandler> copyComms() {
        List<LobbyCommunicationHandler> copy;
        synchronized (comms) {
            copy = new ArrayList<>(comms);
        }
        return copy;
    }

    private void removePlayer(LobbyCommunicationHandler comm) {
        synchronized (comms) {
            comms.remove(comm);
        }
        playerListUpdated.set(true);
    }

    @Override
    public void run() {
        while (running.get()) {
            if (playerListUpdated.getAndSet(false)) {
                if (comms.isEmpty() || !leader.running.get() || !comms.contains(leader))
                    return; // TODO: Should something more be done here?
                broadcastPlayerList();
            }
            for (LobbyCommunicationHandler comm : copyComms()) {
                Message<ClientLobbyCommand> clientMsg = comm.recvBuffer.poll();
                if (clientMsg != null) {
                    switch (clientMsg.command) {
                        case EXIT:
                            removePlayer(comm);
                            break;
                        case START_GAME:
                            running.set(comm == leader);
                    }
                }
            }
        }
        synchronized (comms) {
            GameController gameController;
            barrier(
                    new Message<>(ServerLobbyCommand.ENTER_GAME),
                    new Message<>(ClientLobbyCommand.GAME_READY)
            );
            try {
                gameController = new GameController(comms);
                new Thread(gameController).start();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
