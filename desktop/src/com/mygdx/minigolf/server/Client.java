package com.mygdx.minigolf.server;


import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.mygdx.minigolf.HeadlessGame;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.InputHandler;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.server.messages.GameMessage;
import com.mygdx.minigolf.server.messages.GameState;
import com.mygdx.minigolf.server.messages.LobbyMessage;
import com.mygdx.minigolf.server.messages.Message;
import com.mygdx.minigolf.server.messages.Message.ServerLobbyCommand;
import com.mygdx.minigolf.view.GameView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.mygdx.minigolf.server.messages.GameMessage.Command.LOAD_LEVEL;

class Client {
    Socket socket;
    BufferedWriter out;
    BufferedReader in;
    PushbackInputStream pbin;
    ObjectInputStream objIn;
    String name;
    String lobbyID;

    State state;

    boolean headless = true;

    public Client(String name) throws IOException {
        this("localhost", 8888);
        this.name = name;
        headless = !name.contentEquals("leader");
    }

    private Client(String url, int port) throws IOException {
        socket = new Socket(url, port);
        socket.setTcpNoDelay(true);
        pbin = new PushbackInputStream(socket.getInputStream());
        in = new BufferedReader(new InputStreamReader(pbin));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        objIn = new ObjectInputStream(socket.getInputStream());
    }

    public String createLobby() throws IOException {
        send("CREATE NAME: " + name);
        return recv();
    }

    public void joinLobby(String id) throws IOException {
        send("JOIN " + id + " NAME: " + name);
    }

    public void startGame() throws IOException {
        send("ENTER GAME");
    }

    public void send(String msg) throws IOException {
        System.out.println(name + " sends: " + msg);
        out.write(msg + "\n");
        out.flush();
    }

    public String recv() throws IOException {
        String msg = in.readLine();
        System.out.println(name + " recv: " + msg);
        return msg;
    }

    public void close() throws IOException {
        socket.close();
    }

    public void runAsThread() {
        new Thread(() -> {
            HeadlessGame game;
            String[] playerList = null;
            Thread.currentThread().setName(this.getClass().getName() + "-" + name);

            Map<String, Entity> players;
            Entity self;
            Map<String, Physical> playerPhysicalComponents = new HashMap<>();

            while (true) {
                String msg;
                try {
                    Message<ServerLobbyCommand> lm;
                    GameMessage gm;
                    switch (state) {
                        // TODO: Consider combining cases into two: LobbyMessage & GameMessage (using fallthrough)
                        case IN_LOBBY:
                            lm = (Message<ServerLobbyCommand>) objIn.readObject();
                            switch (lm.command) {
                                case NAME:
                                    name = (String) lm.data;
                                    break;
                                case ENTER_GAME:
                                    state = State.LOADING_GAME;
                                    break;
                                case PLAYER_LIST:
                                    playerList = (String[]) lm.data;
                                    break;
                            }
                            break;
                        case LOADING_GAME:
                            // TODO: Setup game
                            send("GAME READY");
                            state = State.WAITING_FOR_LEVEL_INFO;
                            break;
                        case WAITING_FOR_LEVEL_INFO:
                            gm = (GameMessage) objIn.readObject();
                            switch (gm.command) {
                                case LOAD_LEVEL:
                                    state = State.WAITING_FOR_START;
                                    // TODO: Load level
                                    break;
                            }
                        case WAITING_FOR_START:
                            gm = (GameMessage) objIn.readObject();
                            switch (gm.command) {
                                case START_GAME:
                                    state = State.IN_GAME;
                                    break;
                            }
                            break;
                        case IN_GAME:
                            gm = (GameMessage) objIn.readObject();
                            switch (gm.command) {
                                case GAME_DATA:
                                    // TODO: Apply data
                                    break;
                                case PLAYER_EXIT:
                                    // TODO: Remove player from player list and destroy entity
                                    break;
                                case LEVEL_COMPLETE:
                                    // TODO: Set score
                                    state = State.SCORE_SCREEN;
                                    break;
                            }
                    }

                    if (Utils.isEOF(socket, pbin)) {
                        break;
                    }
                    msg = recv();
                    if (msg != null && msg.contentEquals("ENTER GAME")) {
                        if (headless) {
                            game = new HeadlessGame();
                            new HeadlessApplication(game);
                            Thread.sleep(2_000); // Sleep to allow create method to run
                        } else {
                            game = new GameView();
                            LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
                            config.x = 100;
                            config.y = 100;
                            config.width = 1280;
                            config.height = 720;
                            new LwjglApplication(game, config);
                            Thread.sleep(2_000); // Sleep to allow create method to run
                        }
                        HeadlessGame finalGame = game;

                        System.out.println("PLAYER LIST: " + Arrays.toString(playerList));
                        players = Arrays.stream(playerList)
                                .collect(Collectors.toMap(
                                        player -> player,
                                        player -> finalGame.getFactory().createPlayer(10, 10)
                                ));
                        self = players.get(name);
                        Map<String, Entity> finalPlayers = players;
                        playerPhysicalComponents = players.keySet().stream()
                                .collect(Collectors.toMap(
                                        comm -> comm,
                                        comm -> finalPlayers.get(comm).getComponent(Physical.class)
                                ));
                        if (!headless && self != null) {
                            OrthographicCamera cam = ((GameView) game).getGraphicsSystem().getCam();
                            Gdx.input.setInputProcessor(new InputHandler(cam, PhysicalMapper.get(self).getBody()));
                        }
                        send("GAME READY");
                    }
                    if (msg != null && msg.contentEquals("START GAME")) {
                        Random r = new Random();
                        send(5 * r.nextGaussian() + ", " + 5 * r.nextFloat());
                        // Assume names are unique. TODO: set names server-side and send them to clients when they join a lobby
                        while (true) {
                            synchronized (InputHandler.input) {
                                if (!Float.isNaN(InputHandler.input.x) && !Float.isNaN(InputHandler.input.y) && !InputHandler.input.isZero()) {
                                    System.out.println("PLAYER INPUT: " + InputHandler.input);
                                    send(InputHandler.input.x + ", " + InputHandler.input.y);
                                    InputHandler.input.setZero();
                                }
                            }
                            GameState gameState = (GameState) objIn.readObject();
                            if (!headless) {
                                System.out.println("RECEIVED STATE: " + gameState + "");
                            }
                            if (gameState != null) {
                                Map<String, Physical> finalPlayerPhysicalComponents = playerPhysicalComponents;
                                gameState.data.entrySet().forEach(entry -> {
                                    Physical phys = finalPlayerPhysicalComponents.get(entry.getKey());
                                    phys.setVelocity(entry.getValue().velocity);
                                    phys.setPosition(entry.getValue().position);
                                });
                            }
                        }
                    }
                    else {
                        System.out.println("MSG: " + msg);
                        String[] split = msg.split(", ");
                        playerList = split.length >= 1 ? split : playerList;
                    }
                } catch (IOException | InterruptedException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
            System.out.println(name + "'s printer exiting...");
        }).start();
    }

    private enum State {
        IN_LOBBY, LOADING_GAME, WAITING_FOR_LEVEL_INFO, WAITING_FOR_START, IN_GAME, SCORE_SCREEN, LOADING_LEVEL;
    }
}
