package com.mygdx.minigolf.server;

import java.io.IOException;
import java.util.stream.Collectors;

public class Server {
    public static void main(String... args) {
        try {
            Thread.currentThread().setName(ConnectionDelegator.class.getName());
            new ConnectionDelegator().accept();
            new Thread(() -> {
                while (true) {
                    System.out.println("\nACTIVE THREADS: " + Thread.activeCount());
                    System.out.println(Thread.getAllStackTraces().keySet().stream().map(Thread::toString).collect(Collectors.joining("\n\t")));
                    try {
                        Thread.sleep(30_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
