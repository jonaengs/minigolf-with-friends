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

/** PERFORMANCE TACTICS: Bound queue size, Concurrency
 *
 * The MessageBuffer class keeps two buffers, one for each of the two types of messages the client
 * can receive: control messages and game data. Control messages are important and cannot be lost,
 * so they are kept in an unbounded buffer and only removed when the client retrieves them.
 * For game data messages, on the other hand, only the very newest message is of any relevance
 * to the client (applying old data would be a waste of resources), and as such, the game data buffer
 * only has space for one item, the most recent one. Any old data in the buffer is overwritten
 * once more recent data arrives.
 *
 * The MessageBuffer class is meant to be run as a separate thread, allowing it to continually
 * retrieve the newest data without focusing on any other logic. This ensures that its buffers
 * are filled in as soon as data arrives at the client device.
 */
public class MessageBuffer implements Runnable {
    final AtomicBoolean running = new AtomicBoolean(true);
    private final BlockingQueue<Message> msgBuffer = new LinkedBlockingQueue<>(); // Use take() method to wait for items to appear
    private final NetworkedGameState[] gameDataBuffer = new NetworkedGameState[1]; // Bounded buffer.
    private final ObjectInputStream objIn;

    protected MessageBuffer(ObjectInputStream objIn) {
        this.objIn = objIn;
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
                System.out.println("recv\t" + msg);
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
        }
    }
}
