package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.physics.box2d.Contact;
import com.mygdx.minigolf.controller.GameController;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.levels.LevelLoader;
import com.mygdx.minigolf.model.powerup.Effect;
import com.mygdx.minigolf.model.powerup.StrokeConstraint;
import com.mygdx.minigolf.model.powerup.UseConstraint;
import com.mygdx.minigolf.util.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.util.ComponentMappers.PlayerMapper;
import com.mygdx.minigolf.util.ConcurrencyUtils;
import com.mygdx.minigolf.util.Constants;

import java.util.List;


public class PowerUpSystem extends EntitySystem {
    private LevelLoader.Level currentLevel;

    public void setLevel(LevelLoader.Level level) {
        currentLevel = level;
    }

    public void update(float dt) {
        super.update(dt);
    }

    public void givePowerUp(Entity player, Effect effect) {
        PlayerMapper.get(player).addEffect(effect);
        if (effect instanceof Effect.ExplodingEffect) {
            PhysicalMapper.get(player).addContactListener(new Physical.ContactListener(1) {
                @Override
                public void ignoreContact(Entity colliding, Contact contact) {
                    if (PlayerMapper.get(colliding) != null) {
                        contact.setEnabled(false);
                    }
                }

                @Override
                public void endContact(Entity colliding, Contact contact) {
                    if (PlayerMapper.get(colliding) != null) {
                        ((UseConstraint) effect.getConstraint()).decrementUse();
                        ConcurrencyUtils.postRunnable(() -> {
                            List<Effect> effects = PlayerMapper.get(player).getEffects();
                            for (Effect effect : effects) {
                                if (effect instanceof Effect.ExplodingEffect)
                                    GameController.resetPhysicals(colliding, currentLevel);
                            }
                            removeExhaustedEffects(this, player);
                        });
                    }
                }
            });
        } else if (effect instanceof Effect.NoCollisionEffect) {
            StrokeConstraint constraint = (StrokeConstraint) effect.getConstraint();
            constraint.setPlayerStartStroke(PlayerMapper.get(player).getStrokes());
            PhysicalMapper.get(player).addContactListener(new Physical.ContactListener(1) {
                @Override
                public void ignoreContact(Entity other, Contact contact) {
                    if (effect.getConstraint().powerExhausted(PlayerMapper.get(player).getStrokes())) {
                        ConcurrencyUtils.postRunnable(() -> removeExhaustedEffects(this, player));
                    } else {
                        if (PhysicalMapper.get(other).getFixture().getFilterData().categoryBits == Constants.BIT_OBSTACLE) {
                            contact.setEnabled(false);
                        }
                    }

                }
            });
        }
    }

    private void removeExhaustedEffects(Physical.ContactListener listener, Entity p) {
        PhysicalMapper.get(p).removeContactListener(listener);
        for (Entity player : this.getEngine().getEntitiesFor(Family.all(Player.class).get())) {
            Player playerComponent = PlayerMapper.get(player);
            playerComponent.removeEffects();
        }
    }
}
