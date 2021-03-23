package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.minigolf.model.Effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PowerUpTaker implements Component {

    private final List<Effect> effects = new ArrayList<>();

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
