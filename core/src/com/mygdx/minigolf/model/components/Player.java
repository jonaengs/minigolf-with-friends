package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;


public class Player implements Component {
    private int strokes = 0;
    private boolean completed = false;

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }

    public int getStrokes() {
        return strokes;
    }

    public void incrementStrokes() {
        this.strokes += 1;
    }

}
