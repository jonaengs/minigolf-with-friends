package com.mygdx.minigolf.server;

import com.mygdx.minigolf.server.messages.Message;
import com.mygdx.minigolf.server.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.server.messages.Message.ServerLobbyCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.mygdx.minigolf.server.messages.Message.ClientLobbyCommand.GAME_READY;

class LobbyController implements Runnable {
    private static final String[] names = {"Leader", "Hannah", "Yatzy", "Katie", "Katie", "Cuthbert", "Phineas", "Sirius", "Amelia", "Susan", "Terry", "Lavender", "Millicent", "Charity", "Frank", "Alecto", "Amycus", "Reginald", "Mary", "Cho", "Penelope", "Michael", "Vincent", "Vincent", "Colin", "Dennis", "Dirk", "Bartemius", "Bartemius", "Roger", "Dawlish", "Fleur", "Gabrielle", "Dedalus", "Amos", "Cedric", "Elphias", "Antonin", "Aberforth", "Albus", "Dudley", "Marjorie", "Petunia", "Vernon", "Marietta", "Arabella", "Argus", "Justin", "Seamus", "Marcus", "Mundungus", "Filius", "Florean", "Cornelius", "Marvolo", "Merope", "Morfin", "Anthony", "Goyle", "Gregory", "Hermione", "Astoria", "Gregorovitch", "Fenrir", "Gellert", "Wilhelmina", "Godric", "Rubeus", "Madam", "Mafalda", "Helga", "Lee", "Bertha", "Igor", "Viktor", "Bellatrix", "Rabastan", "Rodolphus", "Gilderoy", "Alice", "Augusta", "Frank", "Neville", "Luna", "Xenophilius", "Remus", "Edward", "Walden", "Draco", "Lucius", "Narcissa", "Scorpius", "Madam", "Griselda", "Madam", "Olympe", "Ernie", "Minerva", "Cormac", "Graham", "Alastor", "Auntie", "Theodore", "Bob", "Garrick", "Pansy", "Padma", "Parvati", "Peter", "Antioch", "Cadmus", "Ignotus", "Irma", "Sturgis", "Poppy", "Harry", "James", "Lily", "Quirinus", "Helena", "Rowena", "Tom", "Demelza", "Augustus", "Albert", "Newt", "Rufus", "Kingsley", "Stanley", "Aurora", "Rita", "Horace", "Salazar", "Hepzibah", "Zacharias", "Severus", "Alicia", "Pomona", "Pius", "Dean", "Andromeda", "Nymphadora", "Ted", "Travers", "Sybill", "Wilky", "Dolores", "Emmeline", "Romilda", "Septima", "Lord", "Angelina", "Myrtle", "Arthur", "Bill", "Charlie", "Fred", "George", "Ginny", "Hugo", "Molly", "Percy", "Ron", "Rose", "Oliver", "Yaxley", "Blaise"};
    private final AtomicInteger nameIndex = new AtomicInteger(0);

    private final List<CommunicationHandler> comms;
    private final CommunicationHandler leader;
    private final AtomicBoolean playerListUpdated = new AtomicBoolean(true);
    public final Integer lobbyID;
    private State state = State.INITIALIZING;

    public LobbyController(CommunicationHandler leader, Integer lobbyID) {
        this.lobbyID = lobbyID;
        this.leader = leader;
        comms = new ArrayList<>();
        addPlayer(leader);
    }

    public void addPlayer(CommunicationHandler comm) {
        synchronized (comms) {
            comms.add(comm);
        }
        comm.name = names[nameIndex.getAndIncrement()];
        comm.sendBuffer.add(new Message<>(ServerLobbyCommand.LOBBY_ID, lobbyID));
        comm.sendBuffer.add(new Message<>(ServerLobbyCommand.NAME, comm.name));
        playerListUpdated.set(true);
    }

    ///////////////////////////
    // THREAD FUNCTIONS BELOW
    ///////////////////////////

    private void broadcastPlayerList() {
        List<String> playerNames;
        synchronized (comms) {
            playerNames = comms.stream().map(comm -> comm.name).collect(Collectors.toList());
        }
        broadcastMsg(new Message<>(ServerLobbyCommand.PLAYER_LIST, playerNames));
    }

    private List<CommunicationHandler> copyComms() {
        List<CommunicationHandler> copy;
        synchronized (comms) {
            copy = new ArrayList<>(comms);
        }
        return copy;
    }

    private void broadcastMsg(Message<ServerLobbyCommand> msg) {
        synchronized (comms) {
            for (CommunicationHandler ch : comms) {
                ch.sendBuffer.add(msg);
            }
        }
    }

    @Override
    public void run() {
        state = State.NORMAL;
        while (true) {
            switch (state) {
                case NORMAL:
                    // If the player list (comms) has been updated, broadcast it.
                    if (playerListUpdated.getAndSet(false)) {
                        broadcastPlayerList();
                        if (!leader.running.get() || !comms.contains(leader)) { // If leader no longer running, shut down lobby.
                            state = State.CLOSING;
                        }
                    }
                    for (CommunicationHandler comm : copyComms()) {
                        Message<ClientLobbyCommand> clientMsg = comm.recvBuffer.poll();
                        if (clientMsg != null) {
                            System.out.println("LC recv: " + clientMsg);
                            switch (clientMsg.command) {
                                case EXIT:
                                    synchronized (comms) {
                                        comms.remove(comm);
                                    }
                                    playerListUpdated.set(true);
                                    break;
                                case START_GAME:
                                    if (comm == leader) state = State.STARTING_GAME;
                                    break;
                            }
                        }
                    }
                    break;
                case CLOSING:
                    System.out.println("CLOSING");
                    synchronized (comms) {
                        for (CommunicationHandler comm : comms) {
                            comm.close();
                        }
                    }
                    return;
                case STARTING_GAME:
                    System.out.println("STARTING GAME");
                    GameController gameController;
                    synchronized (comms) {
                        broadcastMsg(new Message<>(ServerLobbyCommand.ENTER_GAME));
                        // Receive GAME_READY from all players
                        for (CommunicationHandler comm : comms) {
                            Message<ClientLobbyCommand> data;
                            // TODO: Change to not do spin waiting
                            do {
                                data = comm.recvBuffer.poll();
                            } while (data != null && data.command != GAME_READY);
                        }
                        System.out.println("RECEIVED ALL READIES");
                        gameController = new GameController(comms);
                    }
                    new Thread(gameController).start();
                    return;
            }
        }
    }

    private enum State {
        INITIALIZING, NORMAL, CLOSING, STARTING_GAME
    }
}
