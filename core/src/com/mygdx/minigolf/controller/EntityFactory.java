package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.ComponentMappers.PhysicalMapper;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpTaker;
import com.mygdx.minigolf.util.Constants;

import java.util.Arrays;
import java.util.Objects;

public class EntityFactory {

    public final static float DEFAULT_BOUNCE = 0.7f;
    private final Engine engine;
    private final World world;
    public boolean showGraphics;

    static public float[] getVertices(PolygonShape shape) {
        int nVertices = shape.getVertexCount();
        float[] vertices = new float[2 * nVertices];
        Vector2 v = new Vector2();
        for (int i = 0; i < nVertices; i++) {
            shape.getVertex(i, v);
            vertices[2 * i] = v.x;
            vertices[2 * i + 1] = v.y;
        }
        return vertices;
    }

    public EntityFactory(Engine engine, World world) {
        this(engine, world, true);
    }

    public EntityFactory(Engine engine, World world, boolean showGraphics) {
        this.engine = engine;
        this.world = world;
        this.showGraphics = showGraphics;
    }

    private Entity createEntity(Component... components) {
        Entity entity = engine.createEntity();
        Arrays.stream(components).filter(Objects::nonNull).forEach(entity::add);
        engine.addEntity(entity);
        return entity;
    }

    public Entity createPlayer(Vector2 position) {
        return createPlayer(position.x, position.y);
    }

    public Entity createPlayer(float x, float y) {
        CircleShape shape = new CircleShape();
        shape.setRadius(0.15f);
        Physical physical = createPhysical(
                x,
                y,
                shape,
                BodyDef.BodyType.DynamicBody,
                Constants.BIT_PLAYER,
                (short) (Constants.BIT_WALL | Constants.BIT_HOLE | Constants.BIT_POWERUP | Constants.BIT_PLAYER | Constants.BIT_SPAWN),
                false);

        /* Set bounce to 0. This way we can more easily control bounce between the player and other objects.
        E.g. if we set another wall to have 0 bounce, then the player will not bounce at all against it.*/
        physical.setBounce(0);
        physical.setFriction(8f);

        return createEntity(
                physical,
                showGraphics ? new Graphical(Sprite.Player, 1) : null,
                new Player(),
                new PowerUpTaker()
        );
    }

    public Entity createControllablePlayer(float x, float y, OrthographicCamera cam) {
        Entity player = createPlayer(x, y);
        Gdx.input.setInputProcessor(new InputHandler(cam, PhysicalMapper.get(player).getBody()));
        return player;
    }

    public Entity createHole(float x, float y, CircleShape shape) {
        Physical physical = createPhysical(
                x + shape.getRadius(),
                y + shape.getRadius(),
                shape,
                BodyDef.BodyType.StaticBody,
                Constants.BIT_HOLE,
                Constants.BIT_PLAYER,
                false);
        return createEntity(
                physical,
                showGraphics ? new Graphical(Sprite.Hole, 0) : null,
                new Objective(physical)
        );
    }

    public Entity createObstacle(float x, float y, PolygonShape shape) {
        return createEntity(
                createPhysical(
                        x,
                        y,
                        shape,
                        BodyDef.BodyType.StaticBody,
                        Constants.BIT_WALL,
                        Constants.BIT_PLAYER,
                        false),
                showGraphics ? new Graphical(Sprite.Obstacle.color, 1, getVertices(shape)) : null
        );
    }

    public Entity createPowerup(float x, float y, CircleShape shape) {
        return createEntity(
                createPhysical(
                        x + shape.getRadius(),
                        y + shape.getRadius(),
                        shape,
                        BodyDef.BodyType.StaticBody,
                        Constants.BIT_POWERUP,
                        Constants.BIT_PLAYER,
                        true),
                showGraphics ? new Graphical(Sprite.Powerup, 1) : null

        );
    }

    public Entity createSpawn(float x, float y) {
        CircleShape shape = new CircleShape();
        shape.setRadius(1.1f);
        return createEntity(createPhysical(
                x,
                y,
                shape,
                BodyDef.BodyType.StaticBody,
                Constants.BIT_SPAWN,
                Constants.BIT_PLAYER,
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
                        Constants.BIT_WALL,
                        Constants.BIT_PLAYER,
                        false),
                showGraphics ? new Graphical(Sprite.SurfaceB.color, 1, getVertices(shape)) : null

        );
    }

    public Entity createSurface(float x, float y, Sprite sprite, PolygonShape shape, int layer) {
        return createEntity(
                createPhysical(
                        x,
                        y,
                        shape,
                        BodyDef.BodyType.StaticBody,
                        Constants.BIT_COURSE,
                        Constants.BIT_COURSE,
                        false),
            showGraphics ? new Graphical(sprite.color, layer, getVertices(shape)) : null
        );
    }

    public Entity createCourse(float x, float y, PolygonShape shape) {
        return createSurface(x, y, Sprite.SurfaceA, shape, -1);
    }

    private Physical createPhysical(float x, float y, Shape shape, BodyDef.BodyType type, short cBits, short mBits, boolean sensor) {
        BodyDef def = new BodyDef();
        def.type = type;
        def.position.set(x, y);
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
        SurfaceA(Color.GREEN),
        SurfaceB(Color.FIREBRICK),
        Obstacle(Color.FIREBRICK);

        public final Color color;

        Sprite(Color color) {
            this.color = color;
        }
    }

}
