package com.mygdx.minigolf.network;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.controller.InputHandler;
import com.mygdx.minigolf.controller.ScreenController;
import com.mygdx.minigolf.model.GameData;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.network.messages.NetworkedGameState;
import com.mygdx.minigolf.network.messages.Message;
import com.mygdx.minigolf.network.messages.Message.ClientGameCommand;
import com.mygdx.minigolf.network.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.network.messages.Message.ServerGameCommand;
import com.mygdx.minigolf.network.messages.Message.ServerLobbyCommand;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client implements Runnable{
    Socket socket;
    ObjectInputStream objIn;
    ObjectOutputStream objOut;
    String name;
    GameData gameData;

    State state = State.IN_LOBBY;
    public List<String> playerList = new ArrayList<>();

    public Client(GameData gameData) throws IOException {
        this.gameData = gameData;
        socket = new Socket("localhost", 8888);
        socket.setTcpNoDelay(true);
        objOut = new ObjectOutputStream(socket.getOutputStream());
        objIn = new ObjectInputStream(socket.getInputStream()); // Must be instantiated after objOut
    }

    public void createLobby() throws IOException {
        send(new Message<>(ClientLobbyCommand.CREATE));
    }

    public void joinLobby(Integer lobbyID) throws IOException, ClassNotFoundException, IllegalArgumentException {
        send(new Message<>(ClientLobbyCommand.JOIN, lobbyID));
        new Thread(this).start();
    }

    public void startGame() throws IOException {
        send(new Message<>(ClientLobbyCommand.START_GAME));
        new Thread(this).start();
    }

    private void send(Message msg) throws IOException {
        System.out.println(name + " sends:\t" + msg);
        objOut.writeObject(msg);
        objOut.flush();
    }

    private Message waitRecv() throws IOException, ClassNotFoundException {
        Message msg = (Message) objIn.readObject();
        if (msg != null) System.out.println(name + " recvs:\t" + msg);
        return msg;
    }

    private Message recv() throws IOException, ClassNotFoundException {
        Object o = Utils.readObject(socket, objIn);
        if (o != null) System.out.println(name + " recvs:\t" + o);
        return (Message) o;
    }

    // TODO: Fix ConcurrentModificationError that will happen here
    public void exit() throws IOException {
        if (state == State.IN_LOBBY) {
            send(new Message<>(ClientLobbyCommand.EXIT));
        } else {
            send(new Message<>(ClientGameCommand.EXIT));
        }
        state = State.EXITING;
        socket.close();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getName() + "-" + name);
        final Map<String, Physical> playerPhysicals = new HashMap<>();

        // TODO: Handle GAME_COMPLETE. Handle score screen.
        // TODO: Connect to game interface / screens.
        State prevState = state;
        while (true) {
            try {
                Message<ServerLobbyCommand> lm;
                Message<ServerGameCommand> gm;
                Message msg = recv();
                if (prevState != state) System.out.println(state);
                prevState = state;
                // TODO: Handle unexpected messages (invalid state & msg.cmd combinations)
                switch (state) {
                    case IN_LOBBY:
                        if (msg != null) {
                            lm = msg;

                            switch (lm.command) {
                                case LOBBY_NOT_FOUND:
                                    gameData.lobbyID.set(-1);
                                case LOBBY_ID:
                                    gameData.lobbyID.set((Integer) lm.data);
                                case NAME:
                                    gameData.localPlayerName.set((String) lm.data);
                                    Thread.currentThread().setName(this.getClass().getName() + "-" + name);
                                    break;
                                case ENTER_GAME:
                                    state = State.LOADING_GAME;
                                    break;
                                case PLAYER_LIST:
                                    gameData.playerNames.set((List<String>) lm.data);
                                    break;
                            }
                        }
                        break;
                    case LOADING_GAME:
                        gameData.state.set(GameData.State.INITIALIZING_GAME);
                        gameData.players.get().entrySet().forEach(entry -> playerPhysicals.put(
                                entry.getKey(),
                                entry.getValue().getComponent(Physical.class)
                        ));
                        send(new Message<>(ClientLobbyCommand.GAME_READY));
                        state = State.WAITING_FOR_LEVEL_INFO;
                        gameData.state.set(GameData.State.LOADING_LEVEL);
                        break;
                    case WAITING_FOR_LEVEL_INFO:
                        gm = msg == null ? waitRecv() : msg;
                        if (gm.command == ServerGameCommand.LOAD_LEVEL) {
                            String levelName = (String) gm.data;
                            gameData.levelName.set(levelName);
                            send(new Message<>(ClientGameCommand.LEVEL_LOADED, levelName));
                            state = State.WAITING_FOR_START;
                        } else if (gm.command == ServerGameCommand.GAME_COMPLETE) {
                            System.out.println("GAME COMPLETE");
                            gameData.state.set(GameData.State.GAME_OVER);
                            state = State.EXITING;
                        } else
                            new RuntimeException("Expected level info (or game complete). Got " + gm).printStackTrace();
                        break;
                    case WAITING_FOR_START:
                        gm = msg == null ? waitRecv() : msg;
                        if (gm.command == ServerGameCommand.START_GAME) {
                            gameData.state.set(GameData.State.IN_GAME);
                            state = State.IN_GAME;
                        } else
                            new RuntimeException("Expected level info. Got " + gm).printStackTrace();
                        break;
                    case IN_GAME:
                        synchronized (InputHandler.input) {
                            if (!Float.isNaN(InputHandler.input.x) && !Float.isNaN(InputHandler.input.y) && !InputHandler.input.isZero()) {
                                System.out.println("PLAYER INPUT: " + InputHandler.input);
                                // Must sent new vector for input each time or call objOut.reset() for each input,
                                // otherwise objOut will cache input values and always send duplicates of those
                                send(new Message<>(ClientGameCommand.INPUT, new Vector2(InputHandler.input)));
                                InputHandler.input.setZero();
                            }
                        }
                        gm = msg == null ? waitRecv() : msg;
                        switch (gm.command) {
                            case GAME_DATA:
                                NetworkedGameState networkedGameState = (NetworkedGameState) gm.data;
                                if (networkedGameState != null) {
                                    Gdx.app.postRunnable(() -> {
                                        networkedGameState.stateMap.entrySet().forEach(entry -> {
                                            Physical phys = playerPhysicals.get(entry.getKey());
                                            phys.setVelocity(entry.getValue().velocity);
                                            phys.setPosition(entry.getValue().position);
                                            // phys.moveTowards(entry.getValue().position);
                                        });
                                    });
                                }
                                break;
                            case PLAYER_EXIT:
                                String exitingPlayer = (String) gm.data;
                                gameData.players.remove(exitingPlayer);
                                // TODO: Notify player of the removal of exitingPlayer
                                break;
                            case LEVEL_COMPLETE:
                                // TODO: Set score
                                state = State.SCORE_SCREEN;
                                break;
                        }
                        break;
                    case SCORE_SCREEN:
                        gm = msg == null ? waitRecv() : msg;
                        if (gm.command == ServerGameCommand.GAME_SCORE) {
                            gameData.scores.set((HashMap<String, Integer>) msg.data);
                            state = State.WAITING_FOR_LEVEL_INFO;
                        } else
                            new RuntimeException("Expected level info. Got " + gm).printStackTrace();
                        break;
                    case EXITING:
                        Gdx.app.postRunnable(() -> ScreenController.changeScreen(ScreenController.MAIN_MENU_VIEW));
                        System.out.println(name + "Exiting...");
                        return;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private enum State {
        INITIALIZING, IN_LOBBY, LOADING_GAME, WAITING_FOR_LEVEL_INFO, WAITING_FOR_START, IN_GAME, SCORE_SCREEN, EXITING;
    }
}
