package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;

public class Objective implements Component {

    private int par;


    //
    //  GETTERS AND SETTERS
    //

    public int getPar() {
        return par;
    }

    public void setPar(int par) {
        this.par = par;
    }
}
