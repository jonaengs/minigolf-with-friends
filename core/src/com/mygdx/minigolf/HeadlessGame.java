package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.GameController;
import com.mygdx.minigolf.controller.systems.PhysicsSystem;
import com.mygdx.minigolf.model.levels.LevelLoader;
import com.mygdx.minigolf.model.levels.LevelLoader.Level;

import java.io.IOException;

// See link below for example of use
// https://github.com/TomGrill/gdx-testing/blob/master/tests/src/de/tomgrill/gdxtesting/GdxTestRunner.java
public class HeadlessGame implements ApplicationListener {
    public Engine engine;
    public World world;
    public EntityFactory factory;
    public LevelLoader levelLoader;
    public Level currentLevel;

    private long t0;

    @Override
    public void create()  {
        world = new World(new Vector2(0, 0), true);
        engine = new Engine();
        engine.addSystem(new PhysicsSystem(world, engine));
        factory = new EntityFactory(engine, world, false);
        levelLoader = new LevelLoader(factory);

        t0 = System.currentTimeMillis();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
        long t1 = System.currentTimeMillis();
        engine.update((t1 - t0) / 1000f);
        t0 = t1;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    public void loadLevel(String levelName, Application app) {
        // Tasks game thread (through game application) with loading level.
        // https://github.com/libgdx/libgdx/wiki/Threading
        Object lock = new Object();
        synchronized (lock) {
            app.postRunnable(() -> {
                        if (currentLevel != null) {
                            // dispose of the previous level before loading the new one
                            currentLevel.dispose(engine);
                        }
                        currentLevel = levelLoader.load(levelName);
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
            );
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Engine getEngine() {
        return engine;
    }

    public EntityFactory getFactory() {
        return factory;
    }
}
