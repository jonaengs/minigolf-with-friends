package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;

import java.util.Arrays;
import java.util.List;

public class EntityFactory {

    private final Engine engine;
    private final List<String> validTypes;

    public EntityFactory(Engine engine){
        this.engine = engine;
        this.validTypes = Arrays.asList("golfball", "hole");
    }

    /**
     *
     * @param entityType A string stating an entity type to be returned
     * @return An Entity based on the argument passed
     * @throws IllegalArgumentException if argument is not a valid entity
     */
    public Entity createEntity(String entityType){
        if(!validTypes.contains(entityType)){
            throw new IllegalArgumentException();
        }
        Entity e = new Entity();
        switch(entityType){
            case "golfball":
                e.add(new Player());
                e.add(new Physical());
                e.add(new Graphical());
                //e.add(new PowerUpAffectable());
                break;
            case "hole":
                e.add(new Physical());
                e.add(new Objective());
                e.add(new Graphical());
        }
        engine.addEntity(e);
        return e;
    }
}
