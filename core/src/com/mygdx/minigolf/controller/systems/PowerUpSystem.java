package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.model.Effect;
import com.mygdx.minigolf.model.Power;
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

    public PowerUpSystem(Engine engine, EntityFactory ef, World world){
        super();
        this.entityFactory = ef;
        this.world = world;
        this.players = engine.getEntitiesFor(Family.all(Player.class).get());
        this.powerups = engine.getEntitiesFor(Family.all(PowerUpGiver.class).get());

    }

    public void update(float dt){
        super.update(dt);

        for(Entity player : players){
            Player playerComponent = playerMapper.get(player);
            playerComponent.removeEffects();
        }
    }

    public void givePowerUp(Entity player, Effect effect){
        playerMapper.get(player).addEffect(effect);
        effect.setConstraintStart(playerMapper.get(player).getStrokes());
        switch (effect.getPower()){
            case EXPLODING:
                physicalMapper.get(player).addContactListener(new Physical.ContactListener(1) {
                    @Override
                    public void beginContact(Entity other, Contact contact) {
                        if(other.getComponent(Player.class) != null){
                            applyEffectToPlayer(player, other);
                        }

                    }
                    @Override
                    public void endContact(Entity other, Contact contact) {
                        if(other.getComponent(Player.class) != null){
                            Player playerComponent = playerMapper.get(player);
                            playerComponent.decrementConstraint(effect);
                        }
                    }
                });
                break;
            case NO_COLLISION:
                physicalMapper.get(player).addContactListener(new Physical.ContactListener(1) {
                    @Override
                    public void ignoreContact(Entity other, Contact contact) {
                        if(effect.getConstraintStart() + 3 >= playerMapper.get(player).getStrokes()){
                            playerMapper.get(player).removeEffect(effect);
                            contact.setEnabled(true);
                        }
                        else{
                            contact.setEnabled(false);
                        }

                    }
                });
                break;
        }

    }

    private void applyEffectToPlayer(Entity effectApplier, Entity effectReciever){
        List<Effect> effects = playerMapper.get(effectApplier).getEffects();
        for(Effect effect : effects){
            if (effect.getPower() == Power.EXPLODING) {
                Vector2 collisionVector = physicalMapper.get(effectReciever).getPosition();
                Vector2[] explosionShape = new Vector2[]{
                        new Vector2(0, 0),
                        new Vector2(2, 0),
                        new Vector2(2, 2),
                        new Vector2(0,2)
                };
                PolygonShape shape = new PolygonShape();
                shape.set(explosionShape);
                entityFactory.createParticle(collisionVector.x, collisionVector.y, shape);
                physicalMapper.get(effectReciever).setPosition(Game.spawnPosition);
            }
        }
    }



}
