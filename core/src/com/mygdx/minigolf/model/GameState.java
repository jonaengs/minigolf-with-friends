package com.mygdx.minigolf.model;

import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.levels.LevelLoader.Level;
import com.mygdx.minigolf.network.Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mygdx.minigolf.model.GameState.Event.CLIENT_CHANGE;
import static com.mygdx.minigolf.model.GameState.Event.LEVEL_CHANGE;
import static com.mygdx.minigolf.model.GameState.Event.LEVEL_NAME_CHANGE;
import static com.mygdx.minigolf.model.GameState.Event.PLAYER_NAMES_CHANGE;
import static com.mygdx.minigolf.model.GameState.Event.PLAYER_REMOVED;
import static com.mygdx.minigolf.model.GameState.Event.SCORES_CHANGE;
import static com.mygdx.minigolf.model.GameState.Event.STATE_CHANGE;

// TODO: Create GameController class. Has methods for: Spawning players, hiding completed players, etc.
// TODO: Consider implementing listeners/observer pattern here
// TODO: Make thread safe, or make all calls run in app thread
public class GameState {
    private static final Map<Event, List<Observer>> observers = Arrays.stream(Event.values()).collect(Collectors.toMap(
            event -> event,
            event -> new ArrayList<>()
    ));
    private static Map<String, Entity> players;
    private static List<String> playerNames;
    private static Map<String, Integer> scores;
    private static String levelName;
    private static String localPlayerName;
    private static Level level;
    private static Client client;
    private static State state; // TODO: Find better name. Should indicate that this is a stage in a lifecycle

    public static void setLevelName(String _levelName) {
        observers.get(LEVEL_NAME_CHANGE).forEach(observer -> observer.update(_levelName, LEVEL_NAME_CHANGE));
        levelName = _levelName;
    }

    public static void removePlayer(String player) {
        observers.get(Event.PLAYER_REMOVED).forEach(observer -> observer.update(player, PLAYER_REMOVED));
        players.remove(player);
    }

    public static void addObserver(Observer observer, Event... events) {
        Arrays.stream(events).forEach(e -> observers.get(e).add(observer));
    }

    public static List<String> getPlayerNames() {
        return playerNames;
    }

    public static void setPlayerNames(List<String> _playerList) {
        observers.get(PLAYER_NAMES_CHANGE).forEach(observer -> observer.update(_playerList, PLAYER_NAMES_CHANGE));
        playerNames = _playerList;
    }

    public static Map<String, Integer> getScores() {
        return scores;
    }

    public static void setScores(Map<String, Integer> _scores) {
        observers.get(SCORES_CHANGE).forEach(observer -> observer.update(_scores, SCORES_CHANGE));
        scores = _scores;
    }

    public static Level getLevel() {
        return level;
    }

    public static void setLevel(Level _currentLevel) {
        observers.get(Event.LEVEL_CHANGE).forEach(observer -> observer.update(_currentLevel, LEVEL_CHANGE));
        level = _currentLevel;
    }

    public static Client getClient() {
        return client;
    }

    public static void setClient(Client _client) {
        observers.get(Event.CLIENT_CHANGE).forEach(observer -> observer.update(client, CLIENT_CHANGE));
        client = _client;
    }

    public static State getState() {
        return state;
    }

    public static void setState(State _state) {
        observers.get(Event.STATE_CHANGE).forEach(observer -> observer.update(_state, STATE_CHANGE));
        state = _state;
    }

    public static Map<String, Entity> getPlayers() {
        return players;
    }

    // Should only used by gameController when game is first loaded, but there's no way to actually enforce this...
    public static void setPlayers(Map<String, Entity> _players) {
        observers.get(Event.PLAYERS_SET).forEach(observer -> observer.update(_players, PLAYER_REMOVED));
        players = _players;
    }

    public static String getLocalPlayerName() {
        return localPlayerName;
    }

    public static void setLocalPlayerName(String localPlayerName) {
        observers.get(Event.PLAYER_NAME_SET).forEach(observer -> observer.update(localPlayerName, PLAYER_REMOVED));
        GameState.localPlayerName = localPlayerName;
    }

    public enum State {
        IN_LOBBY, INITIALISING_GAME, IN_GAME, SCORE_SCREEN, LOADING_LEVEL, GAME_OVER
    }

    public enum Event {
        PLAYER_NAMES_CHANGE,
        SCORES_CHANGE, 
        LEVEL_CHANGE, 
        STATE_CHANGE, 
        CLIENT_CHANGE, 
        PLAYER_REMOVED, 
        PLAYERS_SET, 
        LEVEL_NAME_CHANGE,
        PLAYER_NAME_SET
    }

    public interface Observer {
        void update(Object change, Event event);
    }
}
