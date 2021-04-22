package com.mygdx.minigolf.network;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.TypedEnum;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Utils {

    // Read object stream. Returns null if timeout is reached.
    public static <T extends TypedEnum> Message<T> readObject(Socket socket, ObjectInputStream objIn) throws IOException, ClassNotFoundException {
        socket.setSoTimeout(500);
        try {
            return (Message<T>) objIn.readObject();
        } catch (SocketTimeoutException e) {
            return null;
        } finally {
            socket.setSoTimeout(0);
        }
    }
}
