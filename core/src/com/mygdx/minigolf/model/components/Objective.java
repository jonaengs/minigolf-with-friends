package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.mygdx.minigolf.util.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.util.ComponentMappers.PlayerMapper;
import com.mygdx.minigolf.model.components.Physical.ContactListener;
import com.mygdx.minigolf.util.Constants;

public class Objective extends ContactListener implements Component {

    private Physical physical;

    public Objective(Physical physical) {
        super(20);
        this.physical = physical;
        physical.addContactListener(this);
    }

    @Override
    public void ignoreContact(Entity other, Contact contact) {
        contact.setEnabled(false);
        Player player = PlayerMapper.get(other);
        Physical physical = PhysicalMapper.get(other);
        if (player != null && physical != null) {
            if (!player.isCompleted() &&
                    physical.getVelocity().isZero(Constants.MOVING_MARGIN) &&
                    physical.getPosition().sub(this.physical.getPosition()).isZero(Constants.MOVING_MARGIN)) {
                player.setCompleted(true);
            } else {
                Vector2 force = this.physical.getPosition().sub(physical.getPosition());
                physical.getBody().applyLinearImpulse(force, physical.getBody().getLocalCenter(), true);
            }
        }
    }

}
