package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.minigolf.model.Effect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Player implements Component {
    private int strokes = 0;
    private boolean completed = false;
    private Set<Effect> affectedBy = new HashSet<>();

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

    public void addAffix(Effect effect){
        affectedBy.add(effect);
    }
    public void removeAffix(Effect effect) {
        affectedBy.remove(effect);
    }

    public Set<Effect> getAffixes(){
        return this.affectedBy;
    }

}
