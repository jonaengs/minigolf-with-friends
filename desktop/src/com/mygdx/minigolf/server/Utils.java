package com.mygdx.minigolf.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Utils {

    // Read object stream. Returns null if timeout is reached.
    public static Object readObject(Socket socket, ObjectInputStream objIn) throws IOException, ClassNotFoundException {
        socket.setSoTimeout(10);
        try {
            return objIn.readObject();
        } catch (SocketTimeoutException e) {
            return null;
        } finally {
            socket.setSoTimeout(0);
        }
    }

    public static boolean isEOF(Socket socket, PushbackInputStream pbin) throws IOException {
        socket.setSoTimeout(500);
        try {
            int i = pbin.read();
            pbin.unread(i);
            return i == -1;
        } catch (SocketTimeoutException e) {
            return false;
        } finally {
            socket.setSoTimeout(0);
        }
    }

    // Attempt at creating non-blocking readLine.
    // Immediately returns empty string if no data available. Otherwise reads until newline.
    public static String readLine(PushbackInputStream in) throws IOException {
        StringBuilder data = new StringBuilder();
        if (in.available() > 0) {
            while (true) {
                char c = (char) in.read();
                if (c == '\n') break;
                data.append(c);
            }
        }
        return data.toString();
    }
}
