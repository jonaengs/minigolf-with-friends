package com.mygdx.minigolf.server;

import java.io.IOException;

public class Server {
    public static void main(String... args) {
        try {
            Thread.currentThread().setName(ConnectionDelegator.class.getName());
            new ConnectionDelegator().accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
