package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;

// All logic should go in the system
public class ObjectiveSystem extends EntitySystem{
    private ImmutableArray<Entity> objectives;
    private ImmutableArray<Entity> players;

    private ComponentMapper<Physical> physicalMapper;
    private ComponentMapper<Objective> objectiveMapper;
    private ComponentMapper<Player> playerMapper;

    public ObjectiveSystem(){
        physicalMapper = ComponentMapper.getFor(Physical.class);
        objectiveMapper = ComponentMapper.getFor(Objective.class);
        playerMapper = ComponentMapper.getFor(Player.class);
    }

    public void addedToEngine(Engine engine) {
        objectives = engine.getEntitiesFor(Family.all(Physical.class, Objective.class).get());
        players = engine.getEntitiesFor(Family.all(Player.class).get());
    }

    // Every frame the objectives system checks if the objective components has had a player component collide with it
    public void update(float deltaTime) {
        for (int i = 0; i < objectives.size(); i++) {
            Entity objective = objectives.get(i);
            Objective area = objectiveMapper.get(objective);
            for(int j = 0; j < players.size(); j++){
                Entity player = players.get(j);
                Physical position = physicalMapper.get(player);
                // if player position is inside objective area set player completion to true
                if(area.getArea().contains(position.getPosition())){
                    Player completed = playerMapper.get(player);
                    completed.setCompleted(true);
                }
            }


        }
    }

}
