package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.GraphicsSystem;
import com.mygdx.minigolf.controller.systems.Physics;
import com.mygdx.minigolf.model.components.Physical;

public class Game extends ApplicationAdapter {

    Engine engine;
    World world;
    EntityFactory factory;

    @Override
    public void create() {
        engine = new Engine();
        world = new World(new Vector2(0, -10), true);

        GraphicsSystem graphicsSystem = new GraphicsSystem();

        engine.addSystem(graphicsSystem);
        engine.addSystem(new Physics(world, engine));
        // engine.addSystem(new PhysicsDebugSystem(world, graphicsSystem.getCam()));

        factory = new EntityFactory(engine, world);

        // --- Start dummy demo code ---
        factory.createPlayer(9, 10, false);

        Vector2[] triangle = new Vector2[]{
                new Vector2(0, 0),
                new Vector2(2, 0),
                new Vector2(2, 1),
        };
        factory.createObstacle(8, 1, triangle);
        factory.createObstacle(8, 5, triangle).getComponent(Physical.class).addContactListener(new Physical.ContactListener(1) {
            @Override
            public void ignoreContact(Entity other, Contact contact) {
                contact.setEnabled(false);
            }
        });
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
