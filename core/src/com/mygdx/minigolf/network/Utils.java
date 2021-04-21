package com.mygdx.minigolf.network;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.mygdx.minigolf.HeadlessGame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
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
}
