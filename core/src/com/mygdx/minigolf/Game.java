package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.controller.systems.Physics;
import com.mygdx.minigolf.controller.systems.PowerUpSystem;
import com.mygdx.minigolf.model.Constraint;
import com.mygdx.minigolf.model.ConstraintType;
import com.mygdx.minigolf.model.Effect;
import com.mygdx.minigolf.model.Power;
import com.mygdx.minigolf.model.components.Physical;

import javax.xml.bind.ValidationEvent;

public class Game extends ApplicationAdapter {

    Engine engine;
    World world;
    EntityFactory factory;
    public static final Vector2 spawnPosition = new Vector2(0,0);

    @Override
    public void create() {
        engine = new Engine();
        world = new World(new Vector2(0, 0), true);


        GraphicsSystem graphicsSystem = new GraphicsSystem();
        factory = new EntityFactory(engine, world, graphicsSystem.getCam());

        engine.addSystem(graphicsSystem);
        engine.addSystem(new Physics(world, engine));
        engine.addSystem(new PowerUpSystem(engine, factory, world));
        // engine.addSystem(new PhysicsDebugSystem(world, graphicsSystem.getCam()));

        // --- Scenario 1: Exploding power up ---
        factory.createPlayer(9, 12, false);

        //Creating a power up effect with a constraint
        Constraint constraint = new Constraint(ConstraintType.USES, 1).setStart();
        Effect effect = new Effect(Power.EXPLODING, constraint);
        Vector2[] powerupShape = new Vector2[]{
                new Vector2(0,0),
                new Vector2(2,0),
                new Vector2(1,1),
        };
        PolygonShape ps = new PolygonShape();
        ps.set(powerupShape);
        factory.createPowerup(9, 9, ps, effect);


        factory.createPlayer(9,5, true);

        // --- Scenario 2: No collision power-up ----
        factory.createPlayer(12,5, true);







    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        engine.update(Gdx.graphics.getDeltaTime()); // TODO: Move stuff to GameView
    }

    @Override
    public void dispose() {
    }
}