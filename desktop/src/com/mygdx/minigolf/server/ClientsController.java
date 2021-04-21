package com.mygdx.minigolf.server;

import com.mygdx.minigolf.network.messages.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ClientsController<T extends CommunicationHandler> implements Runnable {
    final List<T> comms = new ArrayList<>();

    // TODO: Handle IOException by removing comm (and player) that it resulted from.
    protected void broadcast(Message msg) {
        for (T comm : comms) {
            try {
                comm.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void waitForRecv(Message recv) {
        for (T comm : comms) {
            Message msg;
            do { // TODO: Change to not do spin waiting
                msg = (Message) comm.recvBuffer.poll();
            } while (msg == null || msg.command != recv.command);
        }
    }

    protected void barrier(Message send, Message recv) {
        broadcast(send);
        waitForRecv(recv);
    }
}
