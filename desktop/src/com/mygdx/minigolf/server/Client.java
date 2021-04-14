package com.mygdx.minigolf.server;


import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.InputHandler;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.server.messages.GameState;
import com.mygdx.minigolf.server.messages.Message;
import com.mygdx.minigolf.server.messages.Message.ClientGameCommand;
import com.mygdx.minigolf.server.messages.Message.ClientLobbyCommand;
import com.mygdx.minigolf.server.messages.Message.ServerGameCommand;
import com.mygdx.minigolf.server.messages.Message.ServerLobbyCommand;
import com.mygdx.minigolf.view.GameView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

class Client {
    Socket socket;
    ObjectInputStream objIn;
    ObjectOutputStream objOut;
    String name;

    State state = State.IN_LOBBY;

    boolean headless = true;

    public Client(String name) throws IOException {
        this("localhost", 8888);
        this.name = name;
    }

    private Client(String url, int port) throws IOException {
        socket = new Socket(url, port);
        socket.setTcpNoDelay(true);
        objOut = new ObjectOutputStream(socket.getOutputStream());
        objIn = new ObjectInputStream(socket.getInputStream()); // Must be instantiated after objOut
    }

    public Integer createLobby() throws IOException, ClassNotFoundException {
        send(new Message<>(ClientLobbyCommand.CREATE));
        Message<ServerLobbyCommand> msg = (Message<ServerLobbyCommand>) recv();
        return (Integer) msg.data;
    }

    public void joinLobby(Integer lobbyID) throws IOException {
        send(new Message<>(ClientLobbyCommand.JOIN, lobbyID));
    }

    public void startGame() throws IOException {
        send(new Message<>(ClientLobbyCommand.START_GAME));
    }

    private void send(Message msg) throws IOException {
        System.out.println(name + " sends:\t" + msg);
        objOut.writeObject(msg);
        objOut.flush();
    }

    private Message recv() throws IOException, ClassNotFoundException {
        Message msg = (Message) Utils.readObject(socket, objIn);
        if (msg != null) System.out.println(name + " recvs:\t" + msg);
        return msg;
    }

    public void exit() throws IOException {
        send(new Message<>(ClientLobbyCommand.EXIT));
        state = State.EXITING;
        socket.close();
    }

    public void runAsThread() {
        new Thread(() -> {
            final HeadlessGame game = headless ? new HeadlessGame() : new GameView();
            List<String> playerList = new ArrayList<>();
            Thread.currentThread().setName(this.getClass().getName() + "-" + name);

            Entity self;
            final Map<String, Entity> players = new HashMap<>();
            final Map<String, Physical> playerPhysicalComponents = new HashMap<>();

            while (true) {
                try {
                    Message<ServerLobbyCommand> lm;
                    Message<ServerGameCommand> gm;
                    Message msg = recv();
                    switch (state) {
                        // TODO: Consider combining cases into two: LobbyMessage & GameMessage (using fallthrough)
                        case IN_LOBBY:
                            if (msg != null) {
                                lm = msg;

                                switch (lm.command) {
                                    case NAME:
                                        name = (String) lm.data;
                                        break;
                                    case ENTER_GAME:
                                        state = State.LOADING_GAME;
                                        break;
                                    case PLAYER_LIST:
                                        playerList = (List<String>) lm.data;
                                        break;
                                }
                            }
                            break;
                        case LOADING_GAME:
                            if (headless) Utils.initHeadlessGame(game);
                            else Utils.initGameView((GameView) game);
                            Thread.sleep(500); // Sleep to allow create method to run

                            playerList.forEach(player -> players.put(
                                    player,
                                    game.getFactory().createPlayer(5, 5)
                            ));
                            players.entrySet().forEach(entry -> playerPhysicalComponents.put(
                                    entry.getKey(),
                                    entry.getValue().getComponent(Physical.class)
                            ));
                            self = players.get(name);
                            if (!headless && self != null) {
                                OrthographicCamera cam = ((GameView) game).getGraphicsSystem().getCam();
                                Gdx.input.setInputProcessor(new InputHandler(cam, PhysicalMapper.get(self).getBody()));
                            }
                            send(new Message<>(ClientLobbyCommand.GAME_READY));
                            state = State.WAITING_FOR_START;
                            // Assume names are unique. TODO: set names server-side and send them to clients when they join a lobby
                            break;
                        case WAITING_FOR_LEVEL_INFO:
                            if (msg != null) {
                                gm = msg;
                                switch (gm.command) {
                                    case LOAD_LEVEL:
                                        state = State.WAITING_FOR_START;
                                        // TODO: Load level
                                        break;
                                }
                            }
                        case WAITING_FOR_START:
                            if (msg != null) {
                                gm = msg;
                                switch (gm.command) {
                                    case START_GAME:
                                        state = State.IN_GAME;
                                        Random r = new Random();
                                        // send(new Message<>(ClientGameCommand.INPUT, new Vector2(5 * (float) r.nextGaussian(), 5 * r.nextFloat())));
                                        break;
                                }
                            }
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
                            if (msg != null) {
                                gm = msg;
                                switch (gm.command) {
                                    case GAME_DATA:
                                        GameState gameState = (GameState) gm.data;
                                        if (gameState != null) {
                                            gameState.stateMap.entrySet().forEach(entry -> {
                                                Physical phys = playerPhysicalComponents.get(entry.getKey());
                                                phys.setVelocity(entry.getValue().velocity);
                                                phys.setPosition(entry.getValue().position);
                                            });
                                        }
                                        break;
                                    case PLAYER_EXIT:
                                        String exitingPlayer = (String) gm.data;
                                        playerList.remove(exitingPlayer);
                                        players.remove(exitingPlayer).removeAll();
                                        playerPhysicalComponents.remove(exitingPlayer);
                                        // TODO: Remove player from player list and destroy entity
                                        break;
                                    case LEVEL_COMPLETE:
                                        // TODO: Set score
                                        state = State.SCORE_SCREEN;
                                        break;
                                }
                            }
                            break;
                        case EXITING:
                            System.out.println(name + "Exiting...");
                            return;
                    }
                } catch (IOException | InterruptedException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
            System.out.println(name + " Exiting...");
        }).start();
    }

    private enum State {
        IN_LOBBY, LOADING_GAME, WAITING_FOR_LEVEL_INFO, WAITING_FOR_START, IN_GAME, SCORE_SCREEN, LOADING_LEVEL, EXITING;
    }
}
