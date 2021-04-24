package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ShortArray;
import com.mygdx.minigolf.controller.systems.PowerUpSystem;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpGiver;
import com.mygdx.minigolf.model.powerup.Effect;
import com.mygdx.minigolf.util.ComponentMappers.PowerUpGiverMapper;

import java.util.Arrays;
import java.util.Objects;

import static com.mygdx.minigolf.util.Constants.BIT_COURSE;
import static com.mygdx.minigolf.util.Constants.BIT_HOLE;
import static com.mygdx.minigolf.util.Constants.BIT_OBSTACLE;
import static com.mygdx.minigolf.util.Constants.BIT_PLAYER;
import static com.mygdx.minigolf.util.Constants.BIT_POWERUP;
import static com.mygdx.minigolf.util.Constants.BIT_SPAWN;
import static com.mygdx.minigolf.util.Constants.BIT_WALL;

public class EntityFactory {

    public final static float DEFAULT_BOUNCE = 0.7f;
    private static final EarClippingTriangulator triangulator = new EarClippingTriangulator();
    private final Engine engine;
    private final World world;
    public boolean showGraphics;

    public EntityFactory(Engine engine, World world) {
        this(engine, world, true);
    }

    public EntityFactory(Engine engine, World world, boolean showGraphics) {
        this.engine = engine;
        this.world = world;
        this.showGraphics = showGraphics;
    }

    static public float[] getTriangles(PolygonShape shape) {
        int nVertices = shape.getVertexCount();
        float[] vertices = new float[2 * nVertices];
        Vector2 v = new Vector2();
        for (int i = 0; i < nVertices; i++) {
            shape.getVertex(i, v);
            vertices[2 * i] = v.x;
            vertices[2 * i + 1] = v.y;
        }

        ShortArray triangleIndices = triangulator.computeTriangles(vertices);

        float[] triangles = new float[triangleIndices.size / 3 * 6];

        for (int i = 0; i < triangleIndices.size; i += 3) {
            triangles[i * 2] = vertices[triangleIndices.get(i) * 2];
            triangles[i * 2 + 1] = vertices[triangleIndices.get(i) * 2 + 1];
            triangles[i * 2 + 2] = vertices[triangleIndices.get(i + 1) * 2];
            triangles[i * 2 + 3] = vertices[triangleIndices.get(i + 1) * 2 + 1];
            triangles[i * 2 + 4] = vertices[triangleIndices.get(i + 2) * 2];
            triangles[i * 2 + 5] = vertices[triangleIndices.get(i + 2) * 2 + 1];
        }

        return triangles;
    }

    private Entity createEntity(Component... components) {
        Entity entity = engine.createEntity();
        Arrays.stream(components).filter(Objects::nonNull).forEach(entity::add);
        engine.addEntity(entity);
        return entity;
    }

    public Entity createPlayer(float x, float y) {
        return createPlayer(x, y, null);
    }

    public Entity createPlayer(float x, float y, String name) {
        CircleShape shape = new CircleShape();
        shape.setRadius(0.15f);
        Physical physical = createPhysical(
                x,
                y,
                shape,
                BodyDef.BodyType.DynamicBody,
                BIT_PLAYER,
                (short) (BIT_WALL | BIT_HOLE | BIT_POWERUP | BIT_PLAYER | BIT_OBSTACLE),
                false,
                true);

        /* Set bounce to 0. This way we can more easily control bounce between the player and other objects.
        E.g. if we set another wall to have 0 bounce, then the player will not bounce at all against it.*/
        physical.setBounce(0);
        physical.setFriction(8f);

        return createEntity(
                physical,
                showGraphics ? new Graphical(Sprite.Player, 2) : null,
                new Player(name)
        );
    }

    public Entity createInputDirectionIndicator(Vector2 p) {
        return createInputDirectionIndicator(p.x, p.y);
    }

    public Entity createInputDirectionIndicator(float x, float y) {
        float[] vertices = {-0.15f, -0.4f, 0.15f, -0.4f, 0f, -0.8f};

        PolygonShape shape = new PolygonShape();
        shape.set(vertices);
        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.KinematicBody,
                (short) 0, (short) 0, false, false);

