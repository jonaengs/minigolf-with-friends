package com.mygdx.minigolf.controller;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Objective;
import com.mygdx.minigolf.model.components.Physical;
import com.mygdx.minigolf.model.components.Player;
import com.mygdx.minigolf.model.components.PowerUpTaker;

import java.util.Arrays;

public class EntityFactory {

    public final static float DEFAULT_BOUNCE = 0.7f;

    public static enum Sprite {
        Player("/sprite/player.png"),
        Hole("/sprite/hole.png"),
        Powerup("/sprite/powerup.png"),
        SurfaceA("/sprite/surface-a.png"),
        SurfaceB("/sprite/surface-b.png"),
        Obstacle("/sprite/obstacle.png");

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

    public EntityFactory(Engine engine, World world) {
        this.engine = engine;
        this.world = world;
    }

    private Entity createEntity(Component... components) {
        Entity entity = engine.createEntity();
        Arrays.stream(components).forEach(entity::add);
        engine.addEntity(entity);
        return entity;
    }

    public Entity createPlayer(float x, float y, boolean controllable) {
        CircleShape shape = new CircleShape();
        shape.setRadius(0.15f);
        Physical physical = createPhysical(x, y, shape, BodyDef.BodyType.DynamicBody);

        /* Set bounce to 0. This way we can more easily control bounce between the player and other objects.
        E.g. if we set another wall to have 0 bounce, then the player will not bounce at all against it.*/
        physical.setBounce(0);

        if (controllable) {
            // TODO: add input controller
        }

        return createEntity(
                physical,
                new Graphical(Sprite.Player, 1),
                new Player(),
                new PowerUpTaker()
        );
    }

    public Entity createHole(float x, float y) {
        CircleShape shape = new CircleShape();
        shape.setRadius(0.15f);

        return createEntity(
                createPhysical(x, y, shape, BodyDef.BodyType.StaticBody),
                new Graphical(Sprite.Hole.sprite, 0),
                new Objective()
        );
    }

    public Entity createObstacle(float x, float y, Vector2... points) {
        PolygonShape shape = new PolygonShape();
        shape.set(points);
        return createObstacle(x, y, shape);
    }

    public Entity createObstacle(float x, float y, Shape shape) {
        return createEntity(
                createPhysical(x, y, shape, BodyDef.BodyType.StaticBody),
                new Graphical(Sprite.Obstacle.sprite, 1)
        );
    }

    public Entity createPowerup(float x, float y) {
        Shape shape = new CircleShape();
        shape.setRadius(1f);
        return createEntity(
                createPhysical(x, y, shape, BodyDef.BodyType.StaticBody),
                new Graphical(Sprite.Powerup, 1)
        );
    }

    public Entity createSpawn(float x, float y) {
        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);

        return createEntity(createPhysical(x, y, shape, BodyDef.BodyType.StaticBody));
    }


    public Entity createSurface(float x, float y, Sprite sprite, Vector2... points) {
        PolygonShape shape = new PolygonShape();
        shape.set(points);
        return createSurface(x, y, sprite, shape);
    }

    public Entity createSurface(float x, float y, Sprite sprite, Shape shape) {
        return createSurface(x, y, sprite, shape, 0);
    }

    public Entity createSurface(float x, float y, Sprite sprite, Shape shape, int layer) {
        return createEntity(
                createPhysical(x, y, shape, BodyDef.BodyType.StaticBody),
                new Graphical(sprite, layer)
        );
    }

    public Entity createCourse(float width, float height) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height);
        return createSurface(0, 0, Sprite.SurfaceA, shape, -1);
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
