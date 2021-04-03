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
    // Immediately returns empty string if no data available. Otherwise reads until newline.
    public static String readStream(PushbackInputStream in) throws IOException {
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
