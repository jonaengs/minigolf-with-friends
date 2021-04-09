package com.mygdx.minigolf.server;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.Map;

public class GameState implements Serializable {
    Map<String, PlayerState> data;

    public GameState(Map<String, PlayerState> states) {
        this.data = states;
    }

    public static class PlayerState implements Serializable {
        Vector2 position;
        Vector2 velocity;

        public PlayerState(Vector2 pos, Vector2 vel) {
            position = pos;
            velocity = vel;
        }

        @Override
        public String toString() {
            return "PlayerState{" +
                    "position=" + position +
                    ", velocity=" + velocity +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GameState{" +
                "data=" + data +
                '}';
    }
}

