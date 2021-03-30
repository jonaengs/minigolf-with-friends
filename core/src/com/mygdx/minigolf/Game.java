package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.controller.systems.Physics;
import com.mygdx.minigolf.controller.systems.PhysicsDebugSystem;
import com.mygdx.minigolf.model.components.Physical;

public class Game extends ApplicationAdapter {

    Engine engine;
    World world;
    EntityFactory factory;

    @Override
    public void create() {
        engine = new Engine();
        world = new World(new Vector2(0, 0), true);

        GraphicsSystem graphicsSystem = new GraphicsSystem();

        engine.addSystem(graphicsSystem);
        engine.addSystem(new Physics(world));
        // engine.addSystem(new PhysicsDebugSystem(world, graphicsSystem.getCam()));

        factory = new EntityFactory(engine, world, graphicsSystem.getCam());

        // --- Start dummy demo code ---
        Entity player = factory.createPlayer(6, 10, true);

        Vector2[] triangle = new Vector2[]{
                new Vector2(0, 0),
                new Vector2(2, 0),
                new Vector2(2, 1),
        };
        factory.createObstacle(5, 5, triangle);

        Vector2[] surfaceShape = new Vector2[]{
                new Vector2(-1, -1),
                new Vector2(12, -1),
                new Vector2(12, 24),
                new Vector2(-1, 24),
        };

        Entity surface = factory.createSurface(0, 0, EntityFactory.Sprite.SurfaceA, surfaceShape);

        // Friction between surface and player
        Physical playerPhys = player.getComponent(Physical.class);
        Physical surfacePhys = surface.getComponent(Physical.class);

        FrictionJointDef frictionJointDef = new FrictionJointDef();
        frictionJointDef.initialize(surfacePhys.getBody(), playerPhys.getBody(), new Vector2(0, 0));
        frictionJointDef.maxForce = 10.0f;
        world.createJoint(frictionJointDef);

        // --- End dummy demo code ---

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
