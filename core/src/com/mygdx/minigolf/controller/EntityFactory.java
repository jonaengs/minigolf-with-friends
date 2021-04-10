package com.mygdx.minigolf.controller;
import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.systems.PowerUpSystem;
import com.mygdx.minigolf.model.Effect;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;

import java.util.Arrays;

public class EntityFactory {

    public final static float DEFAULT_BOUNCE = 0.7f;

    public static enum Sprite {
        Player("/sprite/player.png"),
        Hole("/sprite/hole.png"),
        Powerup("/sprite/powerup.png"),
        SurfaceA("/sprite/surface-a.png"),
        SurfaceB("/sprite/surface-b.png"),
        Obstacle("/sprite/obstacle.png"),
        Particle("/sprite/particle.png");

        public final String path;
        public final Texture texture;
        public final com.badlogic.gdx.graphics.g2d.Sprite sprite;

        Sprite(String path) {
            this.path = path;
            // texture = new Texture(path);
            texture = new Texture("badlogic.jpg"); // TODO: load actual path, temp to suppress errors
            sprite = new com.badlogic.gdx.graphics.g2d.Sprite(texture);
        }

        @Override
        public String toString() {
            return path;
        }
    }

    private final Engine engine;
    private final World world;
    private final OrthographicCamera cam;

    public EntityFactory(Engine engine, World world, OrthographicCamera cam) {
        this.engine = engine;
        this.world = world;
        this.cam = cam;
    }

    private Entity createEntity(Component... components) {
        Entity entity = engine.createEntity();
        Arrays.stream(components).forEach(c -> entity.add(c));
        engine.addEntity(entity);
        return entity;
    }

    public Entity createPlayer(float x, float y, boolean controllable) {
        Entity entity = engine.createEntity();

        CircleShape shape = new CircleShape();
        shape.setRadius(0.15f);
        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.DynamicBody);
        /*
        Set bounce to 0. This way we can more easily control bounce between the player and other objects.
        E.g. if we set another wall to have 0 bounce, then the player will not bounce at all against it.
         */
        physical.setBounce(0);
        physical.addContactListener(new Physical.ContactListener(1) {

        });
        entity.add(physical);

        entity.add(new Graphical(Sprite.Player, 0));

        if (controllable) {
            Gdx.input.setInputProcessor(new InputHandler(cam, physical.getBody()));
        }

        entity.add(new Player());

        engine.addEntity(entity);
        return entity;
    }

    public Entity createHole(float x, float y) {
        Entity entity = engine.createEntity();

        CircleShape shape = new CircleShape();
        shape.setRadius(0.15f);
        entity.add(createPhysical(x, y, shape, BodyDef.BodyType.StaticBody));

        entity.add(new Graphical(Sprite.Hole.sprite, 0));
        entity.add(new Objective());

        engine.addEntity(entity);
        return entity;
    }

    public Entity createObstacle(float x, float y, Vector2... points) {
        Entity entity = engine.createEntity();

        PolygonShape shape = new PolygonShape();
        shape.set(points);
        entity.add(createPhysical(x, y, shape, BodyDef.BodyType.StaticBody));

        entity.add(new Graphical(Sprite.Obstacle.sprite, 0));

        engine.addEntity(entity);
        return entity;
    }

    public Entity createPowerup(float x, float y, PolygonShape shape,  Effect effect) {
        Entity entity = engine.createEntity();

        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.StaticBody);

        physical.addContactListener(new Physical.ContactListener(1) {
            @Override
            public void beginContact(Entity other, Contact contact) {
                engine.getSystem(PowerUpSystem.class).givePowerUp(other, effect);
            }
        });

        entity.add(physical);

        entity.add(new Graphical(Sprite.Powerup, 0));

        engine.addEntity(entity);
        return entity;
    }

    public Entity createSpawn(float x, float y) {
        Entity entity = engine.createEntity();

        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);
        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.StaticBody);
        entity.add(physical);

        engine.addEntity(entity);
        return entity;
    }

    public Entity createSurface(float x, float y, Sprite sprite, Vector2... points) {
        Entity entity = engine.createEntity();

        PolygonShape shape = new PolygonShape();
        shape.set(points);
        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.StaticBody);
        entity.add(physical);

        entity.add(new Graphical(sprite, 0));

        engine.addEntity(entity);
        return entity;
    }

    public Entity createParticle(float x, float y, Vector2... points) {
        Entity entity = engine.createEntity();

        PolygonShape shape = new PolygonShape();
        shape.set(points);
        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.StaticBody);

        entity.add(physical);
        entity.add(new Graphical(Sprite.Particle, 0));

        engine.addEntity(entity);
        return entity;
    }

    private Physical createPhysical(float x, float y, Shape shape, BodyDef.BodyType type) {
        BodyDef def = new BodyDef();
        def.type = type;
        def.position.set(x, y);
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.restitution = DEFAULT_BOUNCE;
        Body body = world.createBody(def);
        body.createFixture(fix);
        return new Physical(body);
    }

}

