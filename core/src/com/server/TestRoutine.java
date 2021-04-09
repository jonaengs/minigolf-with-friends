package com.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Random;
import java.util.stream.Collectors;


public class TestRoutine {

    public static void main(String... args) throws IOException, InterruptedException {
        Thread connectionDelegator = new Thread(() -> {
            try {
                Thread.currentThread().setName(ConnectionDelegator.class.getName());
                new ConnectionDelegator().accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        connectionDelegator.start();
        System.out.println(Thread.activeCount());
        Thread.sleep(2000);

        Client leader = new Client("leader");
        String lobbyID = leader.createLobby();

        Client follower1 = new Client("f1");
        follower1.joinLobby(lobbyID);
        follower1.runAsThread();

        Client follower2 = new Client("f2");
        follower2.joinLobby(lobbyID);
        follower2.runAsThread();

        Client follower3 = new Client("f3");
        follower3.joinLobby(lobbyID);
        follower3.runAsThread();

        System.out.println(Thread.activeCount());
        Thread.sleep(2_000);
        leader.startGame();
        leader.runAsThread();
        Thread.sleep(2_000);
        System.out.println(Thread.activeCount()); // should equal first value printed
        Thread.sleep(2_000);
        System.out.println("\nACTIVE THREADS:");
        System.out.println(Thread.getAllStackTraces().keySet().stream().map(Thread::toString).collect(Collectors.joining("\n\t")));

        connectionDelegator.join();
    }



}
