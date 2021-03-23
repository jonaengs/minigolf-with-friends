package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.Physics;
import com.mygdx.minigolf.controller.systems.PhysicsDebugSystem;

public class Game extends ApplicationAdapter {

    Engine engine;
    EntityFactory factory;

    @Override
    public void create() {
        engine = new Engine();
        World world = new World(new Vector2(0, -10), true);
        OrthographicCamera camera = new OrthographicCamera(200, 200);
        engine.addSystem(new Physics(world));
        engine.addSystem(new PhysicsDebugSystem(world, camera));
        factory = new EntityFactory(engine, world);

        // --- Start dummy demo code ---
        factory.createPlayer(50, 50, false);

        Vector2[] triangle = new Vector2[]{
                new Vector2(5, 5),
                new Vector2(5, 0),
                new Vector2(-5, 0),
        };
        factory.createObstacle(50, -50, triangle);
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
