package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;

public class ObjectiveSystem extends EntitySystem {
    private final ComponentMapper<Physical> physicalMapper = ComponentMapper.getFor(Physical.class);
    private final ComponentMapper<Objective> objectiveMapper = ComponentMapper.getFor(Objective.class);
    private final ComponentMapper<Player> playerMapper = ComponentMapper.getFor(Player.class);
    private ImmutableArray<Entity> objectives;
    private ImmutableArray<Entity> players;

    public void addedToEngine(Engine engine) {
        objectives = engine.getEntitiesFor(Family.all(Objective.class).get());
        players = engine.getEntitiesFor(Family.all(Player.class).get());
    }

    // Every frame the objectives system checks if the objective components has had a player component collide with it
    public void update(float deltaTime) {
        for (Entity objectiveEntity : objectives) {
            Objective objective = objectiveMapper.get(objectiveEntity);
            for (Entity playerEntity : players) {
                Vector2 playerPosition = physicalMapper.get(playerEntity).getPosition();
                Player player = playerMapper.get(playerEntity);
                if (objective.contains(playerPosition) && !player.isCompleted()) {
                    player.complete();
                }
            }


        }
    }

}
