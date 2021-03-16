package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.minigolf.model.Effect;

public class PowerUpGiver implements Component {

    private Effect effect = null;

    public Effect getPowerup() {
        return effect;
    }

    public void setPowerup(Effect effect, int constraintAmount) {
        if(this.effect == null) {
            this.effect = effect;
            this.effect.setConstraintAmount(constraintAmount);
        }
        else throw new IllegalArgumentException();
    }

}
