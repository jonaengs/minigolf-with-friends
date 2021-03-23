package com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class Testing {

    public static void main(String... args) throws IOException, InterruptedException {
        Thread netController = new Thread(() -> {
            try {
                new NetworkController().accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        netController.start();
        Thread.sleep(2000);

        Client leader = new Client();
        leader.send("CREATE");
        String lobbyID = leader.rcv();
        leader.printRcv();

        Client follower1 = new Client();
        follower1.send("JOIN " + lobbyID);

        Client follower2 = new Client();
        follower2.send("JOIN " + lobbyID);

        Thread.sleep(3000);
        follower1.close();
        follower2.close();

        Client follower3 = new Client();
        follower3.send("JOIN " + lobbyID);

        netController.join();
    }

    static class Client {
        Socket socket;
        BufferedWriter out;
        BufferedReader in;

        public Client() throws IOException {
            this("localhost", 8888);
        }

        public Client(String url, int port) throws IOException {
            socket = new Socket(url, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }

        public void send(String msg) throws IOException {
            System.out.println("Client send: " + msg);
            out.write(msg + "\n");
            out.flush();
        }

        public String rcv() throws IOException {
            return in.readLine();
        }

        public void close() throws IOException {
            in.close();
            out.close();
            socket.close();
        }

        public void printRcv() {
            new Thread(() -> {
                while (true) {
                    try {
                        System.out.println("Client rcv: " + rcv());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

}
