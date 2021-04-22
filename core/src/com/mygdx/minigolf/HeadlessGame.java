package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.systems.PhysicsSystem;
import com.mygdx.minigolf.model.levels.LevelLoader;
import com.mygdx.minigolf.model.levels.LevelLoader.Level;
import com.mygdx.minigolf.util.ConcurrencyUtils;

public class HeadlessGame implements ApplicationListener {
    public Engine engine;
    public World world;
    public EntityFactory factory;
    public LevelLoader levelLoader;
    public Level currentLevel;

    private long t0;

    @Override
    public void create() {
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
        engine.removeAllEntities();
        engine.getSystems().forEach(engine::removeSystem);
        world.dispose();
    }

    // TODO: Phase out
    public void loadLevel(String levelName, Application app) {
        ConcurrencyUtils.waitForPostRunnable(() -> {
            if (currentLevel != null) {
                currentLevel.dispose(engine);
            }
            currentLevel = levelLoader.load(levelName);
        });
    }

    public Engine getEngine() {
        return engine;
    }

    public EntityFactory getFactory() {
        return factory;
    }
}
