package com.mygdx.minigolf.controller.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.model.components.Physical;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class Physics extends IteratingSystem implements ContactListener, EntityListener {

    private static final float MAX_STEP_TIME = 1 / 30f;
    private static float accumulator = 0f;

    private World world;
    private Engine engine;
    private ComponentMapper<Physical> mapper = ComponentMapper.getFor(Physical.class);
    private Map<Body, Entity> cache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public Physics(World world, Engine engine) {
        super(Family.all(Physical.class).get());
        this.world = world;
        this.engine = engine;
        world.setContactListener(this);
        engine.addEntityListener(this.getFamily(), this);
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
    }

    @Override
    public void beginContact(Contact contact) {
        callListeners(contact, e -> e.getKey().beginContact(e.getValue(), contact));
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

    private void callListeners(Contact contact, Consumer<Map.Entry<Physical.ContactListener, Entity>> func) {
        Entity entityA = getEntity(contact.getFixtureA().getBody());
        mapper.get(entityA).getContactListeners().forEach(listener -> func.accept(new AbstractMap.SimpleEntry(listener, entityA)));
        Entity entityB = getEntity(contact.getFixtureB().getBody());
        mapper.get(entityB).getContactListeners().forEach(listener -> func.accept(new AbstractMap.SimpleEntry(listener, entityB)));
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
