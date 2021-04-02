package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.controller.ComponentMappers.ObjectiveMapper;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.controller.ComponentMappers.PlayerMapper;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Player;

public class ObjectiveSystem extends EntitySystem {
    private ImmutableArray<Entity> objectives;
    private ImmutableArray<Entity> players;

    public void addedToEngine(Engine engine) {
        objectives = engine.getEntitiesFor(Family.all(Objective.class).get());
        players = engine.getEntitiesFor(Family.all(Player.class).get());
    }

    // Every frame the objectives system checks if the objective components has had a player component collide with it
    public void update(float deltaTime) {
        for (Entity objectiveEntity : objectives) {
            Objective objective = ObjectiveMapper.get(objectiveEntity);
            for (Entity playerEntity : players) {
                Vector2 playerPosition = PhysicalMapper.get(playerEntity).getPosition();
                Player player = PlayerMapper.get(playerEntity);
                if (objective.contains(playerPosition) && !player.isCompleted()) {
                    player.complete();
                }
            }
        }
    }

}
