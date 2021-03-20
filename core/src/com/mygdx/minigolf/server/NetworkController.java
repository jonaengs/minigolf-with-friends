package com.mygdx.minigolf.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


// TODO: Change strings to enums. Serialize and send. Receive and deserialize ("marshall" in Java?).
public class NetworkController {
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
                    int lobbyID = 123456;
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
        Socket player;
        Integer id;
        public LobbyLeader(Socket s, int ID) throws IOException {
            player = s;
            players = Arrays.asList(s);
        }

        public void addPlayer(Socket s) {
            players.add(s);
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

    ServerSocket ss;
    private static HashMap<Integer, LobbyLeader> lobbies = new HashMap<>();

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

}
