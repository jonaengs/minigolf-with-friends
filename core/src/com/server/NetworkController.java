package com.server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;


// TODO: Change strings to enums. Serialize and send. Receive and deserialize ("marshall" in Java?).
public class NetworkController {
    ServerSocket ss;
    final static int MIN_ID = 100_000;
    final static int MAX_ID = 1_000_000 - MIN_ID;
    static Random random = new Random();

    private static final HashMap<Integer, LobbyController> lobbies = new HashMap<>();

    private static int getId() {
        IntStream intStream = random.ints(Integer.MAX_VALUE, 32, MAX_ID); // middle number is seed?
        return intStream
                .map(i -> i + MIN_ID)
                .filter(id -> !lobbies.containsKey(id))
                .findFirst().getAsInt();
    }

    public NetworkController() throws IOException {
        this(8888);
    }

    public NetworkController(int port) throws IOException {
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
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader((socket.getInputStream()))
                );
                String data = reader.readLine();
                System.out.println(tn + " Received data: " + data);
                CommunicationHandler comm = new CommunicationHandler(socket);
                int lobbyID = 0;
                if (data.startsWith("CREATE")) {
                    lobbyID = getId();
                    LobbyController lobby = new LobbyController(socket, comm, lobbyID);
                    lobbies.put(lobbyID, lobby);
                    new Thread(lobby, "LC" + lobbyID).start();
                    System.out.println(tn + " Created lobby " + lobbyID);
                } else if (data.startsWith("JOIN")) { // FORMAT: "JOIN XXXXXX"
                    lobbyID = Integer.parseInt(data.split(" ")[1]);
                    lobbies.get(lobbyID).addPlayer(socket, comm);
                    System.out.println(tn + " Joined lobby " + lobbyID);
                }
                new Thread(comm, "CH" + lobbyID).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class CommunicationHandler implements Runnable {
        public final PriorityQueue<String> sendBuffer = new PriorityQueue<>();
        public final PriorityQueue<String> recvBuffer = new PriorityQueue<>();
        private final Socket socket;

        public CommunicationHandler(Socket s) {
            socket = s;
        }

        @Override
        public void run() {
            String tn = Thread.currentThread().getName();
            String send, recv;
            try {
                BufferedWriter sendBuf = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader recvBuf;
                while (socket.isConnected()) {
                    synchronized (sendBuffer) {
                        send = sendBuffer.poll();
                    }
                    if (send != null) {
                        System.out.println(tn + " Sending msg: " + send);
                        sendBuf.write(send + "\n");
                        sendBuf.flush();
                    }
                    recvBuf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    if (recvBuf.ready()) {
                        recv = recvBuf.readLine();
                        System.out.println(tn + " Received msg: " + recv);
                        synchronized (recvBuffer) {
                            recvBuffer.add(recv);
                        }
                    }
                }
            } catch (IOException e) {
                synchronized (recvBuffer) {
                    System.out.println(tn + " Exiting");
                    recvBuffer.add("EXIT");
                }
                e.printStackTrace();
            }
        }
    }

    public static class LobbyController implements Runnable {
        final private Map<Socket, CommunicationHandler> comms;
        private final Socket leader;
        final int lobbyID;
        AtomicBoolean update;

        public LobbyController(Socket leader, CommunicationHandler comm, Integer id) {
            this.leader = leader;
            lobbyID = id;
            comms = new HashMap<>();
            comms.put(leader, comm);
            synchronized (comm.sendBuffer) {
                comm.sendBuffer.add(id.toString());
            }
            update.set(true);
        }

        public void addPlayer(Socket player, CommunicationHandler comm) {
            synchronized (comms) {
                comms.put(player, comm);
                update.set(true);
            }
        }

        private void broadCastState() {
            String players;
            synchronized (comms) {
                players = comms.keySet().toString();
            }
            for (CommunicationHandler ch: comms.values()) {
                synchronized (ch.sendBuffer) {
                    ch.sendBuffer.add(players);
                }
            }
        }

        private void startGame() {

        }

        @Override
        public void run() {
            String tn = Thread.currentThread().getName();
            while (true) {
                if (update.get()) {
                    broadCastState();
                    update.set(false);
                }
                Set<Map.Entry<Socket, CommunicationHandler>> entries;
                synchronized (comms) {
                    // entrySet reflects map, so shallow-copy to prevent ConcurrentModificationError
                    entries = new HashMap<>(comms).entrySet();
                }
                for (Map.Entry<Socket, CommunicationHandler> entry : entries) {
                    Socket player = entry.getKey();
                    CommunicationHandler comm = entry.getValue();
                    String msg;
                    do {
                        synchronized (comm.recvBuffer) {
                            msg = comm.recvBuffer.poll();
                        }
                        if (msg != null) {
                            System.out.println(tn + " Read msg: " + msg);
                            if (msg.contentEquals("EXIT")) {
                                comms.remove(player);
                                update.set(true);
                            }
                            else if (msg.contentEquals("START GAME") && player == leader) {
                                startGame();
                                return;
                            }
                        }
                    } while (msg != null);
                }
            }
        }
    }
}
