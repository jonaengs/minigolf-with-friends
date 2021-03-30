package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.model.Effect;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpGiver;

import java.util.List;

public class PowerUpSystem extends EntitySystem {

    private final EntityFactory entityFactory;
    private final World world;

    private final ImmutableArray<Entity> players;
    private final ImmutableArray<Entity> powerups;

    private final ComponentMapper<Player> playerMapper = ComponentMapper.getFor(Player.class);
    private final ComponentMapper<Physical> physicalMapper = ComponentMapper.getFor(Physical.class);
    private final ComponentMapper<PowerUpGiver> powerUpGiverMapper = ComponentMapper.getFor(PowerUpGiver.class);

    PowerUpSystem(Engine engine, EntityFactory ef, World world){
        super();
        this.entityFactory = ef;
        this.world = world;
        this.players = engine.getEntitiesFor(Family.all(Player.class).get());
        this.powerups = engine.getEntitiesFor(Family.all(PowerUpGiver.class).get());
    }

    //loop through all players, check if they have collided with other players or powerups, perform updates based on this.
    public void update(float dt){
        super.update(dt);
        //use physics system to determine collisions between players and obstacles
    }

    private void givePowerUp(Entity player, Entity powerUp){
        playerMapper.get(player).addEffect(powerUpGiverMapper.get(powerUp).getPowerup());
    }

    private void applyEffectToPlayer(Entity effectApplier, Entity effectReciever){
        List<Effect> effects = playerMapper.get(effectApplier).getEffects();
        for(Effect effect : effects){
            if (effect == Effect.EXPLODING) {
                Vector2 collisionVector = physicalMapper.get(effectReciever).getPosition();
                createExplosionParticle(collisionVector);
                playerMapper.get(effectReciever).addAffix(effect);
                physicalMapper.get(effectReciever).setPosition(Game.spawnPosition);
            }
        }
    }

    private void createExplosionParticle(Vector2 explosionPostion){
        //Entity explosionParticle = entityFactory.createEntity(EntityFactory.EntityType.PARTICLE);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(explosionPostion.x, explosionPostion.y);
        Body explosionBody = world.createBody(bodyDef);
        //physicalMapper.get(explosionParticle).setBody(explosionBody);
    }

}
