package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.minigolf.model.Effect;

import java.time.Duration;
import java.util.Map;

public class PowerUpAffectable implements Component {

    Map<Effect, Duration> effect;

    public Map<Effect, Duration> getEffect() {
        return effect;
    }

    public void setEffect(Map<Effect, Duration> effect) {
        this.effect = effect;
    }
}
