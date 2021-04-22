package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.minigolf.model.powerup.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Player implements Component {
    public boolean completed = false;
    private int totalStrokes = 0;
    private int levelStrokes = 0;
    private List<Effect> effects = new ArrayList<>();

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }

    public int getStrokes() {
        return levelStrokes;
    }

    public int getLevelStrokes() {
        return levelStrokes;
    }

    public void incrementStrokes() {
        this.levelStrokes += 1;
    }

    public void addEffect(Effect effect) {
        effects.add(effect);
    }

    public List<Effect> getEffects() {
        return this.effects;
    }

    public void removeEffects() {
        this.effects = effects.stream().filter(effect -> effect.getConstraint().powerExhausted(this.levelStrokes)).collect(Collectors.toList());
    }

    public void removeEffect(Effect effect) {
        this.effects.remove(effects.indexOf(effect));
    }
}
