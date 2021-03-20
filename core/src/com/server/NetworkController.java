package com.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.IntStream;


// TODO: Change strings to enums. Serialize and send. Receive and deserialize ("marshall" in Java?).
public class NetworkController {
    ServerSocket ss;
    final static int MIN_ID = 100_000;
    final static int MAX_ID = 1_000_000 - MIN_ID;
    static Random random = new Random();
    static IntStream intStream = random.ints(Integer.MAX_VALUE, MAX_ID);
    private static final HashMap<Integer, LobbyLeader> lobbies = new HashMap<>();

    private static int getId() {
        return intStream
                .map(i -> i + MIN_ID)
                .filter(id -> !lobbies.containsKey(id))
                .findFirst().getAsInt();
    }

    public NetworkController() throws IOException {
        new NetworkController(8888);
    }

    public NetworkController(int port) throws IOException {
        ss = new ServerSocket(port);
    }

    private void accept() throws IOException {
        while (true) {
            Socket s = ss.accept();
            new Thread(new ConnectionHandler(s));
        }
    }

    private static class CommunicationHandler implements Runnable {
        public final PriorityQueue<String> sendBuffer = new PriorityQueue<>();
        public final PriorityQueue<String> recvBuffer;
        private final Socket socket;

        public CommunicationHandler(Socket s, PriorityQueue<String> writeBuffer) {
            socket = s;
            recvBuffer = writeBuffer;
        }

        @Override
        public void run() {
            String send, recv;
            try {
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                DataInputStream din = new DataInputStream(socket.getInputStream());
                while (true) {
                    synchronized (sendBuffer) {
                        send = sendBuffer.poll();
                    }
                    if (send != null) {
                        dout.writeUTF(send);
                        dout.flush();
                        dout.close();
                    }
                    if (din.available() > 0) {
                        recv = din.readUTF();
                        synchronized (recvBuffer) {
                            recvBuffer.add(recv);
                        }
                    }
                }
            } catch (IOException e) {
                synchronized (recvBuffer) {
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
        boolean update = false;

        public LobbyController(Socket leader, CommunicationHandler leaderComm, int id) {
            this.leader = leader;
            lobbyID = id;
            comms = new HashMap<>();
            comms.put(leader, leaderComm);
        }

        public void addPlayer(Socket player, CommunicationHandler comm) {
            comms.put(player, comm);
            update = true;
        }

        private void broadCastState() {
            for (CommunicationHandler ch: comms.values()) {
                synchronized (ch.sendBuffer) {
                    ch.sendBuffer.add(comms.keySet().toString());
                }
            }
        }

        private void startGame() {

        }

        @Override
        public void run() {
            while (true) {
                if (update) {
                    broadCastState();
                    update = false;
                }
                for (Map.Entry<Socket, CommunicationHandler> entry : comms.entrySet()) {
                    Socket player = entry.getKey();
                    CommunicationHandler comm = entry.getValue();
                    String msg;
                    do {
                        synchronized (comm.recvBuffer) {
                            msg = comm.recvBuffer.poll();
                        }
                        if (msg.contentEquals("START GAME") && player == leader) {
                            startGame();
                            return;
                        }
                    } while (msg != null);
                }
            }
        }
    }

    private static class ConnectionHandler implements Runnable {
        Socket socket;
        public ConnectionHandler(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            try {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                String data = dis.readUTF();
                System.out.println(data);
                if (data.startsWith("CREATE")) {
                    int lobbyID = getId();
                    LobbyLeader leader = new LobbyLeader(socket, lobbyID);
                    new Thread(leader);
                    lobbies.put(lobbyID, leader);
                } else if (data.startsWith("JOIN")) { // FORMAT: "JOIN XXXXXX"
                    LobbyLeader leader = lobbies.get(Integer.parseInt(data.split(" ")[1]));
                    new Thread(new LobbyFollower(socket, leader));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class LobbyLeader implements Runnable {
        public List<Socket> players;
        boolean playerAdded;
        Socket player;
        Integer id;
        public LobbyLeader(Socket s, int ID) throws IOException {
            player = s;
            players = Arrays.asList(s);
        }

        synchronized public void addPlayer(Socket s) {
            players.add(s);
            playerAdded = true;
        }

        @Override
        public void run() {
            DataOutputStream dout = null;
            try {
                dout = new DataOutputStream(player.getOutputStream());
                dout.writeUTF("CREATED LOBBY: " + id);
                dout.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class LobbyFollower implements Runnable {
        private LobbyLeader leader;
        private Socket player;
        public LobbyFollower(Socket s, LobbyLeader l) {
            leader = l;
            player = s;
        }

        @Override
        public void run() {

        }
    }

}
