package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.minigolf.model.Effect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class Player implements Component {
    private int strokes = 0;
    private boolean completed = false;
    private List<Effect> effects = new ArrayList<>();

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

    public void addEffect(Effect effect){
        effects.add(effect);
    }

    public List<Effect> getEffects() { return this.effects; }

    public void removeEffects(){
        this.effects = effects.stream().filter(effect -> effect.getConstraintAmount() > 0).collect(Collectors.toList());
    }

    public void removeEffect(Effect effect){
        this.effects.remove(effects.indexOf(effect));
    }

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
