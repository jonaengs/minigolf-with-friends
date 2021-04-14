package com.mygdx.minigolf.server.messages;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

public class GameState implements Serializable {
    public Map<String, PlayerState> stateMap;

    public GameState(Map<String, PlayerState> states) {
        this.stateMap = states;
    }

    public static class PlayerState implements Serializable {
        public float[] position;
        public float[] velocity;

        public PlayerState(Vector2 pos, Vector2 vel) {
            position = new float[]{pos.x, pos.y};
            velocity = new float[]{vel.x, vel.y};
        }

        @Override
        public String toString() {
            return "{" +
                    "pos=" + Arrays.toString(position) +
                    ", vel=" + Arrays.toString(velocity) +
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


