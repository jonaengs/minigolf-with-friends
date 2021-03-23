package com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.stream.Collectors;

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

        Socket leader = new Socket("localhost", 8888);
        sendMsg("CREATE", leader);
        String LobbyID = rcv(leader);
        printRcv(leader);

        Socket follower1 = new Socket("localhost", 8888);
        sendMsg("JOIN " + LobbyID, follower1);

        Socket follower2 = new Socket("localhost", 8888);
        sendMsg("JOIN " + LobbyID, follower2);

        rcv(follower1);
        follower1.close();
        sendMsg("START GAME", leader);


        netController.join();
    }

    private static void sendMsg(String msg, Socket socket) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        out.write(msg + "\n");
        out.flush();
    }

    private static String rcv(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return in.readLine();
    }

    private static void printRcv(Socket socket) throws IOException {
        new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                do {
                    line = in.readLine();
                    System.out.println("Client received: " + line);
                } while (line != null && !line.isEmpty());
                System.out.println("Client read finished");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
