package com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Random;


public class TestRoutine {

    public static void main(String... args) throws IOException, InterruptedException {
        Thread netController = new Thread(() -> {
            try {
                new ConnectionDelegator().accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        netController.start();
        System.out.println(Thread.activeCount());
        Thread.sleep(2000);

        Client leader = new Client();
        String lobbyID = leader.createLobby();
        leader.printRcv();

        Client follower1 = new Client();
        follower1.joinLobby(lobbyID);

        Client follower2 = new Client();
        follower2.joinLobby(lobbyID);

        Client follower3 = new Client();
        follower3.joinLobby(lobbyID);

        System.out.println(Thread.activeCount());
        Thread.sleep(3000);
        leader.close();
        Thread.sleep(6000);
        System.out.println(Thread.activeCount()); // should equal first value printed

        netController.join();
    }



}
