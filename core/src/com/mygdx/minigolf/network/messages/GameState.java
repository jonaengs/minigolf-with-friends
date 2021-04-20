package com.mygdx.minigolf.network.messages;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.Map;

public class GameState implements Serializable {
    public Map<String, PlayerState> stateMap;

    public GameState(Map<String, PlayerState> states) {
        this.stateMap = states;
    }

    public static class PlayerState implements Serializable {
        public Vector2 position;
        public Vector2 velocity;
        public int strokes = 0;

        public PlayerState(Vector2 pos, Vector2 vel) {
            position = new Vector2(pos);
            velocity = new Vector2(vel);
        }

        @Override
        public String toString() {
            return "{" +
                    "pos=" + position +
                    ", vel=" + velocity +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GameState{" +
                stateMap +
                '}';
    }
}


