package com.mygdx.minigolf.model.components;
import com.badlogic.ashley.core.Component;


public class Player implements Component{
    private int strokes = 0;
    private boolean completed = false;

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getStrokes() {
        return strokes;
    }

    public void setStrokes(int strokes) {
        this.strokes = strokes;
    }

}
