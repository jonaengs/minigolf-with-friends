package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;

public class Physical implements Component {

    private Body body;
    private boolean collidable; // TODO: handle this

    public Physical(Body body) {
        this.body = body;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Fixture getFixture() {
        return this.body.getFixtureList().get(0);
    }

    public boolean isCollidable() {
        return collidable;
    }

    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
    }

    public float getBounce() {
        return this.getFixture().getRestitution();
    }

    /**
     * Set bounce [0, 1].<br>
     * 1 => 100% bounce, would never stop bouncing on a flat surface.<br>
     * 0 => No bounce.
     *
     * @param bounce Float from 0 to 1.
     */
    public void setBounce(float bounce) {
        this.getFixture().setRestitution(bounce);
    }

    public Vector2 getVelocity() {
        return this.body.getLinearVelocity();
    }

    public void setVelocity(Vector2 velocity) {
        this.body.setLinearVelocity(velocity);
    }

    public Vector2 getPosition() {
        return this.body.getPosition();
    }

    public void setPosition(Vector2 position) {
        this.body.getPosition().set(position.x, position.y);
    }

    public Shape getShape() {
        return this.getFixture().getShape();
    }

    public BodyDef.BodyType getType() {
        return body.getType();
    }

    public float getFriction() {
        return this.getFixture().getFriction();
    }

    public void setFriction(float friction) {
        this.getFixture().setFriction(friction);
    }

    public float getDensity() {
        return this.getFixture().getDensity();
    }

    public void setDensity(float density) {
        this.getFixture().setDensity(density);
    }

}
