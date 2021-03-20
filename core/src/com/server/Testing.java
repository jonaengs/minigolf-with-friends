package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Testing {

    public static void main(String... args) throws IOException {
        new NetworkController().accept();
    }
}
