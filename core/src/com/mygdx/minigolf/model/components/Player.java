package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;


public class Player implements Component {
    private int totalStrokes = 0;
    private int levelStrokes = 0;
    public boolean completed = false;

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }

    public int getLevelStrokes() {
        return levelStrokes;
    }

    public void incrementStrokes() {
        this.levelStrokes += 1;
    }

    // TODO: public levelComplete()

}
