package com.mygdx.minigolf.model;

public class StrokeConstraint implements Constraint{

    private int playerStartStroke;
    private int allowedStrokes;

    public StrokeConstraint(int allowedStrokes){
        setConstrainingInt(allowedStrokes);
    }

    public void setPlayerStartStroke(int currentStrokeCount){
        this.playerStartStroke = currentStrokeCount;
    }

    /**
     * @param compareValue used to compare current player strokes to the constraining int
     * @return true if the Effect is exhausted, false otherwise
     */
    @Override
    public boolean powerExhausted(int... compareValue) {
        int currentPlayerStrokes = compareValue[0];
        return currentPlayerStrokes >= playerStartStroke + allowedStrokes;
    }

    @Override
    public void setConstrainingInt(int constrainingInt) {
        this.allowedStrokes = constrainingInt;
    }

    @Override
    public String toString(){
        return "StrokeConstraint with " + allowedStrokes + "allowed strokes";
    }
}
