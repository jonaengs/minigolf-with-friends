package com.server;

import java.io.PushbackInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;


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

    private static boolean isStreamEOF(PushbackInputStream pbin) throws IOException {
        int i = pbin.read();
        pbin.unread(i);
        return i == -1;
    }

    // Attempt at creating non-blocking readLine.
    // Possible that this will return incomplete data?
    private static String readData(PushbackInputStream in, boolean dropNewline) throws IOException {
        StringBuilder data = new StringBuilder();
        while (in.available() > 0) {
            char c = (char) in.read();
            if (dropNewline && c == '\n') break;
            data.append(c);
        }
        return data.toString();
    }

    private static String readData(PushbackInputStream in) throws IOException {
        return readData(in, true);
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
                PushbackInputStream in = new PushbackInputStream((socket.getInputStream()));
                String data = readData(in);
                System.out.println(tn + " Received data: " + data);
                CommunicationHandler comm = new CommunicationHandler(socket);
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

    private static class CommunicationHandler implements Runnable {
        public final PriorityQueue<String> sendBuffer = new PriorityQueue<>();
        public final PriorityQueue<String> recvBuffer = new PriorityQueue<>();
        private final Socket socket;

        public void close() {
            System.out.println(Thread.currentThread().getName() + " told to stop");
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }

        public CommunicationHandler(Socket s) {
            socket = s;
        }

        @Override
        public void run() {
            String tn = Thread.currentThread().getName();
            String sendMsg, recvMsg;
            try {
                BufferedWriter sendStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                PushbackInputStream recvStream = new PushbackInputStream(socket.getInputStream());
                while (socket.isConnected()) {
                    synchronized (this.sendBuffer) {
                        sendMsg = this.sendBuffer.poll();
                    }
                    if (sendMsg != null) {
                        System.out.println(tn + " Sending msg: " + sendMsg);
                        sendStream.write(sendMsg + "\n");
                        sendStream.flush();
                    }
                    if (isStreamEOF(recvStream)) {
                        socket.close();
                        break;
                    }
                    recvMsg = readData(recvStream);
                    if (!recvMsg.isEmpty()) {
                        System.out.println(tn + " Received msg: " + recvMsg);
                        synchronized (this.recvBuffer) {
                            this.recvBuffer.add(recvMsg);
                        }
                    }
                }
            } catch (IOException ignored) {
            } finally {
                synchronized (recvBuffer) {
                    recvBuffer.add("EXIT");
                }
                System.out.println(tn + " Exiting");
            }
        }
    }

    public static class LobbyController implements Runnable {
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

        private void broadCastState() {
            String playerSocks;
            synchronized (comms) {
                playerSocks = comms.stream().map(comm -> comm.socket).collect(toList()).toString();
            }
            broadcastMsg(playerSocks);
        }

        private List<CommunicationHandler> copyComms() {
            List<CommunicationHandler> copy;
            synchronized (comms) {
                copy = new ArrayList<>(comms);
            }
            return copy;
        }

        private void broadcastMsg(String msg) {
            for (CommunicationHandler ch : copyComms()) {
                synchronized (ch.sendBuffer) {
                    ch.sendBuffer.add(msg);
                }
            }
        }

        private void startGame() {
            broadcastMsg("GAME STARTING");
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
                    broadCastState();
                    update.set(false);
                }
                for (CommunicationHandler comm : copyComms()) {
                    String msg;
                    do {
                        synchronized (comm.recvBuffer) {
                            msg = comm.recvBuffer.poll();
                        }
                        if (msg != null) {
                            System.out.println(tn + " Read msg: " + msg);
                            if (msg.contentEquals("EXIT")) {
                                System.out.println(tn + " Removing player: " + comm.socket.toString());
                                comms.remove(comm);
                                update.set(true);
                            } else if (msg.contentEquals("START GAME") && comm == leader) {
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
