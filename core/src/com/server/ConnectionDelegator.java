package com.server;

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
        System.out.println("Accepting connections...");
        while (true) {
            Socket s = ss.accept();
            System.out.println("Accepted socket:" + s.toString());
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
            String tn = Thread.currentThread().getName();
            System.out.println(tn + " Starting connection handler");
            try {
                PushbackInputStream in = new PushbackInputStream((socket.getInputStream()));
                String data = Utils.readStream(in);
                System.out.println(tn + " Received data: " + data);
                String name = data.contains("NAME: ") ? data.substring(data.indexOf("NAME: ")) : "";
                CommunicationHandler comm = new CommunicationHandler(socket, name);
                if (data.startsWith("CREATE")) {
                    int lobbyID = getId();
                    LobbyController lobby = new LobbyController(comm, lobbyID);
                    lobbies.put(lobbyID, lobby);
                    new Thread(lobby, "Lobby-" + lobbyID).start();
                    System.out.println(tn + " Created lobby " + lobbyID);
                } else if (data.startsWith("JOIN")) { // FORMAT: "JOIN XXXXXX"
                    int lobbyID = Integer.parseInt(data.split(" ")[1]);
                    lobbies.get(lobbyID).addPlayer(comm);
                    System.out.println(tn + " Joined lobby " + lobbyID);
                }
                new Thread(comm).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
