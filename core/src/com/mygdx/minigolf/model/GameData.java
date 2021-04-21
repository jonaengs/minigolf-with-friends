package com.mygdx.minigolf.model;

import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.levels.LevelLoader.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.mygdx.minigolf.model.GameData.Event.LEVEL_SET;
import static com.mygdx.minigolf.model.GameData.Event.LOBBY_ID_SET;
import static com.mygdx.minigolf.model.GameData.Event.PLAYERS_SET;
import static com.mygdx.minigolf.model.GameData.Event.PLAYER_NAMES_SET;
import static com.mygdx.minigolf.model.GameData.Event.PLAYER_REMOVED;
import static com.mygdx.minigolf.model.GameData.Event.SCORES_SET;
import static com.mygdx.minigolf.model.GameData.Event.STATE_SET;
import static com.mygdx.minigolf.model.GameData.State.IN_MENU;


// TODO: Make thread safe, or make all calls run in app thread
public class GameData {
    public final MutableObservable<Map<String, Entity>, String> players = new MutableObservable<>(new HashMap<>(), PLAYERS_SET, PLAYER_REMOVED);
    public final MutableObservable<List<String>, String> playerNames = new MutableObservable<>(new ArrayList<>(), PLAYER_NAMES_SET, null);
    public final MutableObservable<Map<String, Integer>, Integer> scores = new MutableObservable<>(new HashMap<>(), SCORES_SET, null);
    public final Observable<String> levelName = new Observable<>("", Event.LEVEL_NAME_SET);
    public final Observable<String> localPlayerName = new Observable<>("", Event.LOCAL_PLAYER_NAME_SET);;
    public final Observable<Level> level = new Observable<>(null, LEVEL_SET);
    public final Observable<State> state = new Observable<>(IN_MENU, STATE_SET);
    public final Observable<Integer> lobbyID = new Observable<>(-1, LOBBY_ID_SET);

    public enum State {
        IN_MENU, IN_LOBBY, INITIALIZING_GAME, IN_GAME, SCORE_SCREEN, LOADING_LEVEL, GAME_OVER
    }

    public enum Event {
        PLAYER_NAMES_SET,
        SCORES_SET,
        LEVEL_SET,
        STATE_SET,
        PLAYER_REMOVED,
        PLAYERS_SET,
        LEVEL_NAME_SET,
        LOCAL_PLAYER_NAME_SET,
        LOBBY_ID_SET
    }

    public interface Observer {
        void notify(Object change, Event changeEvent);
    }

    public void subscribe(Observer observer, Observable... observables) {
        Stream.of(observables).forEach(o -> o.subscribe(observer));
    }

    public static class Observable<T> {
        List<Observer> observers = new ArrayList<>();
        T data;
        Event changeEvent;

        private Observable(T data, Event changeEvent) {
            this.data = data;
            this.changeEvent = changeEvent;
        }

        public synchronized T get() {
            return data;
        }

        // Notify before changing so observer can still access old data if necessary
        public synchronized void set(T data) {
            observers.forEach(o -> o.notify(data, changeEvent));
            this.data = data;
        }

        public synchronized void subscribe(Observer observer) {
            observers.add(observer);
        }

        public synchronized void cancelSubscription(Observer observer) {
            observers.remove(observer);
        }
    }

    public static class MutableObservable<T, U> extends Observable<T> {
        Event removeEvent;

        private MutableObservable(T data, Event changeEvent, Event removeEvent) {
            super(data, changeEvent);
            this.removeEvent = removeEvent;
        }

        public synchronized void remove(U entry) {
            observers.forEach(o -> o.notify(entry, removeEvent));
            if (data instanceof List)
                ((List) data).remove(entry);
            if (data instanceof Map)
                ((Map) data).remove(entry);
        }
    }
}
