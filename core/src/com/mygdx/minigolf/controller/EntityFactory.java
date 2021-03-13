package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.EntityType;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpAffectable;
import com.mygdx.minigolf.model.components.PowerUpGiver;

public class EntityFactory {

    private final Engine engine;

    public EntityFactory(Engine engine){
        this.engine = engine;
    }

    /**
     * @param entityType A valid entity type
     * @return An Entity based on the argument passed
     */
    public Entity createEntity(EntityType entityType){
        Entity e = new Entity();
        switch(entityType){
            case golfball:
                e.add(new Player());
                e.add(new Physical());
                e.add(new Graphical());
                e.add(new PowerUpAffectable());
                break;
            case hole:
                e.add(new Physical());
                e.add(new Objective());
                e.add(new Graphical());
                break;
            case obstacle:
                e.add(new Physical());
                e.add(new Graphical());
                break;
            case spawn:
                e.add(new Physical());
            case powerup:
                e.add(new PowerUpGiver());
                e.add(new Physical());
                e.add(new Graphical());
            case surface:
                e.add(new Physical());
                e.add(new Graphical());
        }
        engine.addEntity(e);
        return e;
    }
}
