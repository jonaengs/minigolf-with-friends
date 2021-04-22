package com.mygdx.minigolf.network;

import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ServerGameCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;
import com.mygdx.minigolf.network.messages.NetworkedGameState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageBuffer implements Runnable {
    private final BlockingQueue<Message> msgBuffer = new LinkedBlockingQueue<>(); // Use take() method to wait for items to appear
    private final NetworkedGameState[] gameDataBuffer = new NetworkedGameState[1];
    final AtomicBoolean running = new AtomicBoolean(false);
    private final ObjectInputStream objIn;

    protected MessageBuffer(ObjectInputStream objIn) {
        this.objIn = objIn;
        running.set(true);
        new Thread(this, this.getClass().getName()).start();
    }

    protected Message pollMsg() {
        return msgBuffer.poll();
    }

    protected Message waitMsg() {
        try {
            return msgBuffer.take();
        } catch (InterruptedException e) {
            return waitMsg(); // TODO: This could be bad. Consider passing error on to caller
        }
    }

    protected synchronized NetworkedGameState pollGameData() {
        NetworkedGameState data = gameDataBuffer[0];
        gameDataBuffer[0] = null;
        return data;
    }

    protected synchronized NetworkedGameState peekGameData() {
        return gameDataBuffer[0];
    }

    protected synchronized void setGameData(NetworkedGameState msg) {
        gameDataBuffer[0] = msg;
    }

    @Override
    public void run() {
        Message msg;
        Message<ServerGameCommand> gameMsg;
        try {
            while (running.get()) {
                msg = (Message) objIn.readObject();
                System.out.println(msg);
                if (msg.command instanceof ServerLobbyCommand) {
                    msgBuffer.add(msg);
                } else if (msg.command instanceof ServerGameCommand) {
                    gameMsg = msg;
                    if (gameMsg.command == ServerGameCommand.GAME_DATA) {
                        setGameData((NetworkedGameState) gameMsg.data);
                    } else {
                        msgBuffer.add(msg);
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            running.set(false);
        }
    }
}
