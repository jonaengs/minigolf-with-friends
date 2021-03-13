package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;

public class Player implements Component {

    private int strokes = 0;


    //
    //  GETTERS AND SETTERS
    //

    public int getStrokes() {
        return strokes;
    }

    public void setStrokes(int strokes) {
        this.strokes = strokes;
    }
}
