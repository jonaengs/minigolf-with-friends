package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.FrictionJoint;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.util.Constants;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class Physics extends IteratingSystem implements ContactListener, EntityListener {

    private static final float MAX_STEP_TIME = 1 / 30f;
    private static float accumulator = 0f;

    private final World world;
    private final Engine engine;
    private final ComponentMapper<Physical> mapper = ComponentMapper.getFor(Physical.class);
    private final Map<Body, Entity> cache = new HashMap<>();

    // A common friction body to be used by all bodies that want friction
    public final Body frictionBody;

    @SuppressWarnings("unchecked")
    public Physics(World world, Engine engine) {
        super(Family.all(Physical.class).get());
        this.world = world;
        this.engine = engine;
        world.setContactListener(this);
        engine.addEntityListener(this.getFamily(), this);
        this.frictionBody = world.createBody(new BodyDef());
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        if (accumulator >= MAX_STEP_TIME) {
            world.step(MAX_STEP_TIME, 6, 2);
            accumulator -= MAX_STEP_TIME;
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Physical physical = mapper.get(entity);

        // Check if a new friction value has been set
        if (physical != null && physical.getFriction() != physical.getBodyFriction()) {
            // Destroy old friction joint
            if (physical.getFrictionJoint() != null) {
                world.destroyJoint(physical.getFrictionJoint());
            }

            // Create new friction joint with new friction value
            FrictionJointDef frictionJointDef = new FrictionJointDef();
            frictionJointDef.initialize(frictionBody, physical.getBody(), new Vector2(0, 0));
            frictionJointDef.maxForce = physical.getFriction();
            FrictionJoint frictionJoint = (FrictionJoint) world.createJoint(frictionJointDef);

            physical.setFrictionJoint(frictionJoint);
        }
    }

    @Override
    public void beginContact(Contact contact) {
        callListeners(contact, e -> e.getKey().beginContact(e.getValue(), contact));
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();

        if(fa.isSensor() && contact.isTouching()) {
            if(fa.getFilterData().categoryBits == Constants.BIT_HOLE) {
                System.out.println("Hole sensor triggered");
                // Hole finished
            }
            else if(fa.getFilterData().categoryBits == Constants.BIT_POWERUP) {
                System.out.println("Powerup sensor triggered");
                // Add power-up to user
            }
            else if(fa.getFilterData().categoryBits == Constants.BIT_SPAWN) {
                System.out.println("Spawn sensor triggered");
                // Remove player collision here
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        callListeners(contact, e -> e.getKey().endContact(e.getValue(), contact));
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        callListeners(contact, e -> e.getKey().ignoreContact(e.getValue(), contact));
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    @SuppressWarnings("unchecked")
    private void callListeners(Contact contact, Consumer<Map.Entry<Physical.ContactListener, Entity>> func) {
        Entity entityA = getEntity(contact.getFixtureA().getBody());
        Entity entityB = getEntity(contact.getFixtureB().getBody());
        mapper.get(entityA).getContactListeners().forEach(listener -> func.accept(new AbstractMap.SimpleEntry(listener, entityB)));
        mapper.get(entityB).getContactListeners().forEach(listener -> func.accept(new AbstractMap.SimpleEntry(listener, entityA)));
    }

    public Entity getEntity(Body body) {
        return cache.computeIfAbsent(body, b -> StreamSupport
                .stream(engine.getEntitiesFor(this.getFamily()).spliterator(), true)
                .filter(e -> mapper.get(e).getBody().equals(body))
                .findFirst().get());
    }

    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
        world.destroyBody(mapper.get(entity).getBody());
    }
}
