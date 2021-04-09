package com.mygdx.minigolf.server;

import java.io.IOException;
import java.io.PushbackInputStream;
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

    private static int getId() {
        IntStream intStream = random.ints(Integer.MAX_VALUE, 32, MAX_ID); // middle number is seed?
        return intStream
                .map(i -> i + MIN_ID)
                .filter(id -> !lobbies.containsKey(id))
                .findFirst().getAsInt();
    }

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

    /**
     * API:
     * - Create lobby: CREATE [NAME: name]
     * - Join lobby: JOIN XXXXXX [NAME: name]
     */
    private static class ConnectionHandler implements Runnable {
        Socket socket;

        public ConnectionHandler(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            Thread.currentThread().setName(this.getClass().getName());
            try {
                PushbackInputStream pbin = new PushbackInputStream((socket.getInputStream()));
                String data = Utils.readLine(pbin);
                String name = data.contains("NAME: ") ? data.substring(data.indexOf("NAME: ") + "NAME: ".length()) : "";
                CommunicationHandler comm = new CommunicationHandler(socket, name);
                if (data.startsWith("CREATE")) {
                    int lobbyID = getId();
                    LobbyController lobby = new LobbyController(comm, lobbyID);
                    lobbies.put(lobbyID, lobby);
                    // Start lobby thread plus a separate thread to remove it from the lobbies index once it terminates
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
                        ConnectionDelegator.lobbies.remove(lobbyID);
                    }).start();
                } else if (data.startsWith("JOIN")) { // FORMAT: "JOIN XXXXXX"
                    int lobbyID = Integer.parseInt(data.split(" ")[1]);
                    lobbies.get(lobbyID).addPlayer(comm);
                }
                new Thread(comm).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
