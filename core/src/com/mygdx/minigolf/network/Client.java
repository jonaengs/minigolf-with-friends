package com.mygdx.minigolf.network;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.controller.InputHandler;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientGameCommand;
import com.mygdx.minigolf.network.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.network.messages.Message.ServerGameCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;
import com.mygdx.minigolf.network.messages.NetworkedGameState;
import com.mygdx.minigolf.util.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.util.ConcurrencyUtils;
import com.mygdx.minigolf.util.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.mygdx.minigolf.model.GameData.State.INITIALIZING_GAME;
import static com.mygdx.minigolf.model.GameData.State.IN_LOBBY;
import static com.mygdx.minigolf.model.GameData.State.IN_MENU;
import static com.mygdx.minigolf.model.GameData.State.JOINING_LOBBY;

public class Client implements Runnable {
    Socket socket;
    ObjectOutputStream objOut;
    MessageBuffer recvBuffer;

    public Client() throws IOException {
        socket = new Socket(getIP(), 8888);
        socket.setTcpNoDelay(true);
        objOut = new ObjectOutputStream(socket.getOutputStream());
        recvBuffer = new MessageBuffer(new ObjectInputStream(socket.getInputStream())); // Must be instantiated after objOut
    }

    private String getIP() {
        if (Constants.SERVER_IP != null)
            return Constants.SERVER_IP;
        else if (Gdx.app.getType() == Application.ApplicationType.Desktop)
            return "localhost";
        else if (Gdx.app.getType() == Application.ApplicationType.Android)
            return "10.0.2.2";
        throw new RuntimeException();
    }

    public void createLobby() throws IOException {
        send(new Message<>(ClientLobbyCommand.CREATE));
    }

    public void joinLobby(Integer lobbyID) throws IOException {
        send(new Message<>(ClientLobbyCommand.JOIN, lobbyID));
    }

    public void startGame() throws IOException {
        send(new Message<>(ClientLobbyCommand.START_GAME));
    }

    private void send(Message msg) throws IOException {
        System.out.println("send:\t" + msg);
        objOut.writeObject(msg);
        objOut.flush();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName());
        GameData gameData = GameData.get();

        // TODO: Handle GAME_COMPLETE. Handle score screen.
        // TODO: Consider wrapping state in an AtomicReference so it can be changed by other threads
        // TODO: Handle unexpected messages (invalid state & msg.cmd combinations)
        Message<ServerLobbyCommand> lm;
        Message<ServerGameCommand> gm;
        while (true) {
            try {
                switch (gameData.state.get()) {
                    /* INITIAL SETUP STATES */
                    case JOINING_LOBBY:
                        lm = recvBuffer.waitMsg();
                        switch (lm.command) {
                            case LOBBY_ID:
                                gameData.lobbyID.set((Integer) lm.data);
                                gameData.state.waitSet(IN_LOBBY);
                                break;
                            case LOBBY_NOT_FOUND:
                                gameData.lobbyID.set(-1);
                                // TODO: Handle better. Maybe set state in GameController if lobbyID = -1 is detected and then do more logic there
                                break;
                            case LOBBY_FULL:
                                gameData.lobbyID.set(-2);
                                break;
                        }
                        break;
                    case IN_LOBBY:
                        lm = recvBuffer.waitMsg();
                        switch (lm.command) {
                            case NAME:
                                String name = (String) lm.data;
                                gameData.localPlayerName.set(name);
                                Thread.currentThread().setName(this.getClass().getName() + "-" + name);
                                break;
                            case ENTER_GAME:
                                gameData.state.waitSet(INITIALIZING_GAME);
                                break;
                            case PLAYER_LIST:
                                gameData.playerNames.set((List<String>) lm.data);
                                break;
                        }
                        break;
                    case INITIALIZING_GAME:
                        send(new Message<>(ClientLobbyCommand.GAME_READY));
                        gameData.state.waitSet(GameData.State.WAITING_FOR_LEVEL_INFO);
                        break;

                    /* GAME LOOP STATES */
                    case WAITING_FOR_LEVEL_INFO:
                        gm = recvBuffer.waitMsg();
                        if (gm.command == ServerGameCommand.LOAD_LEVEL) {
                            String levelName = (String) gm.data;
                            gameData.levelName.waitSet(levelName);
                            gameData.state.waitSet(GameData.State.WAITING_FOR_START);
                            send(new Message<>(ClientGameCommand.LEVEL_LOADED, levelName));
                        } else if (gm.command == ServerGameCommand.GAME_COMPLETE) {
                            gameData.state.waitSet(GameData.State.GAME_OVER);
                        } else
                            new RuntimeException("Expected level info (or game complete). Got " + gm).printStackTrace();
                        break;
                    case WAITING_FOR_START:
                        gm = recvBuffer.waitMsg();
                        if (gm.command == ServerGameCommand.START_GAME) {
                            gameData.state.waitSet(GameData.State.IN_GAME);
                        } else
                            new RuntimeException("Expected level info. Got " + gm).printStackTrace();
                        break;
                    case IN_GAME:
                        synchronized (InputHandler.input) {
                            if (!InputHandler.input.isZero(0.1f)) {
                                System.out.println("PLAYER INPUT: " + InputHandler.input);
                                // Must send new vector for input each time or call objOut.reset() for each input,
                                // otherwise objOut will cache input values and always send duplicates of those
                                send(new Message<>(ClientGameCommand.INPUT, new Vector2(InputHandler.input)));
                                InputHandler.input.setZero();
                            }
                        }
                        NetworkedGameState receivedState = recvBuffer.pollGameData();
                        if (receivedState != null) {
                            ConcurrencyUtils.skipPostRunnable(() ->
                                    receivedState.stateMap.entrySet().forEach(entry -> {
                                        Physical phys = PhysicalMapper.get(gameData.players.get().get(entry.getKey()));
                                        phys.setVelocity(entry.getValue().velocity);
                                        phys.setPosition(entry.getValue().position);
                                        // phys.moveTowards(entry.getValue().position);
                                    })
                            );
                        } else {
                            gm = recvBuffer.pollMsg();
                            if (gm != null) {
                                switch (gm.command) {
                                    case PLAYER_EXIT:
                                        String exitingPlayer = (String) gm.data;
                                        gameData.players.remove(exitingPlayer);
                                        // TODO: Notify player of the removal of exitingPlayer
                                        break;
                                    case LEVEL_COMPLETE:
                                        gameData.state.waitSet(GameData.State.SCORE_SCREEN);
                                        break;
                                }
                            }
                        }
                        break;
                    case SCORE_SCREEN:
                        gm = recvBuffer.waitMsg();
                        if (gm.command == ServerGameCommand.GAME_SCORE) {
                            gameData.scores.set((HashMap<String, Integer>) gm.data);
                            gameData.state.waitSet(GameData.State.WAITING_FOR_LEVEL_INFO);
                        } else
                            new RuntimeException("Expected level info. Got " + gm).printStackTrace();
                        break;
                    case GAME_OVER:
                        System.out.println("Exiting... (GAME OVER)");
                        return;
                }
            } catch (Exception e1) {
                recvBuffer.running.set(false);
                try {
                    if (Arrays.asList(JOINING_LOBBY, IN_LOBBY, IN_MENU, INITIALIZING_GAME).contains(gameData.state.get()))
                        send(new Message<>(ClientLobbyCommand.EXIT));
                    else
                        send(new Message<>(ClientGameCommand.EXIT));
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                e1.printStackTrace();
                return;
            }
        }
    }
}
