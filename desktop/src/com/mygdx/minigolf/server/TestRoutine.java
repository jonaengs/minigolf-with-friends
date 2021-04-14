package com.mygdx.minigolf.server;

import java.io.IOException;


public class TestRoutine {

    public static void main(String... args) throws IOException, InterruptedException, ClassNotFoundException {
        new Thread(() -> {
            try {
                Thread.currentThread().setName(ConnectionDelegator.class.getName());
                new ConnectionDelegator().accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("NThreads: " + Thread.activeCount());
        Thread.sleep(200);

        Client leader = new Client("leader");
        leader.headless = false;
        Integer lobbyID = leader.createLobby();
        leader.runAsThread();

        Client follower1 = new Client("follow1");
        follower1.joinLobby(lobbyID);
        follower1.runAsThread();

        Client follower2 = new Client("follow2");
        follower2.joinLobby(lobbyID);
        follower2.runAsThread();

        // Client follower3 = new Client("follow3");
        // follower3.joinLobby(lobbyID);
        // follower3.runAsThread();

        Thread.sleep(2_000);
        leader.startGame();
        // System.out.println(Thread.activeCount()); // should equal first value printed
        // System.out.println("\nACTIVE THREADS:");
        // System.out.println(Thread.getAllStackTraces().keySet().stream().map(Thread::toString).collect(Collectors.joining("\n\t")));
    }



}
