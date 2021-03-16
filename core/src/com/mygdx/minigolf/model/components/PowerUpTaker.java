package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.minigolf.model.Effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PowerUpTaker implements Component {

    //liste med effects spilleren har og constraints på hver av dem
    private List<Effect> effects = new ArrayList<>();

    public void addEffect(Effect effect){
        effects.add(effect);
    }

    public void removeEffect(Effect effect){
        effects.remove(effect);
    }


    //metoder for å fjerne/adde effects og endre constraints

}
