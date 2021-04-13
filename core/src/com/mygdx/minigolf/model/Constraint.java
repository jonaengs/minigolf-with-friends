package com.mygdx.minigolf.model;

public interface Constraint {

    /**
     * @param compareValue used to compare againts the constraining int
     * @return true if the Effect is exhausted, false otherwise
     */
    boolean powerExhausted(int... compareValue);
    void setConstrainingInt(int constrainingInt);
}
