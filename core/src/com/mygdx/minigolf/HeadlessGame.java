package com.mygdx.minigolf;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.minigolf.controller.ComponentMappers;
import com.mygdx.minigolf.controller.EntityFactory;
import com.mygdx.minigolf.controller.InputHandler;
import com.mygdx.minigolf.controller.systems.Physics;
import com.mygdx.minigolf.model.levels.CourseLoader;
import com.mygdx.minigolf.model.levels.LevelLoader;
import com.mygdx.minigolf.view.GameView;

import java.util.List;

// See link below for example of use
// https://github.com/TomGrill/gdx-testing/blob/master/tests/src/de/tomgrill/gdxtesting/GdxTestRunner.java
public class HeadlessGame implements ApplicationListener {
    public Engine engine;
    public World world;
    public EntityFactory factory;
    public LevelLoader levelLoader;
    private List<Entity> level;

    private long t0;

    @Override
    public void create() {
        world = new World(new Vector2(0, 0), true);
        engine = new Engine();

        engine.addSystem(new Physics(world, engine));

        factory = new EntityFactory(engine, world, false);
        levelLoader = new LevelLoader(factory);
        level = levelLoader.loadLevel(CourseLoader.getCourses().get(1));

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

    public void loadLevel(List<Entity> level) {
        if (this.level != null) {
            level.forEach(Entity::removeAll);
        }
        this.level = level;
    }

    public void loadLevel(String levelName) {
        loadLevel(levelLoader.loadLevel(levelName));
    }

    public Engine getEngine() {
        return engine;
    }

    public EntityFactory getFactory() {
        return factory;
    }
}
