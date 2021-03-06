package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.minigolf.model.powerup.Effect;

public class PowerUpGiver implements Component {

    private Effect effect = null;

    public PowerUpGiver(Effect effect) {
        this.effect = effect;
    }

    public Effect getPowerup() {
        return effect;
    }

    public void setPowerup(Effect effect) {
        if(this.effect == null) {
            this.effect = effect;
        }
        else throw new IllegalArgumentException();
    }

}
