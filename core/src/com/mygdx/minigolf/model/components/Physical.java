package com.mygdx.minigolf.model.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.joints.FrictionJoint;
import com.mygdx.minigolf.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Physical implements Component {

    private Body body;
    private float friction;
    private FrictionJoint frictionJoint = null;
    private final List<ContactListener> contactListeners = new ArrayList<>();

    public Physical(Body body) {
        this.body = body;
        this.friction = 0f;
    }

    public Physical(Body body, float friction) {
        this.body = body;
        this.friction = friction;
    }

    public boolean isMoving() {
        return !getVelocity().isZero(Constants.MOVING_MARGIN);
    }

    public Body getBody() {
        return body;
    }

    public Fixture getFixture() {
        return this.body.getFixtureList().get(0);
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

    public void setVelocity(float x, float y) {
        this.body.setLinearVelocity(x, y);
    }

    public Vector2 getPosition() {
        return this.body.getPosition();
    }


    public void setPosition(Vector2 position) {
        this.body.setTransform(position, this.body.getAngle());
    }

    public void moveTowards(Vector2 position) {
        Vector2 diff = position.sub(body.getPosition());
        diff.scl(5);
        body.setLinearVelocity(body.getLinearVelocity().add(diff));
    }

    public boolean isActive() {
        return this.body.isActive();
    }

    public float getAngle() {
        return (float) Math.toDegrees(this.body.getAngle());
    }

    public Shape getShape() {
        return this.getFixture().getShape();
    }

    public BodyDef.BodyType getType() {
        return body.getType();
    }

    public float getFriction() {
        return this.friction;
    }

    public float getBodyFriction() {
        if (frictionJoint != null) {
            return frictionJoint.getMaxForce();
        }

        return 0f;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public FrictionJoint getFrictionJoint() {
        return frictionJoint;
    }

    public void setFrictionJoint(FrictionJoint frictionJoint) {
        this.frictionJoint = frictionJoint;
    }

    public void addContactListener(ContactListener listener) {
        contactListeners.add(listener);
        contactListeners.sort(Comparator.comparingInt(a -> a.priority));
    }

    public void removeContactListener(ContactListener listener) {
        contactListeners.remove(listener);
    }

    public List<ContactListener> getContactListeners() {
        return Collections.unmodifiableList(contactListeners);
    }

    public static class ContactListener {

        public final int priority;

        public ContactListener(int priority) {
            this.priority = priority;
        }

        public void beginContact(Entity other, Contact contact) {
        }

        public void endContact(Entity other, Contact contact) {
        }

        /**
         * Called before contact happens. Set contact. Set <code>contact.setEnabled(false);</code> to ignore this contact.
         */
        public void ignoreContact(Entity other, Contact contact) {
        }
    }

}
