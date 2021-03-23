package com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Random;

class Client {
    static final String[] names = {"Xzavier", "Alfonso", "Darian", "Kylian", "Maison", "Foster", "Keenan", "Yahya", "Heath", "Javion", "Jericho", "Aziel", "Darwin", "Marquis", "Mylo", "Ambrose", "Anakin", "Jordy", "Juelz", "Toby", "Yael"};

    Socket socket;
    BufferedWriter out;
    BufferedReader in;
    String name;

    public Client() throws IOException {
        this("localhost", 8888);
    }

    public Client(String url, int port) throws IOException {
        socket = new Socket(url, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

    public void send(String msg) throws IOException {
        System.out.println(name + " sends: " + msg);
        out.write(msg + "\n");
        out.flush();
    }

    public String recv() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        socket.close();
    }

    public void printRcv() {
        new Thread(() -> {
            while (true) {
                try {
                    System.out.println(name + " recvs: " + recv());
                } catch (IOException e) {
                    break;
                }
            }
            System.out.println(name + "'s printer exiting...");
        }).start();
    }
}
