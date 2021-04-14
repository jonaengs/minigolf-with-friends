package com.mygdx.minigolf.server;

import com.mygdx.minigolf.desktop.DesktopLauncher;

import java.io.IOException;

public class Server {
    public static void main(String... args) {
        Thread t = new Thread(() -> {
            try {
                Thread.currentThread().setName(ConnectionDelegator.class.getName());
                new ConnectionDelegator().accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
        DesktopLauncher.main(null);
        // t.join();
    }
}
