package com.mygdx.minigolf.server;

import java.io.IOException;
import java.util.stream.Collectors;

public class Server {
    public static void main(String... args) {
        try {
            Thread.currentThread().setName(ConnectionDelegator.class.getName());
            new Thread(() -> {
                String pastThreads = "", nowThreads = "";
                while (true) {
                    nowThreads = "\t" + Thread.getAllStackTraces().keySet().stream()
                            .map(Thread::toString)
                            .collect(Collectors.joining("\n\t"));
                    if (!nowThreads.contentEquals(pastThreads)) {
                        System.out.println("\nACTIVE THREADS: " + Thread.activeCount());
                        System.out.println(nowThreads);
                        pastThreads = nowThreads;
                    }
                    try {
                        Thread.sleep(3_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new ConnectionDelegator().accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
