package com.server;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Utils {

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
