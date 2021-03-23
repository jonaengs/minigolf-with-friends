package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Shape;

public class Physical implements Component {

    private Body body;
    private boolean collidable;

    public void setBody(Body body) {
        this.body = body;
    }

    public Body getBody() {
        return this.body;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void setPosition(Vector2 position) {
        body.getPosition().set(position.x, position.y);
    }

    public Vector2 getVelocity() {
        return this.body.getLinearVelocity();
    }

    public void setVelocity(Vector2 velocity) {
        this.body.setLinearVelocity(velocity);
    }

    public Shape getShape() {
        return this.body.getFixtureList().get(0).getShape();
    }

    public BodyType getBodyType() {
        return this.body.getType();
    }
}
