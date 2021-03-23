package com.mygdx.minigolf;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.model.components.Graphical;
import com.mygdx.minigolf.model.components.Physical;

public class Game extends ApplicationAdapter {

    private World world;
    public static final Vector2 spawnPosition = new Vector2(0,0);

    private SpriteBatch batch;
    private Engine engine;
    private EntityFactory entityFactory;

    private final ComponentMapper<Physical> physicalMapper = ComponentMapper.getFor(Physical.class);
    private final ComponentMapper<Graphical> graphicalMapper = ComponentMapper.getFor(Graphical.class);

    @Override
    public void create() {
        world = new World(new Vector2(0, 0), true);

        batch = new SpriteBatch();

        GraphicsSystem graphicsSystem = new GraphicsSystem(batch);

        engine = new Engine();
        engine.addSystem(graphicsSystem);

        entityFactory = new EntityFactory(engine);

        // Create a golf ball and obstacle for testing purposes
        createGolfBall();
        createObstacle();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(Gdx.graphics.getDeltaTime());

        world.step(1 / 60f, 6, 2);
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
    }

    private void createGolfBall() {
        Entity golfBall = entityFactory.createEntity(EntityFactory.EntityType.GOLFBALL);

        Physical physical = physicalMapper.get(golfBall);

        // Create body for ball
        BodyDef ballBodyDef = new BodyDef();
        ballBodyDef.type = BodyDef.BodyType.DynamicBody;
        ballBodyDef.position.set(1, 1);

        Body ballBody = world.createBody(ballBodyDef);
        CircleShape circle = new CircleShape();
        circle.setRadius(0.15f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        ballBody.createFixture(fixtureDef);
        circle.dispose();

        // Add ball body to physical component
        physical.setBody(ballBody);

        Graphical graphical = graphicalMapper.get(golfBall);
        graphical.setLayer(1);
    }

    private void createObstacle() {
        Entity obstacle = entityFactory.createEntity(EntityFactory.EntityType.OBSTACLE);

        Physical physical = physicalMapper.get(obstacle);

        // Create body for obstacle
        BodyDef obstacleBodyDef = new BodyDef();
        obstacleBodyDef.type = BodyDef.BodyType.StaticBody;
        obstacleBodyDef.position.set(2, 4);

        Body obstacleBody = world.createBody(obstacleBodyDef);
        PolygonShape poly = new PolygonShape();
        poly.setAsBox(3f / 2, 2f / 2);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = poly;
        obstacleBody.createFixture(fixtureDef);
        poly.dispose();

        // Add obstacle body to physical component
        physical.setBody(obstacleBody);

        Graphical graphical = graphicalMapper.get(obstacle);
        graphical.setLayer(1);
        graphical.setWidth(3);
        graphical.setHeight(2);
    }
}
