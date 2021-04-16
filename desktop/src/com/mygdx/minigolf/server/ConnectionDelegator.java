package com.mygdx.minigolf.server;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.IntStream;


// TODO: Change strings to enums. Serialize and send. Receive and deserialize ("marshall" in Java?).
public class ConnectionDelegator {
    ServerSocket ss;
    final static int MIN_ID = 100_000;
    final static int MAX_ID = 1_000_000 - MIN_ID;
    static Random random = new Random();
    static final HashMap<Integer, LobbyController> lobbies = new HashMap<>();

    public ConnectionDelegator() throws IOException {
        this(8888);
    }

    public ConnectionDelegator(int port) throws IOException {
        ss = new ServerSocket(port);
    }

    public void accept() throws IOException {
        while (true) {
            Socket s = ss.accept();
            s.setTcpNoDelay(true);
            new Thread(new ConnectionHandler(s)).start();
        }
    }

    private static int generateLobbyID() {
        IntStream intStream = random.ints(Integer.MAX_VALUE, 32, MAX_ID); // middle number is seed?
        return intStream
                .map(i -> i + MIN_ID)
                .filter(id -> !lobbies.containsKey(id))
                .findFirst().getAsInt();
    }

    private static class ConnectionHandler implements Runnable {
        Socket socket;

        public ConnectionHandler(Socket s) {
            this.socket = s;
        }

        // Start lobby thread plus a separate thread to remove it from the lobbies index once it terminates
        private void startLobbyAndSupervisor(LobbyController lobby) {
            new Thread(() -> {
                Thread.currentThread().setName("LobbySupervisor");
                Thread lobbyThread = new Thread(lobby);
                lobbyThread.start();
                try {
                    lobbyThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Problem: This solution will make lobby unjoinable if this thread is ever interrupted
                ConnectionDelegator.lobbies.remove(lobby.lobbyID);
            }).start();
        }

        @Override
        public void run() {
            Thread.currentThread().setName(this.getClass().getName());
            try {
                ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
                CommunicationHandler comm = new CommunicationHandler(socket, objIn, objOut);

                Message<ClientLobbyCommand> msg = (Message<ClientLobbyCommand>) objIn.readObject();
                Integer lobbyID;
                switch (msg.command) {
                    case CREATE:
                        lobbyID = generateLobbyID();
                        LobbyController lobby = new LobbyController(comm, lobbyID);
                        lobbies.put(lobbyID, lobby);
                        startLobbyAndSupervisor(lobby);
                        break;
                    case JOIN:
                        lobbyID = (Integer) msg.data;
                        try {
                            lobbies.get(lobbyID).addPlayer(comm);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            objOut.writeObject(new Message<>(ServerLobbyCommand.LOBBY_NOT_FOUND));
                        }
                        break;
                }
                new Thread(comm).start();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