        return createEntity(
                physical,
                new Graphical(Sprite.DirectionIndicator.color, 2, vertices)
        );
    }

    public Entity createInputStrengthIndicator(Vector2 p) {
        return createInputStrengthIndicator(p.x, p.y);
    }

    public Entity createInputStrengthIndicator(float x, float y) {
        float[] vertices = {-0.07f, -0.4f, 0.07f, -0.4f, 0f, -0.41f};

        PolygonShape shape = new PolygonShape();
        shape.set(vertices);
        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.KinematicBody,
                (short) 0, (short) 0, false, false);

        return createEntity(
                physical,
                new Graphical(Sprite.StrengthIndicator.color, 2, vertices)
        );
    }

    public Entity createHole(float x, float y, CircleShape shape) {
        Physical physical = createPhysical(x + shape.getRadius(), y + shape.getRadius(),
                shape, BodyDef.BodyType.StaticBody, BIT_HOLE,
                BIT_PLAYER, false, true);

        return createEntity(
                physical,
                new Graphical(Sprite.Hole, 0),
                new Objective(physical)
        );
    }

    public Entity createObstacle(float x, float y, PolygonShape shape) {
        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.StaticBody,
                BIT_OBSTACLE, BIT_PLAYER, false, true);

        return createEntity(
                physical,
                showGraphics ? new Graphical(Sprite.Obstacle.color, 1, getTriangles(shape)) : null
        );
    }

    private Entity createPowerup(float x, float y, CircleShape shape, Effect effect) {
        Physical physical = createPhysical(
                x + shape.getRadius(),
                y + shape.getRadius(),
                shape,
                BodyDef.BodyType.StaticBody,
                BIT_POWERUP,
                BIT_PLAYER,
                true,
                true);

        Graphical graphical = null;
        if (showGraphics) {
            Sprite sprite;
            if (effect instanceof Effect.ExplodingEffect)
                sprite = Sprite.ExplodingPowerup;
            else if (effect instanceof Effect.NoCollisionEffect)
                sprite = Sprite.NoCollisionPowerup;
            else
                sprite = Sprite.Powerup;
            graphical = new Graphical(sprite, 1);
        }

        Entity entity = createEntity(
                physical,
                graphical,
                new PowerUpGiver(effect)
        );

        physical.addContactListener(new Physical.ContactListener(1) {
            @Override
            public void beginContact(Entity other, Contact contact) {
                engine.getSystem(PowerUpSystem.class).givePowerUp(other, PowerUpGiverMapper.get(entity).getPowerup());
                engine.removeEntity(entity);
            }
        });

        return entity;
    }

    public Entity createExplodingPowerup(float x, float y, CircleShape shape) {
        return createPowerup(x, y, shape, new Effect.ExplodingEffect());
    }

    public Entity createNoCollisionPowerUp(float x, float y, CircleShape shape) {
        return createPowerup(x, y, shape, new Effect.NoCollisionEffect());
    }

    public Entity createSpawn(float x, float y) {
        CircleShape shape = new CircleShape();
        shape.setRadius(1.1f);
        return createEntity(createPhysical(
                x,
                y,
                shape,
                BodyDef.BodyType.StaticBody,
                BIT_SPAWN,
                BIT_PLAYER,
                true,
                true));
    }

    public Entity createSurface(float x, float y, Sprite sprite, PolygonShape shape) {
        return createSurface(x, y, sprite, shape, 0);
    }

    public Entity createWall(float x, float y, PolygonShape shape) {
        return createEntity(
                createPhysical(
                        x,
                        y,
                        shape,
                        BodyDef.BodyType.StaticBody,
                        BIT_WALL,
                        BIT_PLAYER,
                        false,
                        true),
                showGraphics ? new Graphical(Sprite.Wall.color, 1, getTriangles(shape)) : null
        );
    }

    public Entity createSurface(float x, float y, Sprite sprite, PolygonShape shape, int layer) {
        return createEntity(
                createPhysical(
                        x,
                        y,
                        shape,
                        BodyDef.BodyType.StaticBody,
                        BIT_COURSE,
                        (short) 0, // this was BIT_COURSE before. Why?
                        false,
                        true),
                showGraphics ? new Graphical(sprite.color, layer, getTriangles(shape)) : null
        );
    }

    public Entity createCourse(float x, float y, PolygonShape shape) {
        return createSurface(x, y, Sprite.SurfaceA, shape, -1);
    }

    private Physical createPhysical(float x, float y, Shape shape, BodyDef.BodyType type,
                                    short cBits, short mBits, boolean sensor, boolean active) {
        BodyDef def = new BodyDef();
        def.type = type;
        def.position.set(x, y);
        def.active = active;
        FixtureDef fix = new FixtureDef();
        fix.shape = shape;
        fix.filter.categoryBits = cBits;
        fix.filter.maskBits = mBits;
        fix.isSensor = sensor;
        fix.restitution = DEFAULT_BOUNCE;
        Body body = world.createBody(def);
        body.createFixture(fix);
        return new Physical(body);
    }

    public enum Sprite {
        Player(Color.WHITE),
        Hole(Color.BLACK),
        Powerup(Color.BLUE),
        ExplodingPowerup(Color.RED),
        NoCollisionPowerup(Color.BLUE),
        SurfaceA(new Color(66 / 255f, 134 / 255f, 0f, 1f)),
        Wall(new Color(83 / 255f, 42 / 255f, 0f, 1f)),
        Obstacle(new Color(83 / 255f, 42 / 255f, 0f, 1f)),
        DirectionIndicator(new Color(1f, 255 / 215f, 0f, 0.9f)),
        StrengthIndicator(new Color(1f, 1f, 1f, 0.5f));

        public final Color color;

        Sprite(Color color) {
            this.color = color;
        }
    }

}
