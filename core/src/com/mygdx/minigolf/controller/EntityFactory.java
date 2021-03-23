package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpTaker;
import com.mygdx.minigolf.model.components.PowerUpGiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntityFactory {
    public enum EntityType {
        GOLFBALL(Player.class, Physical.class, Graphical.class, PowerUpTaker.class),
        HOLE(Physical.class, Graphical.class, Objective.class),
        OBSTACLE(Physical.class, Graphical.class),
        POWERUP(Physical.class, Graphical.class, PowerUpGiver.class),
        SPAWN(Physical.class),
        SURFACE(Physical.class, Graphical.class),
        PARTICLE(Physical.class, Graphical.class);

        public final List<Class<? extends Component>> components;

        @SafeVarargs
        EntityType(Class<? extends Component>... components) {
            this.components = Collections.unmodifiableList(Arrays.asList(components));
        }
    }

    private final Engine engine;

    public EntityFactory(Engine engine) {
        this.engine = engine;
    }

    public Entity createEntity(EntityType entityType) {
        Entity entity = new Entity();
        entityType.components.forEach(c -> {
            try {
                entity.add((Component) c.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        engine.addEntity(entity);
        return entity;
    }
}
