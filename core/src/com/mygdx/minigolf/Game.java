package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
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
        engine.addSystem(new Physics(world, engine));
        // engine.addSystem(new PhysicsDebugSystem(world, graphicsSystem.getCam()));

        factory = new EntityFactory(engine, world, graphicsSystem.getCam());

        // --- Start dummy demo code ---
        Entity player = factory.createPlayer(9, 12, true);

        Vector2[] triangle = new Vector2[]{
                new Vector2(0, 0),
                new Vector2(2, 0),
                new Vector2(2, 1),
        };

        // demo highest priority wins
        Entity e1 = factory.createObstacle(8, 1, triangle);
        // added first, but highest priority, will be executed last and have final say
        e1.getComponent(Physical.class).addContactListener(new Physical.ContactListener(100) {
            @Override
            public void ignoreContact(Entity other, Contact contact) {
                contact.setEnabled(true);
            }
        });
        // added second, but lower priority, will be executed first and later listeners might
        // override decisions this listener caused
        e1.getComponent(Physical.class).addContactListener(new Physical.ContactListener(10) {
            @Override
            public void ignoreContact(Entity other, Contact contact) {
                contact.setEnabled(false);
            }
        });

        // demo disable contact; fall through entity
        factory.createObstacle(8, 4, triangle).getComponent(Physical.class).addContactListener(new Physical.ContactListener(1) {
            @Override
            public void ignoreContact(Entity other, Contact contact) {
                contact.setEnabled(false);
            }
        });

        // demo removing entity on contact (e.g. power-up)
        Entity e3 = factory.createObstacle(8, 7, triangle);
        e3.getComponent(Physical.class).addContactListener(new Physical.ContactListener(1) {
            @Override
            public void ignoreContact(Entity other, Contact contact) {
                engine.removeEntity(e3);
                contact.setEnabled(false);
            }
        });
        factory.createObstacle(5, 5, triangle);

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
