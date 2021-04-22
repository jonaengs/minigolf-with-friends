package com.mygdx.minigolf.server.controllers;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.TypedEnum;
import com.mygdx.minigolf.server.communicators.CommunicationHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseController<T extends CommunicationHandler<SendCmd, RecvCmd>, SendCmd extends TypedEnum, RecvCmd extends TypedEnum> implements Runnable {
    final List<T> comms = new ArrayList<>();

    // TODO: Handle IOException by removing comm (and player) that it resulted from.
    protected void broadcast(Message<SendCmd> msg) {
        for (T comm : comms) {
            try {
                comm.send(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void waitForRecv(Message<RecvCmd> recv) {
        for (T comm : comms) {
            while (comm.blockingRead().command != recv.command) {
            }
        }
    }

    protected void barrier(Message<SendCmd> send, Message<RecvCmd> recv) {
        broadcast(send);
        waitForRecv(recv);
    }
}
