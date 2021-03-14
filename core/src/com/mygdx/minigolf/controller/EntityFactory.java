package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.EntityType;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpAffectable;
import com.mygdx.minigolf.model.components.PowerUpGiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EntityFactory {

    private final Engine engine;

    Map<EntityType, HashSet<Class<? extends Component>>> componentMap = new HashMap<EntityType, HashSet<Class<? extends Component>>>() {{
        put(EntityType.GOLFBALL, new HashSet<>(Arrays.asList(Player.class, Physical.class, Graphical.class, PowerUpAffectable.class)));
        put(EntityType.HOLE, new HashSet<>(Arrays.asList(Physical.class, Graphical.class, Objective.class)));
        put(EntityType.SPAWN, new HashSet<>(Collections.singletonList(Physical.class)));
        put(EntityType.POWERUP, new HashSet<>(Arrays.asList(Physical.class, Graphical.class, PowerUpGiver.class)));
        put(EntityType.DEFAULT, new HashSet<>(Arrays.asList(Physical.class, Graphical.class)));
    }};

    public EntityFactory(Engine engine) {
        this.engine = engine;
    }

    public Entity createEntity(EntityType entityType) {
        Entity e = new Entity();
        componentMap.get(entityType).forEach(c -> {
            try {
                e.add(c.newInstance());
            } catch (InstantiationException | IllegalAccessException instantiationException) {
                instantiationException.printStackTrace();
            }
        });
        engine.addEntity(e);
        return e;
    }
}
