package com.mygdx.minigolf.server;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;
import com.mygdx.minigolf.server.communicators.LobbyCommunicationHandler;
import com.mygdx.minigolf.server.controllers.LobbyController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

// TODO: Make all static, singleton, or just a normal class
public class ConnectionDelegator {
    final static int MIN_ID = 100_000;
    final static int MAX_ID = 1_000_000 - MIN_ID;
    public static final ConcurrentHashMap<Integer, LobbyController> lobbies = new ConcurrentHashMap<>();
    static Random random = new Random();
    ServerSocket ss;

    public ConnectionDelegator() throws IOException {
        this(8888);
    }

    public ConnectionDelegator(int port) throws IOException {
        ss = new ServerSocket(port);
    }

    private static int generateLobbyID() {
        IntStream intStream = random.ints(Integer.MAX_VALUE, 32, MAX_ID); // middle number is seed?
        return intStream
                .map(i -> i + MIN_ID)
                .filter(id -> !lobbies.containsKey(id))
                .findFirst().getAsInt();
    }

    // TODO: Use a threadpool of connectionhandlers
    public void accept() throws IOException {
        while (true) {
            Socket s = ss.accept();
            s.setTcpNoDelay(true);
            new Thread(new ConnectionHandler(s)).start();
        }
    }

    private static class ConnectionHandler implements Runnable {
        Socket socket;

        public ConnectionHandler(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            Thread.currentThread().setName(this.getClass().getName());
            try {
                ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
                LobbyCommunicationHandler comm = new LobbyCommunicationHandler(socket, objIn, objOut);

                Message<ClientLobbyCommand> msg;
                Integer lobbyID;
                findLobby: do {
                    msg = (Message<ClientLobbyCommand>) objIn.readObject();
                    switch (msg.command) {
                        case CREATE:
                            lobbyID = generateLobbyID();
                            com.mygdx.minigolf.server.controllers.LobbyController lobby = new LobbyController(comm, lobbyID);
                            lobbies.put(lobbyID, lobby);
                            new Thread(lobby).start();
                            break findLobby;
                        case JOIN:
                            lobbyID = (Integer) msg.data;
                            try {
                                lobbies.get(lobbyID).addPlayer(comm);
                                break findLobby;
                            } catch (NullPointerException e) {
                                objOut.writeObject(new Message<>(ServerLobbyCommand.LOBBY_NOT_FOUND, lobbyID));
                            } catch (IllegalArgumentException e) {
                                objOut.writeObject(new Message<>(ServerLobbyCommand.LOBBY_FULL, lobbyID));
                            }
                    }
                } while(true);
                new Thread(comm).start();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
