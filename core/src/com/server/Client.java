package com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.Random;

class Client {
    static final String[] names = {"Xzavier", "Alfonso", "Darian", "Kylian", "Maison", "Foster", "Keenan", "Yahya", "Heath", "Javion", "Jericho", "Aziel", "Darwin", "Marquis", "Mylo", "Ambrose", "Anakin", "Jordy", "Juelz", "Toby", "Yael"};

    String lobbyId;
    Socket socket;
    BufferedWriter out;
    PushbackInputStream pbin;
    BufferedReader in;
    String name;

    public Client() throws IOException {
        this("localhost", 8888);
    }

    public Client(String name) throws IOException {
        this("localhost", 8888);
        this.name = name;
    }

    public Client(String url, int port) throws IOException {
        socket = new Socket(url, port);
        pbin = new PushbackInputStream(socket.getInputStream());
        in = new BufferedReader(new InputStreamReader(pbin));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        name = names[new Random().nextInt(names.length)];
    }

    public String createLobby() throws IOException {
        send("CREATE NAME: " + name);
        return recv();
    }

    public void joinLobby(String id) throws IOException {
        send("JOIN " + id + " NAME: " + name);
    }

    public void startGame() throws IOException {
        send("ENTER GAME");
    }

    public void send(String msg) throws IOException {
        System.out.println(name + " sends: " + msg);
        out.write(msg + "\n");
        out.flush();
    }

    public String recv() throws IOException {
        String msg = in.readLine();
        System.out.println(name + " recv: " + msg);
        return msg;
    }

    public void close() throws IOException {
        socket.close();
    }

    public void runAsThread() {
        new Thread(() -> {
            Thread.currentThread().setName(this.getClass().getName());
            while (true) {
                String msg;
                try {
                    if (Utils.isEOF(socket, pbin)) {
                        break;
                    }
                    msg = recv();
                    if (msg.contentEquals("ENTER GAME")) {
                        send("GAME READY");
                    }
                } catch (IOException e) {
                    break;
                }
            }
            System.out.println(name + "'s printer exiting...");
        }).start();
    }
}
