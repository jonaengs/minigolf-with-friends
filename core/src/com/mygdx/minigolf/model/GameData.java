package com.mygdx.minigolf.model;

import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.util.ConcurrencyUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.mygdx.minigolf.model.GameData.Event.LOBBY_ID_SET;
import static com.mygdx.minigolf.model.GameData.Event.PLAYERS_SET;
import static com.mygdx.minigolf.model.GameData.Event.PLAYER_NAMES_SET;
import static com.mygdx.minigolf.model.GameData.Event.PLAYER_REMOVED;
import static com.mygdx.minigolf.model.GameData.Event.SCORES_SET;
import static com.mygdx.minigolf.model.GameData.Event.STATE_SET;
import static com.mygdx.minigolf.model.GameData.State.IN_MENU;


// Singleton
public class GameData {
    private static GameData instance;
    public final MutableObservable<Map<String, Entity>, String> players;
    public final MutableObservable<List<String>, String> playerNames;
    public final MutableObservable<Map<String, Integer>, Integer> scores;
    public final Observable<String> levelName;
    public final Observable<String> localPlayerName;
    public final Observable<State> state;
    public final Observable<Integer> lobbyID; // -1 => Lobby not found. -2 => Lobby full.

    private GameData() {
        players = new MutableObservable<>(new HashMap<>(), PLAYERS_SET, PLAYER_REMOVED);
        playerNames = new MutableObservable<>(new ArrayList<>(), PLAYER_NAMES_SET, null);
        scores = new MutableObservable<>(new HashMap<>(), SCORES_SET, null);
        levelName = new Observable<>("", Event.LEVEL_NAME_SET);
        localPlayerName = new Observable<>("", Event.LOCAL_PLAYER_NAME_SET);
        state = new Observable<>(IN_MENU, STATE_SET);
        lobbyID = new Observable<>(0, LOBBY_ID_SET);
    }

    public static synchronized GameData get() {
        if (instance == null) {
            instance = new GameData();
        }
        return instance;
    }

    public static synchronized GameData reset() {
        // TODO: Remove all references to old object: Drop all subscribers, etc.
        instance = new GameData();
        return instance;
    }

    public static void subscribe(Notifiable subscriber, Observable... observables) {
        Arrays.asList(observables).forEach(o -> o.subscribe(subscriber));
    }

    public enum State {
        IN_MENU,
        JOINING_LOBBY,
        IN_LOBBY,
        INITIALIZING_GAME,
        WAITING_FOR_LEVEL_INFO,
        WAITING_FOR_START,
        IN_GAME,
        SCORE_SCREEN,
        GAME_OVER
    }

    public enum Event {
        PLAYER_NAMES_SET,
        SCORES_SET,
        STATE_SET,
        PLAYERS_SET,
        LEVEL_NAME_SET,
        LOCAL_PLAYER_NAME_SET,
        LOBBY_ID_SET,

        PLAYER_REMOVED,
    }

    public interface Notifiable {
        void notify(Object change, Event changeEvent);
    }

    public abstract static class Subscriber implements Notifiable {
        private final Set<Observable> observables = new HashSet<>();

        protected Subscriber(Observable... baseObservables) {
            observables.addAll(Arrays.asList(baseObservables));
        }

        public void removeSubscriptions(Observable... toRemove) {
            if (toRemove.length > 0) {
                observables.removeAll(Arrays.asList(toRemove));
            } else {
                observables.clear();
            }
            observables.forEach(o -> o.cancelSubscription(this));
        }

        public void setupSubscriptions(Observable... toAdd) {
            observables.addAll(Arrays.asList(toAdd));
            observables.forEach(o -> o.subscribe(this));
        }
    }

    public static class Observable<T> {
        Set<Notifiable> subscribers = new HashSet<>();
        AtomicReference<T> data = new AtomicReference<>();
        Event changeEvent;

        private Observable(T data, Event changeEvent) {
            this.data.set(data);
            this.changeEvent = changeEvent;
        }

        public synchronized T get() {
            return data.get();
        }

        public void set(T newData) {
            ConcurrencyUtils.postRunnable(() -> _set(newData));
        }

        public void waitSet(T newData) {
            ConcurrencyUtils.waitForPostRunnable(() -> _set(newData));
        }

        private void _set(T newData) {
            System.out.println(Arrays.toString(subscribers.toArray()));
            // TODO: Find out which order these should happen in
            subscribers.forEach(o -> o.notify(newData, changeEvent));
            data.set(newData);
        }

        public void subscribe(Notifiable notifiable) {
            subscribers.add(notifiable);
        }

        public void cancelSubscription(Notifiable notifiable) {
            subscribers.remove(notifiable);
        }
    }

    public static class MutableObservable<T, U> extends Observable<T> {
        Event removeEvent;

        private MutableObservable(T data, Event changeEvent, Event removeEvent) {
            super(data, changeEvent);
            this.removeEvent = removeEvent;
        }

        public synchronized void remove(U entry) {
            ConcurrencyUtils.postRunnable(() -> {
                subscribers.forEach(o -> o.notify(entry, removeEvent));
                if (data.get() instanceof List)
                    ((List) data.get()).remove(entry);
                if (data.get() instanceof Map)
                    ((Map) data.get()).remove(entry);
            });
        }
    }
}
