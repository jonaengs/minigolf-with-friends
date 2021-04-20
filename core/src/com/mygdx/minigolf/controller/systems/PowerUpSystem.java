package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.Game;
import com.mygdx.minigolf.controller.ComponentMappers;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.model.Effect;
import com.mygdx.minigolf.model.StrokeConstraint;
import com.mygdx.minigolf.model.UseConstraint;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpGiver;
import com.mygdx.minigolf.util.Constants;

import java.util.List;

public class PowerUpSystem extends EntitySystem {

    private final ImmutableArray<Entity> players;

    public PowerUpSystem(Engine engine){
        super();
        this.players = engine.getEntitiesFor(Family.all(Player.class).get());
    }

    public void update(float dt){
        super.update(dt);
    }

    public void givePowerUp(Entity player, Effect effect){
        ComponentMappers.PlayerMapper.get(player).addEffect(effect);
        if (effect instanceof Effect.ExplodingEffect) {
            ComponentMappers.PhysicalMapper.get(player).addContactListener(new Physical.ContactListener(1) {
                @Override
                public void endContact(Entity other, Contact contact) {
                    if (other.getComponent(Player.class) != null) {
                        UseConstraint constraint = (UseConstraint) effect.getConstraint();
                        constraint.decrementUse();
                        Gdx.app.postRunnable(()->removeExhaustedEffects(this, player));
                    }
                }
                @Override
                public void ignoreContact(Entity other, Contact contact){
                    if (other.getComponent(Player.class) != null) {
                        contact.setEnabled(false);
                        Gdx.app.postRunnable(() ->
                            applyEffectToPlayer(player, other)
                        );
                    }
                }
            });
        } else if (effect instanceof Effect.NoCollisionEffect) {
            StrokeConstraint constraint = (StrokeConstraint) effect.getConstraint();
            constraint.setPlayerStartStroke(ComponentMappers.PlayerMapper.get(player).getStrokes());
            ComponentMappers.PhysicalMapper.get(player).addContactListener(new Physical.ContactListener(1) {
                @Override
                public void ignoreContact(Entity other, Contact contact) {
                    if (effect.getConstraint().powerExhausted(ComponentMappers.PlayerMapper.get(player).getStrokes())) {
                        Gdx.app.postRunnable(()->removeExhaustedEffects(this, player));
                    } else {
                        if(other.getComponent(Physical.class).getFixture().getFilterData().categoryBits == Constants.BIT_OBSTACLE){
                            contact.setEnabled(false);
                        }
                    }

                }
            });
        }

    }

    private void applyEffectToPlayer(Entity effectApplier, Entity effectReciever){
        List<Effect> effects = ComponentMappers.PlayerMapper.get(effectApplier).getEffects();
        System.out.println(effects);
        for(Effect effect : effects){
            if (Effect.ExplodingEffect.class.equals(effect.getClass())) {
                ComponentMappers.PhysicalMapper.get(effectReciever).setPosition(Game.spawnPosition);
            }
        }
    }

    private void removeExhaustedEffects(Physical.ContactListener listener, Entity p){
        ComponentMappers.PhysicalMapper.get(p).removeContactListener(listener);
        for(Entity player : players){
            Player playerComponent = ComponentMappers.PlayerMapper.get(player);
            playerComponent.removeEffects();
        }
    }

}
