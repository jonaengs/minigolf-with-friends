package com.server;

import java.io.IOException;
import java.io.PushbackInputStream;

public class Utils {

    public static boolean isEOF(PushbackInputStream pbin) throws IOException {
        int i = pbin.read();
        pbin.unread(i);
        return i == -1;
    }

    // Attempt at creating non-blocking readLine.
    // Possible that this will return incomplete data?
    public static String readStream(PushbackInputStream in) throws IOException {
        return readStream(in, true);
    }

    public static String readStream(PushbackInputStream in, boolean dropNewline) throws IOException {
        StringBuilder data = new StringBuilder();
        while (in.available() > 0) {
            char c = (char) in.read();
            if (dropNewline && c == '\n') break;
            data.append(c);
        }
        return data.toString();
    }
}
