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
    private final List<Effect> effects = new ArrayList<>();

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

    public void addEffect(Effect effect){
        effects.add(effect);
    }

    public List<Effect> getEffects() { return this.effects; }

    public void removeEffect(Effect effect){
        effects.remove(effect);
    }

    //Kanskje skummelt å la poweruptakers endre constraint amount på effects de har fått?
    public void setEffectConstraintAmount(Effect effect, int amount){
        effects.get(effects.indexOf(effect)).setConstraintAmount(amount);
    }

    public void decrementConstraint(Effect effect){
        effects.get(effects.indexOf(effect)).decrementConstraintAmount();
    }

    public int getEffectConstraintAmount(Effect effect){
        return effects.get(effects.indexOf(effect)).getConstraintAmount();
    }

}
