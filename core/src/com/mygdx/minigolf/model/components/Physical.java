package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;

public class Physical implements Component {

    private Body body;
    private boolean collidable; // TODO: handle this

    public Physical(Body body) {
        this.body = body;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
    }

    public float getBounce() {
        return this.body.getFixtureList().get(0).getRestitution();
    }

    /**
     * Set bounce [0, 1].<br>
     * 1 => 100% bounce, would never stop bouncing on a flat surface.<br>
     * 0 => No bounce.
     *
     * @param bounce Float from 0 to 1.
     */
    public void setBounce(float bounce) {
        this.body.getFixtureList().get(0).setRestitution(bounce);
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

}
