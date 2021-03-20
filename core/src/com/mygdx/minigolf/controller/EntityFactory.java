package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpGiver;
import com.mygdx.minigolf.model.components.PowerUpTaker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntityFactory {
    private static EntityFactory instance = null;
    private final Engine engine;

    private EntityFactory(Engine engine) {
        this.engine = engine;
    }

    public static EntityFactory get() {
        if (instance == null) {
            throw new IllegalArgumentException("Factory has not been set up yet");
        }
        return instance;
    }

    public static void setEngine(Engine engine) {
        if (instance == null) {
            EntityFactory.instance = new EntityFactory(engine);
        } else {
            throw new IllegalArgumentException("Factory has already been set up");
        }
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

    public enum EntityType {
        GOLFBALL(Player.class, Physical.class, Graphical.class, PowerUpTaker.class),
        HOLE(Physical.class, Graphical.class, Objective.class),
        OBSTACLE(Physical.class, Graphical.class),
        POWERUP(Physical.class, Graphical.class, PowerUpGiver.class),
        SPAWN(Physical.class),
        SURFACE(Physical.class, Graphical.class);

        public final List<Class<? extends Component>> components;

        @SafeVarargs
        EntityType(Class<? extends Component>... components) {
            this.components = Collections.unmodifiableList(Arrays.asList(components));
        }
    }
}
